package com.byd.qrcode.service.impl;

import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.byd.qrcode.dto.WechatJssdkSignatureDTO;
import com.byd.qrcode.entity.WechatConfig;
import com.byd.qrcode.service.WechatConfigService;
import com.byd.qrcode.service.WechatJssdkService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.UUID;

/**
 * WeChat JS-SDK signature service.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WechatJssdkServiceImpl implements WechatJssdkService {

    private static final String TOKEN_ENDPOINT = "https://api.weixin.qq.com/cgi-bin/token";
    private static final String TICKET_ENDPOINT = "https://api.weixin.qq.com/cgi-bin/ticket/getticket";
    private static final int WECHAT_TIMEOUT_MS = 8000;
    private static final long REFRESH_BUFFER_SECONDS = 300;

    private final WechatConfigService wechatConfigService;

    @Value("${wechat.mp-app-id:${WECHAT_MP_APPID:}}")
    private String mpAppId;

    @Value("${wechat.mp-app-secret:${WECHAT_MP_SECRET:}}")
    private String mpAppSecret;

    private volatile String accessTokenCache;
    private volatile long accessTokenExpiresAt;
    private volatile String jsapiTicketCache;
    private volatile long jsapiTicketExpiresAt;

    private final Object tokenLock = new Object();
    private final Object ticketLock = new Object();

    @Override
    public WechatJssdkSignatureDTO generateSignature(String url) {
        if (!StringUtils.hasText(url)) {
            throw new IllegalArgumentException("签名参数 url 不能为空");
        }

        String normalizedUrl = normalizeUrl(url);
        WechatCredential credential = resolveCredentials();
        String jsapiTicket = getJsapiTicket();
        long timestamp = Instant.now().getEpochSecond();
        String nonceStr = UUID.randomUUID().toString().replace("-", "").substring(0, 16);

        String signRaw = "jsapi_ticket=" + jsapiTicket
                + "&noncestr=" + nonceStr
                + "&timestamp=" + timestamp
                + "&url=" + normalizedUrl;

        WechatJssdkSignatureDTO dto = new WechatJssdkSignatureDTO();
        dto.setAppId(credential.appId);
        dto.setTimestamp(timestamp);
        dto.setNonceStr(nonceStr);
        dto.setSignature(sha1Hex(signRaw));
        return dto;
    }

    private String getJsapiTicket() {
        long now = Instant.now().getEpochSecond();
        if (StringUtils.hasText(jsapiTicketCache) && jsapiTicketExpiresAt > now + 30) {
            return jsapiTicketCache;
        }

        synchronized (ticketLock) {
            now = Instant.now().getEpochSecond();
            if (StringUtils.hasText(jsapiTicketCache) && jsapiTicketExpiresAt > now + 30) {
                return jsapiTicketCache;
            }

            String accessToken = getAccessToken();
            String url = TICKET_ENDPOINT + "?access_token=" + urlEncode(accessToken) + "&type=jsapi";
            String responseBody = HttpRequest.get(url)
                    .timeout(WECHAT_TIMEOUT_MS)
                    .execute()
                    .body();

            JSONObject json = JSONUtil.parseObj(responseBody);
            Integer errCode = json.getInt("errcode");
            if (errCode == null || errCode != 0 || !StringUtils.hasText(json.getStr("ticket"))) {
                String errMsg = json.getStr("errmsg");
                log.warn("Failed to fetch jsapi_ticket, errCode={}, errMsg={}", errCode, errMsg);
                throw new IllegalArgumentException("获取微信 JS-SDK ticket 失败: " + (StringUtils.hasText(errMsg) ? errMsg : "unknown error"));
            }

            String ticket = json.getStr("ticket");
            Integer expiresInValue = json.getInt("expires_in");
            int expiresIn = expiresInValue == null ? 7200 : expiresInValue;
            jsapiTicketCache = ticket;
            jsapiTicketExpiresAt = Instant.now().getEpochSecond()
                    + Math.max(expiresIn - REFRESH_BUFFER_SECONDS, 60);
            return ticket;
        }
    }

    private String getAccessToken() {
        long now = Instant.now().getEpochSecond();
        if (StringUtils.hasText(accessTokenCache) && accessTokenExpiresAt > now + 30) {
            return accessTokenCache;
        }

        synchronized (tokenLock) {
            now = Instant.now().getEpochSecond();
            if (StringUtils.hasText(accessTokenCache) && accessTokenExpiresAt > now + 30) {
                return accessTokenCache;
            }

            WechatCredential credential = resolveCredentials();
            String url = TOKEN_ENDPOINT
                    + "?grant_type=client_credential"
                    + "&appid=" + urlEncode(credential.appId)
                    + "&secret=" + urlEncode(credential.appSecret);

            String responseBody = HttpRequest.get(url)
                    .timeout(WECHAT_TIMEOUT_MS)
                    .execute()
                    .body();

            JSONObject json = JSONUtil.parseObj(responseBody);
            Integer errCode = json.getInt("errcode");
            if ((errCode != null && errCode != 0) || !StringUtils.hasText(json.getStr("access_token"))) {
                String errMsg = json.getStr("errmsg");
                log.warn("Failed to fetch jssdk access_token, errCode={}, errMsg={}", errCode, errMsg);
                throw new IllegalArgumentException("获取微信 access_token 失败: " + (StringUtils.hasText(errMsg) ? errMsg : "unknown error"));
            }

            String accessToken = json.getStr("access_token");
            Integer expiresInValue = json.getInt("expires_in");
            int expiresIn = expiresInValue == null ? 7200 : expiresInValue;
            accessTokenCache = accessToken;
            accessTokenExpiresAt = Instant.now().getEpochSecond()
                    + Math.max(expiresIn - REFRESH_BUFFER_SECONDS, 60);
            return accessToken;
        }
    }

    private WechatCredential resolveCredentials() {
        if (StringUtils.hasText(mpAppId) && StringUtils.hasText(mpAppSecret)) {
            return new WechatCredential(mpAppId, mpAppSecret);
        }

        WechatConfig config = wechatConfigService.getActiveConfig();
        if (config != null && StringUtils.hasText(config.getAppId()) && StringUtils.hasText(config.getAppSecret())) {
            log.warn("Using active mini program appId/appSecret as JSSDK credential fallback. " +
                    "Recommend setting WECHAT_MP_APPID and WECHAT_MP_SECRET.");
            return new WechatCredential(config.getAppId(), config.getAppSecret());
        }

        throw new IllegalArgumentException("未配置微信公众号凭证，请设置 WECHAT_MP_APPID 和 WECHAT_MP_SECRET");
    }

    private String normalizeUrl(String url) {
        String normalized = url.trim();
        int hashIndex = normalized.indexOf('#');
        if (hashIndex >= 0) {
            normalized = normalized.substring(0, hashIndex);
        }
        return normalized;
    }

    private String sha1Hex(String content) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] digest = md.digest(content.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception ex) {
            throw new RuntimeException("生成微信签名失败", ex);
        }
    }

    private String urlEncode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private static class WechatCredential {
        private final String appId;
        private final String appSecret;

        private WechatCredential(String appId, String appSecret) {
            this.appId = appId;
            this.appSecret = appSecret;
        }
    }
}

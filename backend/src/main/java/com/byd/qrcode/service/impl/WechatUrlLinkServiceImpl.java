package com.byd.qrcode.service.impl;

import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.byd.qrcode.entity.WechatConfig;
import com.byd.qrcode.mapper.WechatConfigMapper;
import com.byd.qrcode.service.WechatUrlLinkService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

/**
 * Generate WeChat official URL Link.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WechatUrlLinkServiceImpl implements WechatUrlLinkService {

    private static final String TOKEN_ENDPOINT = "https://api.weixin.qq.com/cgi-bin/token";
    private static final String URL_LINK_ENDPOINT = "https://api.weixin.qq.com/wxa/generate_urllink";
    private static final int WECHAT_TIMEOUT_MS = 8000;
    private static final long TOKEN_REFRESH_BUFFER_SECONDS = 300;

    private final WechatConfigMapper wechatConfigMapper;

    @Override
    public String generateUrlLink(WechatConfig config, String query, String envVersion) {
        if (!isReadyForOfficialUrlLink(config)) {
            return null;
        }

        String accessToken = getValidAccessToken(config);
        if (!StringUtils.hasText(accessToken)) {
            return null;
        }

        UrlLinkResult firstTry = requestUrlLink(config, query, envVersion, accessToken);
        if (firstTry.success()) {
            return firstTry.urlLink();
        }

        // Retry once if token is expired/invalid.
        if (isTokenInvalid(firstTry.errCode())) {
            clearCachedToken(config);
            String refreshedToken = refreshAccessToken(config);
            if (!StringUtils.hasText(refreshedToken)) {
                return null;
            }

            UrlLinkResult secondTry = requestUrlLink(config, query, envVersion, refreshedToken);
            if (secondTry.success()) {
                return secondTry.urlLink();
            }
            log.warn("WeChat URL Link retry failed, errCode={}, errMsg={}", secondTry.errCode(), secondTry.errMsg());
            return null;
        }

        log.warn("WeChat URL Link failed, errCode={}, errMsg={}", firstTry.errCode(), firstTry.errMsg());
        return null;
    }

    private boolean isReadyForOfficialUrlLink(WechatConfig config) {
        return config != null
                && StringUtils.hasText(config.getAppId())
                && StringUtils.hasText(config.getAppSecret())
                && StringUtils.hasText(config.getPagePath());
    }

    private String getValidAccessToken(WechatConfig config) {
        long now = Instant.now().getEpochSecond();
        if (StringUtils.hasText(config.getAccessToken())
                && config.getAccessTokenExpiresAt() != null
                && config.getAccessTokenExpiresAt() > now + 30) {
            return config.getAccessToken();
        }
        return refreshAccessToken(config);
    }

    private String refreshAccessToken(WechatConfig config) {
        try {
            String tokenUrl = TOKEN_ENDPOINT
                    + "?grant_type=client_credential"
                    + "&appid=" + urlEncode(config.getAppId())
                    + "&secret=" + urlEncode(config.getAppSecret());

            String responseBody = HttpRequest.get(tokenUrl)
                    .timeout(WECHAT_TIMEOUT_MS)
                    .execute()
                    .body();

            JSONObject json = JSONUtil.parseObj(responseBody);
            Integer errCode = json.getInt("errcode");
            if (errCode != null && errCode != 0) {
                log.warn("Failed to fetch WeChat access token, errCode={}, errMsg={}", errCode, json.getStr("errmsg"));
                return null;
            }

            String accessToken = json.getStr("access_token");
            Integer expiresIn = json.getInt("expires_in");
            if (!StringUtils.hasText(accessToken)) {
                log.warn("Missing access_token in WeChat token response: {}", responseBody);
                return null;
            }

            cacheToken(config, accessToken, expiresIn == null ? 7200 : expiresIn);
            config.setAccessToken(accessToken);
            return accessToken;
        } catch (Exception ex) {
            log.warn("Error fetching WeChat access token", ex);
            return null;
        }
    }

    private UrlLinkResult requestUrlLink(WechatConfig config, String query, String envVersion, String accessToken) {
        try {
            JSONObject body = new JSONObject();
            body.set("path", config.getPagePath());
            body.set("query", StringUtils.hasText(query) ? query : "");
            body.set("env_version", normalizeEnvVersion(envVersion, config.getDefaultEnvVersion()));
            body.set("is_expire", false);

            String requestUrl = URL_LINK_ENDPOINT + "?access_token=" + urlEncode(accessToken);
            String responseBody = HttpRequest.post(requestUrl)
                    .timeout(WECHAT_TIMEOUT_MS)
                    .header("Content-Type", "application/json")
                    .body(body.toString())
                    .execute()
                    .body();

            JSONObject json = JSONUtil.parseObj(responseBody);
            Integer errCode = json.getInt("errcode");
            if ((errCode == null || errCode == 0) && StringUtils.hasText(json.getStr("url_link"))) {
                return new UrlLinkResult(json.getStr("url_link"), 0, "ok");
            }

            return new UrlLinkResult(null, errCode == null ? -1 : errCode, json.getStr("errmsg"));
        } catch (Exception ex) {
            log.warn("Error generating WeChat URL Link", ex);
            return new UrlLinkResult(null, -1, ex.getMessage());
        }
    }

    private String normalizeEnvVersion(String requestEnvVersion, String defaultEnvVersion) {
        String env = StringUtils.hasText(requestEnvVersion) ? requestEnvVersion : defaultEnvVersion;
        if ("trial".equals(env) || "develop".equals(env) || "release".equals(env)) {
            return env;
        }
        return "release";
    }

    private void cacheToken(WechatConfig config, String accessToken, int expiresInSeconds) {
        if (config.getId() == null) {
            return;
        }

        long expiresAt = Instant.now().getEpochSecond()
                + Math.max(expiresInSeconds - TOKEN_REFRESH_BUFFER_SECONDS, 60);

        WechatConfig update = new WechatConfig();
        update.setId(config.getId());
        update.setAccessToken(accessToken);
        update.setAccessTokenExpiresAt(expiresAt);
        wechatConfigMapper.updateById(update);
        config.setAccessTokenExpiresAt(expiresAt);
    }

    private void clearCachedToken(WechatConfig config) {
        if (config == null || config.getId() == null) {
            return;
        }
        WechatConfig update = new WechatConfig();
        update.setId(config.getId());
        update.setAccessToken(null);
        update.setAccessTokenExpiresAt(null);
        wechatConfigMapper.updateById(update);
        config.setAccessToken(null);
        config.setAccessTokenExpiresAt(null);
    }

    private boolean isTokenInvalid(Integer errCode) {
        return errCode != null && (errCode == 40001 || errCode == 40014 || errCode == 42001);
    }

    private String urlEncode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private record UrlLinkResult(String urlLink, Integer errCode, String errMsg) {
        private boolean success() {
            return StringUtils.hasText(urlLink);
        }
    }
}

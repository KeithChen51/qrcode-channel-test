package com.byd.qrcode.controller;

import com.byd.qrcode.common.Result;
import com.byd.qrcode.dto.LandingResponseDTO;
import com.byd.qrcode.entity.Campaign;
import com.byd.qrcode.entity.QrcodeRecord;
import com.byd.qrcode.entity.ScanRecord;
import com.byd.qrcode.entity.WechatConfig;
import com.byd.qrcode.service.CampaignService;
import com.byd.qrcode.service.QrcodeService;
import com.byd.qrcode.service.ScanRecordService;
import com.byd.qrcode.service.WechatConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * 方案B落地页公开接口
 */
@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
public class PublicLandingController {

    private final QrcodeService qrcodeService;
    private final ScanRecordService scanRecordService;
    private final CampaignService campaignService;
    private final WechatConfigService wechatConfigService;

    @GetMapping("/landing")
    public Result<LandingResponseDTO> landing(
            @RequestParam Integer qid,
            HttpServletRequest request) {
        QrcodeRecord record = qrcodeService.getById(qid);
        if (record == null) {
            throw new IllegalArgumentException("二维码不存在: " + qid);
        }

        ScanRecord scanRecord = scanRecordService.recordFromH5(
                qid, getClientIp(request), request.getHeader("User-Agent"));

        LandingResponseDTO dto = new LandingResponseDTO();
        dto.setQid(qid);
        dto.setScanId(scanRecord.getScanId());
        String jumpUrlLink = buildJumpUrlLink(record, scanRecord.getScanId());
        dto.setUrlLink(jumpUrlLink);

        Campaign campaign = record.getCampaignId() == null
                ? null
                : campaignService.getById(record.getCampaignId());

        dto.setTitle(campaign != null && StringUtils.hasText(campaign.getTitle())
                ? campaign.getTitle()
                : "欢迎扫码");
        dto.setSubtitle(campaign != null ? campaign.getSubtitle() : "");
        dto.setButtonText(campaign != null && StringUtils.hasText(campaign.getButtonText())
                ? campaign.getButtonText()
                : "立即进入小程序");
        dto.setThemeColor(campaign != null && StringUtils.hasText(campaign.getThemeColor())
                ? campaign.getThemeColor()
                : "#1d4ed8");
        dto.setBackgroundColor(campaign != null && StringUtils.hasText(campaign.getBackgroundColor())
                ? campaign.getBackgroundColor()
                : "#f5f7fa");
        dto.setLogoUrl(campaign != null ? campaign.getLogoUrl() : "");
        dto.setBackgroundImageUrl(campaign != null ? campaign.getBackgroundImageUrl() : "");

        boolean canJump = StringUtils.hasText(jumpUrlLink);
        dto.setCanJump(canJump);
        dto.setMessage(canJump ? "ok" : "当前二维码缺少跳转链接，请重新生成二维码");
        return Result.success(dto);
    }

    private String buildJumpUrlLink(QrcodeRecord record, String scanId) {
        String trackingParams = buildTrackingParams(record, scanId);

        WechatConfig config = record.getConfigId() == null
                ? null
                : wechatConfigService.getById(record.getConfigId());
        String appId = StringUtils.hasText(record.getAppId())
                ? record.getAppId()
                : (config != null ? config.getAppId() : null);
        String pagePath = config != null ? config.getPagePath() : null;

        // 优先按当前配置动态拼接，确保每次访问都携带 sid/qid/cid/s/f
        if (StringUtils.hasText(appId) && StringUtils.hasText(pagePath)) {
            String fullScene = buildFullScene(record, trackingParams);
            return "weixin://dl/business/?appid=" + encode(appId)
                    + "&path=" + encode(pagePath)
                    + "&query=" + encode(fullScene);
        }

        // 兼容历史数据：若已有 urlLink，则在其 scene 里补充 sid/qid/cid 参数
        return appendTrackingToLegacyUrl(record.getUrlLink(), trackingParams);
    }

    private String buildFullScene(QrcodeRecord record, String trackingParams) {
        StringBuilder scene = new StringBuilder();
        appendParam(scene, "s", record.getStoreId());
        appendParam(scene, "f", record.getStaffId());
        if (scene.length() > 0 && StringUtils.hasText(trackingParams)) {
            scene.append("&");
        }
        scene.append(trackingParams);
        return scene.toString();
    }

    private String buildTrackingParams(QrcodeRecord record, String scanId) {
        StringBuilder tracking = new StringBuilder();
        appendParam(tracking, "sid", scanId);
        appendParam(tracking, "qid", record.getId() == null ? null : String.valueOf(record.getId()));
        appendParam(tracking, "cid", record.getCampaignId() == null ? null : String.valueOf(record.getCampaignId()));
        return tracking.toString();
    }

    private String appendTrackingToLegacyUrl(String urlLink, String trackingParams) {
        if (!StringUtils.hasText(urlLink) || !StringUtils.hasText(trackingParams)) {
            return urlLink;
        }

        int queryIndex = urlLink.indexOf("query=");
        if (queryIndex < 0) {
            char separator = urlLink.contains("?") ? '&' : '?';
            return urlLink + separator + "query=" + encode(trackingParams);
        }

        int valueStart = queryIndex + "query=".length();
        int valueEnd = urlLink.indexOf('&', valueStart);
        String encodedScene = valueEnd >= 0
                ? urlLink.substring(valueStart, valueEnd)
                : urlLink.substring(valueStart);
        String scene = decode(encodedScene);

        String mergedScene = StringUtils.hasText(scene)
                ? scene + "&" + trackingParams
                : trackingParams;
        String mergedEncodedScene = encode(mergedScene);

        if (valueEnd >= 0) {
            return urlLink.substring(0, valueStart) + mergedEncodedScene + urlLink.substring(valueEnd);
        }
        return urlLink.substring(0, valueStart) + mergedEncodedScene;
    }

    private void appendParam(StringBuilder builder, String key, String value) {
        if (!StringUtils.hasText(value)) {
            return;
        }
        if (builder.length() > 0) {
            builder.append("&");
        }
        builder.append(key).append("=").append(value);
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private String decode(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (!StringUtils.hasText(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (!StringUtils.hasText(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}

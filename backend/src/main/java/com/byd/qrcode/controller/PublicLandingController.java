package com.byd.qrcode.controller;

import com.byd.qrcode.common.Result;
import com.byd.qrcode.dto.LandingResponseDTO;
import com.byd.qrcode.entity.Campaign;
import com.byd.qrcode.entity.QrcodeRecord;
import com.byd.qrcode.entity.ScanRecord;
import com.byd.qrcode.service.CampaignService;
import com.byd.qrcode.service.QrcodeService;
import com.byd.qrcode.service.ScanRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

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
        dto.setUrlLink(record.getUrlLink());

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

        boolean canJump = StringUtils.hasText(record.getUrlLink());
        dto.setCanJump(canJump);
        dto.setMessage(canJump ? "ok" : "当前二维码缺少跳转链接，请重新生成二维码");
        return Result.success(dto);
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

package com.byd.qrcode.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.byd.qrcode.dto.QrcodeGenerateRequest;
import com.byd.qrcode.entity.QrcodeRecord;
import com.byd.qrcode.entity.WechatConfig;
import com.byd.qrcode.mapper.QrcodeRecordMapper;
import com.byd.qrcode.service.QrcodeService;
import com.byd.qrcode.service.WechatConfigService;
import com.byd.qrcode.service.WechatUrlLinkService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 二维码服务实现（仅方案B：H5中转）
 */
@Service
@RequiredArgsConstructor
public class QrcodeServiceImpl extends ServiceImpl<QrcodeRecordMapper, QrcodeRecord> implements QrcodeService {

    private final WechatConfigService wechatConfigService;
    private final WechatUrlLinkService wechatUrlLinkService;

    @Value("${app.base-url:}")
    private String baseUrl;

    @Override
    @Transactional
    public QrcodeRecord generate(QrcodeGenerateRequest request) {
        WechatConfig config = request.getConfigId() != null
                ? wechatConfigService.getById(request.getConfigId())
                : wechatConfigService.getActiveConfig();

        if (config == null) {
            throw new IllegalArgumentException("没有可用的小程序配置，请先添加并激活配置");
        }
        if (!StringUtils.hasText(config.getAppId()) || !StringUtils.hasText(config.getPagePath())) {
            throw new IllegalArgumentException("小程序配置缺少 appId 或 pagePath");
        }

        String scene = String.format("s=%s&f=%s", request.getStoreId(), request.getStaffId());

        QrcodeRecord record = new QrcodeRecord();
        record.setConfigId(config.getId());
        record.setAppId(config.getAppId());
        String envVersion = StringUtils.hasText(request.getEnvVersion())
                ? request.getEnvVersion()
                : config.getDefaultEnvVersion();
        record.setEnvVersion(envVersion);
        record.setStoreId(request.getStoreId());
        record.setStoreName(request.getStoreName());
        record.setStaffId(request.getStaffId());
        record.setStaffName(request.getStaffName());
        record.setCampaignId(request.getCampaignId());
        record.setScanCount(0);
        record.setRegisterCount(0);
        save(record);

        String jumpPageUrl = buildJumpPageUrl(record.getId());
        String urlLink = wechatUrlLinkService.generateUrlLink(config, scene, envVersion);
        if (!StringUtils.hasText(urlLink)) {
            urlLink = buildFallbackUrlLink(config.getAppId(), config.getPagePath(), scene);
        }

        record.setJumpPageUrl(jumpPageUrl);
        record.setUrlLink(urlLink);
        record.setQrcodeUrl(buildQrcodeImageUrl(record.getId()));
        updateById(record);

        return record;
    }

    @Override
    @Transactional
    public boolean batchDelete(List<Integer> ids) {
        return removeByIds(ids);
    }

    private String buildJumpPageUrl(Integer qrcodeId) {
        return String.format("%s/jump?qid=%d", normalizedBaseUrl(), qrcodeId);
    }

    private String buildQrcodeImageUrl(Integer qrcodeId) {
        return String.format("%s/api/qrcodes/%d/image", normalizedBaseUrl(), qrcodeId);
    }

    private String normalizedBaseUrl() {
        if (!StringUtils.hasText(baseUrl)) {
            return "";
        }
        String normalized = baseUrl.trim();
        return normalized.endsWith("/")
                ? normalized.substring(0, normalized.length() - 1)
                : normalized;
    }

    private String buildFallbackUrlLink(String appId, String pagePath, String scene) {
        return "weixin://dl/business/?appid=" + encode(appId)
                + "&path=" + encode(pagePath)
                + "&query=" + encode(scene);
    }

    private String encode(String value) {
        try {
            return URLEncoder.encode(value, StandardCharsets.UTF_8.name());
        } catch (Exception e) {
            throw new RuntimeException("Failed to encode url parameter", e);
        }
    }

}

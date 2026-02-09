package com.byd.qrcode.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.byd.qrcode.dto.QrcodeGenerateRequest;
import com.byd.qrcode.entity.QrcodeRecord;
import com.byd.qrcode.entity.WechatConfig;
import com.byd.qrcode.mapper.QrcodeRecordMapper;
import com.byd.qrcode.service.QrcodeGeneratorService;
import com.byd.qrcode.service.QrcodeService;
import com.byd.qrcode.service.StorageService;
import com.byd.qrcode.service.WechatConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
@Service
@RequiredArgsConstructor
public class QrcodeServiceImpl extends ServiceImpl<QrcodeRecordMapper, QrcodeRecord> implements QrcodeService {

    private final WechatConfigService wechatConfigService;
    private final QrcodeGeneratorService qrcodeGeneratorService;
    private final StorageService storageService;

    @Value("${app.base-url:http://localhost:8080}")
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
        record.setEnvVersion(StringUtils.hasText(request.getEnvVersion())
                ? request.getEnvVersion()
                : config.getDefaultEnvVersion());
        record.setStoreId(request.getStoreId());
        record.setStoreName(request.getStoreName());
        record.setStaffId(request.getStaffId());
        record.setStaffName(request.getStaffName());
        record.setCampaignId(request.getCampaignId());
        record.setScanCount(0);
        record.setRegisterCount(0);
        save(record);

        String jumpPageUrl = buildJumpPageUrl(record.getId());
        String urlLink = buildFallbackUrlLink(config.getAppId(), config.getPagePath(), scene);

        byte[] qrcodeImage = qrcodeGeneratorService.generate(jumpPageUrl);
        String objectName = String.format("qrcode/%d/%d.png", config.getId(), record.getId());
        String qrcodeUrl = storageService.upload(objectName, qrcodeImage, "image/png");

        record.setJumpPageUrl(jumpPageUrl);
        record.setUrlLink(urlLink);
        record.setQrcodeUrl(qrcodeUrl);
        updateById(record);

        return record;
    }

    @Override
    @Transactional
    public boolean batchDelete(List<Integer> ids) {
        List<QrcodeRecord> records = listByIds(ids);
        for (QrcodeRecord record : records) {
            if (record.getQrcodeUrl() != null) {
                try {
                    String objectName = extractObjectName(record.getQrcodeUrl());
                    storageService.delete(objectName);
                } catch (Exception e) {
                    log.warn("Failed to delete qrcode image: {}", record.getQrcodeUrl(), e);
                }
            }
        }
        return removeByIds(ids);
    }

    private String buildJumpPageUrl(Integer qrcodeId) {
        String normalizedBaseUrl = baseUrl.endsWith("/")
                ? baseUrl.substring(0, baseUrl.length() - 1)
                : baseUrl;
        return String.format("%s/jump?qid=%d", normalizedBaseUrl, qrcodeId);
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

    private String extractObjectName(String url) {
        int bucketEnd = url.indexOf('/', url.indexOf("://") + 3);
        bucketEnd = url.indexOf('/', bucketEnd + 1);
        return url.substring(bucketEnd + 1);
    }
}

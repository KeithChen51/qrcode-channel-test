package com.byd.qrcode.service;

import com.byd.qrcode.entity.WechatConfig;

/**
 * WeChat URL Link generation service.
 */
public interface WechatUrlLinkService {

    /**
     * Generate an official WeChat URL Link (wxaurl.cn).
     *
     * @param config     mini program config
     * @param query      query string that will be passed into mini program page
     * @param envVersion env version: release/trial/develop
     * @return official URL Link, or {@code null} if generation failed
     */
    String generateUrlLink(WechatConfig config, String query, String envVersion);
}

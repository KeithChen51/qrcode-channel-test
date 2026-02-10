package com.byd.qrcode.service;

import com.byd.qrcode.dto.WechatJssdkSignatureDTO;

/**
 * WeChat JS-SDK service.
 */
public interface WechatJssdkService {

    /**
     * Generate JS-SDK signature for current url.
     */
    WechatJssdkSignatureDTO generateSignature(String url);
}

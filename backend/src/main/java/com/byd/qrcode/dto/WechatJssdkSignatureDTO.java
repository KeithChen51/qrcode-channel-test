package com.byd.qrcode.dto;

import lombok.Data;

/**
 * WeChat JS-SDK signature payload.
 */
@Data
public class WechatJssdkSignatureDTO {

    private String appId;
    private Long timestamp;
    private String nonceStr;
    private String signature;
}

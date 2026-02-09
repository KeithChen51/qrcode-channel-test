package com.byd.qrcode.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 小程序配置脱敏视图对象
 */
@Data
public class WechatConfigVO {

    private Integer id;
    private String name;
    private String appId;
    private String originalId;
    private String pagePath;
    private String defaultEnvVersion;
    private Integer isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

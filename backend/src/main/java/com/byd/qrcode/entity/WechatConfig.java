package com.byd.qrcode.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 微信小程序配置实体
 */
@Data
@TableName("wechat_config")
public class WechatConfig {

    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 小程序名称
     */
    private String name;

    /**
     * 小程序AppID
     */
    private String appId;

    /**
     * 小程序原始ID（gh_开头）
     */
    private String originalId;

    /**
     * 小程序AppSecret
     */
    private String appSecret;

    /**
     * 小程序页面路径
     */
    private String pagePath;

    /**
     * 默认环境版本：release/trial/develop
     */
    private String defaultEnvVersion;

    /**
     * 是否为当前激活配置
     */
    private Integer isActive;

    /**
     * access_token缓存
     */
    private String accessToken;

    /**
     * access_token过期时间戳
     */
    private Long accessTokenExpiresAt;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}

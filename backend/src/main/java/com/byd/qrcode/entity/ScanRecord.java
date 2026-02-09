package com.byd.qrcode.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 扫码记录实体
 */
@Data
@TableName("scan_records")
public class ScanRecord {

    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 扫码记录唯一ID
     */
    private String scanId;

    /**
     * 关联的二维码ID
     */
    private Integer qrcodeId;

    /**
     * 关联的小程序配置ID
     */
    private Integer configId;

    /**
     * 小程序AppID
     */
    private String appId;

    /**
     * 门店ID
     */
    private String storeId;

    /**
     * 门店名称
     */
    private String storeName;

    /**
     * 服务人员ID
     */
    private String staffId;

    /**
     * 服务人员名称
     */
    private String staffName;

    /**
     * 关联的活动ID
     */
    private Integer campaignId;

    /**
     * 活动名称
     */
    private String campaignName;

    /**
     * 扫码用户OpenID
     */
    private String userOpenId;

    /**
     * 用户昵称
     */
    private String userNickname;

    /**
     * 用户手机号
     */
    private String userPhone;

    /**
     * 是否已注册
     */
    private Integer isRegistered;

    /**
     * 注册用户ID
     */
    private String registeredUserId;

    /**
     * 注册时间
     */
    private LocalDateTime registeredAt;

    /**
     * IP地址
     */
    private String ipAddress;

    /**
     * User Agent
     */
    private String userAgent;

    /**
     * 扫码时间
     */
    private LocalDateTime scanTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}

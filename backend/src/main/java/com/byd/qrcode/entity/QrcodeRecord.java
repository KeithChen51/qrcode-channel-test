package com.byd.qrcode.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 二维码记录实体
 */
@Data
@TableName("qrcode_records")
public class QrcodeRecord {

    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 关联的小程序配置ID
     */
    private Integer configId;

    /**
     * 小程序AppID（冗余存储）
     */
    private String appId;

    /**
     * 小程序环境版本
     */
    private String envVersion;

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
     * 微信URL Link
     */
    private String urlLink;

    /**
     * H5跳转页面URL
     */
    private String jumpPageUrl;

    /**
     * 关联的活动ID
     */
    private Integer campaignId;

    /**
     * 二维码图片URL
     */
    private String qrcodeUrl;

    /**
     * 扫码次数
     */
    private Integer scanCount;

    /**
     * 注册次数
     */
    private Integer registerCount;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}

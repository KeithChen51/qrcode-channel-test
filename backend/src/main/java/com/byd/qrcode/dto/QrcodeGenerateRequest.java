package com.byd.qrcode.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;

/**
 * 二维码生成请求DTO
 */
@Data
public class QrcodeGenerateRequest {

    /**
     * 小程序配置ID
     */
    private Integer configId;

    /**
     * 门店ID
     */
    @NotBlank(message = "门店ID不能为空")
    private String storeId;

    /**
     * 门店名称
     */
    private String storeName;

    /**
     * 服务人员ID
     */
    @NotBlank(message = "服务人员ID不能为空")
    private String staffId;

    /**
     * 服务人员名称
     */
    private String staffName;

    /**
     * 活动ID
     */
    private Integer campaignId;

    /**
     * 环境版本：release/trial/develop
     */
    private String envVersion;
}

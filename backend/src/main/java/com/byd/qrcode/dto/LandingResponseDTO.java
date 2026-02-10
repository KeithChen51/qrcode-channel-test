package com.byd.qrcode.dto;

import lombok.Data;

/**
 * H5落地页返回数据
 */
@Data
public class LandingResponseDTO {

    private Integer qid;
    private String scanId;
    private String urlLink;
    private Boolean canJump;
    private String message;

    private String title;
    private String subtitle;
    private String buttonText;
    private String themeColor;
    private String backgroundColor;
    private String logoUrl;
    private String backgroundImageUrl;

    /**
     * Mini program original id (gh_xxx), used by wechat open tag.
     */
    private String miniProgramOriginalId;

    /**
     * Mini program path with query, used by wechat open tag.
     */
    private String miniProgramPath;

    /**
     * Mini program env version: release/trial/develop.
     */
    private String miniProgramEnvVersion;
}

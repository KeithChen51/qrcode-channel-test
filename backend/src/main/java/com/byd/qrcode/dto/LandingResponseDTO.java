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
}

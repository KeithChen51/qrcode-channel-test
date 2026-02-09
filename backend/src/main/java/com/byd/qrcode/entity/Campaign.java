package com.byd.qrcode.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 活动配置实体
 */
@Data
@TableName("campaigns")
public class Campaign {

    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 活动名称
     */
    private String name;

    /**
     * 活动描述
     */
    private String description;

    /**
     * 活动状态：active/inactive
     */
    private String status;

    /**
     * 主题色
     */
    private String themeColor;

    /**
     * 背景色
     */
    private String backgroundColor;

    /**
     * 标题文字
     */
    private String title;

    /**
     * 副标题文字
     */
    private String subtitle;

    /**
     * 按钮文字
     */
    private String buttonText;

    /**
     * Logo URL
     */
    private String logoUrl;

    /**
     * 背景图片URL
     */
    private String backgroundImageUrl;

    /**
     * 活动开始时间
     */
    private LocalDateTime startTime;

    /**
     * 活动结束时间
     */
    private LocalDateTime endTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}

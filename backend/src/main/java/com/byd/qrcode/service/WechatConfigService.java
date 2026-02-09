package com.byd.qrcode.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.byd.qrcode.entity.WechatConfig;

/**
 * 微信配置服务接口
 */
public interface WechatConfigService extends IService<WechatConfig> {

    /**
     * 获取当前激活配置
     */
    WechatConfig getActiveConfig();

    /**
     * 设置激活配置
     */
    boolean setActiveConfig(Integer id);
}

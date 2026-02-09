package com.byd.qrcode.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.byd.qrcode.entity.WechatConfig;

/**
 * 微信配置服务接口
 */
public interface WechatConfigService extends IService<WechatConfig> {

    /**
     * 获取当前激活的配置
     */
    WechatConfig getActiveConfig();

    /**
     * 设置激活配置
     */
    boolean setActiveConfig(Integer id);

    /**
     * 测试配置是否有效
     */
    boolean testConfig(Integer id);

    /**
     * 获取access_token（带缓存）
     */
    String getAccessToken(Integer id);
}

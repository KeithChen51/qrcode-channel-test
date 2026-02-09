package com.byd.qrcode.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.byd.qrcode.entity.WechatConfig;
import com.byd.qrcode.mapper.WechatConfigMapper;
import com.byd.qrcode.service.WechatConfigService;
import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.api.impl.WxMaServiceImpl;
import cn.binarywang.wx.miniapp.config.impl.WxMaDefaultConfigImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 微信配置服务实现
 */
@Slf4j
@Service
public class WechatConfigServiceImpl extends ServiceImpl<WechatConfigMapper, WechatConfig> implements WechatConfigService {

    @Override
    public WechatConfig getActiveConfig() {
        return getOne(new LambdaQueryWrapper<WechatConfig>()
                .eq(WechatConfig::getIsActive, 1)
                .last("LIMIT 1"));
    }

    @Override
    @Transactional
    public boolean setActiveConfig(Integer id) {
        // 先将所有配置设为非激活
        update(new LambdaUpdateWrapper<WechatConfig>()
                .set(WechatConfig::getIsActive, 0));
        
        // 再将指定配置设为激活
        return update(new LambdaUpdateWrapper<WechatConfig>()
                .eq(WechatConfig::getId, id)
                .set(WechatConfig::getIsActive, 1));
    }

    @Override
    public boolean testConfig(Integer id) {
        WechatConfig config = getById(id);
        if (config == null) {
            return false;
        }
        
        try {
            String token = getAccessToken(id);
            return token != null && !token.isEmpty();
        } catch (Exception e) {
            log.error("Failed to test config: {}", id, e);
            return false;
        }
    }

    @Override
    public String getAccessToken(Integer id) {
        WechatConfig config = getById(id);
        if (config == null) {
            throw new RuntimeException("Config not found: " + id);
        }
        
        // 检查缓存是否有效
        Long expiresAt = config.getAccessTokenExpiresAt();
        if (config.getAccessToken() != null && expiresAt != null && expiresAt > System.currentTimeMillis()) {
            return config.getAccessToken();
        }
        
        // 调用微信API获取新token
        try {
            WxMaService wxMaService = getWxMaService(config);
            String accessToken = wxMaService.getAccessToken(true);
            
            // 缓存token（有效期2小时，提前5分钟过期）
            long newExpiresAt = System.currentTimeMillis() + (7200 - 300) * 1000L;
            update(new LambdaUpdateWrapper<WechatConfig>()
                    .eq(WechatConfig::getId, id)
                    .set(WechatConfig::getAccessToken, accessToken)
                    .set(WechatConfig::getAccessTokenExpiresAt, newExpiresAt));
            
            return accessToken;
        } catch (Exception e) {
            log.error("Failed to get access token for config: {}", id, e);
            throw new RuntimeException("Failed to get access token", e);
        }
    }

    /**
     * 获取WxMaService实例
     */
    public WxMaService getWxMaService(WechatConfig config) {
        WxMaDefaultConfigImpl wxConfig = new WxMaDefaultConfigImpl();
        wxConfig.setAppid(config.getAppId());
        wxConfig.setSecret(config.getAppSecret());
        
        WxMaService wxMaService = new WxMaServiceImpl();
        wxMaService.setWxMaConfig(wxConfig);
        return wxMaService;
    }
}

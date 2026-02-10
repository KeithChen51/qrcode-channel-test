package com.byd.qrcode.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.byd.qrcode.entity.WechatConfig;
import com.byd.qrcode.mapper.WechatConfigMapper;
import com.byd.qrcode.service.WechatConfigService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 微信配置服务实现
 */
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
}

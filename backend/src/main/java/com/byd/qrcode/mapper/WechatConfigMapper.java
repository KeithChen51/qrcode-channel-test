package com.byd.qrcode.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.byd.qrcode.entity.WechatConfig;
import org.apache.ibatis.annotations.Mapper;

/**
 * 微信配置Mapper
 */
@Mapper
public interface WechatConfigMapper extends BaseMapper<WechatConfig> {
}

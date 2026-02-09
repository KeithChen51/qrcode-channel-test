package com.byd.qrcode.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.byd.qrcode.entity.Campaign;

import java.util.List;

/**
 * 活动服务接口
 */
public interface CampaignService extends IService<Campaign> {

    /**
     * 获取活跃的活动列表
     */
    List<Campaign> getActiveCampaigns();
}

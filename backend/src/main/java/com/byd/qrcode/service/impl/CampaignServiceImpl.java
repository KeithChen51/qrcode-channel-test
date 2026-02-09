package com.byd.qrcode.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.byd.qrcode.entity.Campaign;
import com.byd.qrcode.mapper.CampaignMapper;
import com.byd.qrcode.service.CampaignService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 活动服务实现
 */
@Service
public class CampaignServiceImpl extends ServiceImpl<CampaignMapper, Campaign> implements CampaignService {

    @Override
    public List<Campaign> getActiveCampaigns() {
        return list(new LambdaQueryWrapper<Campaign>()
                .eq(Campaign::getStatus, "active")
                .orderByDesc(Campaign::getCreatedAt));
    }
}

package com.byd.qrcode.controller;

import com.byd.qrcode.common.Result;
import com.byd.qrcode.entity.Campaign;
import com.byd.qrcode.service.CampaignService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 活动管理Controller
 */
@RestController
@RequestMapping("/api/campaigns")
@RequiredArgsConstructor
public class CampaignController {

    private final CampaignService campaignService;

    /**
     * 获取所有活动列表
     */
    @GetMapping
    public Result<List<Campaign>> list() {
        return Result.success(campaignService.list());
    }

    /**
     * 获取活跃的活动列表
     */
    @GetMapping("/active")
    public Result<List<Campaign>> getActive() {
        return Result.success(campaignService.getActiveCampaigns());
    }

    /**
     * 根据ID获取活动
     */
    @GetMapping("/{id}")
    public Result<Campaign> getById(@PathVariable Integer id) {
        return Result.success(campaignService.getById(id));
    }

    /**
     * 创建活动
     */
    @PostMapping
    public Result<Campaign> create(@RequestBody Campaign campaign) {
        campaignService.save(campaign);
        return Result.success(campaign);
    }

    /**
     * 更新活动
     */
    @PutMapping("/{id}")
    public Result<Boolean> update(@PathVariable Integer id, @RequestBody Campaign campaign) {
        campaign.setId(id);
        return Result.success(campaignService.updateById(campaign));
    }

    /**
     * 删除活动
     */
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Integer id) {
        return Result.success(campaignService.removeById(id));
    }
}

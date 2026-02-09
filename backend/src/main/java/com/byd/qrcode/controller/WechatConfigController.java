package com.byd.qrcode.controller;

import com.byd.qrcode.common.Result;
import com.byd.qrcode.dto.WechatConfigVO;
import com.byd.qrcode.entity.WechatConfig;
import com.byd.qrcode.service.WechatConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 微信配置管理Controller
 */
@RestController
@RequestMapping("/api/wechat-configs")
@RequiredArgsConstructor
public class WechatConfigController {

    private final WechatConfigService wechatConfigService;

    /**
     * 获取所有配置列表
     */
    @GetMapping
    public Result<List<WechatConfigVO>> list() {
        List<WechatConfigVO> result = wechatConfigService.list().stream()
                .map(this::toVO)
                .collect(Collectors.toList());
        return Result.success(result);
    }

    /**
     * 获取当前激活的配置
     */
    @GetMapping("/active")
    public Result<WechatConfigVO> getActive() {
        return Result.success(toVO(wechatConfigService.getActiveConfig()));
    }

    /**
     * 根据ID获取配置
     */
    @GetMapping("/{id}")
    public Result<WechatConfigVO> getById(@PathVariable Integer id) {
        return Result.success(toVO(wechatConfigService.getById(id)));
    }

    /**
     * 创建配置
     */
    @PostMapping
    public Result<WechatConfigVO> create(@RequestBody WechatConfig config) {
        wechatConfigService.save(config);
        return Result.success(toVO(config));
    }

    /**
     * 更新配置
     */
    @PutMapping("/{id}")
    public Result<Boolean> update(@PathVariable Integer id, @RequestBody WechatConfig config) {
        WechatConfig exist = wechatConfigService.getById(id);
        if (exist == null) {
            throw new IllegalArgumentException("配置不存在: " + id);
        }
        config.setId(id);
        if (config.getAppSecret() == null || config.getAppSecret().trim().isEmpty()) {
            config.setAppSecret(exist.getAppSecret());
        }
        return Result.success(wechatConfigService.updateById(config));
    }

    /**
     * 删除配置
     */
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Integer id) {
        return Result.success(wechatConfigService.removeById(id));
    }

    /**
     * 激活配置
     */
    @PostMapping("/{id}/activate")
    public Result<Boolean> activate(@PathVariable Integer id) {
        return Result.success(wechatConfigService.setActiveConfig(id));
    }

    /**
     * 测试配置
     */
    @PostMapping("/{id}/test")
    public Result<Boolean> test(@PathVariable Integer id) {
        return Result.success(wechatConfigService.testConfig(id));
    }

    private WechatConfigVO toVO(WechatConfig config) {
        if (config == null) {
            return null;
        }
        WechatConfigVO vo = new WechatConfigVO();
        vo.setId(config.getId());
        vo.setName(config.getName());
        vo.setAppId(config.getAppId());
        vo.setOriginalId(config.getOriginalId());
        vo.setPagePath(config.getPagePath());
        vo.setDefaultEnvVersion(config.getDefaultEnvVersion());
        vo.setIsActive(config.getIsActive());
        vo.setCreatedAt(config.getCreatedAt());
        vo.setUpdatedAt(config.getUpdatedAt());
        return vo;
    }
}

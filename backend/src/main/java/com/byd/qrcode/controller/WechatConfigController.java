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
 * 微信配置管理 Controller
 */
@RestController
@RequestMapping("/api/wechat-configs")
@RequiredArgsConstructor
public class WechatConfigController {

    private final WechatConfigService wechatConfigService;

    @GetMapping
    public Result<List<WechatConfigVO>> list() {
        List<WechatConfigVO> result = wechatConfigService.list().stream()
                .map(this::toVO)
                .collect(Collectors.toList());
        return Result.success(result);
    }

    @GetMapping("/active")
    public Result<WechatConfigVO> getActive() {
        return Result.success(toVO(wechatConfigService.getActiveConfig()));
    }

    @GetMapping("/{id}")
    public Result<WechatConfigVO> getById(@PathVariable Integer id) {
        return Result.success(toVO(wechatConfigService.getById(id)));
    }

    @PostMapping
    public Result<WechatConfigVO> create(@RequestBody WechatConfig config) {
        wechatConfigService.save(config);
        return Result.success(toVO(config));
    }

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

    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Integer id) {
        return Result.success(wechatConfigService.removeById(id));
    }

    @PostMapping("/{id}/activate")
    public Result<Boolean> activate(@PathVariable Integer id) {
        return Result.success(wechatConfigService.setActiveConfig(id));
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

package com.byd.qrcode.controller;

import com.byd.qrcode.common.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 健康检查Controller
 */
@RestController
@RequestMapping("/api")
public class HealthController {

    @GetMapping("/health")
    public Result<Map<String, Object>> health() {
        Map<String, Object> info = new HashMap<>();
        info.put("status", "UP");
        info.put("service", "qrcode-channel");
        info.put("timestamp", System.currentTimeMillis());
        return Result.success(info);
    }
}

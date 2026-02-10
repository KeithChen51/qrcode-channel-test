package com.byd.qrcode.controller;

import com.byd.qrcode.common.Result;
import com.byd.qrcode.dto.WechatJssdkSignatureDTO;
import com.byd.qrcode.service.WechatJssdkService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * WeChat JS-SDK endpoints for jump page.
 */
@RestController
@RequestMapping("/api/public/wechat-jssdk")
@RequiredArgsConstructor
public class WechatJssdkController {

    private final WechatJssdkService wechatJssdkService;

    @GetMapping("/signature")
    public Result<WechatJssdkSignatureDTO> signature(@RequestParam String url) {
        return Result.success(wechatJssdkService.generateSignature(url));
    }
}

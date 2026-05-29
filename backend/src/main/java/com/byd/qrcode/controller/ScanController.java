package com.byd.qrcode.controller;

import com.byd.qrcode.common.Result;
import com.byd.qrcode.dto.ScanStatsDTO;
import com.byd.qrcode.entity.ScanRecord;
import com.byd.qrcode.security.RateLimitExceededException;
import com.byd.qrcode.security.RateLimitService;
import com.byd.qrcode.service.ScanRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/scans")
@RequiredArgsConstructor
public class ScanController {

    private final ScanRecordService scanRecordService;
    private final RateLimitService rateLimitService;

    @Value("${security.rate-limit.scan.max-requests:60}")
    private int scanMaxRequests = 60;

    @Value("${security.rate-limit.scan.window-seconds:60}")
    private long scanWindowSeconds = 60;

    @PostMapping("/h5")
    public Result<ScanRecord> recordFromH5(
            @RequestParam(value = "qid", required = false) Integer qid,
            @RequestParam(value = "qrcodeId", required = false) Integer qrcodeId,
            HttpServletRequest request) {
        Integer targetQid = qid != null ? qid : qrcodeId;
        if (targetQid == null) {
            throw new IllegalArgumentException("Missing qid parameter");
        }
        String ip = getClientIp(request);
        checkScanRateLimit("h5|" + targetQid, ip);
        String ua = request.getHeader("User-Agent");
        return Result.success(scanRecordService.recordFromH5(targetQid, ip, ua));
    }

    @PostMapping("/register")
    public Result<Boolean> registerUser(@RequestBody Map<String, String> params, HttpServletRequest request) {
        String scanId = params.get("scanId");
        String userId = params.get("userId");
        String phone = params.get("phone");
        checkScanRateLimit("register|" + scanId, getClientIp(request));
        return Result.success(scanRecordService.registerUser(scanId, userId, phone));
    }

    @GetMapping
    public Result<List<ScanRecord>> list(
            @RequestParam(required = false) String storeId,
            @RequestParam(required = false) String staffId) {
        return Result.success(scanRecordService.lambdaQuery()
                .eq(storeId != null && !storeId.isEmpty(), ScanRecord::getStoreId, storeId)
                .eq(staffId != null && !staffId.isEmpty(), ScanRecord::getStaffId, staffId)
                .orderByDesc(ScanRecord::getScanTime)
                .list());
    }

    @GetMapping("/stats")
    public Result<ScanStatsDTO> stats() {
        return Result.success(scanRecordService.getStats());
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(ip)) {
            return ip.split(",")[0].trim();
        }
        ip = request.getHeader("X-Real-IP");
        if (StringUtils.hasText(ip)) {
            return ip.trim();
        }
        return request.getRemoteAddr();
    }

    private void checkScanRateLimit(String actionKey, String clientKey) {
        String key = actionKey + "|" + clientKey;
        if (!rateLimitService.tryAcquire("public-scan", key, scanMaxRequests, scanWindowSeconds)) {
            throw new RateLimitExceededException("Too many scan requests. Please try again later.");
        }
    }
}

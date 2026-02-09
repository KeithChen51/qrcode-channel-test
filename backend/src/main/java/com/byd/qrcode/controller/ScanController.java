package com.byd.qrcode.controller;

import com.byd.qrcode.common.Result;
import com.byd.qrcode.dto.ScanStatsDTO;
import com.byd.qrcode.entity.ScanRecord;
import com.byd.qrcode.service.ScanRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * 扫码记录Controller
 */
@RestController
@RequestMapping("/api/scans")
@RequiredArgsConstructor
public class ScanController {

    private final ScanRecordService scanRecordService;

    /**
     * H5扫码记录
     */
    @PostMapping("/h5")
    public Result<ScanRecord> recordFromH5(
            @RequestParam(value = "qid", required = false) Integer qid,
            @RequestParam(value = "qrcodeId", required = false) Integer qrcodeId,
            HttpServletRequest request) {
        Integer targetQid = qid != null ? qid : qrcodeId;
        if (targetQid == null) {
            throw new IllegalArgumentException("缺少参数 qid");
        }
        String ip = getClientIp(request);
        String ua = request.getHeader("User-Agent");
        return Result.success(scanRecordService.recordFromH5(targetQid, ip, ua));
    }

    /**
     * 注册用户
     */
    @PostMapping("/register")
    public Result<Boolean> registerUser(@RequestBody Map<String, String> params) {
        String scanId = params.get("scanId");
        String userId = params.get("userId");
        String phone = params.get("phone");
        return Result.success(scanRecordService.registerUser(scanId, userId, phone));
    }

    /**
     * 获取扫码记录列表
     */
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

    /**
     * 获取扫码统计
     */
    @GetMapping("/stats")
    public Result<ScanStatsDTO> stats() {
        return Result.success(scanRecordService.getStats());
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}

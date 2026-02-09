package com.byd.qrcode.controller;

import com.byd.qrcode.common.Result;
import com.byd.qrcode.dto.QrcodeGenerateRequest;
import com.byd.qrcode.entity.QrcodeRecord;
import com.byd.qrcode.service.QrcodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 二维码管理Controller
 */
@RestController
@RequestMapping("/api/qrcodes")
@RequiredArgsConstructor
public class QrcodeController {

    private final QrcodeService qrcodeService;

    /**
     * 生成二维码
     */
    @PostMapping
    public Result<QrcodeRecord> generate(@Valid @RequestBody QrcodeGenerateRequest request) {
        return Result.success(qrcodeService.generate(request));
    }

    /**
     * 获取二维码列表
     */
    @GetMapping
    public Result<List<QrcodeRecord>> list(
            @RequestParam(required = false) String storeId,
            @RequestParam(required = false) String staffId) {
        return Result.success(qrcodeService.lambdaQuery()
                .eq(storeId != null && !storeId.isEmpty(), QrcodeRecord::getStoreId, storeId)
                .eq(staffId != null && !staffId.isEmpty(), QrcodeRecord::getStaffId, staffId)
                .orderByDesc(QrcodeRecord::getCreatedAt)
                .list());
    }

    /**
     * 根据ID获取二维码
     */
    @GetMapping("/{id}")
    public Result<QrcodeRecord> getById(@PathVariable Integer id) {
        return Result.success(qrcodeService.getById(id));
    }

    /**
     * 删除二维码
     */
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Integer id) {
        return Result.success(qrcodeService.removeById(id));
    }

    /**
     * 批量删除二维码
     */
    @PostMapping("/batch-delete")
    public Result<Boolean> batchDelete(@RequestBody List<Integer> ids) {
        return Result.success(qrcodeService.batchDelete(ids));
    }

}

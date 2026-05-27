package com.byd.qrcode.controller;

import com.byd.qrcode.common.Result;
import com.byd.qrcode.dto.QrcodeGenerateRequest;
import com.byd.qrcode.entity.QrcodeRecord;
import com.byd.qrcode.service.QrcodeGeneratorService;
import com.byd.qrcode.service.QrcodeService;
import com.byd.qrcode.web.PublicUrlResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
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
    private final QrcodeGeneratorService qrcodeGeneratorService;
    private final PublicUrlResolver publicUrlResolver;

    /**
     * 生成二维码
     */
    @PostMapping
    public Result<QrcodeRecord> generate(
            @Valid @RequestBody QrcodeGenerateRequest request,
            HttpServletRequest httpRequest) {
        return Result.success(attachPublicUrls(qrcodeService.generate(request), httpRequest));
    }

    /**
     * 获取二维码列表
     */
    @GetMapping
    public Result<List<QrcodeRecord>> list(
            @RequestParam(required = false) String storeId,
            @RequestParam(required = false) String staffId,
            HttpServletRequest request) {
        List<QrcodeRecord> records = qrcodeService.lambdaQuery()
                .eq(storeId != null && !storeId.isEmpty(), QrcodeRecord::getStoreId, storeId)
                .eq(staffId != null && !staffId.isEmpty(), QrcodeRecord::getStaffId, staffId)
                .orderByDesc(QrcodeRecord::getCreatedAt)
                .list();
        records.forEach(record -> attachPublicUrls(record, request));
        return Result.success(records);
    }

    /**
     * 根据ID获取二维码
     */
    @GetMapping("/{id}")
    public Result<QrcodeRecord> getById(@PathVariable Integer id, HttpServletRequest request) {
        return Result.success(attachPublicUrls(qrcodeService.getById(id), request));
    }

    @GetMapping(value = "/{id}/image", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> image(
            @PathVariable Integer id,
            @RequestParam(defaultValue = "false") boolean download,
            HttpServletRequest request) {
        QrcodeRecord record = qrcodeService.getById(id);
        if (record == null) {
            return ResponseEntity.notFound().build();
        }

        byte[] image = qrcodeGeneratorService.generate(publicUrlResolver.buildJumpPageUrl(request, id));
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_PNG);
        if (download) {
            headers.setContentDisposition(ContentDisposition.attachment()
                    .filename("qrcode-" + id + ".png")
                    .build());
        }

        return ResponseEntity.ok()
                .headers(headers)
                .body(image);
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

    private QrcodeRecord attachPublicUrls(QrcodeRecord record, HttpServletRequest request) {
        if (record != null && record.getId() != null) {
            record.setJumpPageUrl(publicUrlResolver.buildJumpPageUrl(request, record.getId()));
            record.setQrcodeUrl(publicUrlResolver.buildQrcodeImageUrl(request, record.getId()));
        }
        return record;
    }

}

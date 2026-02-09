package com.byd.qrcode.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.byd.qrcode.entity.QrcodeRecord;
import com.byd.qrcode.dto.QrcodeGenerateRequest;

import java.util.List;

/**
 * 二维码服务接口
 */
public interface QrcodeService extends IService<QrcodeRecord> {

    /**
     * 生成二维码（仅方案B：H5中转）
     */
    QrcodeRecord generate(QrcodeGenerateRequest request);

    /**
     * 批量删除
     */
    boolean batchDelete(List<Integer> ids);
}

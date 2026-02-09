package com.byd.qrcode.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.byd.qrcode.entity.ScanRecord;
import com.byd.qrcode.dto.ScanStatsDTO;

/**
 * 扫码记录服务接口
 */
public interface ScanRecordService extends IService<ScanRecord> {

    /**
     * 记录H5扫码
     */
    ScanRecord recordFromH5(Integer qrcodeId, String ipAddress, String userAgent);

    /**
     * 注册用户信息
     */
    boolean registerUser(String scanId, String userId, String phone);

    /**
     * 获取扫码统计
     */
    ScanStatsDTO getStats();
}

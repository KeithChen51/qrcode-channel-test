package com.byd.qrcode.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.byd.qrcode.dto.ScanStatsDTO;
import com.byd.qrcode.entity.QrcodeRecord;
import com.byd.qrcode.entity.ScanRecord;
import com.byd.qrcode.mapper.ScanRecordMapper;
import com.byd.qrcode.service.QrcodeService;
import com.byd.qrcode.service.ScanRecordService;
import cn.hutool.core.util.IdUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;

/**
 * 扫码记录服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ScanRecordServiceImpl extends ServiceImpl<ScanRecordMapper, ScanRecord> implements ScanRecordService {

    private final QrcodeService qrcodeService;

    @Override
    @Transactional
    public ScanRecord recordFromH5(Integer qrcodeId, String ipAddress, String userAgent) {
        // 查找二维码记录
        QrcodeRecord qrcode = qrcodeService.getById(qrcodeId);
        if (qrcode == null) {
            throw new IllegalArgumentException("二维码不存在: " + qrcodeId);
        }

        // 创建扫码记录
        ScanRecord record = new ScanRecord();
        record.setScanId("scan_" + System.currentTimeMillis() + "_" + IdUtil.fastSimpleUUID().substring(0, 8));
        record.setQrcodeId(qrcode.getId());
        record.setConfigId(qrcode.getConfigId());
        record.setAppId(qrcode.getAppId());
        record.setStoreId(qrcode.getStoreId());
        record.setStoreName(qrcode.getStoreName());
        record.setStaffId(qrcode.getStaffId());
        record.setStaffName(qrcode.getStaffName());
        record.setCampaignId(qrcode.getCampaignId());
        record.setIpAddress(ipAddress);
        record.setUserAgent(userAgent);
        record.setScanTime(LocalDateTime.now());
        record.setIsRegistered(0);

        save(record);

        // 更新二维码扫码次数
        qrcodeService.update(new LambdaUpdateWrapper<QrcodeRecord>()
                .eq(QrcodeRecord::getId, qrcode.getId())
                .setSql("scan_count = scan_count + 1"));

        return record;
    }

    @Override
    @Transactional
    public boolean registerUser(String scanId, String userId, String phone) {
        ScanRecord record = getOne(new LambdaQueryWrapper<ScanRecord>()
                .eq(ScanRecord::getScanId, scanId));
        
        if (record == null) {
            log.warn("Scan record not found: {}", scanId);
            return false;
        }

        // 幂等：已经注册过则不重复累加
        if (Objects.equals(record.getIsRegistered(), 1)) {
            return true;
        }

        boolean updated = update(new LambdaUpdateWrapper<ScanRecord>()
                .eq(ScanRecord::getScanId, scanId)
                .eq(ScanRecord::getIsRegistered, 0)
                .set(ScanRecord::getIsRegistered, 1)
                .set(ScanRecord::getRegisteredUserId, userId)
                .set(ScanRecord::getUserPhone, phone)
                .set(ScanRecord::getRegisteredAt, LocalDateTime.now()));

        if (updated) {
            qrcodeService.update(new LambdaUpdateWrapper<QrcodeRecord>()
                    .eq(QrcodeRecord::getId, record.getQrcodeId())
                    .setSql("register_count = register_count + 1"));
        }

        return true;
    }

    @Override
    public ScanStatsDTO getStats() {
        ScanStatsDTO stats = new ScanStatsDTO();
        
        // 总扫码次数
        stats.setTotalScans(count());
        
        // 总注册次数
        stats.setTotalRegisters(count(new LambdaQueryWrapper<ScanRecord>()
                .eq(ScanRecord::getIsRegistered, 1)));
        
        // 今日开始时间
        LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        
        // 今日扫码次数
        stats.setTodayScans(count(new LambdaQueryWrapper<ScanRecord>()
                .ge(ScanRecord::getScanTime, todayStart)));
        
        // 今日注册次数
        stats.setTodayRegisters(count(new LambdaQueryWrapper<ScanRecord>()
                .eq(ScanRecord::getIsRegistered, 1)
                .ge(ScanRecord::getRegisteredAt, todayStart)));
        
        // 活跃门店数
        stats.setActiveStores((long) listObjs(new LambdaQueryWrapper<ScanRecord>()
                .select(ScanRecord::getStoreId)
                .groupBy(ScanRecord::getStoreId)).size());
        
        // 活跃员工数
        stats.setActiveStaffs((long) listObjs(new LambdaQueryWrapper<ScanRecord>()
                .select(ScanRecord::getStaffId)
                .groupBy(ScanRecord::getStaffId)).size());
        
        return stats;
    }

}

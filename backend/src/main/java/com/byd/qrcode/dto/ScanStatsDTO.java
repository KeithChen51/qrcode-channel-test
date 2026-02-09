package com.byd.qrcode.dto;

import lombok.Data;

/**
 * 扫码统计DTO
 */
@Data
public class ScanStatsDTO {

    /**
     * 总扫码次数
     */
    private Long totalScans;

    /**
     * 总注册次数
     */
    private Long totalRegisters;

    /**
     * 今日扫码次数
     */
    private Long todayScans;

    /**
     * 今日注册次数
     */
    private Long todayRegisters;

    /**
     * 活跃门店数
     */
    private Long activeStores;

    /**
     * 活跃员工数
     */
    private Long activeStaffs;
}

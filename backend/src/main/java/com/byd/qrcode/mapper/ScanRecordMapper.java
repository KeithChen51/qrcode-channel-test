package com.byd.qrcode.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.byd.qrcode.entity.ScanRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 扫码记录Mapper
 */
@Mapper
public interface ScanRecordMapper extends BaseMapper<ScanRecord> {
}

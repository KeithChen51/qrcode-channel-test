package com.byd.qrcode.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.byd.qrcode.entity.QrcodeRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 二维码记录Mapper
 */
@Mapper
public interface QrcodeRecordMapper extends BaseMapper<QrcodeRecord> {
}

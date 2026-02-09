package com.byd.qrcode;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 地推渠道活码生成系统 - 启动类
 */
@SpringBootApplication
@MapperScan("com.byd.qrcode.mapper")
public class QrcodeChannelApplication {

    public static void main(String[] args) {
        SpringApplication.run(QrcodeChannelApplication.class, args);
    }
}

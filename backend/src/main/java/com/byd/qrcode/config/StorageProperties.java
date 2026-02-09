package com.byd.qrcode.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 存储配置属性
 */
@Data
@Component
@ConfigurationProperties(prefix = "storage")
public class StorageProperties {

    /**
     * 存储类型：minio / obs
     */
    private String type = "minio";

    /**
     * MinIO配置
     */
    private MinioConfig minio = new MinioConfig();

    /**
     * 华为云OBS配置
     */
    private ObsConfig obs = new ObsConfig();

    @Data
    public static class MinioConfig {
        private String endpoint = "http://localhost:9000";
        private String accessKey = "minioadmin";
        private String secretKey = "minioadmin123";
        private String bucket = "qrcode-images";
    }

    @Data
    public static class ObsConfig {
        private String endpoint;
        private String accessKey;
        private String secretKey;
        private String bucket;
    }
}

package com.byd.qrcode.service.impl;

import com.byd.qrcode.config.StorageProperties;
import com.byd.qrcode.service.StorageService;
import io.minio.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * MinIO存储服务实现（开发环境）
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "storage.type", havingValue = "minio", matchIfMissing = true)
public class MinioStorageService implements StorageService {

    private final StorageProperties properties;
    private MinioClient minioClient;

    public MinioStorageService(StorageProperties properties) {
        this.properties = properties;
    }

    @PostConstruct
    public void init() {
        StorageProperties.MinioConfig config = properties.getMinio();
        this.minioClient = MinioClient.builder()
                .endpoint(config.getEndpoint())
                .credentials(config.getAccessKey(), config.getSecretKey())
                .build();

        // 确保bucket存在
        try {
            boolean exists = minioClient.bucketExists(BucketExistsArgs.builder()
                    .bucket(config.getBucket())
                    .build());
            if (!exists) {
                minioClient.makeBucket(MakeBucketArgs.builder()
                        .bucket(config.getBucket())
                        .build());
                log.info("Created MinIO bucket: {}", config.getBucket());
            }
        } catch (Exception e) {
            log.error("Failed to initialize MinIO bucket", e);
        }
    }

    @Override
    public String upload(String objectName, InputStream inputStream, String contentType) {
        try {
            StorageProperties.MinioConfig config = properties.getMinio();
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(config.getBucket())
                    .object(objectName)
                    .stream(inputStream, -1, 10485760) // 10MB part size
                    .contentType(contentType)
                    .build());
            return getUrl(objectName);
        } catch (Exception e) {
            log.error("Failed to upload to MinIO: {}", objectName, e);
            throw new RuntimeException("Failed to upload file", e);
        }
    }

    @Override
    public String upload(String objectName, byte[] data, String contentType) {
        return upload(objectName, new ByteArrayInputStream(data), contentType);
    }

    @Override
    public void delete(String objectName) {
        try {
            StorageProperties.MinioConfig config = properties.getMinio();
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(config.getBucket())
                    .object(objectName)
                    .build());
        } catch (Exception e) {
            log.error("Failed to delete from MinIO: {}", objectName, e);
        }
    }

    @Override
    public String getUrl(String objectName) {
        StorageProperties.MinioConfig config = properties.getMinio();
        return config.getEndpoint() + "/" + config.getBucket() + "/" + objectName;
    }
}

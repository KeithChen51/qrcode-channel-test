package com.byd.qrcode.service;

import java.io.InputStream;

/**
 * 存储服务接口 - 抽象层
 */
public interface StorageService {

    /**
     * 上传文件
     * @param objectName 对象名称（含路径）
     * @param inputStream 文件流
     * @param contentType 内容类型
     * @return 文件访问URL
     */
    String upload(String objectName, InputStream inputStream, String contentType);

    /**
     * 上传字节数组
     * @param objectName 对象名称
     * @param data 字节数据
     * @param contentType 内容类型
     * @return 文件访问URL
     */
    String upload(String objectName, byte[] data, String contentType);

    /**
     * 删除文件
     * @param objectName 对象名称
     */
    void delete(String objectName);

    /**
     * 获取文件访问URL
     * @param objectName 对象名称
     * @return 访问URL
     */
    String getUrl(String objectName);
}

package com.byd.qrcode.service;

/**
 * 二维码生成服务接口
 */
public interface QrcodeGeneratorService {

    /**
     * 生成二维码图片
     * @param content 二维码内容
     * @param width 宽度
     * @param height 高度
     * @return PNG格式字节数组
     */
    byte[] generate(String content, int width, int height);

    /**
     * 生成二维码图片（默认尺寸300x300）
     * @param content 二维码内容
     * @return PNG格式字节数组
     */
    default byte[] generate(String content) {
        return generate(content, 300, 300);
    }

    /**
     * 生成二维码Base64
     * @param content 二维码内容
     * @return Base64编码字符串
     */
    String generateBase64(String content);
}

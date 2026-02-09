package com.byd.qrcode.service.impl;

import com.byd.qrcode.service.QrcodeGeneratorService;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * 二维码生成服务实现（ZXing）
 */
@Slf4j
@Service
public class ZxingQrcodeGeneratorService implements QrcodeGeneratorService {

    @Override
    public byte[] generate(String content, int width, int height) {
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
            hints.put(EncodeHintType.MARGIN, 1);
            
            BitMatrix bitMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, width, height, hints);
            BufferedImage image = MatrixToImageWriter.toBufferedImage(bitMatrix);
            
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "PNG", baos);
            return baos.toByteArray();
        } catch (WriterException e) {
            log.error("Failed to generate QR code for content: {}", content, e);
            throw new RuntimeException("Failed to generate QR code", e);
        } catch (Exception e) {
            log.error("Failed to write QR code image", e);
            throw new RuntimeException("Failed to write QR code image", e);
        }
    }

    @Override
    public String generateBase64(String content) {
        byte[] imageBytes = generate(content);
        return "data:image/png;base64," + Base64.getEncoder().encodeToString(imageBytes);
    }
}

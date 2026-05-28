package com.byd.qrcode.auth;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;

@Component
public class AuthTokenService {

    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final Base64.Encoder URL_ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder URL_DECODER = Base64.getUrlDecoder();

    private final AuthProperties properties;
    private final byte[] secret;

    public AuthTokenService(AuthProperties properties) {
        this.properties = properties;
        if (!StringUtils.hasText(properties.getTokenSecret())) {
            throw new IllegalStateException("AUTH_TOKEN_SECRET must be configured");
        }
        this.secret = properties.getTokenSecret().getBytes(StandardCharsets.UTF_8);
    }

    public String issue(String username) {
        long issuedAt = Instant.now().getEpochSecond();
        long expiresAt = issuedAt + properties.getTokenTtlMinutes() * 60;
        String payload = username + "|" + issuedAt + "|" + expiresAt;
        String encodedPayload = URL_ENCODER.encodeToString(payload.getBytes(StandardCharsets.UTF_8));
        String encodedSignature = URL_ENCODER.encodeToString(sign(encodedPayload));
        return encodedPayload + "." + encodedSignature;
    }

    public VerifiedToken verify(String token) {
        if (!StringUtils.hasText(token)) {
            throw new IllegalArgumentException("missing token");
        }
        String[] parts = token.split("\\.");
        if (parts.length != 2) {
            throw new IllegalArgumentException("invalid token");
        }

        byte[] expectedSignature = sign(parts[0]);
        byte[] actualSignature;
        try {
            actualSignature = URL_DECODER.decode(parts[1]);
        } catch (RuntimeException ex) {
            throw new IllegalArgumentException("invalid token", ex);
        }
        if (!MessageDigest.isEqual(expectedSignature, actualSignature)) {
            throw new IllegalArgumentException("invalid token signature");
        }

        String payload = new String(URL_DECODER.decode(parts[0]), StandardCharsets.UTF_8);
        String[] values = payload.split("\\|");
        if (values.length != 3 || !StringUtils.hasText(values[0])) {
            throw new IllegalArgumentException("invalid token payload");
        }
        long issuedAt = parseLong(values[1]);
        long expiresAt = parseLong(values[2]);
        if (Instant.now().getEpochSecond() > expiresAt) {
            throw new IllegalArgumentException("token expired");
        }
        return new VerifiedToken(values[0], issuedAt, expiresAt);
    }

    private long parseLong(String value) {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("invalid token payload", ex);
        }
    }

    private byte[] sign(String encodedPayload) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(secret, HMAC_ALGORITHM));
            return mac.doFinal(encodedPayload.getBytes(StandardCharsets.UTF_8));
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to sign token", ex);
        }
    }

    public record VerifiedToken(String username, long issuedAt, long expiresAt) {
    }
}

package com.byd.qrcode.dto;

public record AuthResponse(String token, String username, boolean mustChangePassword, long expiresAt) {
}

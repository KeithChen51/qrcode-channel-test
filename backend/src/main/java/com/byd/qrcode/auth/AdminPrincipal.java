package com.byd.qrcode.auth;

public record AdminPrincipal(String username, boolean mustChangePassword, long issuedAt, long expiresAt) {
}

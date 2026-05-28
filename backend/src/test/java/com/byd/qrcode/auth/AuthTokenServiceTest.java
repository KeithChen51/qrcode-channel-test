package com.byd.qrcode.auth;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AuthTokenServiceTest {

    @Test
    void issuesAndVerifiesSignedToken() {
        AuthProperties properties = new AuthProperties();
        properties.setTokenSecret("unit-test-secret-with-enough-length");
        properties.setTokenTtlMinutes(30);
        AuthTokenService service = new AuthTokenService(properties);

        String token = service.issue("admin");
        AuthTokenService.VerifiedToken verified = service.verify(token);

        assertEquals("admin", verified.username());
    }

    @Test
    void rejectsTamperedToken() {
        AuthProperties properties = new AuthProperties();
        properties.setTokenSecret("unit-test-secret-with-enough-length");
        properties.setTokenTtlMinutes(30);
        AuthTokenService service = new AuthTokenService(properties);

        String token = service.issue("admin");
        String tampered = token.substring(0, token.length() - 2) + "xx";

        assertThrows(IllegalArgumentException.class, () -> service.verify(tampered));
    }
}

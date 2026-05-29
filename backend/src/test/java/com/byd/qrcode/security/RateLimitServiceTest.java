package com.byd.qrcode.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RateLimitServiceTest {

    @Test
    void blocksRequestsAfterLimitWithinWindow() {
        RateLimitService service = new RateLimitService();

        assertTrue(service.tryAcquire("public", "1.2.3.4", 2, 60));
        assertTrue(service.tryAcquire("public", "1.2.3.4", 2, 60));
        assertFalse(service.tryAcquire("public", "1.2.3.4", 2, 60));
    }

    @Test
    void resetClearsExistingLimit() {
        RateLimitService service = new RateLimitService();

        assertTrue(service.tryAcquire("login", "admin|1.2.3.4", 1, 600));
        assertFalse(service.tryAcquire("login", "admin|1.2.3.4", 1, 600));

        service.reset("login", "admin|1.2.3.4");

        assertTrue(service.tryAcquire("login", "admin|1.2.3.4", 1, 600));
    }
}

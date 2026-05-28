package com.byd.qrcode.auth;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PasswordHasherTest {

    @Test
    void hashesPasswordWithRandomSaltAndVerifiesIt() {
        PasswordHasher hasher = new PasswordHasher();

        String firstHash = hasher.hash("AdminPass123");
        String secondHash = hasher.hash("AdminPass123");

        assertNotEquals(firstHash, secondHash);
        assertTrue(hasher.matches("AdminPass123", firstHash));
        assertTrue(hasher.matches("AdminPass123", secondHash));
        assertFalse(hasher.matches("wrong-password", firstHash));
    }
}

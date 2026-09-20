package com.jayasrimart.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PasswordUtilTest {

    @Test
    @DisplayName("Password hashing produces valid non-empty hash different from raw password")
    void testHashPassword() {
        String rawPassword = "SecurePassword@123";
        String hash = PasswordUtil.hash(rawPassword);

        assertNotNull(hash);
        assertNotEquals(rawPassword, hash);
        assertTrue(hash.startsWith("$2a$") || hash.startsWith("$2b$") || hash.startsWith("$2y$"));
    }

    @Test
    @DisplayName("Password verification succeeds for correct password and fails for wrong password")
    void testVerifyPassword() {
        String rawPassword = "MySecretPassword123";
        String hash = PasswordUtil.hash(rawPassword);

        assertTrue(PasswordUtil.verify(rawPassword, hash));
        assertFalse(PasswordUtil.verify("WrongPassword123", hash));
        assertFalse(PasswordUtil.verify("", hash));
        assertFalse(PasswordUtil.verify(null, hash));
        assertFalse(PasswordUtil.verify(rawPassword, null));
    }

    @Test
    @DisplayName("Hashing blank or null password throws IllegalArgumentException")
    void testHashInvalidPassword() {
        assertThrows(IllegalArgumentException.class, () -> PasswordUtil.hash(null));
        assertThrows(IllegalArgumentException.class, () -> PasswordUtil.hash("   "));
    }
}

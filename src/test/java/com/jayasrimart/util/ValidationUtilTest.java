package com.jayasrimart.util;

import com.jayasrimart.exception.ValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ValidationUtilTest {

    @Test
    @DisplayName("requireNonBlank trims valid input and throws ValidationException for blank")
    void testRequireNonBlank() {
        assertEquals("validText", ValidationUtil.requireNonBlank("  validText  ", "fieldName"));

        assertThrows(ValidationException.class, () -> ValidationUtil.requireNonBlank(null, "fieldName"));
        assertThrows(ValidationException.class, () -> ValidationUtil.requireNonBlank("   ", "fieldName"));
    }

    @Test
    @DisplayName("validateEmail accepts valid email addresses and rejects invalid formats")
    void testValidateEmail() {
        assertEquals("user@example.com", ValidationUtil.validateEmail("User@Example.COM"));
        assertEquals("test.buyer@jayasrimart.in", ValidationUtil.validateEmail("test.buyer@jayasrimart.in"));

        assertThrows(ValidationException.class, () -> ValidationUtil.validateEmail("invalid-email"));
        assertThrows(ValidationException.class, () -> ValidationUtil.validateEmail("test@"));
        assertThrows(ValidationException.class, () -> ValidationUtil.validateEmail("@example.com"));
    }

    @Test
    @DisplayName("validatePassword requires at least 6 characters")
    void testValidatePassword() {
        assertEquals("123456", ValidationUtil.validatePassword("123456"));
        assertEquals("securePass@123", ValidationUtil.validatePassword("securePass@123"));

        assertThrows(ValidationException.class, () -> ValidationUtil.validatePassword("12345"));
        assertThrows(ValidationException.class, () -> ValidationUtil.validatePassword(""));
        assertThrows(ValidationException.class, () -> ValidationUtil.validatePassword(null));
    }

    @Test
    @DisplayName("validatePhone checks for valid 10-digit Indian mobile numbers")
    void testValidatePhone() {
        assertEquals("9876543210", ValidationUtil.validatePhone("9876543210"));
        assertEquals("7890123456", ValidationUtil.validatePhone("7890123456"));

        assertThrows(ValidationException.class, () -> ValidationUtil.validatePhone("1234567890"));
        assertThrows(ValidationException.class, () -> ValidationUtil.validatePhone("987654321"));
        assertThrows(ValidationException.class, () -> ValidationUtil.validatePhone("98765432100"));
    }

    @Test
    @DisplayName("validatePincode checks for 6-digit postal codes")
    void testValidatePincode() {
        assertEquals("600001", ValidationUtil.validatePincode("600001"));

        assertThrows(ValidationException.class, () -> ValidationUtil.validatePincode("60001"));
        assertThrows(ValidationException.class, () -> ValidationUtil.validatePincode("6000001"));
        assertThrows(ValidationException.class, () -> ValidationUtil.validatePincode("ABCDEF"));
    }

    @Test
    @DisplayName("validatePositiveAmount checks that amount is greater than zero")
    void testValidatePositiveAmount() {
        assertDoesNotThrow(() -> ValidationUtil.validatePositiveAmount(new BigDecimal("10.50"), "price"));

        assertThrows(ValidationException.class, () -> ValidationUtil.validatePositiveAmount(BigDecimal.ZERO, "price"));
        assertThrows(ValidationException.class, () -> ValidationUtil.validatePositiveAmount(new BigDecimal("-5.00"), "price"));
        assertThrows(ValidationException.class, () -> ValidationUtil.validatePositiveAmount(null, "price"));
    }
}

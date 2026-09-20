package com.jayasrimart.util;

import com.jayasrimart.exception.ValidationException;

import java.math.BigDecimal;
import java.util.regex.Pattern;

/**
 * Utility for performing server-side validations across controllers and service layers.
 */
public final class ValidationUtil {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    private static final Pattern PHONE_PATTERN = Pattern.compile(
            "^[6-9]\\d{9}$"
    );

    private static final Pattern PINCODE_PATTERN = Pattern.compile(
            "^\\d{6}$"
    );

    private ValidationUtil() {
        // Prevent instantiation
    }

    /**
     * Validates that a string is neither null nor blank.
     *
     * @param value the string value
     * @param field the field name
     * @return trimmed string
     * @throws ValidationException if blank
     */
    public static String requireNonBlank(String value, String field) {
        if (value == null || value.trim().isEmpty()) {
            throw new ValidationException(field, field + " is required and cannot be empty.");
        }
        return value.trim();
    }

    /**
     * Validates email format.
     *
     * @param email the candidate email
     * @return trimmed lower-cased email
     * @throws ValidationException if invalid
     */
    public static String validateEmail(String email) {
        String trimmed = requireNonBlank(email, "email").toLowerCase();
        if (!EMAIL_PATTERN.matcher(trimmed).matches()) {
            throw new ValidationException("email", "Please enter a valid email address.");
        }
        return trimmed;
    }

    /**
     * Validates password strength (minimum 6 characters).
     *
     * @param password candidate password
     * @return the password
     * @throws ValidationException if invalid
     */
    public static String validatePassword(String password) {
        if (password == null || password.trim().length() < 6) {
            throw new ValidationException("password", "Password must be at least 6 characters long.");
        }
        return password;
    }

    /**
     * Validates a 10-digit Indian mobile number.
     *
     * @param phone candidate phone
     * @return trimmed phone
     * @throws ValidationException if invalid
     */
    public static String validatePhone(String phone) {
        String trimmed = requireNonBlank(phone, "phone");
        if (!PHONE_PATTERN.matcher(trimmed).matches()) {
            throw new ValidationException("phone", "Please enter a valid 10-digit mobile number.");
        }
        return trimmed;
    }

    /**
     * Validates a 6-digit postal pincode.
     *
     * @param pincode candidate pincode
     * @return trimmed pincode
     * @throws ValidationException if invalid
     */
    public static String validatePincode(String pincode) {
        String trimmed = requireNonBlank(pincode, "pincode");
        if (!PINCODE_PATTERN.matcher(trimmed).matches()) {
            throw new ValidationException("pincode", "Please enter a valid 6-digit PIN code.");
        }
        return trimmed;
    }

    /**
     * Validates that an amount is strictly greater than zero.
     *
     * @param amount monetary value
     * @param field field name
     * @throws ValidationException if invalid
     */
    public static void validatePositiveAmount(BigDecimal amount, String field) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException(field, field + " must be greater than zero.");
        }
    }

    /**
     * Validates that quantity is at least 1.
     *
     * @param quantity quantity value
     * @param field field name
     * @throws ValidationException if invalid
     */
    public static void validatePositiveQuantity(int quantity, String field) {
        if (quantity <= 0) {
            throw new ValidationException(field, field + " must be at least 1.");
        }
    }
}

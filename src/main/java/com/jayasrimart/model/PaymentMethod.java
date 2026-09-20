package com.jayasrimart.model;

/**
 * Supported payment methods for checkout.
 */
public enum PaymentMethod {
    UPI,
    CARD,
    COD;

    public static PaymentMethod fromString(String methodStr) {
        if (methodStr == null || methodStr.trim().isEmpty()) {
            return null;
        }
        try {
            return PaymentMethod.valueOf(methodStr.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}

package com.jayasrimart.model;

/**
 * User roles supported in JayasriMart.
 */
public enum Role {
    BUYER,
    SELLER,
    ADMIN;

    /**
     * Safely parses a string into a {@link Role}, case-insensitively.
     *
     * @param roleStr the string value
     * @return the corresponding {@link Role}, or null if invalid
     */
    public static Role fromString(String roleStr) {
        if (roleStr == null || roleStr.trim().isEmpty()) {
            return null;
        }
        try {
            return Role.valueOf(roleStr.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}

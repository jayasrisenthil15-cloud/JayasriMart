package com.jayasrimart.util;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Utility class for secure password hashing and verification using jBCrypt.
 * Passwords are never stored or logged in plain text.
 */
public final class PasswordUtil {

    private static final int LOG_ROUNDS = 10;

    private PasswordUtil() {
        // Prevent instantiation
    }

    /**
     * Hashes a plain-text password using BCrypt with a secure salt.
     *
     * @param plainPassword the plain-text password to hash
     * @return the BCrypt-hashed string
     * @throws IllegalArgumentException if the password is null or blank
     */
    public static String hash(String plainPassword) {
        if (plainPassword == null || plainPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or blank");
        }
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(LOG_ROUNDS));
    }

    /**
     * Verifies whether a candidate plain-text password matches a stored BCrypt hash.
     *
     * @param candidatePassword the plain-text password to verify
     * @param hashedPassword the stored BCrypt hash
     * @return true if the candidate password matches the hash, false otherwise
     */
    public static boolean verify(String candidatePassword, String hashedPassword) {
        if (candidatePassword == null || hashedPassword == null || hashedPassword.trim().isEmpty()) {
            return false;
        }
        try {
            return BCrypt.checkpw(candidatePassword, hashedPassword);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}

package com.jayasrimart.exception;

/**
 * Thrown when attempting to create a record that violates a unique constraint (e.g. duplicate email).
 */
public class DuplicateResourceException extends AppException {

    private static final long serialVersionUID = 1L;

    public DuplicateResourceException(String message) {
        super(message);
    }
}

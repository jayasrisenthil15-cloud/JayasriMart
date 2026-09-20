package com.jayasrimart.exception;

/**
 * Thrown when a requested resource (product, user, order, review) cannot be located.
 */
public class ResourceNotFoundException extends AppException {

    private static final long serialVersionUID = 1L;

    public ResourceNotFoundException(String message) {
        super(message);
    }
}

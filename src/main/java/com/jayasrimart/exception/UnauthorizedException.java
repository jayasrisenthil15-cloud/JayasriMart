package com.jayasrimart.exception;

/**
 * Thrown when an authenticated user attempts to access a resource restricted to another role.
 */
public class UnauthorizedException extends AppException {

    private static final long serialVersionUID = 1L;

    public UnauthorizedException(String message) {
        super(message);
    }
}

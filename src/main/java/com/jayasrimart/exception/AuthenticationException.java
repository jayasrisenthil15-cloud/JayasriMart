package com.jayasrimart.exception;

/**
 * Thrown when credentials fail verification during authentication.
 */
public class AuthenticationException extends AppException {

    private static final long serialVersionUID = 1L;

    public AuthenticationException(String message) {
        super(message);
    }
}

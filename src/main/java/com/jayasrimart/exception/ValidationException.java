package com.jayasrimart.exception;

/**
 * Thrown when client or form input fails business or structural validation rules.
 */
public class ValidationException extends AppException {

    private static final long serialVersionUID = 1L;

    private final String field;

    public ValidationException(String message) {
        super(message);
        this.field = null;
    }

    public ValidationException(String field, String message) {
        super(message);
        this.field = field;
    }

    public String getField() {
        return field;
    }
}

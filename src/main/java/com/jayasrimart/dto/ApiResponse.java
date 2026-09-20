package com.jayasrimart.dto;

import java.io.Serializable;

/**
 * Standard JSON API response envelope used across all /api/v1/... endpoints.
 * <p>
 * Format for success:
 * <pre>
 * {
 *   "success": true,
 *   "data": { ... },
 *   "error": null
 * }
 * </pre>
 * Format for error:
 * <pre>
 * {
 *   "success": false,
 *   "data": null,
 *   "error": {
 *     "code": "VALIDATION_ERROR",
 *     "message": "...",
 *     "field": "..."
 *   }
 * }
 * </pre>
 *
 * @param <T> the payload data type
 */
public class ApiResponse<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    private boolean success;
    private T data;
    private ApiError error;

    public ApiResponse() {
    }

    public ApiResponse(boolean success, T data, ApiError error) {
        this.success = success;
        this.data = data;
        this.error = error;
    }

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, data, null);
    }

    public static <T> ApiResponse<T> error(String code, String message) {
        return new ApiResponse<>(false, null, new ApiError(code, message, null));
    }

    public static <T> ApiResponse<T> error(String code, String message, String field) {
        return new ApiResponse<>(false, null, new ApiError(code, message, field));
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public ApiError getError() {
        return error;
    }

    public void setError(ApiError error) {
        this.error = error;
    }

    /**
     * Standard error details payload.
     */
    public static class ApiError implements Serializable {
        private static final long serialVersionUID = 1L;

        private String code;
        private String message;
        private String field;

        public ApiError() {
        }

        public ApiError(String code, String message, String field) {
            this.code = code;
            this.message = message;
            this.field = field;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getField() {
            return field;
        }

        public void setField(String field) {
            this.field = field;
        }
    }
}

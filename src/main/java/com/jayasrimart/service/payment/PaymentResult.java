package com.jayasrimart.service.payment;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * Result returned upon executing a {@link PaymentStrategy}.
 */
public class PaymentResult implements Serializable {

    private static final long serialVersionUID = 1L;

    private final boolean successful;
    private final String transactionId;
    private final String message;
    private final Timestamp timestamp;

    public PaymentResult(boolean successful, String transactionId, String message) {
        this.successful = successful;
        this.transactionId = transactionId;
        this.message = message;
        this.timestamp = new Timestamp(System.currentTimeMillis());
    }

    public static PaymentResult success(String transactionId, String message) {
        return new PaymentResult(true, transactionId, message);
    }

    public static PaymentResult failure(String message) {
        return new PaymentResult(false, null, message);
    }

    public boolean isSuccessful() {
        return successful;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public String getMessage() {
        return message;
    }

    public Timestamp getTimestamp() {
        return timestamp != null ? new Timestamp(timestamp.getTime()) : null;
    }
}

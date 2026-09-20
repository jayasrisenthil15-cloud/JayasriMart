package com.jayasrimart.service.payment;

import com.jayasrimart.exception.ValidationException;

import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Strategy implementation simulating Unified Payments Interface (UPI) mock processing.
 */
public class UpiPaymentStrategy implements PaymentStrategy {

    private static final Pattern UPI_PATTERN = Pattern.compile("^[a-zA-Z0-9.\\-_]{2,49}@[a-zA-Z]{2,}$");

    @Override
    public PaymentResult processPayment(PaymentRequest request) {
        if (request == null) {
            return PaymentResult.failure("Invalid payment request.");
        }

        String upiId = request.getUpiId();
        if (upiId == null || !UPI_PATTERN.matcher(upiId.trim()).matches()) {
            throw new ValidationException("upiId", "Please provide a valid UPI ID (e.g. name@okhdfcbank, user@upi).");
        }

        String txnId = "UPI-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(java.util.Locale.ROOT);
        return PaymentResult.success(txnId, "UPI payment simulation approved.");
    }
}

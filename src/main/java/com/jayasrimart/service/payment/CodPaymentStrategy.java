package com.jayasrimart.service.payment;

import java.util.UUID;

/**
 * Strategy implementation for Cash on Delivery (COD) mock checkout orders.
 */
public class CodPaymentStrategy implements PaymentStrategy {

    @Override
    public PaymentResult processPayment(PaymentRequest request) {
        String bookingId = "COD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(java.util.Locale.ROOT);
        return PaymentResult.success(bookingId, "Cash on Delivery order confirmed. Payment will be collected upon delivery.");
    }
}

package com.jayasrimart.service.payment;

/**
 * Strategy interface defining mock payment processing algorithms.
 * Implements the Strategy design pattern for simulated checkout mechanisms (UPI, Card, COD).
 */
public interface PaymentStrategy {

    /**
     * Executes mock payment authorization and validation.
     *
     * @param request the payment parameters
     * @return result indicating success or failure with transaction metadata
     */
    PaymentResult processPayment(PaymentRequest request);
}

package com.jayasrimart.service.payment;

import com.jayasrimart.exception.ValidationException;
import com.jayasrimart.model.PaymentMethod;

/**
 * Factory and Strategy Resolver for {@link PaymentStrategy}.
 */
public final class PaymentStrategyFactory {

    private static final PaymentStrategy UPI_STRATEGY = new UpiPaymentStrategy();
    private static final PaymentStrategy CARD_STRATEGY = new CardPaymentStrategy();
    private static final PaymentStrategy COD_STRATEGY = new CodPaymentStrategy();

    private PaymentStrategyFactory() {
        // Prevent instantiation
    }

    /**
     * Resolves the appropriate {@link PaymentStrategy} instance for the chosen payment method.
     *
     * @param method the payment method
     * @return the corresponding PaymentStrategy
     * @throws ValidationException if method is null or unsupported
     */
    public static PaymentStrategy getStrategy(PaymentMethod method) {
        if (method == null) {
            throw new ValidationException("paymentMethod", "Please select a valid payment method (UPI, Card, or COD).");
        }
        return switch (method) {
            case UPI -> UPI_STRATEGY;
            case CARD -> CARD_STRATEGY;
            case COD -> COD_STRATEGY;
        };
    }
}

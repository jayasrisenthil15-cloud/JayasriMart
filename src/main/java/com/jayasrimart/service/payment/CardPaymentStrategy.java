package com.jayasrimart.service.payment;

import com.jayasrimart.exception.ValidationException;

import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Strategy implementation simulating Credit / Debit Card mock processing.
 */
public class CardPaymentStrategy implements PaymentStrategy {

    private static final Pattern CARD_NUMBER_PATTERN = Pattern.compile("^\\d{16}$");
    private static final Pattern CVV_PATTERN = Pattern.compile("^\\d{3,4}$");
    private static final Pattern EXPIRY_PATTERN = Pattern.compile("^(0[1-9]|1[0-2])/\\d{2}$");

    @Override
    public PaymentResult processPayment(PaymentRequest request) {
        if (request == null) {
            return PaymentResult.failure("Invalid payment request.");
        }

        String rawCardNumber = request.getCardNumber() != null ? request.getCardNumber().replaceAll("\\s+", "") : "";
        if (!CARD_NUMBER_PATTERN.matcher(rawCardNumber).matches()) {
            throw new ValidationException("cardNumber", "Please provide a valid 16-digit card number.");
        }

        String expiry = request.getCardExpiry() != null ? request.getCardExpiry().trim() : "";
        if (!EXPIRY_PATTERN.matcher(expiry).matches()) {
            throw new ValidationException("cardExpiry", "Please provide a valid expiry date in MM/YY format.");
        }

        String cvv = request.getCardCvv() != null ? request.getCardCvv().trim() : "";
        if (!CVV_PATTERN.matcher(cvv).matches()) {
            throw new ValidationException("cardCvv", "Please provide a valid 3 or 4 digit CVV.");
        }

        String txnId = "CARD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return PaymentResult.success(txnId, "Card payment simulation approved.");
    }
}

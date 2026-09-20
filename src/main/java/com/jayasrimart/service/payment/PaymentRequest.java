package com.jayasrimart.service.payment;

import com.jayasrimart.model.PaymentMethod;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Encapsulates the mock payment payload passed to a {@link PaymentStrategy}.
 */
public class PaymentRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Long buyerId;
    private final BigDecimal amount;
    private final PaymentMethod paymentMethod;
    private final String upiId;
    private final String cardNumber;
    private final String cardExpiry;
    private final String cardCvv;

    public PaymentRequest(Long buyerId, BigDecimal amount, PaymentMethod paymentMethod,
                          String upiId, String cardNumber, String cardExpiry, String cardCvv) {
        this.buyerId = buyerId;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.upiId = upiId;
        this.cardNumber = cardNumber;
        this.cardExpiry = cardExpiry;
        this.cardCvv = cardCvv;
    }

    public Long getBuyerId() {
        return buyerId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public String getUpiId() {
        return upiId;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public String getCardExpiry() {
        return cardExpiry;
    }

    public String getCardCvv() {
        return cardCvv;
    }
}

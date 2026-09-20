package com.jayasrimart.service.payment;

import com.jayasrimart.exception.ValidationException;
import com.jayasrimart.model.PaymentMethod;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PaymentStrategyTest {

    @Test
    @DisplayName("UpiPaymentStrategy approves valid UPI ID and generates transaction ID")
    void testUpiStrategySuccess() {
        PaymentStrategy strategy = new UpiPaymentStrategy();
        PaymentRequest request = new PaymentRequest(
                1L,
                new BigDecimal("1499.00"),
                PaymentMethod.UPI,
                "aarav@okhdfcbank",
                null, null, null
        );

        PaymentResult result = strategy.processPayment(request);
        assertTrue(result.isSuccessful());
        assertNotNull(result.getTransactionId());
        assertTrue(result.getTransactionId().startsWith("UPI-"));
    }

    @Test
    @DisplayName("UpiPaymentStrategy rejects invalid UPI format")
    void testUpiStrategyInvalid() {
        PaymentStrategy strategy = new UpiPaymentStrategy();
        PaymentRequest request = new PaymentRequest(
                1L,
                new BigDecimal("1499.00"),
                PaymentMethod.UPI,
                "invalid-upi",
                null, null, null
        );

        assertThrows(ValidationException.class, () -> strategy.processPayment(request));
    }

    @Test
    @DisplayName("CardPaymentStrategy approves valid 16-digit card and MM/YY expiry")
    void testCardStrategySuccess() {
        PaymentStrategy strategy = new CardPaymentStrategy();
        PaymentRequest request = new PaymentRequest(
                1L,
                new BigDecimal("2999.00"),
                PaymentMethod.CARD,
                null,
                "4111222233334444",
                "12/28",
                "123"
        );

        PaymentResult result = strategy.processPayment(request);
        assertTrue(result.isSuccessful());
        assertNotNull(result.getTransactionId());
        assertTrue(result.getTransactionId().startsWith("CARD-"));
    }

    @Test
    @DisplayName("CardPaymentStrategy rejects invalid card number or expired CVV")
    void testCardStrategyInvalid() {
        PaymentStrategy strategy = new CardPaymentStrategy();

        PaymentRequest shortCard = new PaymentRequest(1L, new BigDecimal("100.00"), PaymentMethod.CARD, null, "12345", "12/28", "123");
        assertThrows(ValidationException.class, () -> strategy.processPayment(shortCard));

        PaymentRequest invalidExpiry = new PaymentRequest(1L, new BigDecimal("100.00"), PaymentMethod.CARD, null, "4111222233334444", "99/99", "123");
        assertThrows(ValidationException.class, () -> strategy.processPayment(invalidExpiry));

        PaymentRequest invalidCvv = new PaymentRequest(1L, new BigDecimal("100.00"), PaymentMethod.CARD, null, "4111222233334444", "12/28", "1");
        assertThrows(ValidationException.class, () -> strategy.processPayment(invalidCvv));
    }

    @Test
    @DisplayName("CodPaymentStrategy approves Cash on Delivery booking")
    void testCodStrategySuccess() {
        PaymentStrategy strategy = new CodPaymentStrategy();
        PaymentRequest request = new PaymentRequest(1L, new BigDecimal("500.00"), PaymentMethod.COD, null, null, null, null);

        PaymentResult result = strategy.processPayment(request);
        assertTrue(result.isSuccessful());
        assertNotNull(result.getTransactionId());
        assertTrue(result.getTransactionId().startsWith("COD-"));
    }

    @Test
    @DisplayName("PaymentStrategyFactory resolves correct strategies and rejects null")
    void testPaymentStrategyFactory() {
        assertTrue(PaymentStrategyFactory.getStrategy(PaymentMethod.UPI) instanceof UpiPaymentStrategy);
        assertTrue(PaymentStrategyFactory.getStrategy(PaymentMethod.CARD) instanceof CardPaymentStrategy);
        assertTrue(PaymentStrategyFactory.getStrategy(PaymentMethod.COD) instanceof CodPaymentStrategy);

        assertThrows(ValidationException.class, () -> PaymentStrategyFactory.getStrategy(null));
    }
}

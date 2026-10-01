package com.grabseat.payment;

import com.grabseat.exception.PaymentFailedException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Dummy Stripe-like payment service for dev/demo.
 * Approves the well-known test card 4242..., declines 4000...0002,
 * and rejects anything else as unsupported.
 */
@Service
public class DummyStripeService {

    public static final String SUCCESS_CARD = "4242424242424242";
    public static final String DECLINED_CARD = "4000000000000002";

    public ChargeResult charge(String cardNumber, BigDecimal amount) {
        String digits = cardNumber == null ? "" : cardNumber.replaceAll("\\s", "");
        if (!digits.matches("\\d{12,19}")) {
            throw new IllegalArgumentException("Card number must be 12-19 digits");
        }
        if (digits.equals(DECLINED_CARD)) {
            throw new PaymentFailedException("Card declined (dummy Stripe)");
        }
        if (!digits.equals(SUCCESS_CARD)) {
            throw new IllegalArgumentException(
                "Unsupported test card; use 4242 4242 4242 4242 to pay or 4000 0000 0000 0002 to decline");
        }
        return new ChargeResult("ch_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16),
            digits.substring(digits.length() - 4), amount);
    }

    public record ChargeResult(String transactionId, String last4, BigDecimal amount) {
    }
}

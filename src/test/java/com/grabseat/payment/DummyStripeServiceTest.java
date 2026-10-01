package com.grabseat.payment;

import com.grabseat.exception.PaymentFailedException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DummyStripeServiceTest {

    private final DummyStripeService payments = new DummyStripeService();

    @Test
    void approvesSuccessCard() {
        DummyStripeService.ChargeResult res =
            payments.charge("4242424242424242", new BigDecimal("698.00"));

        assertThat(res.transactionId()).startsWith("ch_");
        assertThat(res.last4()).isEqualTo("4242");
    }

    @Test
    void declinesDeclinedCard() {
        assertThatThrownBy(
            () -> payments.charge("4000000000000002", new BigDecimal("698.00")))
            .isInstanceOf(PaymentFailedException.class);
    }

    @Test
    void rejectsUnknownCard() {
        assertThatThrownBy(
            () -> payments.charge("4111111111111111", new BigDecimal("100.00")))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsMalformedNumber() {
        assertThatThrownBy(() -> payments.charge("abcd", new BigDecimal("100.00")))
            .isInstanceOf(IllegalArgumentException.class);
    }
}

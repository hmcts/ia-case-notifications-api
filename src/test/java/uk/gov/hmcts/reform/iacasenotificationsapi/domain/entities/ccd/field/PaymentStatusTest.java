package uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class PaymentStatusTest {

    @Test
    public void has_correct_values() {
        assertEquals("Paid", PaymentStatus.PAID.toString());
        assertEquals("Payment pending", PaymentStatus.PAYMENT_PENDING.toString());
        assertEquals("Failed", PaymentStatus.FAILED.toString());
        assertEquals("Timeout", PaymentStatus.TIMEOUT.toString());
        assertEquals("Not paid", PaymentStatus.NOT_PAID.toString());
    }

    @Test
    public void if_this_test_fails_it_is_because_it_needs_updating_with_your_changes() {
        assertEquals(5, PaymentStatus.values().length);
    }
}

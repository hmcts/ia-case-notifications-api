package uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class AppealDecisionTest {

    @Test
    public void has_correct_values() {
        assertEquals(AppealDecision.DISMISSED.toString(), AppealDecision.DISMISSED.getValue());
        assertEquals(AppealDecision.ALLOWED.toString(), AppealDecision.ALLOWED.getValue());

        assertEquals("dismissed", AppealDecision.DISMISSED.toString());
        assertEquals("allowed", AppealDecision.ALLOWED.toString());

        assertEquals(AppealDecision.DISMISSED, AppealDecision.from("dismissed"));
        assertEquals(AppealDecision.ALLOWED, AppealDecision.from("allowed"));
    }

    @Test
    public void should_throw_exception_when_name_unrecognised() {
        IllegalArgumentException exception =
            assertThrows(IllegalArgumentException.class, () -> AppealDecision.from("unknown"));
        assertEquals("unknown not an AppealDecision", exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    public void if_this_test_fails_it_is_because_it_needs_updating_with_your_changes() {
        assertEquals(2, AppealDecision.values().length);
    }

}

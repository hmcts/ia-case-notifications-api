package uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class ApplicationDecisionTest {

    @Test
    public void check_all_options() {

        assertEquals("Refused", ApplicationDecision.REFUSED.toString());
        assertEquals("Granted", ApplicationDecision.GRANTED.toString());
        assertEquals(2, ApplicationDecision.values().length);
    }
}

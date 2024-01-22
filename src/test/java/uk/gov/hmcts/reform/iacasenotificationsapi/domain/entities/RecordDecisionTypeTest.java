package uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class RecordDecisionTypeTest {

    @Test
    public void check_all_options() {

        assertEquals("refused", RecordDecisionType.REFUSED.toString());
        assertEquals("granted", RecordDecisionType.GRANTED.toString());
        assertEquals("conditionalGrant", RecordDecisionType.CONDITIONAL_GRANT.toString());
        assertEquals("refusedUnderIma", RecordDecisionType.REFUSED_UNDER_IMA.toString());
        assertEquals(4, RecordDecisionType.values().length);
    }
}
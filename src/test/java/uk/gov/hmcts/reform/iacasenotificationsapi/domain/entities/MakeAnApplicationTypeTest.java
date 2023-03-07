package uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class MakeAnApplicationTypeTest {

    @Test
    void has_correct_values() {
        assertEquals("Adjourn", MakeAnApplicationType.ADJOURN.toString());
        assertEquals("Expedite", MakeAnApplicationType.EXPEDITE.toString());
        assertEquals("Link/unlink appeals", MakeAnApplicationType.LINK_OR_UNLINK.toString());
        assertEquals("Judge's review of application decision", MakeAnApplicationType.JUDGE_REVIEW.toString());
        assertEquals("Judge's review of Legal Officer decision", MakeAnApplicationType.JUDGE_REVIEW_LO.toString());
        assertEquals("Time extension", MakeAnApplicationType.TIME_EXTENSION.toString());
        assertEquals("Transfer", MakeAnApplicationType.TRANSFER.toString());
        assertEquals("Withdraw", MakeAnApplicationType.WITHDRAW.toString());
        assertEquals("Update hearing requirements", MakeAnApplicationType.UPDATE_HEARING_REQUIREMENTS.toString());
        assertEquals("Update appeal details", MakeAnApplicationType.UPDATE_APPEAL_DETAILS.toString());
        assertEquals("Reinstate an ended appeal", MakeAnApplicationType.REINSTATE.toString());
        assertEquals("Transfer out of accelerated detained appeals process",
            MakeAnApplicationType.TRANSFER_OUT_OF_ACCELERATED_DETAINED_APPEALS_PROCESS.toString());
        assertEquals("Other", MakeAnApplicationType.OTHER.toString());
    }

    @Test
    void if_this_test_fails_it_is_because_it_needs_updating_with_your_changes() {
        assertEquals(13, MakeAnApplicationType.values().length);
    }
}


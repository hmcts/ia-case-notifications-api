package uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class ApplicantTypeTest {

    @Test
    public void has_correct_values() {
        assertEquals("appellant", ApplicantType.APPELLANT.toString());
        assertEquals("respondent", ApplicantType.RESPONDENT.toString());
    }

    @Test
    public void has_correct_subscriber_types() {
        assertEquals(ApplicantType.APPELLANT, ApplicantType.from("appellant").get());
        assertEquals(ApplicantType.RESPONDENT, ApplicantType.from("respondent").get());
    }

    @Test
    public void if_this_test_fails_it_is_because_it_needs_updating_with_your_changes() {
        assertEquals(2, ApplicantType.values().length);
    }

}

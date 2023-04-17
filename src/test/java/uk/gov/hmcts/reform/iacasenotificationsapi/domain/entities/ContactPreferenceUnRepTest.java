package uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ContactPreferenceUnRepTest {

    @Test
    void has_correct_asylum_contact_preference() {
        assertThat(ContactPreferenceUnRep.from("wantsEmail").get()).isEqualByComparingTo(ContactPreferenceUnRep.WANTS_EMAIL);
        assertThat(ContactPreferenceUnRep.from("wantsSms").get()).isEqualByComparingTo(ContactPreferenceUnRep.WANTS_SMS);
        assertThat(ContactPreferenceUnRep.from("wantsPost").get()).isEqualByComparingTo(ContactPreferenceUnRep.WANTS_POST);
    }

    @Test
    void has_correct_asylum_contact_preference_description() {
        assertEquals("Email", ContactPreferenceUnRep.WANTS_EMAIL.getDescription());
        assertEquals("Text message", ContactPreferenceUnRep.WANTS_SMS.getDescription());
        assertEquals("Postal", ContactPreferenceUnRep.WANTS_POST.getDescription());
    }

    @Test
    void returns_optional_for_unknown_contact_preference() {
        assertThat(ContactPreferenceUnRep.from("some_unknown_type")).isEmpty();
    }

    @Test
    void if_this_test_fails_it_is_because_it_needs_updating_with_your_changes() {
        assertEquals(3, ContactPreferenceUnRep.values().length);
    }
}

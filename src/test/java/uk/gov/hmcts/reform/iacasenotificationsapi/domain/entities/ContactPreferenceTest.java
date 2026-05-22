package uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ContactPreferenceTest {

    @Test
    void has_correct_asylum_contact_preference() {
        assertEquals(ContactPreference.WANTS_EMAIL, ContactPreference.from("wantsEmail").get());
        assertEquals(ContactPreference.WANTS_SMS, ContactPreference.from("wantsSms").get());
    }

    @Test
    void has_correct_asylum_contact_preference_description() {
        assertEquals("Email", ContactPreference.WANTS_EMAIL.getDescription());
        assertEquals("Text message", ContactPreference.WANTS_SMS.getDescription());
    }

    @Test
    void returns_optional_for_unknown_contact_preference() {
        assertTrue(ContactPreference.from("some_unknown_type").isEmpty());
    }

    @Test
    void if_this_test_fails_it_is_because_it_needs_updating_with_your_changes() {
        assertEquals(2, ContactPreference.values().length);
    }
}

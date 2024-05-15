package uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class ListingEventTest {

    @Test
    void has_correct_bail_listing_event() {
        assertThat(ListingEvent.from("initialListing").get()).isEqualByComparingTo(ListingEvent.INITIAL);
        assertThat(ListingEvent.from("relisting").get()).isEqualByComparingTo(ListingEvent.RELISTING);
    }

    @Test
    void returns_optional_for_unknown_listing_event() {
        assertThat(ListingEvent.from("unknown")).isEmpty();
    }

    @Test
    void if_this_test_fails_it_is_because_it_needs_updating_with_your_changes() {
        assertEquals(2, ListingEvent.values().length);
    }
}

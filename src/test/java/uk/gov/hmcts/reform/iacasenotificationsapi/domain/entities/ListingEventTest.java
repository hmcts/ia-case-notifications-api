package uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class ListingEventTest {

    @Test
    void has_correct_bail_listing_event() {
        assertEquals(ListingEvent.INITIAL, ListingEvent.from("initialListing").get());
        assertEquals(ListingEvent.RELISTING, ListingEvent.from("relisting").get());
    }

    @Test
    void returns_optional_for_unknown_listing_event() {
        assertTrue(ListingEvent.from("unknown").isEmpty());
    }

    @Test
    void if_this_test_fails_it_is_because_it_needs_updating_with_your_changes() {
        assertEquals(2, ListingEvent.values().length);
    }
}

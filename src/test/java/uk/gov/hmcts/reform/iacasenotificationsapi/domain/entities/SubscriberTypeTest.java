package uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class SubscriberTypeTest {

    @Test
    public void has_correct_values() {
        assertEquals("appellant", SubscriberType.APPELLANT.toString());
        assertEquals("supporter", SubscriberType.SUPPORTER.toString());
    }

    @Test
    public void has_correct_subscriber_types() {
        assertEquals(SubscriberType.APPELLANT, SubscriberType.from("appellant").get());
        assertEquals(SubscriberType.SUPPORTER, SubscriberType.from("supporter").get());
    }

    @Test
    public void if_this_test_fails_it_is_because_it_needs_updating_with_your_changes() {
        assertEquals(2, SubscriberType.values().length);
    }
}

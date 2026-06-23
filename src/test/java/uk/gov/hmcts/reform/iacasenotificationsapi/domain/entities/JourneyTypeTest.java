package uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class JourneyTypeTest {

    @Test
    public void has_correct_values() {
        assertEquals("rep", JourneyType.REP.toString());
        assertEquals("aip", JourneyType.AIP.toString());
    }

    @Test
    public void has_correct_journey_types() {
        assertEquals(JourneyType.REP, JourneyType.from("rep").get());
        assertEquals(JourneyType.AIP, JourneyType.from("aip").get());
    }

    @Test
    public void if_this_test_fails_it_is_because_it_needs_updating_with_your_changes() {
        assertEquals(2, JourneyType.values().length);
    }

}

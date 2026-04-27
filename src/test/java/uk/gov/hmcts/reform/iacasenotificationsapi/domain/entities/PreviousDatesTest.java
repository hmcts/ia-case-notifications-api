package uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class PreviousDatesTest {

    private final String dateDue = "2019-12-01";
    private final String dateSent = "2018-12-01T12:34:56";


    private final PreviousDates previousDates = new PreviousDates(
        dateDue,
        dateSent
    );

    @Test
    public void should_hold_onto_values() {

        assertEquals(dateDue, previousDates.getDateDue());
        assertEquals(dateSent, previousDates.getDateSent());
    }

    @Test
    public void should_not_allow_null_arguments() {

        assertThrows(NullPointerException.class,
            () -> new PreviousDates(null, dateSent));

        assertThrows(NullPointerException.class,
            () -> new PreviousDates(dateDue, null));
    }
}

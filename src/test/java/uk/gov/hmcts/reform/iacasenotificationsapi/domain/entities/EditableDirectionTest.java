package uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class EditableDirectionTest {

    private final String explanation = "Do the thing";
    private final Parties parties = Parties.RESPONDENT;
    private final String dateDue = "2018-12-31T12:34:56";

    private final EditableDirection editableDirection = new EditableDirection(
        explanation,
        parties,
        dateDue
    );

    @Test
    public void should_hold_onto_values() {

        assertEquals(explanation, editableDirection.getExplanation());
        assertEquals(parties, editableDirection.getParties());
        assertEquals(dateDue, editableDirection.getDateDue());
    }

    @Test
    public void should_not_allow_null_arguments() {

        assertThrows(NullPointerException.class,
            () -> new EditableDirection(null, parties, dateDue));

        assertThrows(NullPointerException.class,
            () -> new EditableDirection(explanation, null, dateDue));

        assertThrows(NullPointerException.class,
            () -> new EditableDirection(explanation, parties, null));
    }
}

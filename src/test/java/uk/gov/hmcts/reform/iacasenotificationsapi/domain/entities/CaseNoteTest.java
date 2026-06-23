package uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class CaseNoteTest {

    private static final String TEST_VALUE = "some-value";

    private final CaseNote caseNote = new CaseNote(
        TEST_VALUE,
        TEST_VALUE,
        TEST_VALUE,
        TEST_VALUE
    );

    @Test
    public void should_hold_onto_values() {

        assertEquals(TEST_VALUE, caseNote.getCaseNoteSubject());
        assertEquals(TEST_VALUE, caseNote.getCaseNoteDescription());
        assertEquals(TEST_VALUE, caseNote.getUser());
        assertEquals(TEST_VALUE, caseNote.getDateAdded());
        assertNull(caseNote.getCaseNoteDocument());

    }

    @Test
    public void should_not_allow_null_arguments() {

        assertThrows(NullPointerException.class,
            () -> new CaseNote(null, null, null, null));

    }
}

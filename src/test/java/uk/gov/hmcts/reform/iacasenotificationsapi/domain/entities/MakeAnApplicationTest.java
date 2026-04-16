package uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.IdValue;


@ExtendWith(MockitoExtension.class)
public class MakeAnApplicationTest {

    private final String type = "Adjourn";
    private final String details = "Some application text";
    private final List<IdValue<Document>> evidence = Collections.emptyList();
    private final String applicant = "Legal representative";
    private final String date = "2020-09-21";
    private final String decision = "Pending";
    private final String state = "LISTING";
    private final String applicantRole = UserRole.LEGAL_REPRESENTATIVE.toString();

    public final MakeAnApplication makeAnApplication =
        new MakeAnApplication(
            applicant,
            type,
            details,
            evidence,
            date,
            decision,
            state,
            applicantRole
        );

    @Test
    public void should_hold_onto_values() {
        assertEquals(type, makeAnApplication.getType());
        assertEquals(details, makeAnApplication.getDetails());
        assertEquals(evidence, makeAnApplication.getEvidence());
        assertEquals(applicant, makeAnApplication.getApplicant());
        assertEquals(date, makeAnApplication.getDate());
        assertEquals(decision, makeAnApplication.getDecision());
        assertEquals(state, makeAnApplication.getState());
        assertEquals(applicantRole, makeAnApplication.getApplicantRole());
    }

    @Test
    public void should_not_allow_null_arguments() {

        assertThrows(NullPointerException.class, 
() -> new MakeAnApplication(
            null, type, details, evidence,
            date, decision, state, applicantRole));

        assertThrows(NullPointerException.class, 
() -> new MakeAnApplication(
            applicant, null, details, evidence,
            date, decision, state, applicantRole));

        assertThrows(NullPointerException.class, 
() -> new MakeAnApplication(
            applicant, type, null, evidence,
            date, decision, state, applicantRole));

        assertThrows(NullPointerException.class, 
() -> new MakeAnApplication(
            applicant, type, details, null,
            date, decision, state, applicantRole));

        assertThrows(NullPointerException.class, 
() -> new MakeAnApplication(
            applicant, type, details, evidence,
            null, decision, state, applicantRole));

        assertThrows(NullPointerException.class, 
() -> new MakeAnApplication(
            applicant, type, details, evidence,
            date, null, state, applicantRole));

        assertThrows(NullPointerException.class, 
() -> new MakeAnApplication(
            applicant, type, details, evidence,
            date, decision, null, applicantRole));

        assertThrows(NullPointerException.class, 
() -> new MakeAnApplication(
            applicant, type, details, evidence,
            date, decision, state, null));

    }
}

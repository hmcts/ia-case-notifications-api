package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.productowner;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_FAMILY_NAME;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_GIVEN_NAMES;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.CASE_NOTES;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.LEGAL_REP_REFERENCE_NUMBER;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.CaseNote;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.caseofficer.CaseOfficerEditDocumentsPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.EmailAddressFinder;

@RunWith(JUnitParamsRunner.class)
public class CaseOfficerEditDocumentsPersonalisationTest {

    @Rule
    public MockitoRule rule = MockitoJUnit.rule().strictness(Strictness.STRICT_STUBS);
    @Mock
    private EmailAddressFinder emailAddressFinder;
    @InjectMocks
    private CaseOfficerEditDocumentsPersonalisation personalisation;

    @Before
    public void setUp() throws Exception {
        ReflectionTestUtils.setField(personalisation, "appealDocumentDeletedTemplateId", "some template id");
    }

    @Test
    public void getReferenceId() {
        assertEquals("1234_APPEAL_DOCUMENT_DELETED", personalisation.getReferenceId(1234L));
    }

    @Test
    public void getTemplateId() {
        assertEquals("some template id", personalisation.getTemplateId());
    }

    @Test
    public void getRecipientsList() {
        given(emailAddressFinder.getEmailAddress(any(AsylumCase.class))).willReturn("hearingCentre@email.com");

        assertTrue(personalisation.getRecipientsList(new AsylumCase()).contains("hearingCentre@email.com"));
    }

    @Test
    @Parameters(method = "generateDifferentCaseNotesScenarios")
    public void getPersonalisation(AsylumCase asylumCase, String expectedReason) {
        Map<String, String> actualPersonalisation = personalisation.getPersonalisation(asylumCase);

        assertEquals("RP/50001/2020", actualPersonalisation.get("appealReferenceNumber"));
        assertEquals("Lacy Dawson", actualPersonalisation.get("appellantGivenNames"));
        assertEquals("Venus Blevins", actualPersonalisation.get("appellantFamilyName"));
        assertEquals("CASE001", actualPersonalisation.get("legalRepReferenceNumber"));
        assertEquals("A1234567", actualPersonalisation.get("homeOfficeReferenceNumber"));
        assertEquals(expectedReason, actualPersonalisation.get("reasonForDeletion"));
    }

    private Object[] generateDifferentCaseNotesScenarios() {
        String multiLineReason = "line 1 reason" + System.lineSeparator() + "line 2 reason";
        String singleLine = "line 1 reason";
        return new Object[]{
            new Object[]{generateSingleCaseNoteWithMultiLineReason(), multiLineReason},
            new Object[]{generateSingleCaseNoteWithSingleLineReason(), singleLine},
            new Object[]{generateTwoCaseNotesWithMultiLineReasons(), multiLineReason}
        };
    }

    private AsylumCase generateTwoCaseNotesWithMultiLineReasons() {
        String multiLineReason = "line 1 reason" + System.lineSeparator() + "line 2 reason";
        IdValue<CaseNote> idCaseNote1 = buildCaseNote(multiLineReason);

        String singleLine = "line 1 reason";
        IdValue<CaseNote> idCaseNote2 = buildCaseNote(singleLine);
        return writeCaseNote(Arrays.asList(idCaseNote1, idCaseNote2));
    }

    private AsylumCase generateSingleCaseNoteWithMultiLineReason() {
        String multiLineReason = "line 1 reason" + System.lineSeparator() + "line 2 reason";
        IdValue<CaseNote> idCaseNote = buildCaseNote(multiLineReason);
        return writeCaseNote(Collections.singletonList(idCaseNote));
    }

    private AsylumCase generateSingleCaseNoteWithSingleLineReason() {
        String singleLine = "line 1 reason";
        IdValue<CaseNote> idCaseNote = buildCaseNote(singleLine);
        return writeCaseNote(Collections.singletonList(idCaseNote));
    }

    private AsylumCase writeCaseNote(List<IdValue<CaseNote>> caseNoteList) {
        AsylumCase asylumCase = new AsylumCase();
        asylumCase.write(CASE_NOTES, caseNoteList);
        asylumCase.write(APPEAL_REFERENCE_NUMBER, "RP/50001/2020");
        asylumCase.write(APPELLANT_GIVEN_NAMES, "Lacy Dawson");
        asylumCase.write(APPELLANT_FAMILY_NAME, "Venus Blevins");
        asylumCase.write(LEGAL_REP_REFERENCE_NUMBER, "CASE001");
        asylumCase.write(HOME_OFFICE_REFERENCE_NUMBER, "A1234567");
        return asylumCase;
    }

    private IdValue<CaseNote> buildCaseNote(String reason) {
        CaseNote caseNote = new CaseNote("subject", buildCaseNoteDescription(reason),
            "user", "2-2-2020");
        return new IdValue<>("1", caseNote);
    }

    private String buildCaseNoteDescription(String reason) {
        return String.format("documentIds: %s" + System.lineSeparator() + "reason: %s" + System.lineSeparator(),
            "[d209e64c-b8fe-4ffa-8f8b-c7ae922c6b65]", reason);
    }

    @Test
    public void should_throw_exception_on_personalisation_when_case_is_null() {
        assertThatThrownBy(() -> personalisation.getPersonalisation((AsylumCase) null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("asylumCase must not be null");
    }
}
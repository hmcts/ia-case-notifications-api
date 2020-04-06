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
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.LEGAL_REPRESENTATIVE_DOCUMENTS;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.LEGAL_REP_REFERENCE_NUMBER;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.DocumentTag;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.DocumentWithMetadata;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.caseofficer.CaseOfficerEditDocumentsPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.EmailAddressFinder;

@RunWith(JUnitParamsRunner.class)
public class CaseOfficerEditDocumentsPersonalisationTest {

    public static final String DOC_ID = "d209e64c-b8fe-4ffa-8f8b-c7ae922c6b65";
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
        CaseDetails<AsylumCase> caseDetails = new CaseDetails<>(1L, "IA", State.APPEAL_SUBMITTED,
            asylumCase, LocalDateTime.now());
        Callback<AsylumCase> callback = new Callback<>(caseDetails, Optional.empty(), Event.EDIT_DOCUMENTS);
        Map<String, String> actualPersonalisation = personalisation.getPersonalisation(callback);

        assertEquals("RP/50001/2020", actualPersonalisation.get("appealReferenceNumber"));
        assertEquals("Lacy Dawson", actualPersonalisation.get("appellantGivenNames"));
        assertEquals("Venus Blevins", actualPersonalisation.get("appellantFamilyName"));
        assertEquals("CASE001", actualPersonalisation.get("legalRepReferenceNumber"));
        assertEquals("A1234567", actualPersonalisation.get("homeOfficeReferenceNumber"));
        assertEquals(expectedReason, actualPersonalisation.get("reasonForEditingOrDeletingDocuments"));
//        assertEquals(String.format("Document: \n "), actualPersonalisation.get("editedOrDeletedDocumentList"));
    }

    private Object[] generateDifferentCaseNotesScenarios() {
        String multiLineReason = "line 1 reason" + System.lineSeparator() + "line 2 reason";
        String singleLine = "line 1 reason";
        return new Object[]{
            new Object[]{generateSingleCaseNoteWithMultiLineReason(), multiLineReason},
            new Object[]{generateSingleCaseNoteWithSingleLineReason(), singleLine},
            new Object[]{generateTwoCaseNotesWithMultiLineReasons(), multiLineReason},
        };
    }

    private AsylumCase generateTwoCaseNotesWithMultiLineReasons() {
        String multiLineReason = "line 1 reason" + System.lineSeparator() + "line 2 reason";
        IdValue<CaseNote> idCaseNote1 = buildCaseNote(multiLineReason);

        String singleLine = "line 1 reason";
        IdValue<CaseNote> idCaseNote2 = buildCaseNote(singleLine);
        AsylumCase asylumCase = new AsylumCase();
        writeCaseNote(asylumCase, Arrays.asList(idCaseNote1, idCaseNote2));
        return asylumCase;
    }

    private AsylumCase generateSingleCaseNoteWithMultiLineReason() {
        String multiLineReason = "line 1 reason" + System.lineSeparator() + "line 2 reason";
        IdValue<CaseNote> idCaseNote = buildCaseNote(multiLineReason);
        AsylumCase asylumCase = new AsylumCase();
        writeCaseNote(asylumCase, Collections.singletonList(idCaseNote));
        return asylumCase;
    }

    private AsylumCase generateSingleCaseNoteWithSingleLineReason() {
        String singleLine = "line 1 reason";
        IdValue<CaseNote> idCaseNote = buildCaseNote(singleLine);
        AsylumCase asylumCase = new AsylumCase();
        writeCaseNote(asylumCase, Collections.singletonList(idCaseNote));
        return asylumCase;
    }

    private void writeCaseNote(AsylumCase asylumCase, List<IdValue<CaseNote>> caseNoteList) {
        asylumCase.write(CASE_NOTES, caseNoteList);
        asylumCase.write(APPEAL_REFERENCE_NUMBER, "RP/50001/2020");
        asylumCase.write(APPELLANT_GIVEN_NAMES, "Lacy Dawson");
        asylumCase.write(APPELLANT_FAMILY_NAME, "Venus Blevins");
        asylumCase.write(LEGAL_REP_REFERENCE_NUMBER, "CASE001");
        asylumCase.write(HOME_OFFICE_REFERENCE_NUMBER, "A1234567");
    }

    private void writeDocument(AsylumCase asylumCase) {
        String documentUrl = "http://dm-store/" + DOC_ID;
        Document doc = new Document(documentUrl, documentUrl + "/binary",
            "some doc name");
        DocumentWithMetadata docWithMetadata = new DocumentWithMetadata(doc, "some doc desc",
            LocalDate.now().toString(), DocumentTag.NONE);
        IdValue<DocumentWithMetadata> idDoc = new IdValue<>("1", docWithMetadata);
        List<IdValue<DocumentWithMetadata>> legalDocs = Collections.singletonList(idDoc);
        asylumCase.write(LEGAL_REPRESENTATIVE_DOCUMENTS, legalDocs);
    }

    private IdValue<CaseNote> buildCaseNote(String reason) {
        CaseNote caseNote = new CaseNote("subject", buildCaseNoteDescription(reason), "user",
            LocalDate.now().toString());
        return new IdValue<>("1", caseNote);
    }

    private String buildCaseNoteDescription(String reason) {
        return String.format("documentIds: %s" + System.lineSeparator() + "reason: %s" + System.lineSeparator(),
            "[" + DOC_ID + "]", reason);
    }

    @Test
    public void should_throw_exception_on_personalisation_when_case_is_null() {
        assertThatThrownBy(() -> personalisation.getPersonalisation((AsylumCase) null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("asylumCase must not be null");
    }
}
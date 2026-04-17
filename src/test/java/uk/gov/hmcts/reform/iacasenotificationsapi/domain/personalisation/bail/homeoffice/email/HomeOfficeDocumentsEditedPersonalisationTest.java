package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.bail.homeoffice.email;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.BailCaseFieldDefinition.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.CaseNote;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.bail.editbaildocuments.EditBailDocumentService;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class HomeOfficeDocumentsEditedPersonalisationTest {

    private final String templateIdWithLegalRep = "someTemplateIdWithLegalRep";
    private final String bailReferenceNumber = "someReferenceNumber";
    private final String legalRepReference = "someLegalRepReference";
    private final String homeOfficeReferenceNumber = "someHomeOfficeReferenceNumber";
    private final String applicantGivenNames = "someApplicantGivenNames";
    private final String applicantFamilyName = "someApplicantFamilyName";
    private final String latestModifiedDocuments = "Document1.pdf,\nDocument2.pdf,\nDocument3.pdf";
    private final String reasonForChange = "someReasonForChange";
    @Mock
    BailCase bailCase;
    @Mock
    BailCase bailCaseBefore;
    @Mock
    Callback<BailCase> callBack;
    @Mock
    CaseDetails<BailCase> caseDetails;
    @Mock
    CaseDetails<BailCase> caseDetailsBefore;
    @Mock
    EditBailDocumentService editBailDocumentService;
    @Mock
    CaseNote caseNote;
    @Mock
    IdValue<CaseNote> caseNoteIdValue;

    private HomeOfficeBailDocumentsEditedPersonalisation homeOfficeBailDocumentEditedPersonalisation;

    @BeforeEach
    public void setup() {
        when(callBack.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(bailCase);
        when(callBack.getCaseDetailsBefore()).thenReturn(Optional.of(caseDetailsBefore));
        when(caseDetailsBefore.getCaseData()).thenReturn(bailCaseBefore);

        when(editBailDocumentService.getFormattedDocumentsGivenCaseAndDocNames(eq(bailCaseBefore), eq(bailCase), anyList()))
            .thenReturn(List.of("Document1.pdf", "Document2.pdf", "Document3.pdf"));

        when(caseNoteIdValue.getValue()).thenReturn(caseNote);
        when(caseNote.getCaseNoteDescription()).thenReturn("Document names: [Document1.pdf, Document2.pdf, Document3.pdf]\nReason: " + reasonForChange);
        when(bailCase.read(CASE_NOTES)).thenReturn(Optional.of(List.of(caseNoteIdValue)));

        when(bailCase.read(BAIL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(bailReferenceNumber));
        when(bailCase.read(LEGAL_REP_REFERENCE, String.class)).thenReturn(Optional.of(legalRepReference));
        when(bailCase.read(APPLICANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(applicantGivenNames));
        when(bailCase.read(APPLICANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(applicantFamilyName));
        when(bailCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(homeOfficeReferenceNumber));
        when(bailCase.read(IS_LEGALLY_REPRESENTED_FOR_FLAG, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        String homeOfficeEmailAddress = "HO_user@example.com";
        String templateIdWithoutLegalRep = "someTemplateIdWithoutLegalRep";
        homeOfficeBailDocumentEditedPersonalisation =
            new HomeOfficeBailDocumentsEditedPersonalisation(templateIdWithLegalRep, templateIdWithoutLegalRep, editBailDocumentService, homeOfficeEmailAddress);
    }

    @Test
    public void should_return_given_template_id() {
        assertEquals(templateIdWithLegalRep, homeOfficeBailDocumentEditedPersonalisation.getTemplateId(bailCase));
    }

    @Test
    public void should_return_given_reference_id() {
        Long caseId = 12345L;
        assertEquals(caseId + "_BAIL_EDITED_DOCUMENTS_HOME_OFFICE",
            homeOfficeBailDocumentEditedPersonalisation.getReferenceId(caseId));
    }

    @Test
    public void should_throw_exception_on_personalisation_when_case_is_null() {

        NullPointerException exception =
            assertThrows(NullPointerException.class,
                () -> homeOfficeBailDocumentEditedPersonalisation.getPersonalisation((Callback<BailCase>) null));
        assertEquals("bailCase must not be null", exception.getMessage());
    }

    @Test
    public void should_return_personalisation_when_all_information_given() {

        Map<String, String> personalisation =
            homeOfficeBailDocumentEditedPersonalisation.getPersonalisation(callBack);

        assertThat(personalisation)
            .containsEntry("bailReferenceNumber", bailReferenceNumber)
            .containsEntry("legalRepReference", legalRepReference)
            .containsEntry("applicantGivenNames", applicantGivenNames)
            .containsEntry("applicantFamilyName", applicantFamilyName)
            .containsEntry("homeOfficeReferenceNumber", homeOfficeReferenceNumber)
            .containsEntry("latestModifiedDocuments", latestModifiedDocuments)
            .containsEntry("reasonForChange", reasonForChange);
    }

    @Test
    public void should_return_personalisation_when_no_LR_all_information_given() {

        when(bailCase.read(IS_LEGALLY_REPRESENTED_FOR_FLAG, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        Map<String, String> personalisation =
            homeOfficeBailDocumentEditedPersonalisation.getPersonalisation(callBack);

        assertThat(personalisation)
            .containsEntry("bailReferenceNumber", bailReferenceNumber)
            .containsEntry("applicantGivenNames", applicantGivenNames)
            .containsEntry("applicantFamilyName", applicantFamilyName)
            .containsEntry("homeOfficeReferenceNumber", homeOfficeReferenceNumber)
            .containsEntry("latestModifiedDocuments", latestModifiedDocuments)
            .containsEntry("reasonForChange", reasonForChange);
    }

    @Test
    public void should_return_personalisation_when_all_mandatory_information_given() {

        when(bailCase.read(BAIL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(bailCase.read(APPLICANT_GIVEN_NAMES, String.class)).thenReturn(Optional.empty());
        when(bailCase.read(APPLICANT_FAMILY_NAME, String.class)).thenReturn(Optional.empty());
        when(bailCase.read(LEGAL_REP_REFERENCE, String.class)).thenReturn(Optional.empty());
        when(bailCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(editBailDocumentService.getFormattedDocumentsGivenCaseAndDocNames(eq(bailCaseBefore), eq(bailCase), anyList()))
            .thenReturn(Collections.emptyList());
        when(bailCase.read(CASE_NOTES)).thenReturn(Optional.empty());

        Map<String, String> personalisation =
            homeOfficeBailDocumentEditedPersonalisation.getPersonalisation(callBack);

        assertThat(personalisation)
            .containsEntry("bailReferenceNumber", "")
            .containsEntry("legalRepReference", "")
            .containsEntry("applicantGivenNames", "")
            .containsEntry("applicantFamilyName", "")
            .containsEntry("homeOfficeReferenceNumber", "")
            .containsEntry("latestModifiedDocuments", "")
            .containsEntry("reasonForChange", "");
    }

}

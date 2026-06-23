package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.bail.legalrepresentative;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.BailCaseFieldDefinition.*;

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
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.BailCaseFieldDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.bail.legalrepresentative.email.LegalRepresentativeBailChangeDirectionDueDatePersonalisation;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class LegalRepresentativeBailChangeDirectionDueDatePersonalisationTest {

    private final String templateId = "someTemplateId";
    private final String legalRepEmailAddress = "legalRep@example.com";
    private final String bailReferenceNumber = "someReferenceNumber";
    private final String legalRepReference = "someLegalRepReference";
    private final String homeOfficeReferenceNumber = "someHomeOfficeReferenceNumber";
    private final String applicantGivenNames = "someApplicantGivenNames";
    private final String applicantFamilyName = "someApplicantFamilyName";
    private final String sendDirectionDescription = "someSendDirectionDescription";
    private final String party = "someParty";
    @Mock
    BailCase bailCase;
    private LegalRepresentativeBailChangeDirectionDueDatePersonalisation legalRepresentativeChangeDirectionDueDatePersonalisation;

    @BeforeEach
    public void setup() {

        when(bailCase.read(BailCaseFieldDefinition.BAIL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(bailReferenceNumber));
        when(bailCase.read(BailCaseFieldDefinition.APPLICANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(applicantGivenNames));
        when(bailCase.read(BailCaseFieldDefinition.APPLICANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(applicantFamilyName));
        when(bailCase.read(BailCaseFieldDefinition.LEGAL_REP_REFERENCE, String.class)).thenReturn(Optional.of(legalRepReference));
        when(bailCase.read(BailCaseFieldDefinition.HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(homeOfficeReferenceNumber));
        when(bailCase.read(BailCaseFieldDefinition.LEGAL_REP_EMAIL, String.class))
            .thenReturn(Optional.of(legalRepEmailAddress));
        String sendDirectionList = "someSendDirectionList";
        when(bailCase.read(BAIL_DIRECTION_EDIT_DATE_SENT, String.class)).thenReturn(Optional.of(sendDirectionList));
        String dateOfCompliance = "2022-05-24";
        when(bailCase.read(BAIL_DIRECTION_EDIT_DATE_DUE, String.class)).thenReturn(Optional.of(dateOfCompliance));
        when(bailCase.read(BAIL_DIRECTION_EDIT_EXPLANATION, String.class)).thenReturn(Optional.of(sendDirectionDescription));
        when(bailCase.read(BAIL_DIRECTION_EDIT_PARTIES, String.class)).thenReturn(Optional.of(party));

        legalRepresentativeChangeDirectionDueDatePersonalisation = new LegalRepresentativeBailChangeDirectionDueDatePersonalisation(
            templateId
        );
    }


    @Test
    public void should_return_given_template_id() {
        assertEquals(templateId, legalRepresentativeChangeDirectionDueDatePersonalisation.getTemplateId());
    }

    @Test
    public void should_return_given_reference_id() {
        Long caseId = 12345L;
        assertEquals(caseId + "_CHANGE_BAIL_DIRECTION_DUE_DATE_LEGAL_REPRESENTATIVE",
            legalRepresentativeChangeDirectionDueDatePersonalisation.getReferenceId(caseId));
    }

    @Test
    public void should_return_given_email_address_from_bail_case() {
        assertTrue(legalRepresentativeChangeDirectionDueDatePersonalisation.getRecipientsList(bailCase)
            .contains(legalRepEmailAddress));
    }

    @Test
    public void should_throw_exception_when_cannot_find_email_address_for_legal_rep() {
        when(bailCase.read(BailCaseFieldDefinition.LEGAL_REP_EMAIL, String.class)).thenReturn(Optional.empty());

        IllegalStateException exception =
            assertThrows(IllegalStateException.class, () -> legalRepresentativeChangeDirectionDueDatePersonalisation.getRecipientsList(bailCase));
        assertEquals("legalRepresentativeEmailAddress is not present", exception.getMessage());
    }

    @Test
    public void should_throw_exception_on_personalisation_when_case_is_null() {

        NullPointerException exception =
            assertThrows(NullPointerException.class,
                () -> legalRepresentativeChangeDirectionDueDatePersonalisation.getPersonalisation((BailCase) null));
        assertEquals("bailCase must not be null", exception.getMessage());
    }

    @Test
    public void should_return_personalisation_when_all_information_given() {

        Map<String, String> personalisation =
            legalRepresentativeChangeDirectionDueDatePersonalisation.getPersonalisation(bailCase);
        assertThat(personalisation)
            .containsEntry("bailReferenceNumber", bailReferenceNumber)
            .containsEntry("legalRepReference", legalRepReference)
            .containsEntry("applicantGivenNames", applicantGivenNames)
            .containsEntry("applicantFamilyName", applicantFamilyName)
            .containsEntry("homeOfficeReferenceNumber", homeOfficeReferenceNumber)
            .containsEntry("party", party)
            .containsEntry("directionDueDate", "24 May 2022")
            .containsEntry("explanation", sendDirectionDescription);
    }

    @Test
    public void should_return_personalisation_when_all_mandatory_information_given() {

        when(bailCase.read(BailCaseFieldDefinition.BAIL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(bailCase.read(BailCaseFieldDefinition.APPLICANT_GIVEN_NAMES, String.class)).thenReturn(Optional.empty());
        when(bailCase.read(BailCaseFieldDefinition.APPLICANT_FAMILY_NAME, String.class)).thenReturn(Optional.empty());
        when(bailCase.read(BailCaseFieldDefinition.LEGAL_REP_REFERENCE, String.class)).thenReturn(Optional.empty());
        when(bailCase.read(BailCaseFieldDefinition.HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(bailCase.read(SEND_DIRECTION_LIST, String.class)).thenReturn(Optional.empty());
        when(bailCase.read(BAIL_DIRECTION_EDIT_DATE_DUE, String.class)).thenReturn(Optional.empty());
        when(bailCase.read(BAIL_DIRECTION_EDIT_EXPLANATION, String.class)).thenReturn(Optional.empty());
        when(bailCase.read(BAIL_DIRECTION_EDIT_PARTIES, String.class)).thenReturn(Optional.empty());

        Map<String, String> personalisation =
            legalRepresentativeChangeDirectionDueDatePersonalisation.getPersonalisation(bailCase);

        assertThat(personalisation).allSatisfy((key, value) -> assertThat(value).isEmpty());
    }

}
package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.bail.legalrepresentative;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

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
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.bail.legalrepresentative.email.LegalRepresentativeBailApplicationEditedSubmittedPersonalisation;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class LegalRepresentativeBailApplicationEditedSubmittedPersonalisationTest {

    @Mock
    BailCase bailCase;

    private final String templateId = "someTemplateId";
    private final String legalRepEmailAddress = "legalRep@example.com";

    private LegalRepresentativeBailApplicationEditedSubmittedPersonalisation legalRepresentativeBailApplicationEditedSubmittedPersonalisation;

    @BeforeEach
    public void setup() {

        String bailReferenceNumber = "someReferenceNumber";
        String applicantGivenNames = "someApplicantGivenNames";
        String applicantFamilyName = "someApplicantFamilyName";
        String legalRepReference = "someLegalRepReference";
        String homeOfficeReferenceNumber = "someHomeOfficeReferenceNumber";

        when(bailCase.read(BailCaseFieldDefinition.BAIL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(bailReferenceNumber));
        when(bailCase.read(BailCaseFieldDefinition.APPLICANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(applicantGivenNames));
        when(bailCase.read(BailCaseFieldDefinition.APPLICANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(applicantFamilyName));
        when(bailCase.read(BailCaseFieldDefinition.LEGAL_REP_REFERENCE, String.class)).thenReturn(Optional.of(legalRepReference));
        when(bailCase.read(BailCaseFieldDefinition.HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(homeOfficeReferenceNumber));
        when(bailCase.read(BailCaseFieldDefinition.LEGAL_REP_EMAIL, String.class))
            .thenReturn(Optional.of(legalRepEmailAddress));

        legalRepresentativeBailApplicationEditedSubmittedPersonalisation = new LegalRepresentativeBailApplicationEditedSubmittedPersonalisation(
            templateId
        );
    }


    @Test
    public void should_return_given_template_id() {
        assertEquals(templateId, legalRepresentativeBailApplicationEditedSubmittedPersonalisation.getTemplateId());
    }

    @Test
    public void should_return_given_reference_id() {
        Long caseId = 12345L;
        assertEquals(caseId + "_BAIL_APPLICATION_EDITED_AND_SUBMITTED_LEGAL_REP",
            legalRepresentativeBailApplicationEditedSubmittedPersonalisation.getReferenceId(caseId));
    }

    @Test
    public void should_return_given_email_address_from_bail_case() {
        assertTrue(legalRepresentativeBailApplicationEditedSubmittedPersonalisation.getRecipientsList(bailCase)
            .contains(legalRepEmailAddress));
    }

    @Test
    public void should_throw_exception_when_cannot_find_email_address_for_legal_rep() {
        when(bailCase.read(BailCaseFieldDefinition.LEGAL_REP_EMAIL, String.class)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> legalRepresentativeBailApplicationEditedSubmittedPersonalisation.getRecipientsList(bailCase))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("legalRepresentativeEmailAddress is not present");
    }

    @Test
    public void should_throw_exception_on_personalisation_when_case_is_null() {

        assertThatThrownBy(
            () -> legalRepresentativeBailApplicationEditedSubmittedPersonalisation.getPersonalisation((BailCase) null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("bailCase must not be null");
    }

    @Test
    public void should_return_personalisation_when_all_information_given() {

        Map<String, String> personalisation =
            legalRepresentativeBailApplicationEditedSubmittedPersonalisation.getPersonalisation(bailCase);

        assertThat(personalisation).isEqualToComparingOnlyGivenFields(bailCase);
    }

    @Test
    public void should_return_personalisation_when_all_mandatory_information_given() {

        when(bailCase.read(BailCaseFieldDefinition.BAIL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(bailCase.read(BailCaseFieldDefinition.APPLICANT_GIVEN_NAMES, String.class)).thenReturn(Optional.empty());
        when(bailCase.read(BailCaseFieldDefinition.APPLICANT_FAMILY_NAME, String.class)).thenReturn(Optional.empty());
        when(bailCase.read(BailCaseFieldDefinition.LEGAL_REP_REFERENCE, String.class)).thenReturn(Optional.empty());
        when(bailCase.read(BailCaseFieldDefinition.HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());

        Map<String, String> personalisation =
            legalRepresentativeBailApplicationEditedSubmittedPersonalisation.getPersonalisation(bailCase);

        assertThat(personalisation).isEqualToComparingOnlyGivenFields(bailCase);
    }

}

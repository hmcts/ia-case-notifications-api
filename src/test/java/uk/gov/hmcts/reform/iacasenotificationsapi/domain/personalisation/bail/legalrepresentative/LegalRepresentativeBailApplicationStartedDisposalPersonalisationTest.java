package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.bail.legalrepresentative;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.BailCaseFieldDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.bail.legalrepresentative.email.LegalRepresentativeBailApplicationStartedDisposalPersonalisationEmail;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class LegalRepresentativeBailApplicationStartedDisposalPersonalisationTest {

    @Mock
    BailCase bailCase;

    private final String templateId = "someTemplateId";
    private final String iaExUiFrontendUrl = "url";
    private final String legalRepEmailAddress = "legalRep@example.com";
    private final String legalRepReference = "someLegalRepReference";
    private final String homeOfficeReferenceNumber = "someHomeOfficeReferenceNumber";
    private final String applicantGivenNames = "someApplicantGivenNames";
    private final String applicantFamilyName = "someApplicantFamilyName";

    private LegalRepresentativeBailApplicationStartedDisposalPersonalisationEmail legalRepresentativeBailApplicationStartedDisposalPersonalisationEmail;

    @BeforeEach
    public void setup() {
        String bailReferenceNumber = "someReferenceNumber";
        when(bailCase.read(BailCaseFieldDefinition.BAIL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(bailReferenceNumber));
        when(bailCase.read(BailCaseFieldDefinition.APPLICANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(applicantGivenNames));
        when(bailCase.read(BailCaseFieldDefinition.APPLICANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(applicantFamilyName));
        when(bailCase.read(BailCaseFieldDefinition.LEGAL_REP_REFERENCE, String.class)).thenReturn(Optional.of(legalRepReference));
        when(bailCase.read(BailCaseFieldDefinition.HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(homeOfficeReferenceNumber));
        when(bailCase.read(BailCaseFieldDefinition.LEGAL_REP_EMAIL, String.class)).thenReturn(Optional.of(legalRepEmailAddress));

        legalRepresentativeBailApplicationStartedDisposalPersonalisationEmail = new LegalRepresentativeBailApplicationStartedDisposalPersonalisationEmail(
            templateId,
            iaExUiFrontendUrl
        );
    }


    @Test
    public void should_return_given_template_id() {
        assertEquals(templateId, legalRepresentativeBailApplicationStartedDisposalPersonalisationEmail.getTemplateId());
    }

    @Test
    public void should_return_given_reference_id() {
        Long caseId = 12345L;
        assertEquals(caseId + "_BAIL_APPLICATION_STARTED_DISPOSAL_LEGAL_REPRESENTATIVE",
            legalRepresentativeBailApplicationStartedDisposalPersonalisationEmail.getReferenceId(caseId));
    }

    @Test
    public void should_return_given_email_address_from_bail_case() {
        assertTrue(legalRepresentativeBailApplicationStartedDisposalPersonalisationEmail.getRecipientsList(bailCase)
            .contains(legalRepEmailAddress));
    }

    @Test
    public void should_throw_exception_when_cannot_find_email_address_for_legal_rep() {
        // given
        when(bailCase.read(BailCaseFieldDefinition.LEGAL_REP_EMAIL, String.class)).thenReturn(Optional.empty());

        // when
        // then
        assertThatThrownBy(() -> legalRepresentativeBailApplicationStartedDisposalPersonalisationEmail.getRecipientsList(bailCase))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("legalRepresentativeEmailAddress is not present");
    }

    @Test
    public void should_throw_exception_on_personalisation_when_case_is_null() {
        assertThatThrownBy(
            () -> legalRepresentativeBailApplicationStartedDisposalPersonalisationEmail.getPersonalisation((BailCase) null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("bailCase must not be null");
    }

    @Test
    public void should_return_personalisation_when_all_information_given() {
        // given
        // when
        Map<String, String> personalisation =
            legalRepresentativeBailApplicationStartedDisposalPersonalisationEmail.getPersonalisation(bailCase);

        // then
        assertEquals(legalRepReference, personalisation.get("legalRepReference"));
        assertEquals(applicantGivenNames, personalisation.get("applicantGivenNames"));
        assertEquals(applicantFamilyName, personalisation.get("applicantFamilyName"));
        assertEquals(homeOfficeReferenceNumber, personalisation.get("homeOfficeReferenceNumber"));
        assertEquals(iaExUiFrontendUrl, personalisation.get("linkToOnlineService"));
        assertNotNull(personalisation.get("creationDate"));
    }

    @Test
    public void should_return_personalisation_when_all_mandatory_information_given() {
        // given
        when(bailCase.read(BailCaseFieldDefinition.APPLICANT_GIVEN_NAMES, String.class)).thenReturn(Optional.empty());
        when(bailCase.read(BailCaseFieldDefinition.APPLICANT_FAMILY_NAME, String.class)).thenReturn(Optional.empty());
        when(bailCase.read(BailCaseFieldDefinition.LEGAL_REP_REFERENCE, String.class)).thenReturn(Optional.empty());
        when(bailCase.read(BailCaseFieldDefinition.HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());

        // when
        Map<String, String> personalisation =
            legalRepresentativeBailApplicationStartedDisposalPersonalisationEmail.getPersonalisation(bailCase);

        // then
        assertEquals("", personalisation.get("legalRepReference"));
        assertEquals("", personalisation.get("applicantGivenNames"));
        assertEquals("", personalisation.get("applicantFamilyName"));
        assertEquals("", personalisation.get("homeOfficeReferenceNumber"));
    }
}

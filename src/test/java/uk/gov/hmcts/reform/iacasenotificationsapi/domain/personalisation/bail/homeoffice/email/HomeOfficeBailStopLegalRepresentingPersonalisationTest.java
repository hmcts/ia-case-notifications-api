package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.bail.homeoffice.email;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.BailCaseFieldDefinition.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class HomeOfficeBailStopLegalRepresentingPersonalisationTest {

    private Long caseId = 12345L;
    private String templateId = "someTemplateId";
    private String homeOfficeEmailAddress = "HO_user@example.com";
    private String bailReferenceNumber = "someReferenceNumber";
    private String legalRepReference = "someLegalRepReference";
    private String legalRepName = "someLegalRepName";
    private String legalRepCompany = "someLegalRepCompany";
    private String legalRepEmail = "someLegalRepEmail";
    private String homeOfficeReferenceNumber = "someHomeOfficeReferenceNumber";
    private String applicantGivenNames = "someApplicantGivenNames";
    private String applicantFamilyName = "someApplicantFamilyName";
    @Mock BailCase bailCase;
    private HomeOfficeBailStopLegalRepresentingPersonalisation homeOfficeBailStopLegalRepresentingPersonalisation;

    @BeforeEach
    public void setup() {

        when(bailCase.read(BAIL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(bailReferenceNumber));
        when(bailCase.read(LEGAL_REP_REFERENCE, String.class)).thenReturn(Optional.of(legalRepReference));
        when(bailCase.read(LEGAL_REP_NAME, String.class)).thenReturn(Optional.of(legalRepName));
        when(bailCase.read(LEGAL_REP_COMPANY, String.class)).thenReturn(Optional.of(legalRepCompany));
        when(bailCase.read(LEGAL_REP_EMAIL, String.class)).thenReturn(Optional.of(legalRepEmail));
        when(bailCase.read(APPLICANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(applicantGivenNames));
        when(bailCase.read(APPLICANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(applicantFamilyName));
        when(bailCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(homeOfficeReferenceNumber));
        when(bailCase.read(IS_LEGALLY_REPRESENTED_FOR_FLAG, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        homeOfficeBailStopLegalRepresentingPersonalisation =
            new HomeOfficeBailStopLegalRepresentingPersonalisation(templateId, homeOfficeEmailAddress);
    }

    @Test
    public void should_return_given_template_id() {
        assertEquals(templateId, homeOfficeBailStopLegalRepresentingPersonalisation.getTemplateId(bailCase));
    }

    @Test
    public void should_return_given_reference_id() {
        assertEquals(caseId + "_BAIL_STOP_LEGAL_REPRESENTING_HOME_OFFICE",
            homeOfficeBailStopLegalRepresentingPersonalisation.getReferenceId(caseId));
    }

    @Test
    public void should_return_given_email_address_from_bail_case() {
        assertTrue(homeOfficeBailStopLegalRepresentingPersonalisation.getRecipientsList(bailCase)
                .contains(homeOfficeEmailAddress));
    }

    @Test
    public void should_throw_exception_on_personalisation_when_case_is_null() {

        assertThatThrownBy(
            () -> homeOfficeBailStopLegalRepresentingPersonalisation.getPersonalisation((BailCase) null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("bailCase must not be null");
    }

    @Test
    public void should_return_personalisation_when_all_information_given() {

        Map<String, String> personalisation =
            homeOfficeBailStopLegalRepresentingPersonalisation.getPersonalisation(bailCase);

        assertEquals(bailReferenceNumber, personalisation.get("bailReferenceNumber"));
        assertEquals(legalRepReference, personalisation.get("legalRepReference"));
        assertEquals(legalRepName, personalisation.get("legalRepName"));
        assertEquals(legalRepCompany, personalisation.get("legalRepCompany"));
        assertEquals(legalRepEmail, personalisation.get("legalRepEmail"));
        assertEquals(applicantGivenNames, personalisation.get("applicantGivenNames"));
        assertEquals(applicantFamilyName, personalisation.get("applicantFamilyName"));
        assertEquals(homeOfficeReferenceNumber, personalisation.get("homeOfficeReferenceNumber"));
    }

    @Test
    public void should_return_personalisation_when_all_mandatory_information_given() {

        when(bailCase.read(BAIL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(bailCase.read(APPLICANT_GIVEN_NAMES, String.class)).thenReturn(Optional.empty());
        when(bailCase.read(APPLICANT_FAMILY_NAME, String.class)).thenReturn(Optional.empty());
        when(bailCase.read(LEGAL_REP_REFERENCE, String.class)).thenReturn(Optional.empty());
        when(bailCase.read(LEGAL_REP_NAME, String.class)).thenReturn(Optional.empty());
        when(bailCase.read(LEGAL_REP_COMPANY, String.class)).thenReturn(Optional.empty());
        when(bailCase.read(LEGAL_REP_EMAIL, String.class)).thenReturn(Optional.empty());
        when(bailCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());

        Map<String, String> personalisation =
            homeOfficeBailStopLegalRepresentingPersonalisation.getPersonalisation(bailCase);

        assertEquals("", personalisation.get("bailReferenceNumber"));
        assertEquals("", personalisation.get("legalRepReference"));
        assertEquals("", personalisation.get("legalRepName"));
        assertEquals("", personalisation.get("legalRepCompany"));
        assertEquals("", personalisation.get("legalRepEmail"));
        assertEquals("", personalisation.get("applicantGivenNames"));
        assertEquals("", personalisation.get("applicantFamilyName"));
        assertEquals("", personalisation.get("homeOfficeReferenceNumber"));
    }

}
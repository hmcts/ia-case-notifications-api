package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.bail.homeoffice.email;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
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
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class HomeOfficeBailSignedDecisionNoticeUploadedPersonalisationTest {

    private Long caseId = 12345L;
    private String templateIdWithLegalRep = "someTemplateIdWithLegalRep";
    private String templateIdWithoutLegalRep = "someTemplateIdWithoutLegalRep";
    private String homeOfficeEmailAddress = "HO_user@example.com";
    private String bailReferenceNumber = "someReferenceNumber";
    private String legalRepReference = "someLegalRepReference";
    private String homeOfficeReferenceNumber = "someHomeOfficeReferenceNumber";
    private String applicantGivenNames = "someApplicantGivenNames";
    private String applicantFamilyName = "someApplicantFamilyName";
    private String decisionGranted = "Granted";
    private String decisionRefused = "Refused";
    @Mock BailCase bailCase;
    private HomeOfficeBailSignedDecisionNoticeUploadedPersonalisation homeOfficeBailSignedDecisionNoticeUploadedPersonalisation;

    @BeforeEach
    public void setup() {

        when(bailCase.read(BAIL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(bailReferenceNumber));
        when(bailCase.read(LEGAL_REP_REFERENCE, String.class)).thenReturn(Optional.of(legalRepReference));
        when(bailCase.read(APPLICANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(applicantGivenNames));
        when(bailCase.read(APPLICANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(applicantFamilyName));
        when(bailCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(homeOfficeReferenceNumber));
        when(bailCase.read(IS_LEGALLY_REPRESENTED_FOR_FLAG, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        homeOfficeBailSignedDecisionNoticeUploadedPersonalisation =
            new HomeOfficeBailSignedDecisionNoticeUploadedPersonalisation(templateIdWithLegalRep, templateIdWithoutLegalRep, homeOfficeEmailAddress);
    }

    @Test
    public void should_return_given_template_id() {
        assertEquals(templateIdWithLegalRep, homeOfficeBailSignedDecisionNoticeUploadedPersonalisation.getTemplateId(bailCase));
    }

    @Test
    public void should_return_given_reference_id() {
        assertEquals(caseId + "_BAIL_UPLOADED_SIGNED_DECISION_NOTICE_HOME_OFFICE",
            homeOfficeBailSignedDecisionNoticeUploadedPersonalisation.getReferenceId(caseId));
    }

    @Test
    public void should_throw_exception_on_personalisation_when_case_is_null() {

        assertThatThrownBy(
            () -> homeOfficeBailSignedDecisionNoticeUploadedPersonalisation.getPersonalisation((BailCase) null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("bailCase must not be null");
    }

    @Test
    public void should_return_personalisation_when_all_information_given_ss_not_needed_bail_granted() {
        when(bailCase.read(SS_CONSENT_DECISION, YesOrNo.class)).thenReturn(Optional.empty());
        when(bailCase.read(DECISION_GRANTED_OR_REFUSED, String.class)).thenReturn(Optional.of("granted"));

        Map<String, String> personalisation =
            homeOfficeBailSignedDecisionNoticeUploadedPersonalisation.getPersonalisation(bailCase);

        assertEquals(bailReferenceNumber, personalisation.get("bailReferenceNumber"));
        assertEquals(legalRepReference, personalisation.get("legalRepReference"));
        assertEquals(applicantGivenNames, personalisation.get("applicantGivenNames"));
        assertEquals(applicantFamilyName, personalisation.get("applicantFamilyName"));
        assertEquals(homeOfficeReferenceNumber, personalisation.get("homeOfficeReferenceNumber"));
        assertEquals(decisionGranted, personalisation.get("decision"));
    }

    @Test
    public void should_return_personalisation_when_all_information_given_ss_not_needed_bail_refused() {
        when(bailCase.read(SS_CONSENT_DECISION, YesOrNo.class)).thenReturn(Optional.empty());
        when(bailCase.read(DECISION_GRANTED_OR_REFUSED, String.class)).thenReturn(Optional.of("refused"));

        Map<String, String> personalisation =
            homeOfficeBailSignedDecisionNoticeUploadedPersonalisation.getPersonalisation(bailCase);

        assertEquals(bailReferenceNumber, personalisation.get("bailReferenceNumber"));
        assertEquals(legalRepReference, personalisation.get("legalRepReference"));
        assertEquals(applicantGivenNames, personalisation.get("applicantGivenNames"));
        assertEquals(applicantFamilyName, personalisation.get("applicantFamilyName"));
        assertEquals(homeOfficeReferenceNumber, personalisation.get("homeOfficeReferenceNumber"));
        assertEquals(decisionRefused, personalisation.get("decision"));
    }

    @Test
    public void should_return_personalisation_when_all_information_given_ss_consented_judge_minded() {
        when(bailCase.read(SS_CONSENT_DECISION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(bailCase.read(RECORD_THE_DECISION_LIST, String.class)).thenReturn(Optional.of("Minded to grant"));

        Map<String, String> personalisation =
            homeOfficeBailSignedDecisionNoticeUploadedPersonalisation.getPersonalisation(bailCase);

        assertEquals(bailReferenceNumber, personalisation.get("bailReferenceNumber"));
        assertEquals(legalRepReference, personalisation.get("legalRepReference"));
        assertEquals(applicantGivenNames, personalisation.get("applicantGivenNames"));
        assertEquals(applicantFamilyName, personalisation.get("applicantFamilyName"));
        assertEquals(homeOfficeReferenceNumber, personalisation.get("homeOfficeReferenceNumber"));
        assertEquals(decisionGranted, personalisation.get("decision"));
    }

    @Test
    public void should_return_personalisation_when_all_information_given_ss_consented_judge_refused() {
        when(bailCase.read(SS_CONSENT_DECISION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(bailCase.read(RECORD_THE_DECISION_LIST, String.class)).thenReturn(Optional.of("Refused"));

        Map<String, String> personalisation =
            homeOfficeBailSignedDecisionNoticeUploadedPersonalisation.getPersonalisation(bailCase);

        assertEquals(bailReferenceNumber, personalisation.get("bailReferenceNumber"));
        assertEquals(legalRepReference, personalisation.get("legalRepReference"));
        assertEquals(applicantGivenNames, personalisation.get("applicantGivenNames"));
        assertEquals(applicantFamilyName, personalisation.get("applicantFamilyName"));
        assertEquals(homeOfficeReferenceNumber, personalisation.get("homeOfficeReferenceNumber"));
        assertEquals(decisionRefused, personalisation.get("decision"));
    }

    @Test
    public void should_return_personalisation_when_all_information_given_ss_refused_judge_refused() {
        when(bailCase.read(SS_CONSENT_DECISION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(bailCase.read(RECORD_THE_DECISION_LIST, String.class)).thenReturn(Optional.of("Refused"));

        Map<String, String> personalisation =
            homeOfficeBailSignedDecisionNoticeUploadedPersonalisation.getPersonalisation(bailCase);

        assertEquals(bailReferenceNumber, personalisation.get("bailReferenceNumber"));
        assertEquals(legalRepReference, personalisation.get("legalRepReference"));
        assertEquals(applicantGivenNames, personalisation.get("applicantGivenNames"));
        assertEquals(applicantFamilyName, personalisation.get("applicantFamilyName"));
        assertEquals(homeOfficeReferenceNumber, personalisation.get("homeOfficeReferenceNumber"));
        assertEquals(decisionRefused, personalisation.get("decision"));
    }

    @Test
    public void should_return_personalisation_when_no_LR_all_information_given() {
        when(bailCase.read(SS_CONSENT_DECISION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(bailCase.read(DECISION_GRANTED_OR_REFUSED, String.class)).thenReturn(Optional.of("Refused"));

        when(bailCase.read(IS_LEGALLY_REPRESENTED_FOR_FLAG, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        Map<String, String> personalisation =
            homeOfficeBailSignedDecisionNoticeUploadedPersonalisation.getPersonalisation(bailCase);

        assertEquals(templateIdWithoutLegalRep, homeOfficeBailSignedDecisionNoticeUploadedPersonalisation.getTemplateId(bailCase));
        assertEquals(bailReferenceNumber, personalisation.get("bailReferenceNumber"));
        assertEquals(applicantGivenNames, personalisation.get("applicantGivenNames"));
        assertEquals(applicantFamilyName, personalisation.get("applicantFamilyName"));
        assertEquals(homeOfficeReferenceNumber, personalisation.get("homeOfficeReferenceNumber"));
        assertEquals(decisionRefused, personalisation.get("decision"));
    }

    @Test
    public void should_return_personalisation_when_all_mandatory_information_given() {
        when(bailCase.read(SS_CONSENT_DECISION, YesOrNo.class)).thenReturn(Optional.empty());
        when(bailCase.read(DECISION_GRANTED_OR_REFUSED, String.class)).thenReturn(Optional.empty());

        when(bailCase.read(BAIL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(bailCase.read(APPLICANT_GIVEN_NAMES, String.class)).thenReturn(Optional.empty());
        when(bailCase.read(APPLICANT_FAMILY_NAME, String.class)).thenReturn(Optional.empty());
        when(bailCase.read(LEGAL_REP_REFERENCE, String.class)).thenReturn(Optional.empty());
        when(bailCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());

        Map<String, String> personalisation =
            homeOfficeBailSignedDecisionNoticeUploadedPersonalisation.getPersonalisation(bailCase);

        assertEquals("", personalisation.get("bailReferenceNumber"));
        assertEquals("", personalisation.get("legalRepReference"));
        assertEquals("", personalisation.get("applicantGivenNames"));
        assertEquals("", personalisation.get("applicantFamilyName"));
        assertEquals("", personalisation.get("homeOfficeReferenceNumber"));
        assertEquals("", personalisation.get("decision"));
    }

}

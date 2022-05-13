package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.bail.applicant;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.BailCaseFieldDefinition.APPLICANT_MOBILE_NUMBER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.BailCaseFieldDefinition.BAIL_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.BailCaseFieldDefinition.DECISION_GRANTED_OR_REFUSED;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.BailCaseFieldDefinition.RECORD_THE_DECISION_LIST;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.BailCaseFieldDefinition.SS_CONSENT_DECISION;

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
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.bail.applicant.sms.ApplicantBailSignedDecisionNoticeUploadedPersonalisationSms;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ApplicantBailSignedDecisionNoticeUploadedPersonalisationSmsTest {

    private final String smsTemplateId = "someTemplateId";
    private String mobileNumber = "111 111 111";
    private final String bailReferenceNumber = "someReferenceNumber";
    private final String yes = "yes";
    private final String no = "no";

    @Mock
    BailCase bailCase;

    private ApplicantBailSignedDecisionNoticeUploadedPersonalisationSms applicantBailSignedDecisionNoticeUploadedPersonalisationSms;

    @BeforeEach
    public void setup() {

        when(bailCase.read(APPLICANT_MOBILE_NUMBER, String.class)).thenReturn(Optional.of(mobileNumber));
        when(bailCase.read(BAIL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(bailReferenceNumber));

        applicantBailSignedDecisionNoticeUploadedPersonalisationSms =
            new ApplicantBailSignedDecisionNoticeUploadedPersonalisationSms(smsTemplateId);
    }

    @Test
    public void should_return_given_template_id() {
        assertEquals(smsTemplateId, applicantBailSignedDecisionNoticeUploadedPersonalisationSms.getTemplateId());
    }

    @Test
    public void should_return_given_reference_id() {
        Long caseId = 12345L;
        assertEquals(caseId + "_BAIL_SIGNED_DECISION_NOTICE_UPLOADED_APPLICANT_SMS",
            applicantBailSignedDecisionNoticeUploadedPersonalisationSms.getReferenceId(caseId));
    }

    @Test
    public void should_return_given_mobile_number() {
        assertTrue(
            applicantBailSignedDecisionNoticeUploadedPersonalisationSms.getRecipientsList(bailCase).contains(mobileNumber));

        when(bailCase.read(APPLICANT_MOBILE_NUMBER, String.class)).thenReturn(Optional.empty());

        assertTrue(applicantBailSignedDecisionNoticeUploadedPersonalisationSms.getRecipientsList(bailCase).isEmpty());
    }

    @Test
    public void should_throw_exception_on_personalisation_when_case_is_null() {

        assertThatThrownBy(
            () -> applicantBailSignedDecisionNoticeUploadedPersonalisationSms.getPersonalisation((BailCase) null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("bailCase must not be null");
    }

    @Test
    public void should_return_personalisation_when_all_information_given_and_bail_granted_ss_not_needed() {
        when(bailCase.read(DECISION_GRANTED_OR_REFUSED, String.class)).thenReturn(Optional.of("granted"));
        when(bailCase.read(SS_CONSENT_DECISION, YesOrNo.class)).thenReturn(Optional.empty());

        Map<String, String> personalisation =
            applicantBailSignedDecisionNoticeUploadedPersonalisationSms.getPersonalisation(bailCase);

        assertEquals(bailReferenceNumber, personalisation.get("bailReferenceNumber"));
        assertEquals(yes, personalisation.get("granted"));
        assertEquals(no, personalisation.get("refused"));
    }

    @Test
    public void should_return_personalisation_when_all_information_given_and_bail_refused_ss_not_needed() {
        when(bailCase.read(DECISION_GRANTED_OR_REFUSED, String.class)).thenReturn(Optional.of("refused"));
        when(bailCase.read(SS_CONSENT_DECISION, YesOrNo.class)).thenReturn(Optional.empty());

        Map<String, String> personalisation =
            applicantBailSignedDecisionNoticeUploadedPersonalisationSms.getPersonalisation(bailCase);

        assertEquals(bailReferenceNumber, personalisation.get("bailReferenceNumber"));
        assertEquals(no, personalisation.get("granted"));
        assertEquals(yes, personalisation.get("refused"));
    }

    @Test
    public void should_return_personalisation_when_all_information_given_and_judge_minded_ss_consented() {
        when(bailCase.read(RECORD_THE_DECISION_LIST, String.class)).thenReturn(Optional.of("Minded to grant"));
        when(bailCase.read(SS_CONSENT_DECISION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));

        Map<String, String> personalisation =
            applicantBailSignedDecisionNoticeUploadedPersonalisationSms.getPersonalisation(bailCase);

        assertEquals(bailReferenceNumber, personalisation.get("bailReferenceNumber"));
        assertEquals(yes, personalisation.get("granted"));
        assertEquals(no, personalisation.get("refused"));
    }

    @Test
    public void should_return_personalisation_when_all_information_given_and_judge_minded_ss_refused() {
        when(bailCase.read(RECORD_THE_DECISION_LIST, String.class)).thenReturn(Optional.of("Minded to grant"));
        when(bailCase.read(SS_CONSENT_DECISION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));

        Map<String, String> personalisation =
            applicantBailSignedDecisionNoticeUploadedPersonalisationSms.getPersonalisation(bailCase);

        assertEquals(bailReferenceNumber, personalisation.get("bailReferenceNumber"));
        assertEquals(no, personalisation.get("granted"));
        assertEquals(yes, personalisation.get("refused"));
    }

    @Test
    public void should_return_personalisation_when_all_information_given_and_judge_refused_ss_refused() {
        when(bailCase.read(RECORD_THE_DECISION_LIST, String.class)).thenReturn(Optional.of("Refused"));
        when(bailCase.read(SS_CONSENT_DECISION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));

        Map<String, String> personalisation =
            applicantBailSignedDecisionNoticeUploadedPersonalisationSms.getPersonalisation(bailCase);

        assertEquals(bailReferenceNumber, personalisation.get("bailReferenceNumber"));
        assertEquals(no, personalisation.get("granted"));
        assertEquals(yes, personalisation.get("refused"));
    }

    @Test
    public void should_return_personalisation_when_only_mandatory_information_given() {
        when(bailCase.read(BAIL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(bailCase.read(RECORD_THE_DECISION_LIST, String.class)).thenReturn(Optional.empty());
        when(bailCase.read(SS_CONSENT_DECISION, YesOrNo.class)).thenReturn(Optional.empty());
        when(bailCase.read(DECISION_GRANTED_OR_REFUSED, String.class)).thenReturn(Optional.empty());

        Map<String, String> personalisation =
            applicantBailSignedDecisionNoticeUploadedPersonalisationSms.getPersonalisation(bailCase);

        assertEquals("", personalisation.get("bailReferenceNumber"));
        assertEquals("", personalisation.get("granted"));
        assertEquals("", personalisation.get("refused"));
    }
}

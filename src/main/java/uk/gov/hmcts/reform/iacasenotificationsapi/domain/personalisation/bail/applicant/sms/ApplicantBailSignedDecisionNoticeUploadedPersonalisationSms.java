package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.bail.applicant.sms;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.BailCaseFieldDefinition.DECISION_GRANTED_OR_REFUSED;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.BailCaseFieldDefinition.RECORD_THE_DECISION_LIST;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.BailCaseFieldDefinition.SS_CONSENT_DECISION;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.BailCaseFieldDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.bail.applicant.ApplicantBailSmsNotificationPersonalisation;

@Service
public class ApplicantBailSignedDecisionNoticeUploadedPersonalisationSms implements ApplicantBailSmsNotificationPersonalisation {

    private final String signedDecisionNoticeUploadedApplicantSmsTemplateId;
    private static final String YES = "yes";
    private static final String NO = "no";

    public ApplicantBailSignedDecisionNoticeUploadedPersonalisationSms(
        @Value("${govnotify.bail.template.uploadSignedDecisionNotice.sms}") String signedDecisionNoticeUploadedApplicantSmsTemplateId) {
        this.signedDecisionNoticeUploadedApplicantSmsTemplateId = signedDecisionNoticeUploadedApplicantSmsTemplateId;
    }

    @Override
    public String getTemplateId() {
        return signedDecisionNoticeUploadedApplicantSmsTemplateId;
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_BAIL_SIGNED_DECISION_NOTICE_UPLOADED_APPLICANT_SMS";
    }

    @Override
    public Map<String, String> getPersonalisation(BailCase bailCase) {
        requireNonNull(bailCase, "bailCase must not be null");

        return ImmutableMap
            .<String, String>builder()
            .put("bailReferenceNumber", bailCase.read(BailCaseFieldDefinition.BAIL_REFERENCE_NUMBER, String.class).orElse(""))
            .put("granted", Boolean.TRUE.equals(isBailGranted(bailCase)) ? YES : Boolean.FALSE.equals(isBailGranted(bailCase)) ? NO : "")
            .put("refused", Boolean.FALSE.equals(isBailGranted(bailCase)) ? YES : Boolean.TRUE.equals(isBailGranted(bailCase)) ? NO : "")
            .build();
    }

    private Boolean isBailGranted(BailCase bailCase) {
        if (bailCase.read(DECISION_GRANTED_OR_REFUSED, String.class).orElse("").equals("granted") ||
               (bailCase.read(RECORD_THE_DECISION_LIST, String.class).orElse("").equals("Minded to grant") &&
                bailCase.read(SS_CONSENT_DECISION, YesOrNo.class).orElse(YesOrNo.NO) == YesOrNo.YES)) {
            return true;
        }
        if (bailCase.read(DECISION_GRANTED_OR_REFUSED, String.class).orElse("").equals("refused") ||
                bailCase.read(RECORD_THE_DECISION_LIST, String.class).orElse("").equals("Refused") ||
                bailCase.read(SS_CONSENT_DECISION, YesOrNo.class).orElse(YesOrNo.YES) == YesOrNo.NO) {
            return false;
        }
        return null;
    }
}

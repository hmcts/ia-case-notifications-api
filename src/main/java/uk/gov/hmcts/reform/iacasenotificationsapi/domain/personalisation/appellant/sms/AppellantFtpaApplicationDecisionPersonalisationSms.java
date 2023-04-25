package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.sms;

import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.FTPA_APPLICANT_TYPE;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.NotificationType;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.SmsNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.utils.FtpaNotificationPersonalisationUtil;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.RecipientsFinder;

@Service
public class AppellantFtpaApplicationDecisionPersonalisationSms implements SmsNotificationPersonalisation, FtpaNotificationPersonalisationUtil {

    private final String ftpaRespondentDecisionGrantedPartiallyGrantedToAppellantSmsTemplateId;
    private final String ftpaRespondentDecisionGrantedNotAdmittedToAppellantSmsTemplateId;
    private final String ftpaRespondentDecisionGrantedRefusedToAppellantSmsTemplateId;
    private final String ftpaAppellantDecisionGrantedPartiallyGrantedToAppellantSmsTemplateId;
    private final String iaAipFrontendUrl;
    private final RecipientsFinder recipientsFinder;

    public AppellantFtpaApplicationDecisionPersonalisationSms(
        @Value("${govnotify.template.applicationGranted.otherParty.citizen.sms}") String ftpaRespondentDecisionGrantedPartiallyGrantedToAppellantSmsTemplateId,
        @Value("${govnotify.template.applicationNotAdmitted.otherParty.citizen.sms}") String ftpaRespondentDecisionGrantedNotAdmittedToAppellantSmsTemplateId,
        @Value("${govnotify.template.applicationRefused.otherParty.citizen.sms}") String ftpaRespondentDecisionGrantedRefusedToAppellantSmsTemplateId,
        @Value("${govnotify.template.applicationGranted.applicant.citizen.sms}") String ftpaAppellantDecisionGrantedPartiallyGrantedToAppellantSmsTemplateId,
        @Value("${iaAipFrontendUrl}") String iaAipFrontendUrl,
        RecipientsFinder recipientsFinder
    ) {
        this.ftpaRespondentDecisionGrantedPartiallyGrantedToAppellantSmsTemplateId = ftpaRespondentDecisionGrantedPartiallyGrantedToAppellantSmsTemplateId;
        this.ftpaRespondentDecisionGrantedNotAdmittedToAppellantSmsTemplateId = ftpaRespondentDecisionGrantedNotAdmittedToAppellantSmsTemplateId;
        this.ftpaRespondentDecisionGrantedRefusedToAppellantSmsTemplateId = ftpaRespondentDecisionGrantedRefusedToAppellantSmsTemplateId;
        this.ftpaAppellantDecisionGrantedPartiallyGrantedToAppellantSmsTemplateId = ftpaAppellantDecisionGrantedPartiallyGrantedToAppellantSmsTemplateId;
        this.iaAipFrontendUrl = iaAipFrontendUrl;
        this.recipientsFinder = recipientsFinder;
    }

    @Override
    public String getTemplateId(AsylumCase asylumCase) {
        String applicantType = asylumCase.read(FTPA_APPLICANT_TYPE, String.class)
            .orElseThrow(() -> new IllegalStateException("ftpaApplicantType is not present"));

        switch (getDecisionOutcomeType(asylumCase)) {
            case FTPA_GRANTED:
            case FTPA_PARTIALLY_GRANTED:
                return applicantType.equals(APPELLANT_APPLICANT)
                    ? ftpaAppellantDecisionGrantedPartiallyGrantedToAppellantSmsTemplateId
                    : ftpaRespondentDecisionGrantedPartiallyGrantedToAppellantSmsTemplateId;
            case FTPA_REFUSED:
                return ftpaRespondentDecisionGrantedRefusedToAppellantSmsTemplateId;
            default:
                return ftpaRespondentDecisionGrantedNotAdmittedToAppellantSmsTemplateId;
        }
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_FTPA_APPLICATION_DECISION_TO_APPELLANT_SMS";
    }

    @Override
    public Set<String> getRecipientsList(final AsylumCase asylumCase) {
        return recipientsFinder.findAll(asylumCase, NotificationType.SMS);
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {

        return ImmutableMap
            .<String, String>builder()
            .put("appealReferenceNumber", asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
            .put("applicationDecision", ftpaDecisionVerbalization(getDecisionOutcomeType(asylumCase)))
            .put("linkToService", iaAipFrontendUrl)
            .build();
    }
}

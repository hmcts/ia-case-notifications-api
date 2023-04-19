package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.sms;

import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.utils.PersonalisationUtil.ftpaRespondentDecisionVerbalization;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.NotificationType;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.SmsNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.RecipientsFinder;

@Service
public class AppellantRespondentFtpaApplicationDecisionPersonalisationSms implements SmsNotificationPersonalisation {

    private final String appellantFtpaRespondentDecisionSmsTemplateId;
    private final String iaAipFrontendUrl;
    private final RecipientsFinder recipientsFinder;

    public AppellantRespondentFtpaApplicationDecisionPersonalisationSms(
        @Value("${govnotify.template.ftpaRespondentDecisionGrantedPartiallyGranted.applicant.sms}") String appellantFtpaRespondentDecisionSmsTemplateId,
        @Value("${iaAipFrontendUrl}") String iaAipFrontendUrl,
        RecipientsFinder recipientsFinder
    ) {
        this.appellantFtpaRespondentDecisionSmsTemplateId = appellantFtpaRespondentDecisionSmsTemplateId;
        this.iaAipFrontendUrl = iaAipFrontendUrl;
        this.recipientsFinder = recipientsFinder;
    }

    @Override
    public String getTemplateId() {
        return appellantFtpaRespondentDecisionSmsTemplateId;
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_RESPONDENT_FTPA_APPLICATION_DECISION_TO_APPELLANT_SMS";
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
            .put("applicationDecision", ftpaRespondentDecisionVerbalization(asylumCase))
            .put("linkToService", iaAipFrontendUrl)
            .build();
    }
}

package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.sms;

import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.NotificationType;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.SmsNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.RecipientsFinder;

import java.util.Map;
import java.util.Set;

import static java.util.Objects.requireNonNull;

@Service
public class AppellantRespondQueryPersonalisationSms implements SmsNotificationPersonalisation {

    private final String templateId;
    private final RecipientsFinder recipientsFinder;
    private final String iaAipFrontendUrl;

    public AppellantRespondQueryPersonalisationSms(
        @Value("${govnotify.template.queryResponseFromTribunal.aip.sms}") String templateId,
        @Value("${iaAipFrontendUrl}") String iaAipFrontendUrl,
        RecipientsFinder recipientsFinder
    ) {
        this.templateId = templateId;
        this.iaAipFrontendUrl = iaAipFrontendUrl;
        this.recipientsFinder = recipientsFinder;
    }

    @Override
    public String getTemplateId(AsylumCase asylumCase) {
        return templateId;
    }

    @Override
    public Set<String> getRecipientsList(final AsylumCase asylumCase) {
        return recipientsFinder.findAll(asylumCase, NotificationType.SMS);
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_AIP_TRIBUNAL_RESPONDED_QUERY_SMS_NOTIFICATION";
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");

        return ImmutableMap.<String, String>builder().put("appealReferenceNumber",
                asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
            .put("appellantGivenNames",
                asylumCase.read(AsylumCaseDefinition.APPELLANT_GIVEN_NAMES, String.class).orElse(""))
            .put("appellantFamilyName",
                asylumCase.read(AsylumCaseDefinition.APPELLANT_FAMILY_NAME, String.class).orElse(""))
            .put("linkToOnlineService", iaAipFrontendUrl)
            .put("viewMessagesUrl", iaAipFrontendUrl + "/view-messages")
            .build();
    }
}

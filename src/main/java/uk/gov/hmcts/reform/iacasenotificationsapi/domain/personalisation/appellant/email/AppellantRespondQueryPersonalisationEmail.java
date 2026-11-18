package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.email;

import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.NotificationType;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.EmailNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.RecipientsFinder;

import java.util.Map;
import java.util.Set;

import static java.util.Objects.requireNonNull;


@Service
public class AppellantRespondQueryPersonalisationEmail implements EmailNotificationPersonalisation {

    private final String templateId;
    private final String iaAipFrontendUrl;
    private final RecipientsFinder recipientsFinder;

    public AppellantRespondQueryPersonalisationEmail(
        @Value("${govnotify.template.queryResponseFromTribunal.aip.email}") String templateId,
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
        return recipientsFinder.findAll(asylumCase, NotificationType.EMAIL);
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_AIP_TRIBUNAL_RESPONDED_QUERY_EMAIL_NOTIFICATION";
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
            .put("viewMessagesUrl", iaAipFrontendUrl + "/view-queries")
            .build();
    }
}

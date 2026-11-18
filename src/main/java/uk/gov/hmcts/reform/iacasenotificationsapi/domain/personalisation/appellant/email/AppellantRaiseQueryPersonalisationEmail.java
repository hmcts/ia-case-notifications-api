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
public class AppellantRaiseQueryPersonalisationEmail implements EmailNotificationPersonalisation {

    private final String appellantRaiseQueryTemplateId;
    private final String iaAipFrontendUrl;
    private final RecipientsFinder recipientsFinder;

    public AppellantRaiseQueryPersonalisationEmail(
        @Value("${govnotify.template.raiseQueryToTribunal.aip.email}") String appellantRaiseQueryTemplateId,
        @Value("${iaAipFrontendUrl}") String iaAipFrontendUrl,
        RecipientsFinder recipientsFinder
    ) {
        this.appellantRaiseQueryTemplateId = appellantRaiseQueryTemplateId;
        this.iaAipFrontendUrl = iaAipFrontendUrl;
        this.recipientsFinder = recipientsFinder;
    }

    @Override
    public String getTemplateId(AsylumCase asylumCase) {
        return appellantRaiseQueryTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(final AsylumCase asylumCase) {
        return recipientsFinder.findAll(asylumCase, NotificationType.EMAIL);
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_AIP_RAISE_QUERY_EMAIL_NOTIFICATION";
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

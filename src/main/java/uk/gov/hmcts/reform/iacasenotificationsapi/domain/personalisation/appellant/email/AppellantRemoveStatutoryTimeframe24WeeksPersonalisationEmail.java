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
public class AppellantRemoveStatutoryTimeframe24WeeksPersonalisationEmail implements EmailNotificationPersonalisation {

    private final String removeStatutoryTimeframe24WeeksAppellantTemplateId;
    private final String iaExUiFrontendUrl;
    private final RecipientsFinder recipientsFinder;

    public AppellantRemoveStatutoryTimeframe24WeeksPersonalisationEmail(
            @Value("${govnotify.template.removeStatutoryTimeframe24Weeks.appellant.email}") String removeStatutoryTimeframe24WeeksAppellantTemplateId,
            @Value("${iaExUiFrontendUrl}") String iaExUiFrontendUrl,
            RecipientsFinder recipientsFinder
    ) {
        this.removeStatutoryTimeframe24WeeksAppellantTemplateId = removeStatutoryTimeframe24WeeksAppellantTemplateId;
        this.iaExUiFrontendUrl = iaExUiFrontendUrl;
        this.recipientsFinder = recipientsFinder;
    }

    @Override
    public String getTemplateId() {
        return removeStatutoryTimeframe24WeeksAppellantTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(final AsylumCase asylumCase) {
        return recipientsFinder.findAll(asylumCase, NotificationType.EMAIL);

    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_REMOVE_STATUTORY_TIMEFRAME_24WEEKS_APPELLANT_EMAIL";
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");

        return ImmutableMap
                .<String, String>builder()
                .put("customerServicesTelephone", "1234")
                .put("customerServicesEmail", "customerServicesEmail@xyz.com")
                .put("AppealIAEmail", "AppealIAEmail@xyz.com")
                .put("email_address", "emailaddress1@xyz.com")
                .put("homeOfficeReferenceNumber", "1212121212")
                .put("appealReferenceNumber", "1212121212")
                .put("ariaListingReference","1212121212")
                .put("legalRepReferenceNumber", "legalRepReferenceNumber1")
                .put("appellantGivenNames", asylumCase.read(AsylumCaseDefinition.APPELLANT_GIVEN_NAMES, String.class).orElse(""))
                .put("appellantFamilyName", asylumCase.read(AsylumCaseDefinition.APPELLANT_FAMILY_NAME, String.class).orElse(""))
                .put("transferOutOfAdaReason", "transferOutOfAdaReason1")
                .put("linkToOnlineService", iaExUiFrontendUrl)
                .build();
    }
}

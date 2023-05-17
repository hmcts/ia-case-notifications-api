package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.email;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.NotificationType;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.EmailNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.RecipientsFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.PersonalisationProvider;

@Service
public class AppellantNonStandardDirectionOfHomeOfficePersonalisationEmail implements EmailNotificationPersonalisation {

    private final String iaAipFrontendUrl;
    private final PersonalisationProvider personalisationProvider;
    private final String appellantNonStandardDirectionBeforeListingTemplateId;
    private final String appellantNonStandardDirectionAfterListingTemplateId;
    private final CustomerServicesProvider customerServicesProvider;
    private final RecipientsFinder recipientsFinder;

    public AppellantNonStandardDirectionOfHomeOfficePersonalisationEmail(
            @Value("${govnotify.template.nonStandardDirectionOfHomeOfficeBeforeListing.appellant.email}") String appellantNonStandardDirectionBeforeListingTemplateId,
            @Value("${govnotify.template.nonStandardDirectionOfHomeOfficeAfterListing.appellant.email}") String appellantNonStandardDirectionAfterListingTemplateId,
            @Value("${iaAipFrontendUrl}") String iaAipFrontendUrl,
            PersonalisationProvider personalisationProvider,
            CustomerServicesProvider customerServicesProvider,
            RecipientsFinder recipientsFinder
    ) {
        this.iaAipFrontendUrl = iaAipFrontendUrl;
        this.appellantNonStandardDirectionBeforeListingTemplateId = appellantNonStandardDirectionBeforeListingTemplateId;
        this.appellantNonStandardDirectionAfterListingTemplateId = appellantNonStandardDirectionAfterListingTemplateId;
        this.personalisationProvider = personalisationProvider;
        this.customerServicesProvider = customerServicesProvider;
        this.recipientsFinder = recipientsFinder;
    }

    @Override
    public String getTemplateId(AsylumCase asylumCase) {
        return isAppealListed(asylumCase)
                ? appellantNonStandardDirectionAfterListingTemplateId : appellantNonStandardDirectionBeforeListingTemplateId;
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_APPELLANT_NON_STANDARD_DIRECTION";
    }

    @Override
    public Set<String> getRecipientsList(final AsylumCase asylumCase) {
        return recipientsFinder.findAll(asylumCase, NotificationType.EMAIL);

    }

    @Override
    public Map<String, String> getPersonalisation(Callback<AsylumCase> callback) {
        requireNonNull(callback, "callback must not be null");

        final ImmutableMap.Builder<String, String> listCaseFields = ImmutableMap
                .<String, String>builder()
                .putAll(customerServicesProvider.getCustomerServicesPersonalisation())
                .put("linkToOnlineService", iaAipFrontendUrl)
                .putAll(personalisationProvider.getPersonalisation(callback));

        return listCaseFields.build();
    }

    protected boolean isAppealListed(AsylumCase asylumCase) {
        final Optional<HearingCentre> appealListed = asylumCase
                .read(AsylumCaseDefinition.LIST_CASE_HEARING_CENTRE, HearingCentre.class);

        return appealListed.isPresent();
    }
}

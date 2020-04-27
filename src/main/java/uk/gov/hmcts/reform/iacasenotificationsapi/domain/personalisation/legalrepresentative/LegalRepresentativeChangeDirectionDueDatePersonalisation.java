package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.legalrepresentative;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableMap;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javax.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.EmailNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.EmailAddressFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.PersonalisationProvider;

@Service
public class LegalRepresentativeChangeDirectionDueDatePersonalisation implements EmailNotificationPersonalisation {

    private static final String legalRepChangeDirectionDueDateSuffix = "_LEGAL_REP_CHANGE_DIRECTION_DUE_DATE";

    private final String legalRepChangeDirectionDueDateBeforeListingTemplateId;
    private final String legalRepChangeDirectionDueDateAfterListingTemplateId;
    private final String iaExUiFrontendUrl;
    private final PersonalisationProvider personalisationProvider;
    private final EmailAddressFinder emailAddressFinder;
    private final CustomerServicesProvider customerServicesProvider;


    public LegalRepresentativeChangeDirectionDueDatePersonalisation(
        @NotNull(message = "legalRepChangeDirectionDueDateTemplateId cannot be null") @Value("${govnotify.template.changeDirectionDueDateBeforeListing.legalRep.email}") String legalRepChangeDirectionDueDateBeforeListingTemplateId,
        @NotNull(message = "legalRepChangeDirectionDueDateTemplateId cannot be null") @Value("${govnotify.template.changeDirectionDueDateAfterListing.legalRep.email}") String legalRepChangeDirectionDueDateAfterListingTemplateId,
        @Value("${iaExUiFrontendUrl}") String iaExUiFrontendUrl,
        PersonalisationProvider personalisationProvider,
        EmailAddressFinder emailAddressFinder,
        CustomerServicesProvider customerServicesProvider
    ) {
        this.legalRepChangeDirectionDueDateBeforeListingTemplateId = legalRepChangeDirectionDueDateBeforeListingTemplateId;
        this.legalRepChangeDirectionDueDateAfterListingTemplateId = legalRepChangeDirectionDueDateAfterListingTemplateId;
        this.iaExUiFrontendUrl = iaExUiFrontendUrl;
        this.personalisationProvider = personalisationProvider;
        this.emailAddressFinder = emailAddressFinder;
        this.customerServicesProvider = customerServicesProvider;
    }

    @Override
    public String getTemplateId(AsylumCase asylumCase) {
        return isAppealListed(asylumCase)
            ? legalRepChangeDirectionDueDateAfterListingTemplateId : legalRepChangeDirectionDueDateBeforeListingTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        return Collections.singleton(emailAddressFinder.getLegalRepEmailAddress(asylumCase));
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + legalRepChangeDirectionDueDateSuffix;
    }

    @Override
    public Map<String, String> getPersonalisation(Callback<AsylumCase> callback) {
        requireNonNull(callback, "callback must not be null");

        final ImmutableMap.Builder<String, String> listCaseFields = ImmutableMap
            .<String, String>builder()
            .putAll(customerServicesProvider.getCustomerServicesPersonalisation())
            .put("linkToOnlineService", iaExUiFrontendUrl)
            .putAll(personalisationProvider.getPersonalisation(callback));

        return listCaseFields.build();
    }

    protected boolean isAppealListed(AsylumCase asylumCase) {

        final Optional<HearingCentre> appealListed = asylumCase
            .read(AsylumCaseDefinition.LIST_CASE_HEARING_CENTRE, HearingCentre.class);

        return appealListed.isPresent();
    }
}

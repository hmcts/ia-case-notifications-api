package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.applyforcosts;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.ARIA_LISTING_REFERENCE;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.isHomeOfficeApplicant;

import com.google.common.collect.ImmutableMap;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.EmailNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.EmailAddressFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.PersonalisationProvider;

@Service
public class AdditionalEvidenceSubmittedLRCostsPersonilisation implements EmailNotificationPersonalisation {

    private final String additionalEvidenceForLegalRepTemplateId;
    private final String homeOfficeEmailAddress;
    private final EmailAddressFinder emailAddressFinder;
    private final PersonalisationProvider personalisationProvider;
    private final CustomerServicesProvider customerServicesProvider;

    public AdditionalEvidenceSubmittedLRCostsPersonilisation(
        @Value("${govnotify.template.addEvidenceForCosts.respondent.email}") String additionalEvidenceForLegalRepTemplateId,
        @Value("${applyForCostsHomeOfficeEmailAddress}") String homeOfficeEmailAddress,
        EmailAddressFinder emailAddressFinder,
        CustomerServicesProvider customerServicesProvider,
        PersonalisationProvider personalisationProvider
    ) {
        this.additionalEvidenceForLegalRepTemplateId = additionalEvidenceForLegalRepTemplateId;
        this.homeOfficeEmailAddress = homeOfficeEmailAddress;
        this.emailAddressFinder = emailAddressFinder;
        this.customerServicesProvider = customerServicesProvider;
        this.personalisationProvider = personalisationProvider;
    }

    @Override
    public String getTemplateId(AsylumCase asylumCase) {
        return additionalEvidenceForLegalRepTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        if (isHomeOfficeApplicant(asylumCase)) {
            return Collections.singleton(emailAddressFinder.getLegalRepEmailAddress(asylumCase));
        } else {
            return Collections.singleton(homeOfficeEmailAddress);
        }
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_RESPOND_TO_COSTS_RESPONDENT_EMAIL";
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");

        ImmutableMap.Builder<String, String> personalisationBuilder = ImmutableMap
            .<String, String>builder()
            .putAll(personalisationProvider.getApplyForCostsPesonalisation(asylumCase))
            .putAll(customerServicesProvider.getCustomerServicesPersonalisation())
            .putAll(personalisationProvider.getRespondToCostsApplicationNumber(asylumCase))
            .put("ariaListingReference", asylumCase.read(ARIA_LISTING_REFERENCE, String.class).orElse(""));

        if (isHomeOfficeApplicant(asylumCase)) {
            personalisationBuilder.putAll(personalisationProvider.getHomeOfficeRecipientHeader(asylumCase));
        } else {
            personalisationBuilder.putAll(personalisationProvider.getLegalRepRecipientHeader(asylumCase));
        }

        return personalisationBuilder.build();
    }
}

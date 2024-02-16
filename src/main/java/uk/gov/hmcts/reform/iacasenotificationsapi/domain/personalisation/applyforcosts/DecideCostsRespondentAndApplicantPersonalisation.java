package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.applyforcosts;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;

import com.google.common.collect.ImmutableMap;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.EmailNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.EmailAddressFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.PersonalisationProvider;

@Service
public class DecideCostsRespondentAndApplicantPersonalisation implements EmailNotificationPersonalisation {

    private final String decideCostsNotificationForRespondentTemplateId;
    private final String homeOfficeEmailAddress;
    private final EmailAddressFinder emailAddressFinder;
    private final PersonalisationProvider personalisationProvider;
    private final CustomerServicesProvider customerServicesProvider;

    public DecideCostsRespondentAndApplicantPersonalisation(
        @Value("${govnotify.template.decideCostsApplication.respondent.email}") String decideCostsNotificationForRespondentTemplateId,
        @Value("${applyForCostsHomeOfficeEmailAddress}") String homeOfficeEmailAddress,
        EmailAddressFinder emailAddressFinder,
        CustomerServicesProvider customerServicesProvider,
        PersonalisationProvider personalisationProvider
    ) {
        this.decideCostsNotificationForRespondentTemplateId = decideCostsNotificationForRespondentTemplateId;
        this.homeOfficeEmailAddress = homeOfficeEmailAddress;
        this.emailAddressFinder = emailAddressFinder;
        this.customerServicesProvider = customerServicesProvider;
        this.personalisationProvider = personalisationProvider;
    }

    @Override
    public String getTemplateId(AsylumCase asylumCase) {
        return decideCostsNotificationForRespondentTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        return new HashSet<>(Arrays.asList(homeOfficeEmailAddress, emailAddressFinder.getLegalRepEmailAddress(asylumCase)));
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_DECIDE_A_COSTS_EMAIL";
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");

        ImmutableMap.Builder<String, String> personalisationBuilder = ImmutableMap
            .<String, String>builder()
            .putAll(personalisationProvider.getApplyForCostsPersonalisation(asylumCase))
            .putAll(personalisationProvider.getDecideCostsPersonalisation(asylumCase))
            .putAll(customerServicesProvider.getCustomerServicesPersonalisation())
            .putAll(personalisationProvider.getTypeForSelectedApplyForCosts(asylumCase, DECIDE_COSTS_APPLICATION_LIST))
            .putAll(personalisationProvider.retrieveSelectedApplicationId(asylumCase, DECIDE_COSTS_APPLICATION_LIST))
            .put("homeOfficeReferenceNumber", asylumCase.read(AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER, String.class).orElse(""));

        return personalisationBuilder.build();
    }
}


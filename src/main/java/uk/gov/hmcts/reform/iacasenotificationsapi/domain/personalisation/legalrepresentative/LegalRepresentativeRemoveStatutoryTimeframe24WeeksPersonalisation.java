package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.legalrepresentative;

import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.EmailNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;

import javax.validation.constraints.NotNull;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.getLegalRepEmailInternalOrLegalRepJourney;

@Service
@Slf4j
public class LegalRepresentativeRemoveStatutoryTimeframe24WeeksPersonalisation implements EmailNotificationPersonalisation {

    private final String removeStatutoryTimeframe24WeeksLegalRepresentativeTemplateId;
    private final String iaExUiFrontendUrl;
    private final CustomerServicesProvider customerServicesProvider;
    private final String nonAdaPrefix;

    public LegalRepresentativeRemoveStatutoryTimeframe24WeeksPersonalisation(
            @NotNull(message = "removeStatutoryTimeframe24WeeksLegalRepresentativeTemplateId cannot be null")
            @Value("${govnotify.template.removeStatutoryTimeframe24Weeks.legalRep.email}") String removeStatutoryTimeframe24WeeksLegalRepresentativeTemplateId,
            @Value("${iaExUiFrontendUrl}") String iaExUiFrontendUrl,
            CustomerServicesProvider customerServicesProvider, @Value("${govnotify.emailPrefix.nonAda}") String nonAdaPrefix) {
        this.removeStatutoryTimeframe24WeeksLegalRepresentativeTemplateId = removeStatutoryTimeframe24WeeksLegalRepresentativeTemplateId;
        this.iaExUiFrontendUrl = iaExUiFrontendUrl;
        this.customerServicesProvider = customerServicesProvider;
        this.nonAdaPrefix = nonAdaPrefix;
    }

    @Override
    public String getTemplateId(AsylumCase asylumCase) {
        return removeStatutoryTimeframe24WeeksLegalRepresentativeTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        return Collections.singleton(getLegalRepEmailInternalOrLegalRepJourney(asylumCase));
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_REMOVE_STATUTORY_TIMEFRAME_24WEEKS_LEGAL_REP_EMAIL";
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");
        return ImmutableMap
                .<String, String>builder()
                .put("subjectPrefix", nonAdaPrefix)
                .putAll(customerServicesProvider.getCustomerServicesPersonalisation())
                .put("homeOfficeReferenceNumber", asylumCase.read(AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER, String.class).orElse(""))
                .put("appealReferenceNumber", asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
                .put("ariaListingReference", asylumCase.read(AsylumCaseDefinition.ARIA_LISTING_REFERENCE, String.class).orElse(""))
                .put("legalRepReferenceNumber", asylumCase.read(AsylumCaseDefinition.LEGAL_REP_REFERENCE_NUMBER, String.class).orElse(""))
                .put("appellantGivenNames", asylumCase.read(AsylumCaseDefinition.APPELLANT_GIVEN_NAMES, String.class).orElse(""))
                .put("appellantFamilyName", asylumCase.read(AsylumCaseDefinition.APPELLANT_FAMILY_NAME, String.class).orElse(""))
                .put("linkToOnlineService", iaExUiFrontendUrl)
                .build();
    }

}

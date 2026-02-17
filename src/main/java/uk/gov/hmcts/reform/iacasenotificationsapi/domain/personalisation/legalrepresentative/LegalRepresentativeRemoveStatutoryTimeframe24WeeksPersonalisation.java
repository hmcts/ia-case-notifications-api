package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.legalrepresentative;

import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.EmailNotificationPersonalisation;

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

    public LegalRepresentativeRemoveStatutoryTimeframe24WeeksPersonalisation(
            @NotNull(message = "removeStatutoryTimeframe24WeeksLegalRepresentativeTemplateId cannot be null")
            @Value("${govnotify.template.removeStatutoryTimeframe24Weeks.legalRep.email}") String removeStatutoryTimeframe24WeeksLegalRepresentativeTemplateId,
            @Value("${iaExUiFrontendUrl}") String iaExUiFrontendUrl
    ) {
        this.removeStatutoryTimeframe24WeeksLegalRepresentativeTemplateId = removeStatutoryTimeframe24WeeksLegalRepresentativeTemplateId;
        this.iaExUiFrontendUrl = iaExUiFrontendUrl;
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
        log.info("Fixing LEGAL_REP_EMAIL");
        return ImmutableMap
                .<String, String>builder()
                .put("customerServicesTelephone", "1234")
                .put("customerServicesEmail", "customerServicesEmail@xyz.com")
                .put("AppealIAEmail", "AppealIAEmail@xyz.com")
                .put("email_address", "emailaddressLegalRep@xyz.com")
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

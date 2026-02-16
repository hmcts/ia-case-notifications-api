package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.legalrepresentative;

import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;

import javax.validation.constraints.NotNull;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.getLegalRepEmailInternalOrLegalRepJourney;

@Service
public class LegalRepresentativeRemoveStatutoryTimeframe24WeeksPersonalisation implements LegalRepresentativeEmailNotificationPersonalisation {

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
        return Collections.singleton("legalrep@xyz.com");
    }
    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_REMOVE_STATUTORY_TIMEFRAME_24WEEKS_LEGAL_REP";
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");
        return ImmutableMap
                .<String, String>builder()
                .put("appealReferenceNumber", asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class).orElse("appealReferenceNumber1"))
                .put("ariaListingReference", asylumCase.read(AsylumCaseDefinition.ARIA_LISTING_REFERENCE, String.class).orElse("ariaListingReference1"))
                .put("legalRepReferenceNumber", asylumCase.read(AsylumCaseDefinition.LEGAL_REP_REFERENCE_NUMBER, String.class).orElse("legalRepReferenceNumber1"))
                .put("appellantGivenNames", asylumCase.read(AsylumCaseDefinition.APPELLANT_GIVEN_NAMES, String.class).orElse("appellantGivenNames1"))
                .put("appellantFamilyName", asylumCase.read(AsylumCaseDefinition.APPELLANT_FAMILY_NAME, String.class).orElse("appellantFamilyName1"))
                .put("linkToOnlineService", iaExUiFrontendUrl)
                .build();
    }

}

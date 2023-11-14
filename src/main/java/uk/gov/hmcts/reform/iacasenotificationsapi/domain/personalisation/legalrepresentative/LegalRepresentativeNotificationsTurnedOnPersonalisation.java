package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.legalrepresentative;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.LEGAL_REP_EMAIL_EJP;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.isAppealListed;

import com.google.common.collect.ImmutableMap;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import javax.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;

@Service
public class LegalRepresentativeNotificationsTurnedOnPersonalisation implements LegalRepresentativeEmailNotificationPersonalisation {

    private final String legalRepresentativeTransferredToFirstTierAfterListingTemplateId;
    private final String legalRepresentativeTransferredToFirstTierBeforeListingTemplateId;
    private final String iaExUiFrontendUrl;


    public LegalRepresentativeNotificationsTurnedOnPersonalisation(
        @NotNull(message = "legalRepresentativeTransferredToFirstTierBeforeListingTemplateId cannot be null")
        @Value("${govnotify.template.notificationsTurnedOn.legalRep.beforeListing.email}") String legalRepresentativeTransferredToFirstTierBeforeListingTemplateId,
        @NotNull(message = "legalRepresentativeTransferredToFirstTierAfterListingTemplateId cannot be null")
        @Value("${govnotify.template.notificationsTurnedOn.legalRep.afterListing.email}") String legalRepresentativeTransferredToFirstTierAfterListingTemplateId,
        @Value("${iaExUiFrontendUrl}") String iaExUiFrontendUrl

    ) {
        this.legalRepresentativeTransferredToFirstTierAfterListingTemplateId = legalRepresentativeTransferredToFirstTierAfterListingTemplateId;
        this.legalRepresentativeTransferredToFirstTierBeforeListingTemplateId = legalRepresentativeTransferredToFirstTierBeforeListingTemplateId;
        this.iaExUiFrontendUrl = iaExUiFrontendUrl;
    }

    @Override
    public String getTemplateId(AsylumCase asylumCase) {
        return isAppealListed(asylumCase)
            ? legalRepresentativeTransferredToFirstTierAfterListingTemplateId : legalRepresentativeTransferredToFirstTierBeforeListingTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        return Collections.singleton(asylumCase
            .read(LEGAL_REP_EMAIL_EJP, String.class)
            .orElseThrow(() -> new IllegalStateException("legalRepresentativeEmailAddress is not present")));
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_NOTIFICATIONS_TURNED_ON";
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");

        return ImmutableMap
            .<String, String>builder()
            .put("appealReferenceNumber", asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
            .put("ariaListingReference", asylumCase.read(AsylumCaseDefinition.ARIA_LISTING_REFERENCE, String.class).orElse(""))
            .put("legalRepReferenceNumberEjp", asylumCase.read(AsylumCaseDefinition.LEGAL_REP_REFERENCE_EJP, String.class).orElse(""))
            .put("appellantGivenNames", asylumCase.read(AsylumCaseDefinition.APPELLANT_GIVEN_NAMES, String.class).orElse(""))
            .put("appellantFamilyName", asylumCase.read(AsylumCaseDefinition.APPELLANT_FAMILY_NAME, String.class).orElse(""))
            .put("dateOfBirth", defaultDateFormat(asylumCase.read(AsylumCaseDefinition.APPELLANT_DATE_OF_BIRTH, String.class).orElse("")))
            .put("linkToOnlineService", iaExUiFrontendUrl)
            .build();
    }
}


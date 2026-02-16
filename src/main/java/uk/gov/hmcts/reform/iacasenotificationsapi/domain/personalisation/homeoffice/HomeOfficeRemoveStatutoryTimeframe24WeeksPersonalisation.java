package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.homeoffice;

import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.EmailNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.EmailAddressFinder;

import javax.validation.constraints.NotNull;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static java.util.Objects.requireNonNull;

@Service
public class HomeOfficeRemoveStatutoryTimeframe24WeeksPersonalisation implements EmailNotificationPersonalisation {

    private final String removeStatutoryTimeframe24WeeksHomeOfficeTemplateId;
    private final String iaExUiFrontendUrl;

    private final String apcPrivateBetaInboxHomeOfficeEmailAddress;
    private final EmailAddressFinder emailAddressFinder;


    public HomeOfficeRemoveStatutoryTimeframe24WeeksPersonalisation(
            @NotNull(message = "removeStatutoryTimeframe24WeeksHomeOfficeTemplateId cannot be null")
            @Value("${govnotify.template.removeStatutoryTimeframe24Weeks.homeOffice.email}") String removeStatutoryTimeframe24WeeksHomeOfficeTemplateId,
            @Value("${apcPrivateHomeOfficeEmailAddress}") String apcPrivateBetaInboxHomeOfficeEmailAddress,
            @Value("${iaExUiFrontendUrl}") String iaExUiFrontendUrl,
            EmailAddressFinder emailAddressFinder
    ) {
        this.removeStatutoryTimeframe24WeeksHomeOfficeTemplateId = removeStatutoryTimeframe24WeeksHomeOfficeTemplateId;
        this.apcPrivateBetaInboxHomeOfficeEmailAddress = apcPrivateBetaInboxHomeOfficeEmailAddress;
        this.iaExUiFrontendUrl = iaExUiFrontendUrl;

        this.emailAddressFinder = emailAddressFinder;
    }

    @Override
    public String getTemplateId(AsylumCase asylumCase) {
        return removeStatutoryTimeframe24WeeksHomeOfficeTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        return Collections.singleton("homeOffice@xyz.com");
    }
    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_REMOVE_STATUTORY_TIMEFRAME_24WEEKS_HOME_OFFICE";
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");

        return ImmutableMap
                .<String, String>builder()
                .put("appealReferenceNumber", asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class).orElse("appealReferenceNumber1"))
                .put("ariaListingReference", asylumCase.read(AsylumCaseDefinition.ARIA_LISTING_REFERENCE, String.class).orElse("ariaListingReference1"))
                .put("homeOfficeReferenceNumber", asylumCase.read(AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER, String.class).orElse("homeOfficeReferenceNumber1"))
                .put("appellantGivenNames", asylumCase.read(AsylumCaseDefinition.APPELLANT_GIVEN_NAMES, String.class).orElse("appellantGivenNames1"))
                .put("appellantFamilyName", asylumCase.read(AsylumCaseDefinition.APPELLANT_FAMILY_NAME, String.class).orElse("appellantFamilyName1"))
                .put("transferOutOfAdaReason", asylumCase.read(AsylumCaseDefinition.TRANSFER_OUT_OF_ADA_REASON, String.class).orElse("transferOutOfAdaReason1"))
                .put("linkToOnlineService", iaExUiFrontendUrl)
                .build();
    }

    protected boolean isAppealListed(AsylumCase asylumCase) {
        final Optional<HearingCentre> appealListed = asylumCase
                .read(AsylumCaseDefinition.LIST_CASE_HEARING_CENTRE, HearingCentre.class);

        return appealListed.isPresent();
    }
}

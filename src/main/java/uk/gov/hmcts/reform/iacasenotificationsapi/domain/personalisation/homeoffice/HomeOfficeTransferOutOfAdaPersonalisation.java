package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.homeoffice;

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
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.EmailNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.EmailAddressFinder;

@Service
public class HomeOfficeTransferOutOfAdaPersonalisation implements EmailNotificationPersonalisation {

    private final String transferOutOfAdaBeforeListingHomeOfficeTemplateId;
    private final String transferOutOfAdaAfterListingHomeOfficeTemplateId;
    private final String iaExUiFrontendUrl;
    private final CustomerServicesProvider customerServicesProvider;
    private final String apcPrivateBetaInboxHomeOfficeEmailAddress;
    private final EmailAddressFinder emailAddressFinder;


    public HomeOfficeTransferOutOfAdaPersonalisation(
            @NotNull(message = "transferOutOfAdaBeforeListingHomeOfficeTemplateId cannot be null")
            @Value("${govnotify.template.transferOutOfAda.homeOffice.beforeListing.email}") String transferOutOfAdaBeforeListingHomeOfficeTemplateId,
            @NotNull(message = "transferOutOfAdaAfterListingHomeOfficeTemplateId cannot be null")
            @Value("${govnotify.template.transferOutOfAda.homeOffice.afterListing.email}") String transferOutOfAdaAfterListingHomeOfficeTemplateId,
            @Value("${apcPrivateHomeOfficeEmailAddress}") String apcPrivateBetaInboxHomeOfficeEmailAddress,
            @Value("${iaExUiFrontendUrl}") String iaExUiFrontendUrl,
            CustomerServicesProvider customerServicesProvider,
            EmailAddressFinder emailAddressFinder
    ) {
        this.transferOutOfAdaBeforeListingHomeOfficeTemplateId = transferOutOfAdaBeforeListingHomeOfficeTemplateId;
        this.transferOutOfAdaAfterListingHomeOfficeTemplateId = transferOutOfAdaAfterListingHomeOfficeTemplateId;
        this.apcPrivateBetaInboxHomeOfficeEmailAddress = apcPrivateBetaInboxHomeOfficeEmailAddress;
        this.iaExUiFrontendUrl = iaExUiFrontendUrl;
        this.customerServicesProvider = customerServicesProvider;
        this.emailAddressFinder = emailAddressFinder;
    }

    @Override
    public String getTemplateId(AsylumCase asylumCase) {
        return isAppealListed(asylumCase)
                ? transferOutOfAdaAfterListingHomeOfficeTemplateId : transferOutOfAdaBeforeListingHomeOfficeTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        return (isAppealListed(asylumCase))
                ? Collections.singleton(emailAddressFinder.getListCaseHomeOfficeEmailAddress(asylumCase)) :
                Collections.singleton(apcPrivateBetaInboxHomeOfficeEmailAddress);
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_TRANSFER_OUT_OF_ADA_HOME_OFFICE";
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");

        return ImmutableMap
                .<String, String>builder()
                .putAll(customerServicesProvider.getCustomerServicesPersonalisation())
                .put("appealReferenceNumber", asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
                .put("ariaListingReference", asylumCase.read(AsylumCaseDefinition.ARIA_LISTING_REFERENCE, String.class).orElse(""))
                .put("homeOfficeReferenceNumber", asylumCase.read(AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER, String.class).orElse(""))
                .put("appellantGivenNames", asylumCase.read(AsylumCaseDefinition.APPELLANT_GIVEN_NAMES, String.class).orElse(""))
                .put("appellantFamilyName", asylumCase.read(AsylumCaseDefinition.APPELLANT_FAMILY_NAME, String.class).orElse(""))
                .put("transferOutOfAdaReason", asylumCase.read(AsylumCaseDefinition.TRANSFER_OUT_OF_ADA_REASON, String.class).orElse(""))
                .put("linkToOnlineService", iaExUiFrontendUrl)
                .build();
    }

    protected boolean isAppealListed(AsylumCase asylumCase) {
        final Optional<HearingCentre> appealListed = asylumCase
                .read(AsylumCaseDefinition.LIST_CASE_HEARING_CENTRE, HearingCentre.class);

        return appealListed.isPresent();
    }
}

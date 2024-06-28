package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.homeoffice.linkunlinkappeal;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.ARIA_LISTING_REFERENCE;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.isAcceleratedDetainedAppeal;

import com.google.common.collect.ImmutableMap;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import javax.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.EmailNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.AppealService;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;

@Service
public class HomeOfficeUnlinkAppealPersonalisation implements EmailNotificationPersonalisation {

    private final String unlinkAppealHomeOfficeBeforeListingTemplateId;
    private final String unlinkAppealHomeOfficeAfterListingTemplateId;
    private final String unlinkAppealEmailAddress;
    private final String iaExUiFrontendUrl;
    private final CustomerServicesProvider customerServicesProvider;
    private final AppealService appealService;

    @Value("${govnotify.emailPrefix.ada}")
    private String adaPrefix;
    @Value("${govnotify.emailPrefix.nonAda}")
    private String nonAdaPrefix;

    public HomeOfficeUnlinkAppealPersonalisation(
        @NotNull(message = "unlinkAppealHomeOfficeBeforeListingTemplateId cannot be null")
        @Value("${govnotify.template.unlinkAppealBeforeListing.homeOffice.email}")
            String unlinkAppealHomeOfficeBeforeListingTemplateId,
        @NotNull(message = "unlinkAppealHomeOfficeAfterTemplateId cannot be null")
        @Value("${govnotify.template.unlinkAppealAfterListing.homeOffice.email}")
            String unlinkAppealHomeOfficeAfterListingTemplateId,
        @Value("${endAppealHomeOfficeEmailAddress}") String unlinkAppealEmailAddress,
        @Value("${iaExUiFrontendUrl}") String iaExUiFrontendUrl,
        CustomerServicesProvider customerServicesProvider,
        AppealService appealService) {
        this.unlinkAppealHomeOfficeBeforeListingTemplateId = unlinkAppealHomeOfficeBeforeListingTemplateId;
        this.unlinkAppealHomeOfficeAfterListingTemplateId = unlinkAppealHomeOfficeAfterListingTemplateId;
        this.unlinkAppealEmailAddress = unlinkAppealEmailAddress;
        this.iaExUiFrontendUrl = iaExUiFrontendUrl;
        this.customerServicesProvider = customerServicesProvider;
        this.appealService = appealService;
    }

    @Override
    public String getTemplateId(AsylumCase asylumCase) {
        return appealService.isAppealListed(asylumCase)
            ? unlinkAppealHomeOfficeAfterListingTemplateId : unlinkAppealHomeOfficeBeforeListingTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        return Collections.singleton(unlinkAppealEmailAddress);
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_UNLINK_APPEAL_HOME_OFFICE";
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");

        return ImmutableMap
            .<String, String>builder()
            .putAll(customerServicesProvider.getCustomerServicesPersonalisation())
            .put("subjectPrefix", isAcceleratedDetainedAppeal(asylumCase) ? adaPrefix : nonAdaPrefix)
            .put("appealReferenceNumber", asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class)
                .orElse(""))
            .put("appellantGivenNames", asylumCase.read(AsylumCaseDefinition.APPELLANT_GIVEN_NAMES, String.class)
                .orElse(""))
            .put("appellantFamilyName", asylumCase.read(AsylumCaseDefinition.APPELLANT_FAMILY_NAME, String.class)
                .orElse(""))
            .put("linkToOnlineService", iaExUiFrontendUrl)
            .put("homeOfficeReferenceNumber",
                asylumCase.read(AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER, String.class).orElse(""))
            .put("ariaListingReference", asylumCase.read(ARIA_LISTING_REFERENCE, String.class).orElse(""))
            .build();
    }

}

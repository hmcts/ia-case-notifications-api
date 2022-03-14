package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.homeoffice;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.AddressFormatter.formatCompanyAddress;

import com.google.common.collect.ImmutableMap;
import java.util.*;
import javax.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.EmailNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.AppealService;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.EmailAddressFinder;


@Service
public class HomeOfficeRemoveRepresentationPersonalisation implements EmailNotificationPersonalisation {

    private final String removeRepresentationHomeOfficeBeforeListingTemplateId;
    private final String removeRepresentationHomeOfficeAfterListingTemplateId;
    private final String apcHomeOfficeEmailAddress;
    private final String lartHomeOfficeEmailAddress;
    private final String iaExUiFrontendUrl;
    private final AppealService appealService;
    private final EmailAddressFinder emailAddressFinder;
    private final CustomerServicesProvider customerServicesProvider;

    public HomeOfficeRemoveRepresentationPersonalisation(
        @NotNull(message = "removeRepresentationHomeOfficeBeforeListingTemplateId cannot be null") @Value("${govnotify.template.removeRepresentation.homeOffice.beforeListing.email}") String removeRepresentationHomeOfficeBeforeListingTemplateId,
        @NotNull(message = "removeRepresentationHomeOfficeAfterListingTemplateId cannot be null") @Value("${govnotify.template.removeRepresentation.homeOffice.afterListing.email}") String removeRepresentationHomeOfficeAfterListingTemplateId,
        @Value("${apcHomeOfficeEmailAddress}") String apcHomeOfficeEmailAddress,
        @Value("${lartHomeOfficeEmailAddress}") String lartHomeOfficeEmailAddress,
        @Value("${iaExUiFrontendUrl}") String iaExUiFrontendUrl,
        AppealService appealService,
        EmailAddressFinder emailAddressFinder,
        CustomerServicesProvider customerServicesProvider
    ) {
        this.removeRepresentationHomeOfficeBeforeListingTemplateId = removeRepresentationHomeOfficeBeforeListingTemplateId;
        this.removeRepresentationHomeOfficeAfterListingTemplateId = removeRepresentationHomeOfficeAfterListingTemplateId;
        this.apcHomeOfficeEmailAddress = apcHomeOfficeEmailAddress;
        this.lartHomeOfficeEmailAddress = lartHomeOfficeEmailAddress;
        this.iaExUiFrontendUrl = iaExUiFrontendUrl;
        this.appealService = appealService;
        this.emailAddressFinder = emailAddressFinder;
        this.customerServicesProvider = customerServicesProvider;
    }

    @Override
    public String getTemplateId(AsylumCase asylumCase) {
        return appealService.isAppealListed(asylumCase)
            ? removeRepresentationHomeOfficeAfterListingTemplateId
            : removeRepresentationHomeOfficeBeforeListingTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {

        State currentState = asylumCase.read(CURRENT_CASE_STATE_VISIBLE_TO_HOME_OFFICE_ALL, State.class).orElse(null);

        if (currentState == null) {
            return Collections.emptySet();
        }

        if (Arrays.asList(State.APPEAL_STARTED,
            State.APPEAL_SUBMITTED,
            State.PENDING_PAYMENT,
            State.AWAITING_RESPONDENT_EVIDENCE,
            State.CASE_BUILDING,
            State.CASE_UNDER_REVIEW,
            State.ENDED
        ).contains(currentState)) {
            return Collections.singleton(apcHomeOfficeEmailAddress);
        } else if (Arrays.asList(State.RESPONDENT_REVIEW,
            State.LISTING,
            State.SUBMIT_HEARING_REQUIREMENTS).contains(currentState)) {
            return Collections.singleton(lartHomeOfficeEmailAddress);
        } else if (Arrays.asList(State.ADJOURNED,
            State.PREPARE_FOR_HEARING,
            State.FINAL_BUNDLING,
            State.PRE_HEARING,
            State.DECISION,
            State.DECIDED,
            State.FTPA_SUBMITTED,
            State.FTPA_DECIDED).contains(currentState)) {
            if (appealService.isAppealListed(asylumCase)) {
                return Collections.singleton(emailAddressFinder.getListCaseHomeOfficeEmailAddress(asylumCase));
            } else {
                return Collections.singleton(emailAddressFinder.getHomeOfficeEmailAddress(asylumCase));
            }
        } else {
            throw new IllegalStateException("homeOffice email Address cannot be found");
        }
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_REMOVE_REPRESENTATION_HOME_OFFICE";
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
            .put("linkToOnlineService", iaExUiFrontendUrl)
            .put("legalRepresentativeName", asylumCase.read(AsylumCaseDefinition.LEGAL_REPRESENTATIVE_NAME, String.class).orElse(""))
            .put("legalRepCompanyAddress", formatCompanyAddress(asylumCase))
            .put("legalRepresentativeEmailAddress", asylumCase.read(AsylumCaseDefinition.LEGAL_REPRESENTATIVE_EMAIL_ADDRESS, String.class).orElse(""))
            .build();
    }

}

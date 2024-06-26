package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.homeoffice;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;

import com.google.common.collect.ImmutableMap;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import javax.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.EmailNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.EmailAddressFinder;

@Service
public class HomeOfficeRemoveDetentionStatusPersonalisation implements EmailNotificationPersonalisation {

    private final String removeDetentionStatusLegalRepresentativeTemplateId;
    private final String iaExUiFrontendUrl;
    private final CustomerServicesProvider customerServicesProvider;
    private final EmailAddressFinder emailAddressFinder;
    private final String respondentEmailAddressUntilRespondentReview;
    private final String respondentEmailAddressAtRespondentReview;

    public HomeOfficeRemoveDetentionStatusPersonalisation(
            @NotNull(message = "removeDetentionStatusLegalRepresentativeTemplateId cannot be null")
            @Value("${govnotify.template.removeDetentionStatus.homeOffice.email}") String removeDetentionStatusLegalRepresentativeTemplateId,
            @Value("${respondentEmailAddresses.nonStandardDirectionUntilListing}")
            String respondentEmailAddressUntilRespondentReview,
            @Value("${respondentEmailAddresses.respondentReviewDirection}") String respondentEmailAddressAtRespondentReview,
            @Value("${iaExUiFrontendUrl}") String iaExUiFrontendUrl,
            EmailAddressFinder emailAddressFinder,
            CustomerServicesProvider customerServicesProvider

    ) {
        this.removeDetentionStatusLegalRepresentativeTemplateId = removeDetentionStatusLegalRepresentativeTemplateId;
        this.respondentEmailAddressUntilRespondentReview = respondentEmailAddressUntilRespondentReview;
        this.respondentEmailAddressAtRespondentReview = respondentEmailAddressAtRespondentReview;
        this.iaExUiFrontendUrl = iaExUiFrontendUrl;
        this.emailAddressFinder = emailAddressFinder;
        this.customerServicesProvider = customerServicesProvider;
    }

    @Override
    public String getTemplateId(AsylumCase asylumCase) {
        return removeDetentionStatusLegalRepresentativeTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        return Collections.singleton(getRespondentEmailAddress(asylumCase));
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_REMOVE_DETENTION_STATUS_HOME_OFFICE";
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");
        String ariaListingReferenceIfPresent = asylumCase.read(ARIA_LISTING_REFERENCE, String.class)
                .map(ariaListingReference -> "Listing reference: " + ariaListingReference)
                .orElse("");
        return ImmutableMap
                .<String, String>builder()
                .putAll(customerServicesProvider.getCustomerServicesPersonalisation())
                .put("appealReferenceNumber", asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
                .put("homeOfficeReferenceNumber", asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class).orElse(""))
                .put("appellantGivenNames", asylumCase.read(APPELLANT_GIVEN_NAMES, String.class).orElse(""))
                .put("appellantFamilyName", asylumCase.read(APPELLANT_FAMILY_NAME, String.class).orElse(""))
                .put("ariaListingReferenceIfPresent", ariaListingReferenceIfPresent)
                .put("linkToOnlineService", iaExUiFrontendUrl)
                .build();
    }

    private String getRespondentEmailAddress(AsylumCase asylumCase) {

        return asylumCase.read(CURRENT_CASE_STATE_VISIBLE_TO_HOME_OFFICE_ALL, State.class)
                .map(s -> {
                    if (Arrays.asList(
                            State.APPEAL_SUBMITTED,
                            State.PENDING_PAYMENT,
                            State.AWAITING_RESPONDENT_EVIDENCE,
                            State.CASE_BUILDING,
                            State.CASE_UNDER_REVIEW
                    ).contains(s)) {
                        return respondentEmailAddressUntilRespondentReview;
                    } else if (Arrays.asList(
                            State.RESPONDENT_REVIEW,
                            State.SUBMIT_HEARING_REQUIREMENTS,
                            State.LISTING
                    ).contains(s)) {
                        return respondentEmailAddressAtRespondentReview;
                    }

                    return emailAddressFinder.getListCaseHomeOfficeEmailAddress(asylumCase);
                })
                .orElseThrow(() -> new IllegalStateException("currentCaseStateVisibleToHomeOfficeAll flag is not present"));
    }
}

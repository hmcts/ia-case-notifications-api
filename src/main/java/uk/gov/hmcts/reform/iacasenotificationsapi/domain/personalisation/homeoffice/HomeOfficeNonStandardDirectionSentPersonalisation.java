package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.homeoffice;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;

import com.google.common.collect.ImmutableMap;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.*;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.EmailNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.DirectionFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.EmailAddressFinder;

@Service
public class HomeOfficeNonStandardDirectionSentPersonalisation implements EmailNotificationPersonalisation {

    private final String homeOfficeNonStandardDirectionSentBeforeListingTemplateId;
    private final String homeOfficeNonStandardDirectionSentAfterListingTemplateId;
    private final String iaExUiFrontendUrl;
    private final String homeOfficeNonStandardDirectionEmailAddress;
    private final DirectionFinder directionFinder;
    private final CustomerServicesProvider customerServicesProvider;
    private EmailAddressFinder emailAddressFinder;

    public HomeOfficeNonStandardDirectionSentPersonalisation(
        @Value("${govnotify.template.nonStandardDirectionBeforeListing.homeOffice.email}") String homeOfficeNonStandardDirectionSentBeforeListingTemplateId,
        @Value("${govnotify.template.nonStandardDirectionAfterListing.homeOffice.email}") String homeOfficeNonStandardDirectionSentAfterListingTemplateId,
        @Value("${iaExUiFrontendUrl}") String iaExUiFrontendUrl,
        @Value("${respondentEmailAddresses.respondentReviewDirection}") String homeOfficeNonStandardDirectionEmailAddress,
        EmailAddressFinder emailAddressFinder,
        DirectionFinder directionFinder,
        CustomerServicesProvider customerServicesProvider
    ) {
        this.homeOfficeNonStandardDirectionSentBeforeListingTemplateId = homeOfficeNonStandardDirectionSentBeforeListingTemplateId;
        this.homeOfficeNonStandardDirectionSentAfterListingTemplateId = homeOfficeNonStandardDirectionSentAfterListingTemplateId;
        this.iaExUiFrontendUrl = iaExUiFrontendUrl;
        this.homeOfficeNonStandardDirectionEmailAddress = homeOfficeNonStandardDirectionEmailAddress;
        this.emailAddressFinder = emailAddressFinder;
        this.directionFinder = directionFinder;
        this.customerServicesProvider = customerServicesProvider;
    }

    @Override
    public String getTemplateId(AsylumCase asylumCase) {
        return isAppealListed(asylumCase)
            ? homeOfficeNonStandardDirectionSentAfterListingTemplateId : homeOfficeNonStandardDirectionSentBeforeListingTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        if (isAppealListed(asylumCase)) {
            return Collections.singleton(emailAddressFinder.getHomeOfficeEmailAddress(asylumCase));
        } else {
            return Collections.singleton(homeOfficeNonStandardDirectionEmailAddress);
        }
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_HOME_OFFICE_NON_STANDARD_DIRECTION_SENT";
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");

        final Direction direction =
            directionFinder
                .findFirst(asylumCase, DirectionTag.NONE)
                .orElseThrow(() -> new IllegalStateException("non-standard direction is not present"));

        final String directionDueDate =
            LocalDate
                .parse(direction.getDateDue())
                .format(DateTimeFormatter.ofPattern("d MMM yyyy"));

        return ImmutableMap
            .<String, String>builder()
            .putAll(customerServicesProvider.getCustomerServicesPersonalisation())
            .put("appealReferenceNumber", asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
            .put("ariaListingReference", asylumCase.read(ARIA_LISTING_REFERENCE, String.class).orElse(""))
            .put("homeOfficeReferenceNumber", asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class).orElse(""))
            .put("appellantGivenNames", asylumCase.read(APPELLANT_GIVEN_NAMES, String.class).orElse(""))
            .put("appellantFamilyName", asylumCase.read(APPELLANT_FAMILY_NAME, String.class).orElse(""))
            .put("linkToOnlineService", iaExUiFrontendUrl)
            .put("explanation", direction.getExplanation())
            .put("dueDate", directionDueDate)
            .build();
    }

    protected boolean isAppealListed(AsylumCase asylumCase) {
        final Optional<HearingCentre> appealListed = asylumCase.read(AsylumCaseDefinition.LIST_CASE_HEARING_CENTRE, HearingCentre.class);

        return appealListed.isPresent();
    }
}

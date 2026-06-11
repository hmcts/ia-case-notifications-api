package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.letter;

import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.LetterNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Set;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.homeoffice.HomeOfficeCompleteCaseReviewStatutoryTimeframe24WeeksPersonalisation.DAYS_14;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.homeoffice.HomeOfficeCompleteCaseReviewStatutoryTimeframe24WeeksPersonalisation.DAYS_42;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.homeoffice.HomeOfficeCompleteCaseReviewStatutoryTimeframe24WeeksPersonalisation.DAYS_56;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.D_MMM_YYYY;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.buildAddressFor24WeeksLetter;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.getAppellantAddressInCountryOrOoc;

@Service
@Slf4j
public class AppellantCompleteCaseReviewStatutoryTimeframe24WeeksPersonalisationLetter implements LetterNotificationPersonalisation {

    private static final String REFERENCE_ID_SUFFIX = "_STATUTORY_TIMEFRAME_24WEEKS_CASE_REVIEW_APPELLANT_LETTER";
    private static final String APPEAL_REFERENCE_NUMBER_KEY = "appealReferenceNumber";
    private static final String APPELLANT_GIVEN_NAMES_KEY = "appellantGivenNames";
    private static final String APPELLANT_FAMILY_NAME_KEY = "appellantFamilyName";
    private static final String LINK_TO_SERVICE_KEY = "linkToService";
    private static final String EMPTY_STRING = "";

    private final String removeStatutoryTimeframe24WeeksAppellantLetterId;
    private final CustomerServicesProvider customerServicesProvider;
    private final String iaAipFrontendUrl;

    public AppellantCompleteCaseReviewStatutoryTimeframe24WeeksPersonalisationLetter(
            @Value("${govnotify.template.completeCaseReviewStatutoryTimeframe24Weeks.appellant.letter}") String removeStatutoryTimeframe24WeeksAppellantLetterId,
            @Value("${iaAipFrontendUrl}") String iaAipFrontendUrl,
            CustomerServicesProvider customerServicesProvider
    ) {
        this.removeStatutoryTimeframe24WeeksAppellantLetterId = removeStatutoryTimeframe24WeeksAppellantLetterId;
        this.iaAipFrontendUrl = iaAipFrontendUrl;
        this.customerServicesProvider = customerServicesProvider;
    }

    @Override
    public String getTemplateId() {
        return removeStatutoryTimeframe24WeeksAppellantLetterId;
    }

    @Override
    public Set<String> getRecipientsList(final AsylumCase asylumCase) {
        Set<String> addresses = getAppellantAddressInCountryOrOoc(asylumCase);
        log.info("Appellant Address {}", addresses);
        return addresses;
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + REFERENCE_ID_SUFFIX;
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");

        LocalDate now = LocalDate.now();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern(D_MMM_YYYY);

        String familyName = asylumCase.read(AsylumCaseDefinition.APPELLANT_FAMILY_NAME, String.class).orElse(EMPTY_STRING);
        String givenNames = asylumCase.read(AsylumCaseDefinition.APPELLANT_GIVEN_NAMES, String.class).orElse(EMPTY_STRING);
        String appealRef = asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class).orElse(EMPTY_STRING);

        ImmutableMap.Builder<String, String> builder = ImmutableMap.<String, String>builder()
                .put(APPEAL_REFERENCE_NUMBER_KEY, appealRef)
                .putAll(customerServicesProvider.getCustomerServicesPersonalisation(asylumCase))
                .put(APPELLANT_GIVEN_NAMES_KEY, givenNames)
                .put(APPELLANT_FAMILY_NAME_KEY, familyName)
                .put("appellantFullName", (givenNames + " " + familyName).trim())
                .put("appealReceivedDate", AsylumCaseUtils.getAppealReceivedDate(asylumCase))
                .put("decisionSentDate", AsylumCaseUtils.getHomeOfficeDecisionDate(asylumCase))
                .put("24WeeksDeadline", AsylumCaseUtils.populateStatutoryTimeFrame24wDate(asylumCase))
                .put("practiceDirection", now.format(dtf))
                .put("14DaysFromDateOfDirection", now.plusDays(DAYS_14).format(dtf))
                .put("42DaysFromDateOfDirection", now.plusDays(DAYS_42).format(dtf))
                .put("56DaysFromDateOfDirection", now.plusDays(DAYS_56).format(dtf))
                .put(LINK_TO_SERVICE_KEY, iaAipFrontendUrl);

        // populate address fields used for the 24 weeks letter
        buildAddressFor24WeeksLetter(asylumCase, builder);

        return builder.build();
    }
}

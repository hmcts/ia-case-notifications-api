package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.legalrepresentative;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_FAMILY_NAME;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_GIVEN_NAMES;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.LEGAL_REP_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.getAppellantOrLegalRepAddressLetterPersonalisation;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.getLegalRepAddressInCountryOrOoc;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.Stf24WeeksUtil.APPEAL_RECEIVED_DATE;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.Stf24WeeksUtil.DAYS_14;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.Stf24WeeksUtil.DAYS_14_FROM_DATE_OF_DIRECTION_KEY;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.Stf24WeeksUtil.DAYS_42;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.Stf24WeeksUtil.DAYS_42_FROM_DATE_OF_DIRECTION_KEY;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.Stf24WeeksUtil.DAYS_56;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.Stf24WeeksUtil.DAYS_56_FROM_DATE_OF_DIRECTION;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.Stf24WeeksUtil.DECISION_SENT_DATE;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.Stf24WeeksUtil.EMPTY_STRING;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.Stf24WeeksUtil.HOME_OFFICE_REFERENCE_NUMBER_KEY;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.Stf24WeeksUtil.PRACTICE_DIRECTION;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.Stf24WeeksUtil.STATUTORY_TIMEFRAME_24WEEKS_CASE_REVIEW_LEGAL_REP_LETTER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.Stf24WeeksUtil.WEEKS_DEADLINE;

import com.google.common.collect.ImmutableMap;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.LetterNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;

@Service
@Slf4j
public class LegalRepresentativeCompleteCaseReviewStatutoryTimeframe24WeeksPersonalisationLetter implements LetterNotificationPersonalisation {

    private static final String APPEAL_REFERENCE_NUMBER_KEY = "appealReferenceNumber";
    private static final String LEGAL_REP_REFERENCE_NUMBER_KEY = "legalRepReferenceNumber";
    private static final String APPELLANT_GIVEN_NAMES_KEY = "appellantGivenNames";
    private static final String APPELLANT_FAMILY_NAME_KEY = "appellantFamilyName";

    private final String templateId;
    private final CustomerServicesProvider customerServicesProvider;

    public LegalRepresentativeCompleteCaseReviewStatutoryTimeframe24WeeksPersonalisationLetter(
            @Value("${govnotify.template.completeCaseReviewStatutoryTimeframe24Weeks.legalRep.letter}") String templateId,
            CustomerServicesProvider customerServicesProvider) {
        this.templateId = templateId;
        this.customerServicesProvider = customerServicesProvider;
    }

    @Override
    public String getTemplateId() {
        return templateId;
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        Set<String> addresses = getLegalRepAddressInCountryOrOoc(asylumCase);
        log.info("LegalRep address for 24-week review letter: {}", addresses);
        return addresses;
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + STATUTORY_TIMEFRAME_24WEEKS_CASE_REVIEW_LEGAL_REP_LETTER;
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");

        LocalDate now = LocalDate.now();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern(AsylumCaseUtils.D_MMM_YYYY);

        ImmutableMap.Builder<String, String> builder = ImmutableMap.<String, String>builder()
                .putAll(customerServicesProvider.getCustomerServicesPersonalisation(asylumCase))
                .put(APPEAL_REFERENCE_NUMBER_KEY, asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class).orElse(EMPTY_STRING))
                .put(LEGAL_REP_REFERENCE_NUMBER_KEY, asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class).orElse(EMPTY_STRING))
                .put(APPELLANT_GIVEN_NAMES_KEY, asylumCase.read(APPELLANT_GIVEN_NAMES, String.class).orElse(EMPTY_STRING))
                .put(APPELLANT_FAMILY_NAME_KEY, asylumCase.read(APPELLANT_FAMILY_NAME, String.class).orElse(EMPTY_STRING))
                .put(HOME_OFFICE_REFERENCE_NUMBER_KEY, asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class).orElse(EMPTY_STRING))
                .put(APPEAL_RECEIVED_DATE, AsylumCaseUtils.getAppealReceivedDate(asylumCase))
                .put(DECISION_SENT_DATE, AsylumCaseUtils.getHomeOfficeDecisionDate(asylumCase))
                .put(WEEKS_DEADLINE, AsylumCaseUtils.populateStatutoryTimeFrame24wDate(asylumCase))
                .put(PRACTICE_DIRECTION, now.format(dtf))
                .put(DAYS_14_FROM_DATE_OF_DIRECTION_KEY, now.plusDays(DAYS_14).format(dtf))
                .put(DAYS_42_FROM_DATE_OF_DIRECTION_KEY, now.plusDays(DAYS_42).format(dtf))
                .put(DAYS_56_FROM_DATE_OF_DIRECTION, now.plusDays(DAYS_56).format(dtf));

        List<String> address = getAppellantOrLegalRepAddressLetterPersonalisation(asylumCase);
        for (int i = 0; i < address.size(); i++) {
            builder.put("address_line_" + (i + 1), address.get(i));
        }

        return builder.build();
    }
}

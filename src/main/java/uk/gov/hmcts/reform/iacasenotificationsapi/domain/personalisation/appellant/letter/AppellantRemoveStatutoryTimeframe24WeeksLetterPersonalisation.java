package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.letter;

import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.LetterNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;

import java.util.Map;
import java.util.Set;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.buildAddressFor24WeeksLetter;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.getAppellantAddressInCountryOrOoc;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.Stf24WeeksUtil.REMOVE_STATUTORY_TIMEFRAME_24WEEKS_APPELLANT_LETTER;

@Slf4j
@Service
public class AppellantRemoveStatutoryTimeframe24WeeksLetterPersonalisation implements LetterNotificationPersonalisation {
    private static final String COMPLETE_CASE_REVIEW_DATE_KEY = "completeCaseReviewDate";

    private final String appellantInternalCaseSubmitAppealWithRemissionLetterTemplateId;
    private final CustomerServicesProvider customerServicesProvider;

    public AppellantRemoveStatutoryTimeframe24WeeksLetterPersonalisation(
            @Value("${govnotify.template.removeStatutoryTimeframe24Weeks.appellant.letter}") String removeStatutoryTimeframe24WeeksAppellantLetterId,
            CustomerServicesProvider customerServicesProvider
    ) {
        this.appellantInternalCaseSubmitAppealWithRemissionLetterTemplateId = removeStatutoryTimeframe24WeeksAppellantLetterId;
        this.customerServicesProvider = customerServicesProvider;
    }

    @Override
    public String getTemplateId() {
        return appellantInternalCaseSubmitAppealWithRemissionLetterTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(final AsylumCase asylumCase) {
        return getAppellantAddressInCountryOrOoc(asylumCase);
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + REMOVE_STATUTORY_TIMEFRAME_24WEEKS_APPELLANT_LETTER;
    }

    @Override
    public Map<String, String> getPersonalisation(Callback<AsylumCase> callback) {
        requireNonNull(callback, "callback must not be null");
        AsylumCase asylumCase =
                callback
                        .getCaseDetails()
                        .getCaseData();

        ImmutableMap.Builder<String, String> personalizationBuilder = ImmutableMap
                .<String, String>builder()
                .putAll(customerServicesProvider.getCustomerServicesPersonalisation(asylumCase))
                .put("appealReferenceNumber", asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
                .put("homeOfficeReferenceNumber", asylumCase.read(AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER, String.class).orElse(""))
                .put("appellantGivenNames", asylumCase.read(AsylumCaseDefinition.APPELLANT_GIVEN_NAMES, String.class).orElse(""))
                .put("appellantFamilyName", asylumCase.read(AsylumCaseDefinition.APPELLANT_FAMILY_NAME, String.class).orElse(""))
                .put(COMPLETE_CASE_REVIEW_DATE_KEY, AsylumCaseUtils.getCompleteCasedReviewDate(asylumCase));
        buildAddressFor24WeeksLetter(asylumCase, personalizationBuilder);
        return personalizationBuilder.build();
    }
}

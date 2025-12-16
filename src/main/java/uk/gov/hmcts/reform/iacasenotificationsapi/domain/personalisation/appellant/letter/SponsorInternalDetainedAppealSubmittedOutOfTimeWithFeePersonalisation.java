package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.letter;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.FEE_AMOUNT_GBP;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.convertAsylumCaseFeeValue;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.getSponserAddressAsList;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.getSponsorAddressInCountryOrOoc;

import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.LetterNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.SystemDateProvider;

@Service
public class SponsorInternalDetainedAppealSubmittedOutOfTimeWithFeePersonalisation implements LetterNotificationPersonalisation {
    private final String appellantInternalCaseSubmitAppealWithFeeOutOfTimeLetterTemplateId;
    private final CustomerServicesProvider customerServicesProvider;
    private final SystemDateProvider systemDateProvider;
    private final int daysAfterSubmitAppeal;

    public SponsorInternalDetainedAppealSubmittedOutOfTimeWithFeePersonalisation(
        @Value("${govnotify.template.appealSubmitted.sponsor.letter.outOfTime.withFee}") String appellantInternalCaseSubmitAppealWithFeeOutOfTimeLetterTemplateId,
        @Value("${appellantDaysToWait.letter.afterSubmitAppealWithFee}") int daysAfterSubmitAppeal,
        CustomerServicesProvider customerServicesProvider,
        SystemDateProvider systemDateProvider
    ) {
        this.appellantInternalCaseSubmitAppealWithFeeOutOfTimeLetterTemplateId = appellantInternalCaseSubmitAppealWithFeeOutOfTimeLetterTemplateId;
        this.customerServicesProvider = customerServicesProvider;
        this.systemDateProvider = systemDateProvider;
        this.daysAfterSubmitAppeal = daysAfterSubmitAppeal;
    }

    @Override
    public String getTemplateId() {
        return appellantInternalCaseSubmitAppealWithFeeOutOfTimeLetterTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(final AsylumCase asylumCase) {
        return getSponsorAddressInCountryOrOoc(asylumCase);
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_INTERNAL_SUBMIT_APPEAL_WITH_FEE_OUT_OF_TIME_SPONSOR_LETTER";
    }

    @Override
    public Map<String, String> getPersonalisation(Callback<AsylumCase> callback) {
        requireNonNull(callback, "callback must not be null");

        AsylumCase asylumCase =
            callback
                .getCaseDetails()
                .getCaseData();

        final String dueDate = systemDateProvider.dueDate(daysAfterSubmitAppeal);

        ImmutableMap.Builder<String, String> personalizationBuilder = ImmutableMap
            .<String, String>builder()
            .putAll(customerServicesProvider.getCustomerServicesPersonalisation())
            .put("appealReferenceNumber", asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
            .put("homeOfficeReferenceNumber", asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class).orElse(""))
            .put("appellantGivenNames", asylumCase.read(SPONSOR_GIVEN_NAMES, String.class).orElse(""))
            .put("appellantFamilyName", asylumCase.read(SPONSOR_FAMILY_NAME, String.class).orElse(""))
            .put("onlineCaseReferenceNumber", asylumCase.read(CCD_REFERENCE_NUMBER_FOR_DISPLAY, String.class).orElse(""))
            .put("feeAmount", convertAsylumCaseFeeValue(asylumCase.read(FEE_AMOUNT_GBP, String.class).orElse("")))
            .put("fourteenDaysAfterSubmitDate", dueDate);

        List<String> address =  getSponserAddressAsList(asylumCase);

        for (int i = 0; i < address.size(); i++) {
            personalizationBuilder.put("address_line_" + (i + 1), address.get(i));
        }
        return personalizationBuilder.build();
    }
}

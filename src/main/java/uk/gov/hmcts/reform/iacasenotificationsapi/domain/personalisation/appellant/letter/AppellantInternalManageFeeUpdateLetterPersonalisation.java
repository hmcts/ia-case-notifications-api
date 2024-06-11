package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.letter;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.convertAsylumCaseFeeValue;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.getAppellantAddressAsList;

import java.math.BigDecimal;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.LetterNotificationPersonalisation;
import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Value;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.SystemDateProvider;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AppellantInternalManageFeeUpdateLetterPersonalisation implements LetterNotificationPersonalisation {

    private final String appellantInternalManageFeeUpdateLetterTemplateId;
    private final CustomerServicesProvider customerServicesProvider;
    private final SystemDateProvider systemDateProvider;
    private final int afterManageFeeEvent;

    public AppellantInternalManageFeeUpdateLetterPersonalisation(
        @Value("${govnotify.template.manageFeeUpdate.appellant.letter}") String appellantInternalManageFeeUpdateLetterTemplateId,
        @Value("${appellantDaysToWait.letter.afterManageFeeEvent}") int afterManageFeeEvent,
        CustomerServicesProvider customerServicesProvider,
        SystemDateProvider systemDateProvider
    ) {
        this.appellantInternalManageFeeUpdateLetterTemplateId = appellantInternalManageFeeUpdateLetterTemplateId;
        this.customerServicesProvider = customerServicesProvider;
        this.systemDateProvider = systemDateProvider;
        this.afterManageFeeEvent = afterManageFeeEvent;
    }

    @Override
    public String getTemplateId() {
        return appellantInternalManageFeeUpdateLetterTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(final AsylumCase asylumCase) {
        return Collections.singleton(getAppellantAddressAsList(asylumCase).stream()
            .map(item -> item.replaceAll("\\s", "")).collect(Collectors.joining("_")));
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_INTERNAL_MANAGE_FEE_UPDATE_APPELLANT_LETTER";
    }

    @Override
    public Map<String, String> getPersonalisation(Callback<AsylumCase> callback) {
        requireNonNull(callback, "callback must not be null");

        AsylumCase asylumCase =
            callback
                .getCaseDetails()
                .getCaseData();

        List<String> appellantAddress = getAppellantAddressAsList(asylumCase);
        final String dueDate = systemDateProvider.dueDate(afterManageFeeEvent);

        String originalFeeTotal = asylumCase.read(AsylumCaseDefinition.FEE_AMOUNT_GBP, String.class).orElse("");
        String newFeeTotal = asylumCase.read(AsylumCaseDefinition.NEW_FEE_AMOUNT, String.class).orElse("");
        String feeDifference = calculateFeeDifference(originalFeeTotal, newFeeTotal);

        ImmutableMap.Builder<String, String> personalizationBuilder = ImmutableMap
            .<String, String>builder()
            .putAll(customerServicesProvider.getCustomerServicesPersonalisation())
            .put("appealReferenceNumber", asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
            .put("homeOfficeReferenceNumber", asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class).orElse(""))
            .put("appellantGivenNames", asylumCase.read(APPELLANT_GIVEN_NAMES, String.class).orElse(""))
            .put("appellantFamilyName", asylumCase.read(APPELLANT_FAMILY_NAME, String.class).orElse(""))
            .put("originalFeeTotal", convertAsylumCaseFeeValue(asylumCase.read(FEE_AMOUNT_GBP, String.class).orElse("")))
            .put("newFeeTotal", convertAsylumCaseFeeValue(asylumCase.read(NEW_FEE_AMOUNT, String.class).orElse("")))
            .put("feeDifference", feeDifference)
            .put("feeUpdateReasonSelected", asylumCase.read(FEE_UPDATE_REASON, String.class).orElse(""))
            .put("onlineCaseRefNumber", asylumCase.read(CCD_REFERENCE_NUMBER_FOR_DISPLAY, String.class).orElse(""))
            .put("dueDate14Days", dueDate);

        for (int i = 0; i < appellantAddress.size(); i++) {
            personalizationBuilder.put("address_line_" + (i + 1), appellantAddress.get(i));
        }
        return personalizationBuilder.build();
    }

    private String calculateFeeDifference(String originalFeeTotal, String newFeeTotal) {
        try {

            BigDecimal originalFee = new BigDecimal(originalFeeTotal);
            BigDecimal newFee = new BigDecimal(newFeeTotal);
            BigDecimal difference = originalFee.subtract(newFee);
            return difference.toString();

        } catch (NumberFormatException e) {

            return "0.00";
        }
    }
}

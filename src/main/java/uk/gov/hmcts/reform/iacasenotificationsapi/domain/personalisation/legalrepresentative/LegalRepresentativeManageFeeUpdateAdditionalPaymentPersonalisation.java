package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.legalrepresentative;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.utils.CommonUtils.convertAsylumCaseFeeValue;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import javax.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.FeeUpdateReason;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.SystemDateProvider;

@Service
public class LegalRepresentativeManageFeeUpdateAdditionalPaymentPersonalisation implements LegalRepresentativeEmailNotificationPersonalisation {

    private final String manageFeeUpdateAdditionalPaymentTemplateId;
    private final String iaExUiFrontendUrl;
    private final CustomerServicesProvider customerServicesProvider;
    private final SystemDateProvider systemDateProvider;
    private final int daysToWaitAfterManageFeeUpdate;


    public LegalRepresentativeManageFeeUpdateAdditionalPaymentPersonalisation(
        @NotNull(message = "manageFeeUpdateAdditionalPaymentTemplateId cannot be null") @Value("${govnotify.template.manageFeeUpdate.legalRep.additionalPayment.email}") String manageFeeUpdateAdditionalPaymentTemplateId,
        @Value("${iaExUiFrontendUrl}") String iaExUiFrontendUrl,
        CustomerServicesProvider customerServicesProvider,
        SystemDateProvider systemDateProvider,
        @Value("${legalRepDaysToWait.afterManageFeeUpdate}") int daysToWaitAfterManageFeeUpdate
    ) {
        this.manageFeeUpdateAdditionalPaymentTemplateId = manageFeeUpdateAdditionalPaymentTemplateId;
        this.iaExUiFrontendUrl = iaExUiFrontendUrl;
        this.customerServicesProvider = customerServicesProvider;
        this.systemDateProvider = systemDateProvider;
        this.daysToWaitAfterManageFeeUpdate = daysToWaitAfterManageFeeUpdate;
    }

    @Override
    public String getTemplateId() {
        return manageFeeUpdateAdditionalPaymentTemplateId;
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_LEGAL_REPRESENTATIVE_ADDITIONAL_PAYMENT_REQUESTED";
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");

        final String dueDate = systemDateProvider.dueDate(daysToWaitAfterManageFeeUpdate);

        return
            ImmutableMap
                .<String, String>builder()
                .putAll(customerServicesProvider.getCustomerServicesPersonalisation())
                .put("appealReferenceNumber", asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
                .put("legalRepReferenceNumber", asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class).orElse(""))
                .put("appellantGivenNames", asylumCase.read(APPELLANT_GIVEN_NAMES, String.class).orElse(""))
                .put("appellantFamilyName", asylumCase.read(APPELLANT_FAMILY_NAME, String.class).orElse(""))
                .put("linkToOnlineService", iaExUiFrontendUrl)
                .put("originalFee", convertAsylumCaseFeeValue(asylumCase.read(PREVIOUS_FEE_AMOUNT_GBP, String.class).orElse("")))
                .put("newFee", convertAsylumCaseFeeValue(asylumCase.read(FEE_AMOUNT_GBP, String.class).orElse("")))
                .put("additionalFee", convertAsylumCaseFeeValue(asylumCase.read(MANAGE_FEE_REQUESTED_AMOUNT, String.class).orElse("")))
                .put("feeUpdateReason", asylumCase.read(FEE_UPDATE_REASON, FeeUpdateReason.class).map(FeeUpdateReason::getNormalizedValue).orElse(""))
                .put("onlineCaseReferenceNumber", asylumCase.read(CCD_REFERENCE_NUMBER_FOR_DISPLAY, String.class).orElse(""))
                .put("dueDate", dueDate)
                .build();
    }
}

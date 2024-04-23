package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.sms;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.CCD_REFERENCE_NUMBER_FOR_DISPLAY;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.FEE_AMOUNT_GBP;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.MANAGE_FEE_REQUESTED_AMOUNT;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.NEW_FEE_AMOUNT;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.utils.CommonUtils.convertAsylumCaseFeeValue;

import java.util.Map;
import java.util.Set;
import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.NotificationType;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.SmsNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.RecipientsFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.SystemDateProvider;

@Service
public class AipAppellantManageFeeUpdatePersonalisationSms implements SmsNotificationPersonalisation {

    private final String aipAppellantManageFeeUpdateSmsTemplateId;
    private final String iaAipFrontendUrl;
    private final int daysAfterRemissionDecision;
    private final RecipientsFinder recipientsFinder;
    private final SystemDateProvider systemDateProvider;

    public AipAppellantManageFeeUpdatePersonalisationSms(
        @Value("${govnotify.template.manageFeeUpdate.appellant.sms}") String aipAppellantManageFeeUpdateSmsTemplateId,
        @Value("${iaAipFrontendUrl}") String iaAipFrontendUrl,
        @Value("${appellantDaysToWait.afterRemissionDecision}") int daysAfterRemissionDecision,
        RecipientsFinder recipientsFinder,
        SystemDateProvider systemDateProvider
    ) {
        this.aipAppellantManageFeeUpdateSmsTemplateId = aipAppellantManageFeeUpdateSmsTemplateId;
        this.iaAipFrontendUrl = iaAipFrontendUrl;
        this.daysAfterRemissionDecision = daysAfterRemissionDecision;
        this.recipientsFinder = recipientsFinder;
        this.systemDateProvider = systemDateProvider;
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_MANAGE_FEE_UPDATE_AIP_APPELLANT_SMS";
    }

    @Override
    public String getTemplateId(AsylumCase asylumCase) {
        return aipAppellantManageFeeUpdateSmsTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(final AsylumCase asylumCase) {
        return recipientsFinder.findAll(asylumCase, NotificationType.SMS);
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");

        final String dueDate = systemDateProvider.dueDate(daysAfterRemissionDecision);

        return ImmutableMap
            .<String, String>builder()
            .put("appealReferenceNumber", asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
            .put("originalTotalFee", convertAsylumCaseFeeValue(asylumCase.read(FEE_AMOUNT_GBP, String.class).orElse("")))
            .put("newTotalFee", convertAsylumCaseFeeValue(asylumCase.read(NEW_FEE_AMOUNT, String.class).orElse("")))
            .put("paymentAmount", convertAsylumCaseFeeValue(asylumCase.read(MANAGE_FEE_REQUESTED_AMOUNT, String.class).orElse("")))
            .put("dueDate", dueDate)
            .put("onlineCaseReferenceNumber", asylumCase.read(CCD_REFERENCE_NUMBER_FOR_DISPLAY, String.class).orElse(""))
            .put("linkToService", iaAipFrontendUrl)
            .put("payByDeadline", dueDate)
            .build();
    }
}

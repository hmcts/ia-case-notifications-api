package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.sms;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.utils.CommonUtils.convertAsylumCaseFeeValue;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.normalizeDecisionHearingOptionText;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.NotificationType;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.SmsNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.RecipientsFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.SystemDateProvider;

@Service
public class AipAppellantRefundConfirmationPersonalisationSms implements SmsNotificationPersonalisation {

    private final String aipAppellantRefundConfirmationTemplateId;
    private final String iaAipFrontendUrl;
    private final int daysAfterNotificationSent;
    private final RecipientsFinder recipientsFinder;
    private final SystemDateProvider systemDateProvider;

    public AipAppellantRefundConfirmationPersonalisationSms(
        @Value("${govnotify.template.refundConfirmation.appellant.sms}") String aipAppellantRefundConfirmationTemplateId,
        @Value("${iaAipFrontendUrl}") String iaAipFrontendUrl,
        @Value("${appellantDaysToWait.afterNotificationSent}") int daysAfterNotificationSent,
        RecipientsFinder recipientsFinder,
        SystemDateProvider systemDateProvider
    ) {
        this.aipAppellantRefundConfirmationTemplateId = aipAppellantRefundConfirmationTemplateId;
        this.iaAipFrontendUrl = iaAipFrontendUrl;
        this.daysAfterNotificationSent = daysAfterNotificationSent;
        this.recipientsFinder = recipientsFinder;
        this.systemDateProvider = systemDateProvider;
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_REFUND_CONFIRMATION_AIP_APPELLANT_SMS";
    }

    @Override
    public String getTemplateId(AsylumCase asylumCase) {
        return aipAppellantRefundConfirmationTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(final AsylumCase asylumCase) {
        return recipientsFinder.findAll(asylumCase, NotificationType.SMS);
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");

        final String dueDate = systemDateProvider.dueDate(daysAfterNotificationSent);

        return ImmutableMap
            .<String, String>builder()
            .put("appealReferenceNumber", asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
            .put("linkToService", iaAipFrontendUrl)
            .put("previousDecisionHearingFeeOption", normalizeDecisionHearingOptionText(asylumCase.read(PREVIOUS_DECISION_HEARING_FEE_OPTION, String.class).orElse("")))
            .put("updatedDecisionHearingFeeOption", normalizeDecisionHearingOptionText(asylumCase.read(DECISION_HEARING_FEE_OPTION, String.class).orElse("")))
            .put("newFee", convertAsylumCaseFeeValue(asylumCase.read(NEW_FEE_AMOUNT, String.class).orElse("")))
            .put("dueDate", dueDate)
            .build();
    }
}

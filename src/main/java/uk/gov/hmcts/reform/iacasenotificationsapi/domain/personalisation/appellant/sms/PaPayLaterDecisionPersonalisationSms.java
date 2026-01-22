package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.sms;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.normalizeDecisionHearingOptionText;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.NotificationType;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.SmsNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.RecipientsFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.SystemDateProvider;

@Service
public class PaPayLaterDecisionPersonalisationSms implements SmsNotificationPersonalisation {

    private final String paPayLaterDecisionTemplateId;
    private final RecipientsFinder recipientsFinder;
    private final String iaAipFrontendUrl;
    private final SystemDateProvider systemDateProvider;
    private final int daysAfterNotificationSent;

    public PaPayLaterDecisionPersonalisationSms(
            @Value("${govnotify.template.decision.paPayLater.sms}") String paPayLaterDecisionTemplateId,
            @Value("${appellantDaysToWait.letter.afterManageFeeEvent}") int daysAfterNotificationSent,
            @Value("${iaAipFrontendUrl}") String iaAipFrontendUrl, RecipientsFinder recipientsFinder,
            SystemDateProvider systemDateProvider
    ) {
        this.paPayLaterDecisionTemplateId = paPayLaterDecisionTemplateId;
        this.iaAipFrontendUrl = iaAipFrontendUrl;
        this.recipientsFinder = recipientsFinder;
        this.systemDateProvider = systemDateProvider;
        this.daysAfterNotificationSent = daysAfterNotificationSent;
    }

    @Override
    public String getTemplateId(AsylumCase asylumCase) {
        return paPayLaterDecisionTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(final AsylumCase asylumCase) {
        return recipientsFinder.findAll(asylumCase, NotificationType.SMS);
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_PA_PAY_LATER_DECISION_SMS";
    }

    @Override
    public Map<String, String> getPersonalisation(Callback<AsylumCase> callback) {
        requireNonNull(callback, "callback must not be null");
        requireNonNull(callback.getCaseDetails(), "caseDetails must not be null");

        AsylumCase asylumCase =
                callback
                        .getCaseDetails()
                        .getCaseData();

        requireNonNull(asylumCase, "asylumCase must not be null");

        final String dueDate = systemDateProvider.dueDate(daysAfterNotificationSent);

        return ImmutableMap
                .<String, String>builder()
                .put("appealReferenceNumber",
                        asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
                .put("previousDecisionHearingFeeOption",
                        normalizeDecisionHearingOptionText(
                                asylumCase.read(PREVIOUS_DECISION_HEARING_FEE_OPTION, String.class).orElse("")))
                .put("updatedDecisionHearingFeeOption",
                        normalizeDecisionHearingOptionText(
                                asylumCase.read(DECISION_HEARING_FEE_OPTION, String.class).orElse("")))
                .put("newFee",
                        convertAsylumCaseFeeValue(
                                asylumCase.read(NEW_FEE_AMOUNT, String.class).orElse("")))
                .put("dueDate", dueDate)
                .put("linkToService", iaAipFrontendUrl)
                .build();
    }

}
package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.sms;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER;

import com.google.common.collect.ImmutableMap;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.NotificationType;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.SmsNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.RecipientsFinder;

    @Service
    public class AiPAppellantRefundRequestedNotificationSms implements SmsNotificationPersonalisation {

        private final String refundRequestedAipSmsTemplateId;
        private final RecipientsFinder recipientsFinder;
        private final String iaAipFrontendUrl;

        public AiPAppellantRefundRequestedNotificationSms(
            @Value("${govnotify.template.requestFeeRemission.appellant.sms}") String refundRequestedAipSmsTemplateId,
            RecipientsFinder recipientsFinder,
            @Value("${iaAipFrontendUrl}") String iaAipFrontendUrl
        ) {
            this.refundRequestedAipSmsTemplateId = refundRequestedAipSmsTemplateId;
            this.recipientsFinder = recipientsFinder;
            this.iaAipFrontendUrl = iaAipFrontendUrl;
        }

        @Override
        public String getTemplateId() {
            return refundRequestedAipSmsTemplateId;
        }

        @Override
        public Set<String> getRecipientsList(AsylumCase asylumCase) {
            return recipientsFinder.findAll(asylumCase, NotificationType.SMS);
        }

        @Override
        public String getReferenceId(Long caseId) {
            return caseId + "_REFUND_REQUESTED_AIP_NOTIFICATION_SMS";
        }

        @Override
        public Map<String, String> getPersonalisation(Callback<AsylumCase> callback) {
            requireNonNull(callback, "callback must not be null");

            AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

            return
                ImmutableMap
                    .<String, String>builder()
                    .put("appealReferenceNumber", asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
                    .put("linkToService", iaAipFrontendUrl)
                    .put("14 days after refund request sent", asylumCase.read(AsylumCaseDefinition.REQUEST_FEE_REMISSION_DATE, String.class)
                        .map(date -> LocalDate.parse(date).plusDays(14).format(DateTimeFormatter.ofPattern("d MMM yyyy")))
                        .orElse(""))
                    .build();
        }
    }


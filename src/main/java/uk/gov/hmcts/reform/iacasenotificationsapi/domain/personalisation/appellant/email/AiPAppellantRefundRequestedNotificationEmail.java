package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.email;

import static java.util.Objects.requireNonNull;

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
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.EmailNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.RecipientsFinder;

@Service
    public class AiPAppellantRefundRequestedNotificationEmail implements EmailNotificationPersonalisation {
        private final String refundRequestedAipEmailTemplateId;
        private final RecipientsFinder recipientsFinder;
        private final String iaAipFrontendUrl;

        public AiPAppellantRefundRequestedNotificationEmail(
            @Value("${govnotify.template.requestFeeRemission.appellant.email}") String refundRequestedAipEmailTemplateId,
            @Value("${iaAipFrontendUrl}") String iaAipFrontendUrl,
            RecipientsFinder recipientsFinder
        ) {
            this.refundRequestedAipEmailTemplateId = refundRequestedAipEmailTemplateId;
            this.recipientsFinder = recipientsFinder;
            this.iaAipFrontendUrl = iaAipFrontendUrl;

        }


        @Override
        public String getTemplateId() {
            return refundRequestedAipEmailTemplateId;
        }

        @Override
        public Set<String> getRecipientsList(AsylumCase asylumCase) {
            return recipientsFinder.findAll(asylumCase, NotificationType.EMAIL);
        }

        @Override
        public String getReferenceId(Long caseId) {
            return caseId + "_REFUND_REQUESTED_AIP_NOTIFICATION_EMAIL";
        }

        @Override
        public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
            requireNonNull(asylumCase, "asylumCase must not be null");

            return
                ImmutableMap
                    .<String, String>builder()
                    .put("Appeal Ref Number", asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
                    .put("homeOfficeReferenceNumber", asylumCase.read(AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER, String.class).orElse(""))
                    .put("appellantGivenNames", asylumCase.read(AsylumCaseDefinition.APPELLANT_GIVEN_NAMES, String.class).orElse(""))
                    .put("appellantFamilyName", asylumCase.read(AsylumCaseDefinition.APPELLANT_FAMILY_NAME, String.class).orElse(""))
                    .put("Hyperlink to service", iaAipFrontendUrl)
                    .put("14 days after refund request sent", asylumCase.read(AsylumCaseDefinition.REQUEST_FEE_REMISSION_DATE, String.class)
                        .map(date -> LocalDate.parse(date).plusDays(14).format(DateTimeFormatter.ofPattern("d MMM yyyy")))
                        .orElse(""))
                    .build();
        }
    }

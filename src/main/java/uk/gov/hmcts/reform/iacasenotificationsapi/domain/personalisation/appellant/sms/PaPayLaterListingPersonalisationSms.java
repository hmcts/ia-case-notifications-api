package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.sms;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AppealType;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.NotificationType;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.SmsNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.RecipientsFinder;

@Service
public class PaPayLaterListingPersonalisationSms implements SmsNotificationPersonalisation {

    private final String paPayLaterListingTemplateId;
    private final RecipientsFinder recipientsFinder;
    private final String iaAipFrontendUrl;

    public PaPayLaterListingPersonalisationSms(
            @Value("${govnotify.template.listing.paPayLater.sms}") String paPayLaterListingTemplateId,
            @Value("${iaAipFrontendUrl}") String iaAipFrontendUrl, RecipientsFinder recipientsFinder
    ) {
        this.paPayLaterListingTemplateId = paPayLaterListingTemplateId;
        this.iaAipFrontendUrl = iaAipFrontendUrl;
        this.recipientsFinder = recipientsFinder;
    }

    @Override
    public String getTemplateId(AsylumCase asylumCase) {

        Optional<AppealType> maybeAppealType = asylumCase.read(APPEAL_TYPE, AppealType.class);

        if (maybeAppealType.isPresent() && maybeAppealType.get() == AppealType.PA) {
            Optional<String> maybePaymentAipOption = asylumCase.read(PA_APPEAL_TYPE_AIP_PAYMENT_OPTION, String.class);
            Optional<String> maybePaymentOption = asylumCase.read(PA_APPEAL_TYPE_PAYMENT_OPTION, String.class);

            if (maybePaymentAipOption.isPresent() || maybePaymentOption.isPresent()) {
                String paymentAipOption = maybePaymentAipOption.get();
                String paymentOption = maybePaymentOption.get();
                if ("payLater".equals(paymentAipOption) || "payLater".equals(paymentOption)) {
                    return paPayLaterListingTemplateId;
                }
            }
        }
        return null;
    }

    @Override
    public Set<String> getRecipientsList(final AsylumCase asylumCase) {
        return recipientsFinder.findAll(asylumCase, NotificationType.SMS);
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_PA_PAY_LATER_CASE_LISTING_SMS";
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");

        return ImmutableMap
                .<String, String>builder()
                .put("appealReferenceNumber", asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
                .put("linkToService", iaAipFrontendUrl)
                .build();
    }
}
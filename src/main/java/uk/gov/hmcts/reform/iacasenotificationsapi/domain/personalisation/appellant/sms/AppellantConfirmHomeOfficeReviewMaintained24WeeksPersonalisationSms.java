package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.sms;

import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.*;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.SmsNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.RecipientsFinder;

import java.util.Map;
import java.util.Set;

import static java.util.Objects.requireNonNull;


@Service
public class AppellantConfirmHomeOfficeReviewMaintained24WeeksPersonalisationSms implements SmsNotificationPersonalisation {

    private final String confirmHomeOfficeReviewMaintainedTemplateId;
    private final String iaAipFrontendUrl;
    private final RecipientsFinder recipientsFinder;

    public AppellantConfirmHomeOfficeReviewMaintained24WeeksPersonalisationSms(
            @Value("${govnotify.template.confirmHOReviewMaintained.appellant.sms24Weeks}") String confirmHomeOfficeReviewMaintainedTemplateId,
            @Value("${iaAipFrontendUrl}") String iaAipFrontendUrl,
            RecipientsFinder recipientsFinder
    ) {
        this.confirmHomeOfficeReviewMaintainedTemplateId = confirmHomeOfficeReviewMaintainedTemplateId;
        this.iaAipFrontendUrl = iaAipFrontendUrl;
        this.recipientsFinder = recipientsFinder;
    }


    @Override
    public String getTemplateId(AsylumCase asylumCase) {
        return confirmHomeOfficeReviewMaintainedTemplateId;
    }


    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        return recipientsFinder.findAll(asylumCase, NotificationType.SMS);
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_STATUTORY_TIMEFRAME_24WEEKS_CONFIRM_HO_REVIEW_MAINTAINED_AIP_SMS";
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");

        return
                ImmutableMap
                        .<String, String>builder()
                        .put("Appeal Ref Number", asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
                        .put("Hyperlink to service", iaAipFrontendUrl)
                        .build();
    }
}

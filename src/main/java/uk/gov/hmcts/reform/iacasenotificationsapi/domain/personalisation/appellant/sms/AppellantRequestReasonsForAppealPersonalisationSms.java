package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.sms;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.hasStf24WeeksStatus;

import com.google.common.collect.ImmutableMap;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.Direction;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.DirectionTag;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.NotificationType;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.SmsNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.DirectionFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.RecipientsFinder;

@Service
public class AppellantRequestReasonsForAppealPersonalisationSms implements SmsNotificationPersonalisation {

    private final String requestReasonForAppealSmsTemplateId;
    private final String requestReasonForAppealSms24WeeksTemplateId;
    private final String iaAipFrontendUrl;
    private final RecipientsFinder recipientsFinder;
    private final DirectionFinder directionFinder;


    public AppellantRequestReasonsForAppealPersonalisationSms(
        @Value("${govnotify.template.requestReasonsForAppeal.appellant.sms}") String requestReasonForAppealSmsTemplateId,
        @Value("${govnotify.template.requestReasonsForAppeal.appellant.smsStf24Weeks}") String requestReasonForAppealSms24WeeksTemplateId,
        @Value("${iaAipFrontendUrl}") String iaAipFrontendUrl,
        RecipientsFinder recipientsFinder,
        DirectionFinder directionFinder
    ) {
        this.requestReasonForAppealSmsTemplateId = requestReasonForAppealSmsTemplateId;
        this.requestReasonForAppealSms24WeeksTemplateId = requestReasonForAppealSms24WeeksTemplateId;
        this.iaAipFrontendUrl = iaAipFrontendUrl;
        this.recipientsFinder = recipientsFinder;
        this.directionFinder = directionFinder;
    }

    @Override
    public String getTemplateId(AsylumCase asylumCase) {
        return hasStf24WeeksStatus(asylumCase)
            ? requestReasonForAppealSms24WeeksTemplateId
            : requestReasonForAppealSmsTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        return recipientsFinder.findAll(asylumCase, NotificationType.SMS);
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_REQUEST_REASONS_FOR_APPEAL_APPELLANT_AIP_SMS";
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");
        final Direction direction =
            directionFinder
                .findFirst(asylumCase, DirectionTag.REQUEST_REASONS_FOR_APPEAL)
                .orElseThrow(() -> new IllegalStateException("direction '" + DirectionTag.REQUEST_REASONS_FOR_APPEAL + "' is not present"));

        final String dueDate =
            LocalDate
                .parse(direction.getDateDue())
                .format(DateTimeFormatter.ofPattern("d MMM yyyy"));

        String appealReferenceNumber = asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class).orElse("");
        return
            ImmutableMap
                .<String, String>builder()
                .put("Appeal Ref Number", appealReferenceNumber)
                .put("appealReferenceNumber", appealReferenceNumber)
                .put("Hyperlink to service", iaAipFrontendUrl)
                .put("due date", dueDate)
                .build();
    }
}

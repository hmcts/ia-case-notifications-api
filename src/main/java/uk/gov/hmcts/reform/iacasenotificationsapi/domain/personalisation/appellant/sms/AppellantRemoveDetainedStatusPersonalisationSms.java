package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.sms;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.NotificationType;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.SmsNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.RecipientsFinder;

import javax.validation.constraints.NotNull;

@Service
public class AppellantRemoveDetainedStatusPersonalisationSms implements SmsNotificationPersonalisation {

    private final String removeDetainedStatusSmsTemplateId;
    private final RecipientsFinder recipientsFinder;

    public AppellantRemoveDetainedStatusPersonalisationSms(
            @NotNull(message = "removeDetentionStatusSmsTemplateId cannot be null")
            @Value("${govnotify.template.removeDetainedStatus.appellant.sms}") String removeDetentionStatusSmsTemplateId,
            RecipientsFinder recipientsFinder
    ) {
        this.removeDetainedStatusSmsTemplateId = removeDetentionStatusSmsTemplateId;
        this.recipientsFinder = recipientsFinder;
    }


    @Override
    public String getTemplateId() {
        return removeDetainedStatusSmsTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        return recipientsFinder.findAll(asylumCase, NotificationType.SMS);
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_REMOVE_DETENTION_STATUS_APPELLANT_SMS";
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");

        return
                ImmutableMap
                        .<String, String>builder()
                        .put("Appeal Ref Number", asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
                        .build();
    }
}

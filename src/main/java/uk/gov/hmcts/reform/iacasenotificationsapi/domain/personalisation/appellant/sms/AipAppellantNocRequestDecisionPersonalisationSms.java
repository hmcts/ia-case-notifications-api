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
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.PersonalisationUtils;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.SmsNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.RecipientsFinder;


@Service
public class AipAppellantNocRequestDecisionPersonalisationSms implements SmsNotificationPersonalisation {

    private final String nocRequestDecisionAppellantSmsTemplateId;
    private final RecipientsFinder recipientsFinder;

    public AipAppellantNocRequestDecisionPersonalisationSms(
        @Value("${govnotify.template.nocRequestDecision.appellant.sms}") String appealSubmittedAppellantSmsTemplateId,
        RecipientsFinder recipientsFinder
    ) {
        this.nocRequestDecisionAppellantSmsTemplateId = appealSubmittedAppellantSmsTemplateId;
        this.recipientsFinder = recipientsFinder;
    }


    @Override
    public String getTemplateId() {
        return nocRequestDecisionAppellantSmsTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        return recipientsFinder.findAll(asylumCase, NotificationType.SMS);
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_NOC_REQUEST_DECISION_APPELLANT_SMS";
    }

    @Override
    public Map<String, String> getPersonalisation(Callback<AsylumCase> callback) {
        requireNonNull(callback, "callback must not be null");

        AsylumCase asylumCase =
            callback
                .getCaseDetails()
                .getCaseData();

        final String dateOfBirth = asylumCase
            .read(AsylumCaseDefinition.APPELLANT_DATE_OF_BIRTH,String.class)
            .orElseThrow(() -> new IllegalStateException("Appellant's date of birth is not present"));

        return
            ImmutableMap
                .<String, String>builder()
                .put("Ref Number", PersonalisationUtils.formatCaseId(callback.getCaseDetails().getId()))
                .put("Given names", asylumCase.read(AsylumCaseDefinition.APPELLANT_GIVEN_NAMES, String.class).orElse(""))
                .put("Family name", asylumCase.read(AsylumCaseDefinition.APPELLANT_FAMILY_NAME, String.class).orElse(""))
                .put("Date Of Birth", PersonalisationUtils.defaultDateFormat(dateOfBirth))
                .build();
    }
}

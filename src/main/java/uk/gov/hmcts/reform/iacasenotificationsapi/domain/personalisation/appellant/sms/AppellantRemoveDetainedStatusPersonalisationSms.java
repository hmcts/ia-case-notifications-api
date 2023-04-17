package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.sms;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;

import com.google.common.collect.ImmutableMap;
import java.util.*;
import javax.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.RequiredFieldMissingException;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ContactPreferenceUnRep;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.SmsNotificationPersonalisation;

@Service
public class AppellantRemoveDetainedStatusPersonalisationSms implements SmsNotificationPersonalisation {

    private final String removeDetainedStatusSmsTemplateId;

    public AppellantRemoveDetainedStatusPersonalisationSms(
            @NotNull(message = "removeDetentionStatusSmsTemplateId cannot be null")
            @Value("${govnotify.template.removeDetentionStatus.appellant.sms}") String removeDetentionStatusSmsTemplateId
    ) {
        this.removeDetainedStatusSmsTemplateId = removeDetentionStatusSmsTemplateId;
    }


    @Override
    public String getTemplateId() {
        return removeDetainedStatusSmsTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        Set<String> recipients = new HashSet<>();

        Optional<List<String>> contactPreference = asylumCase.read(CONTACT_PREFERENCE_UN_REP);
        if (!contactPreference.isPresent()) {
            throw new RequiredFieldMissingException("No contact preference found. At least one contact method should have been provided.");
        }

        boolean emailRequired = contactPreference.get().contains(ContactPreferenceUnRep.WANTS_SMS.getValue());
        if (!emailRequired) {
            return recipients;
        }

        String emailAddress = asylumCase.read(MOBILE_NUMBER, String.class)
                .orElseThrow(() -> new RequiredFieldMissingException("Mobile number not found"));

        recipients.add(emailAddress);
        return recipients;
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
                        .put("appealReferenceNumber", asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
                        .build();
    }
}
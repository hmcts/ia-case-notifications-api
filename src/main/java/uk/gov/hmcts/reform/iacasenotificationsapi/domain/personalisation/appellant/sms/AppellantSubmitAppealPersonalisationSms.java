package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.sms;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.SUBSCRIPTIONS;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo.YES;

import com.google.common.collect.ImmutableMap;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.Subscriber;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.SmsNotificationPersonalisation;

@Service
public class AppellantSubmitAppealPersonalisationSms implements SmsNotificationPersonalisation {

    private final String appealSubmittedAppellantSmsTemplateId;

    public AppellantSubmitAppealPersonalisationSms(
        @Value("${govnotify.template.appealSubmittedAppellant.sms}") String appealSubmittedAppellantSmsTemplateId
    ) {
        this.appealSubmittedAppellantSmsTemplateId = appealSubmittedAppellantSmsTemplateId;
    }


    @Override
    public String getTemplateId() {
        return appealSubmittedAppellantSmsTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");

        final Optional<List<IdValue<Subscriber>>> maybeSubscribers = asylumCase.read(SUBSCRIPTIONS);

        return maybeSubscribers.orElse(Collections.emptyList()).stream()
            .filter(subscriber -> subscriber.getValue() != null && YES.equals(subscriber.getValue().getWantsSms()))
            .map(subscriber -> subscriber.getValue().getMobileNumber())
            .collect(Collectors.toSet());

    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_APPEAL_SUBMITTED_APPELLANT_AIP";
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

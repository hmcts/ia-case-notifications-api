package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.sms;

import com.google.common.collect.ImmutableMap;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.PersonalisationProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.NotificationType;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.SmsNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.RecipientsFinder;


import java.util.Map;
import java.util.Set;

import static java.util.Objects.requireNonNull;

@Service
public class AppellantCmrRelistingPersonalisationSms implements SmsNotificationPersonalisation {
    private final String endAppealAppellantSmsTemplateId;
    private final String iaExUiFrontendUrl;
    private final RecipientsFinder recipientsFinder;
    private final PersonalisationProvider personalisationProvider;

    public AppellantCmrRelistingPersonalisationSms(
            @Value("${govnotify.template.listAssistHearing.caseEdited.appellant.sms}") String endAppealAppellantSmsTemplateId,
            @Value("${iaExUiFrontendUrl}") String iaExUiFrontendUrl,
            RecipientsFinder recipientsFinder,
            PersonalisationProvider personalisationProvider) {
        this.endAppealAppellantSmsTemplateId = endAppealAppellantSmsTemplateId;
        this.iaExUiFrontendUrl = iaExUiFrontendUrl;
        this.recipientsFinder = recipientsFinder;
        this.personalisationProvider = personalisationProvider;
    }


    @Override
    public String getTemplateId() {
        return endAppealAppellantSmsTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        return recipientsFinder.findAll(asylumCase, NotificationType.SMS);
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_CMR_RELISTED_APPELLANT_SMS";
    }

    @Override
    public Map<String, String> getPersonalisation(Callback<AsylumCase> callback) {
        requireNonNull(callback, "asylumCase must not be null");
        return ImmutableMap
                .<String, String>builder()
                .putAll(personalisationProvider.getPersonalisation(callback))
                .put("hyperlink to service", iaExUiFrontendUrl)
                .build();
    }

    protected boolean isAppealListed(AsylumCase asylumCase) {
        return AsylumCaseUtils.isAppealListed(asylumCase);
    }
}

package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.sms;

import com.google.common.collect.ImmutableMap;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo;
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
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.CMR_IS_REMOTE_HEARING;

@Service
public class AppellantCmrRelistingPersonalisationSms implements SmsNotificationPersonalisation {
    private final String legallyReppedAppellantCmrRelistingSmsTemplateId;
    private final String iaExUiFrontendUrl;
    private final RecipientsFinder recipientsFinder;
    private final PersonalisationProvider personalisationProvider;

    public AppellantCmrRelistingPersonalisationSms(
            @Value("${govnotify.template.listAssistHearing.caseEdited.legallyReppedAppellant.sms}") String legallyReppedAppellantCmrRelistingSmsTemplateId,
            @Value("${iaExUiFrontendUrl}") String iaExUiFrontendUrl,
            RecipientsFinder recipientsFinder,
            PersonalisationProvider personalisationProvider) {
        this.legallyReppedAppellantCmrRelistingSmsTemplateId = legallyReppedAppellantCmrRelistingSmsTemplateId;
        this.iaExUiFrontendUrl = iaExUiFrontendUrl;
        this.recipientsFinder = recipientsFinder;
        this.personalisationProvider = personalisationProvider;
    }


    @Override
    public String getTemplateId(AsylumCase asylumCase) {
        return legallyReppedAppellantCmrRelistingSmsTemplateId;
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

    public boolean isLegallyRepped(AsylumCase asylumCase) {
        if (asylumCase.read(AsylumCaseDefinition.LEGAL_REP_NAME).isPresent()) {
            return true;
        } else {
            return false;
        }
    }
}

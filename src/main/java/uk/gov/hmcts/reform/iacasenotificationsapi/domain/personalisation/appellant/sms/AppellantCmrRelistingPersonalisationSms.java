package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.sms;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.isAipJourney;

import com.google.common.collect.ImmutableMap;

import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.NotificationType;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.SmsNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.RecipientsFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.HearingDetailsFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.PersonalisationProvider;

@Service
public class AppellantCmrRelistingPersonalisationSms implements SmsNotificationPersonalisation {

    private final String appellantCaseEditedSmsTemplateId;
    private final String legallyReppedAppellantCaseEditedSmsTemplateId;
    private final String iaAipFrontendUrl;
    private final PersonalisationProvider personalisationProvider;
    private final RecipientsFinder recipientsFinder;
    private final HearingDetailsFinder hearingDetailsFinder;

    public AppellantCmrRelistingPersonalisationSms(
        @Value("${govnotify.template.caseEdited.appellant.sms}") String appellantCaseEditedSmsTemplateId,
        @Value("${govnotify.template.caseEdited.legallyReppedAppellant.sms}") String legallyReppedAppellantCaseEditedSmsTemplateId,
        @Value("${iaAipFrontendUrl}") String iaAipFrontendUrl,
        PersonalisationProvider personalisationProvider,
        RecipientsFinder recipientsFinder,
        HearingDetailsFinder hearingDetailsFinder
    ) {
        this.appellantCaseEditedSmsTemplateId = appellantCaseEditedSmsTemplateId;
        this.legallyReppedAppellantCaseEditedSmsTemplateId = legallyReppedAppellantCaseEditedSmsTemplateId;
        this.iaAipFrontendUrl = iaAipFrontendUrl;
        this.personalisationProvider = personalisationProvider;
        this.recipientsFinder = recipientsFinder;
        this.hearingDetailsFinder = hearingDetailsFinder;
    }

    @Override
    public String getTemplateId(AsylumCase asylumCase) {
        return isAipJourney(asylumCase) ? appellantCaseEditedSmsTemplateId : legallyReppedAppellantCaseEditedSmsTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(final AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");
        return isAipJourney(asylumCase) ?
            recipientsFinder.findAll(asylumCase, NotificationType.SMS) :
            recipientsFinder.findReppedAppellant(asylumCase, NotificationType.SMS);
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_CMR_RE_LISTING_APPELLANT_SMS";
    }

    @Override
    public Map<String, String> getPersonalisation(Callback<AsylumCase> callback) {
        requireNonNull(callback, "callback must not be null");
        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        return ImmutableMap
            .<String, String>builder()
            .putAll(personalisationProvider.getCmrRelistingPersonalisation(callback))
            .put("tribunalCentre", hearingDetailsFinder.getCmrHearingCentreName(asylumCase))
            .put("hyperlink to service", iaAipFrontendUrl)
            .build();
    }
}

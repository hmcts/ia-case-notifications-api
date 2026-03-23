package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.sms;

import com.google.common.collect.ImmutableMap;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.DateTimeExtractor;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.HearingDetailsFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.PersonalisationProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.NotificationType;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.SmsNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.RecipientsFinder;


import java.util.Map;
import java.util.Set;

import static java.util.Objects.requireNonNull;

@Service
public class AppellantCmrListingPersonalisationSms implements SmsNotificationPersonalisation {
    private final String endAppealAppellantSmsTemplateId;
    private final String iaExUiFrontendUrl;
    private final DateTimeExtractor dateTimeExtractor;
    private final RecipientsFinder recipientsFinder;
    private final PersonalisationProvider personalisationProvider;
    private final HearingDetailsFinder hearingDetailsFinder;

    public AppellantCmrListingPersonalisationSms(
            @Value("${govnotify.template.listAssistHearing.caseListed.appellant.sms}") String endAppealAppellantSmsTemplateId,
            @Value("${iaExUiFrontendUrl}") String iaExUiFrontendUrl,
            RecipientsFinder recipientsFinder,
            DateTimeExtractor dateTimeExtractor,
            PersonalisationProvider personalisationProvider, HearingDetailsFinder hearingDetailsFinder) {
        this.endAppealAppellantSmsTemplateId = endAppealAppellantSmsTemplateId;
        this.iaExUiFrontendUrl = iaExUiFrontendUrl;
        this.dateTimeExtractor = dateTimeExtractor;
        this.recipientsFinder = recipientsFinder;
        this.personalisationProvider = personalisationProvider;
        this.hearingDetailsFinder = hearingDetailsFinder;
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
        return caseId + "_CMR_LISTED_APPELLANT_SMS";
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");
        return ImmutableMap
                .<String, String>builder()
                .put("appealReferenceNumber", asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
                .put("hearingDate", dateTimeExtractor.extractHearingDate(hearingDetailsFinder.getCmrHearingDateTime(asylumCase)))
                .put("hearingTime", dateTimeExtractor.extractHearingTime(hearingDetailsFinder.getCmrHearingDateTime(asylumCase)))
                .put("hearingCentreAddress", hearingDetailsFinder.getCmrHearingCentreLocation(asylumCase))
                .put("hyperlink to service", iaExUiFrontendUrl)
                .build();
    }

    protected boolean isAppealListed(AsylumCase asylumCase) {
        return AsylumCaseUtils.isAppealListed(asylumCase);
    }
}

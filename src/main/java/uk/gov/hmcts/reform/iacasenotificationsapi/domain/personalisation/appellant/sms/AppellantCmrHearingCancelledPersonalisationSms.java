package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.sms;

import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.NotificationType;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.SmsNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.RecipientsFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.DateTimeExtractor;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.HearingDetailsFinder;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.isAipJourney;

@Service
public class AppellantCmrHearingCancelledPersonalisationSms implements SmsNotificationPersonalisation {

    private final String cmrCancelledSmsTemplateId;
    private final RecipientsFinder recipientsFinder;
    private final String iaAipFrontendUrl;
    private final DateTimeExtractor dateTimeExtractor;
    private final HearingDetailsFinder hearingDetailsFinder;


    public AppellantCmrHearingCancelledPersonalisationSms(
            @Value("${govnotify.template.cmrCancelled.appellant.sms}") String cmrCancelledSmsTemplateId,
            @Value("${iaAipFrontendUrl}") String iaAipFrontendUrl,
            RecipientsFinder recipientsFinder,
            DateTimeExtractor dateTimeExtractor,
            HearingDetailsFinder hearingDetailsFinder
    ) {
        this.cmrCancelledSmsTemplateId = cmrCancelledSmsTemplateId;
        this.iaAipFrontendUrl = iaAipFrontendUrl;
        this.recipientsFinder = recipientsFinder;
        this.dateTimeExtractor = dateTimeExtractor;
        this.hearingDetailsFinder = hearingDetailsFinder;

    }

    @Override
    public String getTemplateId() {
        return cmrCancelledSmsTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(final AsylumCase asylumCase) {
        return isAipJourney(asylumCase) ? recipientsFinder.findAll(asylumCase, NotificationType.SMS) : Collections.emptySet();
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_CMR_HEARING_CANCELLED_APPELLANT_AIP_SMS";
    }


    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");
        return
            ImmutableMap
                .<String, String>builder()
                .put("appealReferenceNumber", asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
                .put("oldHearingDate", dateTimeExtractor.extractHearingDate(hearingDetailsFinder.getCmrHearingDateTime(asylumCase)))
                .put("oldHearingTime", dateTimeExtractor.extractHearingTime(hearingDetailsFinder.getCmrHearingDateTime(asylumCase)))
                    .put("oldHearingCentreAddress", hearingDetailsFinder.getCmrHearingCentreLocation(asylumCase))
                .put("linkToService", iaAipFrontendUrl)
                .build();
    }

}

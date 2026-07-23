package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.email;

import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.*;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.EmailNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.RecipientsFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.DateTimeExtractor;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.HearingDetailsFinder;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.isAipJourney;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.isInternalCase;

@Service
public class AppellantCmrHearingCancelledPersonalisationEmail implements EmailNotificationPersonalisation {

    private final String appellantCmrCancelledEmailTemplateId;
    private final String appellantManualCmrCancelledEmailTemplateId;
    private final RecipientsFinder recipientsFinder;
    private final DateTimeExtractor dateTimeExtractor;
    private final HearingDetailsFinder hearingDetailsFinder;

    public AppellantCmrHearingCancelledPersonalisationEmail(
            @Value("${govnotify.template.cmrCancelled.appellant.email}") String appellantCmrCancelledEmailTemplateId,
            @Value("${govnotify.template.cmrCancelled.manualAppellant.email}") String appellantManualCmrCancelledEmailTemplateId,
            RecipientsFinder recipientsFinder,
            DateTimeExtractor dateTimeExtractor,
            HearingDetailsFinder hearingDetailsFinder

    ) {
        this.appellantCmrCancelledEmailTemplateId = appellantCmrCancelledEmailTemplateId;
        this.appellantManualCmrCancelledEmailTemplateId = appellantManualCmrCancelledEmailTemplateId;
        this.recipientsFinder = recipientsFinder;
        this.dateTimeExtractor = dateTimeExtractor;
        this.hearingDetailsFinder = hearingDetailsFinder;

    }

    @Override
    public String getTemplateId(AsylumCase asylumCase) {
        return isAipJourney(asylumCase) && !isInternalCase(asylumCase) ? appellantCmrCancelledEmailTemplateId : appellantManualCmrCancelledEmailTemplateId;
    }


    @Override
    public Set<String> getRecipientsList(final AsylumCase asylumCase) {
        return isAipJourney(asylumCase) && !isInternalCase(asylumCase) ? recipientsFinder.findAll(asylumCase, NotificationType.EMAIL) : Collections.emptySet();

    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_CMR_HEARING_CANCELLED_APPELLANT_AIP_EMAIL";
    }


    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");


        return
                ImmutableMap
                        .<String, String>builder()
                        .put("appealReferenceNumber", asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
                        .put("appellantGivenNames", asylumCase.read(AsylumCaseDefinition.APPELLANT_GIVEN_NAMES, String.class).orElse(""))
                        .put("appellantFamilyName", asylumCase.read(AsylumCaseDefinition.APPELLANT_FAMILY_NAME, String.class).orElse(""))
                        .put("oldHearingDate", dateTimeExtractor.extractHearingDate(hearingDetailsFinder.getCmrHearingDateTime(asylumCase)))
                        .put("oldHearingTime", dateTimeExtractor.extractHearingTime(hearingDetailsFinder.getCmrHearingDateTime(asylumCase))).put("oldHearingCentreAddress", hearingDetailsFinder.getCmrHearingCentreLocation(asylumCase))
                        .build();
    }

}

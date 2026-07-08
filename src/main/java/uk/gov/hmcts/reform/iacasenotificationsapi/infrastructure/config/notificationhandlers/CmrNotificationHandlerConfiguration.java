package uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.config.notificationhandlers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.handlers.presubmit.NotificationHandler;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.NotificationGenerator;

import java.util.List;

import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.DetentionFacility.IRC;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.DetentionFacility.PRISON;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.*;

@Slf4j
@Configuration
public class CmrNotificationHandlerConfiguration {
    @Bean
    public PreSubmitCallbackHandler<AsylumCase> cmrInPersonDetainedInPrisonIrcListedNotificationHandler(
        @Qualifier("detainedInPrisonIrcLegalRepInPersonCmrListingNotificationGenerator") List<NotificationGenerator> notificationGenerators
    ) {

        return new NotificationHandler(
                (callbackStage, callback) -> {
                    AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
                    return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                            && Event.CMR_LISTING.equals(callback.getEvent())
                            && isCmrHearingChannel(asylumCase, "INTER")
                            && isDetainedInOneOfFacilityTypes(asylumCase, IRC, PRISON)
                            && isRepJourney(callback.getCaseDetails().getCaseData())
                            && !isInternalCase(asylumCase);
                },
                notificationGenerators
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> cmrInPersonDetainedInPrisonListedNotificationHandler(
            @Qualifier("detainedInPrisonAppellantRepInPersonCmrListingNotificationGenerator") List<NotificationGenerator> notificationGenerators
    ) {

        return new NotificationHandler(
                (callbackStage, callback) -> {
                    AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
                    return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                            && Event.CMR_LISTING.equals(callback.getEvent())
                            && isCmrHearingChannel(asylumCase, "INTER")
                            && isDetainedInOneOfFacilityTypes(asylumCase, PRISON)
                            && isRepJourney(callback.getCaseDetails().getCaseData())
                            && !isInternalCase(asylumCase);
                },
                notificationGenerators
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> cmrInPersonDetainedInIrcListedNotificationHandler(
            @Qualifier("detainedInIrcLegalRepInPersonCmrListingNotificationGenerator") List<NotificationGenerator> notificationGenerators
    ) {

        return new NotificationHandler(
                (callbackStage, callback) -> {
                    AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
                    return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                            && Event.CMR_LISTING.equals(callback.getEvent())
                            && isCmrHearingChannel(asylumCase, "INTER")
                            && isDetainedInOneOfFacilityTypes(asylumCase, IRC)
                            && isRepJourney(callback.getCaseDetails().getCaseData())
                            && !isInternalCase(asylumCase);
                },
                notificationGenerators
        );
    }
}

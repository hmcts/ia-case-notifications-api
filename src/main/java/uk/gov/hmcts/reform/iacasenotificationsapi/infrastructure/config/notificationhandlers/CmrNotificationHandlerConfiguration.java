package uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.config.notificationhandlers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.handlers.presubmit.NotificationHandler;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.NotificationGenerator;

import java.util.List;

import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.DetentionFacility.IRC;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.DetentionFacility.PRISON;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.Event.CMR_LISTING;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.Event.CMR_RE_LISTING;
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
                boolean result = callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && (CMR_LISTING.equals(callback.getEvent()) || CMR_RE_LISTING.equals(callback.getEvent()))
                    && (isCmrHearingChannel(asylumCase, "INTER")
                        || isCmrHearingChannel(asylumCase, "VID")
                        || isCmrHearingChannel(asylumCase, "TEL")
                    )
                    && isDetainedInOneOfFacilityTypes(asylumCase, IRC, PRISON)
                    && isRepJourney(callback.getCaseDetails().getCaseData())
                    && !isInternalCase(asylumCase);
                log.info("cmrInPersonDetainedInPrisonIrcListedNotificationHandler canHandle={} for event={}, stage={}",
                    result, callback.getEvent(), callbackStage);
                return result;
            },
            notificationGenerators
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> nonDetainedCmrRelistingHoCoLrNotificationHandler(
        @Qualifier("nonDetainedCmrRelistingHoCoLrNotificationGenerator") List<NotificationGenerator> notificationGenerators
    ) {

        return new NotificationHandler(
            (callbackStage, callback) -> {
                AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
                boolean result = callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && isNonDetainedCmrRelisting(callback, asylumCase);
                log.info("nonDetainedCmrRelistingHoCoLrNotificationHandler canHandle={} for event={}, stage={}",
                    result, callback.getEvent(), callbackStage);
                return result;
            },
            notificationGenerators
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> nonDetainedCmrRelistingAppellantEmailNotificationHandler(
        @Qualifier("nonDetainedCmrRelistingAppellantEmailNotificationGenerator") List<NotificationGenerator> notificationGenerators
    ) {

        return new NotificationHandler(
            (callbackStage, callback) -> {
                AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
                boolean isEmailPreferred = isReppedAppellantEmailPreferred(asylumCase);
                log.info("nonDetainedCmrRelistingAppellantEmailNotificationHandler isReppedAppellantEmailPreferred={}",
                    isEmailPreferred);
                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && isNonDetainedCmrRelisting(callback, asylumCase)
                    && isEmailPreferred;
            },
            notificationGenerators
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> nonDetainedCmrRelistingAppellantSmsNotificationHandler(
        @Qualifier("nonDetainedCmrRelistingAppellantSmsNotificationGenerator") List<NotificationGenerator> notificationGenerators
    ) {

        return new NotificationHandler(
            (callbackStage, callback) -> {
                AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
                boolean isSmsPreferred = isReppedAppellantSmsPreferred(asylumCase);
                log.info("nonDetainedCmrRelistingAppellantSmsNotificationHandler isReppedAppellantSmsPreferred={}",
                    isSmsPreferred);
                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && isNonDetainedCmrRelisting(callback, asylumCase)
                    && isSmsPreferred;
            },
            notificationGenerators
        );
    }

    private boolean isNonDetainedCmrRelisting(Callback<AsylumCase> callback, AsylumCase asylumCase) {
        boolean isCmrRelistingEvent = CMR_RE_LISTING.equals(callback.getEvent());
        boolean isCmrChannelInPersonOrRemote = isCmrHearingChannel(asylumCase, "INTER")
            || isCmrHearingChannel(asylumCase, "VID")
            || isCmrHearingChannel(asylumCase, "TEL");
        boolean isRepJourney = isRepJourney(asylumCase);
        boolean isNotInternalCase = !isInternalCase(asylumCase);
        boolean isNotAppellantInDetention = !isAppellantInDetention(asylumCase);

        boolean result = isCmrRelistingEvent
            && isCmrChannelInPersonOrRemote
            && isRepJourney
            && isNotInternalCase
            && isNotAppellantInDetention;

        log.info(
            "isNonDetainedCmrRelisting evaluated to {} for event={}, isCmrRelistingEvent={}, "
                + "isCmrChannelInPersonOrRemote={}, isRepJourney={}, isNotInternalCase={}, isNotAppellantInDetention={}",
            result, callback.getEvent(), isCmrRelistingEvent, isCmrChannelInPersonOrRemote,
            isRepJourney, isNotInternalCase, isNotAppellantInDetention
        );

        return result;
    }
}

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
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.Event.*;
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
                    && CMR_LISTING.equals(callback.getEvent())
                    && isCmrHearingInPersonOrRemote(asylumCase)
                    && isDetainedInOneOfFacilityTypes(asylumCase, IRC, PRISON)
                    && isRepJourney(callback.getCaseDetails().getCaseData())
                    && !isInternalCase(asylumCase);
            },
            notificationGenerators
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> cmrListingAipManualNotificationHandler(
        @Qualifier("aipManualCmrListingNotificationGenerator") List<NotificationGenerator> notificationGenerators
    ) {

        return new NotificationHandler(
            (callbackStage, callback) -> {
                AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && CMR_LISTING.equals(callback.getEvent())
                    && isCmrHearingInPersonOrRemote(asylumCase)
                    && isInternalCase(callback.getCaseDetails().getCaseData())
                    && isAipJourney(callback.getCaseDetails().getCaseData());
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
                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && isNonDetainedCmrRelisting(callback, asylumCase);
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
                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && isNonDetainedCmrRelisting(callback, asylumCase)
                    && isReppedAppellantEmailPreferred(asylumCase);
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
                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && isNonDetainedCmrRelisting(callback, asylumCase)
                    && isReppedAppellantSmsPreferred(asylumCase);
            },
            notificationGenerators
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> cmrListingLegalRepDigitalNotificationHandler(
            @Qualifier("legalRepDigitalCmrListingNotificationGenerator") List<NotificationGenerator> notificationGenerators
    ) {

        return new NotificationHandler(
            (callbackStage, callback) -> {
                AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && CMR_LISTING.equals(callback.getEvent())
                    && isCmrHearingInPersonOrRemote(asylumCase)
                    && !isInternalCase(callback.getCaseDetails().getCaseData())
                    && isRepJourney(callback.getCaseDetails().getCaseData())
                    && !isAppellantInDetention(asylumCase);
            },
            notificationGenerators
        );
    }

    private boolean isNonDetainedCmrRelisting(Callback<AsylumCase> callback, AsylumCase asylumCase) {
        return CMR_RE_LISTING.equals(callback.getEvent())
            && (isCmrHearingChannel(asylumCase, "INTER")
                || isCmrHearingChannel(asylumCase, "VID")
                || isCmrHearingChannel(asylumCase, "TEL")
            )
            && isRepJourney(asylumCase)
            && !isInternalCase(asylumCase)
            && !isAppellantInDetention(asylumCase);
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> cmrHearingCancelledNotificationHandler(
            @Qualifier("cmrHearingCancelledNotificationGenerator") List<NotificationGenerator> notificationGenerators
    ) {
        return new NotificationHandler(
                (callbackStage, callback) -> {

                    return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                            && CMR_HEARING_CANCELLED.equals(callback.getEvent());
                },
                notificationGenerators
        );
    }
}

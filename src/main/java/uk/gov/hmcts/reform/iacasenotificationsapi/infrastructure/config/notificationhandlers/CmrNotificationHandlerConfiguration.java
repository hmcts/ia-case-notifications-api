package uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.config.notificationhandlers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.NotificationType;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.handlers.presubmit.NotificationHandler;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.NotificationGenerator;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.RecipientsFinder;

import java.util.List;

import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.DetentionFacility.*;
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
                    && hasBeenSubmittedByAppellantInternalCase(asylumCase);
            },
            notificationGenerators
        );
    }

    // This covers both AIP manual non detained and detained in other
    @Bean
    public PreSubmitCallbackHandler<AsylumCase> cmrRelistingAipManualAppellantPostalNotificationHandler(
        @Qualifier("aipManualCmrRelistingAppellantPostalNotificationGenerator") List<NotificationGenerator> notificationGenerators
    ) {

        return new NotificationHandler(
            (callbackStage, callback) -> {
                AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && isAipManualCmrRelisting(callback, asylumCase);
            },
            notificationGenerators
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> cmrRelistingAipManualHoCoEmailHandler(
        @Qualifier("aipManualCmrRelistingHoCoEmailsGenerator") List<NotificationGenerator> notificationGenerators
    ) {

        return new NotificationHandler(
            (callbackStage, callback) -> {
                AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && isAipManualCmrRelisting(callback, asylumCase);
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
                asylumCase.read(AsylumCaseDefinition.CONTACT_PREFERENCE, String.class);
                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && isNonDetainedCmrRelisting(callback, asylumCase)
                    && isReppedAppellantSmsPreferred(asylumCase);
            },
            notificationGenerators
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> cmrRelistedAipHoCoEmailHandler(
        @Qualifier("cmrRelistedAipHoCoEmailsGenerator") List<NotificationGenerator> notificationGenerators
    ) {

        return new NotificationHandler(
            (callbackStage, callback) -> {
                AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && isAipCmr(callback, asylumCase);
            },
            notificationGenerators
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> cmrRelistedAipAppellantEmailHandler(
        @Qualifier("cmrRelistedAppellantEmailsGenerator") List<NotificationGenerator> notificationGenerators,
        RecipientsFinder recipientsFinder
    ) {

        return new NotificationHandler(
            (callbackStage, callback) -> {
                AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
                boolean wantsEmail = !recipientsFinder.findAll(asylumCase, NotificationType.EMAIL).isEmpty();
                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && isAipCmr(callback, asylumCase)
                    && wantsEmail;
            },
            notificationGenerators
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> cmrRelistedAipAppellantSmslHandler(
        @Qualifier("cmrRelistedAppellantSmsGenerator") List<NotificationGenerator> notificationGenerators,
        RecipientsFinder recipientsFinder
    ) {

        return new NotificationHandler(
            (callbackStage, callback) -> {
                AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
                boolean wantsSms = !recipientsFinder.findAll(asylumCase, NotificationType.SMS).isEmpty();
                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && isAipCmr(callback, asylumCase)
                    && wantsSms;
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

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> cmrListingLegalRepDigitalDetainedOtherNotificationHandler(
            @Qualifier("legalRepDigitalDetainedOtherCmrListingNotificationGenerator") List<NotificationGenerator> notificationGenerators
    ) {

        return new NotificationHandler(
            (callbackStage, callback) -> {
                AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && CMR_LISTING.equals(callback.getEvent())
                    && isCmrHearingInPersonOrRemote(asylumCase)
                    && isRepJourney(callback.getCaseDetails().getCaseData())
                    && !isInternalCase(asylumCase)
                    && isDetainedInFacilityType(asylumCase, OTHER);
            },
            notificationGenerators
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> cmrCancelledManualNotificationHandler(
            @Qualifier("cmrCancelledManualNotificationGenerator") List<NotificationGenerator> notificationGenerators
    ) {

        return new NotificationHandler(
                (callbackStage, callback) -> {
                    AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
                    return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                            && CMR_HEARING_CANCELLED.equals(callback.getEvent())
                            && isCmrHearingInPersonOrRemote(asylumCase)
                            && ((isInternalCase(asylumCase) && !isAppellantInDetention(asylumCase)) || isDetainedInFacilityType(asylumCase, OTHER));
                },
                notificationGenerators
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> cmrCancelledLrManualNotificationHandler(
            @Qualifier("cmrCancelledLrManualNotificationGenerator") List<NotificationGenerator> notificationGenerators
    ) {

        return new NotificationHandler(
                (callbackStage, callback) -> {
                    AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
                    return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                            && CMR_HEARING_CANCELLED.equals(callback.getEvent())
                            && isCmrHearingInPersonOrRemote(asylumCase)
                            && hasBeenSubmittedAsLegalRepresentedInternalCase(asylumCase);
                },
                notificationGenerators
        );
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

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> cmrListingAipDigitalNotificationHandler(
        @Qualifier("aipDigitalCmrListingNotificationGenerator") List<NotificationGenerator> notificationGenerators
    ) {

        return new NotificationHandler(
            (callbackStage, callback) -> {
                AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && CMR_LISTING.equals(callback.getEvent())
                    && isCmrHearingInPersonOrRemote(asylumCase)
                    && !isInternalCase(callback.getCaseDetails().getCaseData())
                    && isAipJourney(callback.getCaseDetails().getCaseData())
                    && !isAppellantInDetention(asylumCase);
            },
            notificationGenerators
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> cmrListingLrManualNotificationHandler(
        @Qualifier("lrManualCmrListingNotificationGenerator") List<NotificationGenerator> notificationGenerators
    ) {

        return new NotificationHandler(
            (callbackStage, callback) -> {
                AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && CMR_LISTING.equals(callback.getEvent())
                    && isCmrHearingInPersonOrRemote(asylumCase)
                    && (!isAppellantInDetention(asylumCase) || isDetainedInFacilityType(asylumCase, OTHER))
                    && hasBeenSubmittedAsLegalRepresentedInternalCase(asylumCase);
            },
            notificationGenerators
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> cmrListingLrManualDetainedInPrisonOrIrcNotificationHandler(
        @Qualifier("lrManualDetainedInPrisonOrIrcCmrListingNotificationGenerator") List<NotificationGenerator> notificationGenerators
    ) {

        return new NotificationHandler(
            (callbackStage, callback) -> {
                AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && CMR_LISTING.equals(callback.getEvent())
                    && isCmrHearingInPersonOrRemote(asylumCase)
                    && isDetainedInOneOfFacilityTypes(asylumCase, IRC, PRISON)
                    && hasBeenSubmittedAsLegalRepresentedInternalCase(asylumCase);
            },
            notificationGenerators
        );
    }

    //    This handler also handles DIAC-2366 LR manual detained in other since notifications for non detained and detained in other behave identically
    @Bean
    public PreSubmitCallbackHandler<AsylumCase> cmrRelistingNonDetainedLegallyRepresentedManualAppeal(
            @Qualifier("cmrRelistingLegallyRepresentedManualNonDetainedAppealGenerator") List<NotificationGenerator> notificationGenerators
    ) {

        return new NotificationHandler(
                (callbackStage, callback) -> {
                    AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
                    return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                            && CMR_RE_LISTING.equals(callback.getEvent())
                            && isCmrHearingInPersonOrRemote(asylumCase)
                            && hasBeenSubmittedAsLegalRepresentedInternalCase(asylumCase)
                            && (!isAppellantInDetention(asylumCase) || isDetainedInOneOfFacilityTypes(asylumCase,OTHER));
                },
                notificationGenerators
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> cmrReListingLegalRepDigitalDetainedInOtherNotificationHandler(
            @Qualifier("cmrReListingLegalRepDigitalDetainedInOtherNotificationGenerator") List<NotificationGenerator> notificationGenerators
    ) {

        return new NotificationHandler(
                (callbackStage, callback) -> {
                    AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
                    return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                            && CMR_RE_LISTING.equals(callback.getEvent())
                            && isCmrHearingInPersonOrRemote(asylumCase)
                            && !isInternalCase(callback.getCaseDetails().getCaseData())
                            && isRepJourney(callback.getCaseDetails().getCaseData())
                            && isDetainedInOneOfFacilityTypes(asylumCase, OTHER);
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

    private boolean isAipManualCmrRelisting(Callback<AsylumCase> callback, AsylumCase asylumCase) {
        return CMR_RE_LISTING.equals(callback.getEvent())
            && isCmrHearingInPersonOrRemote(asylumCase)
            && hasBeenSubmittedByAppellantInternalCase(asylumCase);
    }

    private boolean isAipCmr(Callback<AsylumCase> callback, AsylumCase asylumCase) {
        return CMR_RE_LISTING.equals(callback.getEvent())
            && (isCmrHearingChannel(asylumCase, "INTER")
                || isCmrHearingChannel(asylumCase, "VID")
                || isCmrHearingChannel(asylumCase, "TEL")
            )
            && isAipJourney(asylumCase)
            && !isInternalCase(asylumCase);
    }
}

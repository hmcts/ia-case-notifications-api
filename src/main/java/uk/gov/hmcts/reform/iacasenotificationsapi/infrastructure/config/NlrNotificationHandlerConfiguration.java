package uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.config;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.NLR_DETAILS;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.SHOULD_INVITE_NLR_TO_IDAM;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.Event.JOIN_APPEAL_CONFIRMATION;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.Event.NLR_DETAILS_UPDATED;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.Event.REMOVE_NON_LEGAL_REP;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.Event.SEND_INVITE_TO_NON_LEGAL_REP;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.Event.SEND_PIP_TO_NON_LEGAL_REP;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.Event.SUBMIT_APPEAL;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.isAipJourney;

import java.util.List;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.NonLegalRepDetails;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.handlers.presubmit.NotificationHandler;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.NotificationGenerator;

@Configuration
public class NlrNotificationHandlerConfiguration {

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> generateSendInviteToNonLegalRepNotificationHandler(
        @Qualifier("generateSendInviteToNonLegalRepNotificationGenerator") List<NotificationGenerator> notificationGenerators) {
        return new NotificationHandler(
            (callbackStage, callback) -> {
                List<Event> validNlrEvents = List.of(SEND_INVITE_TO_NON_LEGAL_REP, SUBMIT_APPEAL);
                if (callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT) {
                    AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
                    boolean shouldSend = asylumCase.read(SHOULD_INVITE_NLR_TO_IDAM, YesOrNo.class)
                        .map(flag -> flag.equals(YesOrNo.YES)).orElse(false);
                    String nlrEmail =
                        asylumCase.read(NLR_DETAILS, NonLegalRepDetails.class)
                            .map(NonLegalRepDetails::getEmailAddress)
                            .orElse(null);
                    return validNlrEvents.contains(callback.getEvent())
                        && isAipJourney(asylumCase) && isNotEmpty(nlrEmail) && shouldSend;
                }
                return false;
            },
            notificationGenerators
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> generateSendPipToNonLegalRepNotificationHandler(
        @Qualifier("generateSendPipToNonLegalRepNotificationGenerator") List<NotificationGenerator> notificationGenerators) {
        return new NotificationHandler(
            (callbackStage, callback) -> callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                && callback.getEvent() == SEND_PIP_TO_NON_LEGAL_REP,
            notificationGenerators
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> generateJoinAppealConfirmationNotificationHandler(
        @Qualifier("generateJoinAppealConfirmationNotificationGenerator") List<NotificationGenerator> notificationGenerators) {
        return new NotificationHandler(
            (callbackStage, callback) -> callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                && callback.getEvent() == JOIN_APPEAL_CONFIRMATION,
            notificationGenerators
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> generateNonLegalRepRemovedNotificationHandler(
        @Qualifier("generateNonLegalRepRemovedNotificationHandler") List<NotificationGenerator> notificationGenerators) {
        return new NotificationHandler(
            (callbackStage, callback) -> callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                && callback.getEvent() == REMOVE_NON_LEGAL_REP,
            notificationGenerators
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> generateNlrPhoneNumberSubmittedNotificationHandler(
        @Qualifier("generateNlrPhoneNumberSubmittedNotificationGenerator") List<NotificationGenerator> notificationGenerators) {
        return new NotificationHandler(
            (callbackStage, callback) -> {
                if (callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT) {
                    AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
                    String nlrEmail =
                        asylumCase.read(NLR_DETAILS, NonLegalRepDetails.class)
                            .map(NonLegalRepDetails::getEmailAddress)
                            .orElse(null);
                    return callback.getEvent().equals(NLR_DETAILS_UPDATED)
                        && isAipJourney(asylumCase) && isNotEmpty(nlrEmail);
                }
                return false;
            },
            notificationGenerators
        );
    }
}

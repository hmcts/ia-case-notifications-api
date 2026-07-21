package uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.config.notificationhandlers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.handlers.presubmit.NotificationHandler;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.NotificationGenerator;

import java.util.List;

import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.Event.CMR_HEARING_CANCELLED;


@Slf4j
@Configuration
public class CmrNotificationHandlerConfig {

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
package uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.config;


import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.BailCaseFieldDefinition.IS_LEGALLY_REPRESENTED_FOR_FLAG;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.BailCaseFieldDefinition.SUBMIT_NOTIFICATION_STATUS;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.handlers.ErrorHandler;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.handlers.presubmit.BailNotificationHandler;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.BailNotificationGenerator;

@Slf4j
@Configuration
public class BailNotificationHandlerConfiguration {
    @Bean
    public PreSubmitCallbackHandler<BailCase> submitApplicationHearingCentreNotificationHandler(
        @Qualifier("submitApplicationNotificationGenerator") List<BailNotificationGenerator> bailNotificationGenerators
    ) {
        return new BailNotificationHandler(
                (callbackStage, callback) -> {
                    return (callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                        && callback.getEvent() == Event.SUBMIT_APPLICATION
                    );
                },
                bailNotificationGenerators,
                getErrorHandler()
        );
    }

    @Bean
    public PreSubmitCallbackHandler<BailCase> uploadSummaryNotificationHandler(
            @Qualifier("uploadSummaryNotificationGenerator") List<BailNotificationGenerator> bailNotificationGenerators
    ) {
        return new BailNotificationHandler(
                (callbackStage, callback) -> {
                    BailCase bailCase = callback.getCaseDetails().getCaseData();
                    return (callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                            && callback.getEvent() == Event.UPLOAD_BAIL_SUMMARY
                            && isLegallyRepresented(bailCase));
                },
                bailNotificationGenerators,
                getErrorHandler()
        );
    }

    @Bean
    public PreSubmitCallbackHandler<BailCase> uploadSummaryWithoutLrNotificationHandler(
            @Qualifier("uploadSummaryWithoutLrNotificationGenerator") List<BailNotificationGenerator> bailNotificationGenerators
    ) {
        return new BailNotificationHandler(
                (callbackStage, callback) -> {
                    BailCase bailCase = callback.getCaseDetails().getCaseData();
                    return (callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                            && callback.getEvent() == Event.UPLOAD_BAIL_SUMMARY
                            && !isLegallyRepresented(bailCase));
                },
                bailNotificationGenerators,
                getErrorHandler()
        );
    }

    private ErrorHandler<BailCase> getErrorHandler() {
        ErrorHandler<BailCase> errorHandler = (callback, e) -> {
            callback
                .getCaseDetails()
                .getCaseData()
                .write(SUBMIT_NOTIFICATION_STATUS, "Failed");
        };
        return errorHandler;
    }

    private boolean isLegallyRepresented(BailCase bailCase) {
        return (bailCase.read(IS_LEGALLY_REPRESENTED_FOR_FLAG, YesOrNo.class).orElse(YesOrNo.NO)) == YesOrNo.YES;
    }

}



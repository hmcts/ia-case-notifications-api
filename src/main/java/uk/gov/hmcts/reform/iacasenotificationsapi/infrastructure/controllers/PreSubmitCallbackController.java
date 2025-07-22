package uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.controllers;

import static java.util.Objects.requireNonNull;
import static org.springframework.http.ResponseEntity.ok;

import io.swagger.v3.oas.annotations.Parameter;
import lombok.extern.slf4j.Slf4j;

import javax.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.CaseData;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.PreSubmitCallbackDispatcher;

@Slf4j
public class PreSubmitCallbackController<T extends CaseData> {

    private final PreSubmitCallbackDispatcher<T> callbackDispatcher;

    public PreSubmitCallbackController(
        PreSubmitCallbackDispatcher<T> callbackDispatcher
    ) {
        requireNonNull(callbackDispatcher, "callbackDispatcher must not be null");

        this.callbackDispatcher = callbackDispatcher;
    }

    public ResponseEntity<PreSubmitCallbackResponse<T>> ccdAboutToStart(
        @Parameter(name = "Asylum case data", required = true) @NotNull @RequestBody Callback<T> callback
    ) {
        log.info(
            "Asylum Case CCD `ABOUT_TO_START` event `{}` received for Case ID `{}`",
            callback.getEvent(),
            callback.getCaseDetails().getId()
        );
        return performStageRequest(PreSubmitCallbackStage.ABOUT_TO_START, callback);
    }

    public ResponseEntity<PreSubmitCallbackResponse<T>> ccdAboutToSubmit(
        @Parameter(name = "Asylum case data", required = true) @NotNull @RequestBody Callback<T> callback
    ) {
        log.info("Asylum Case CCD `ABOUT_TO_SUBMIT` event `{}` received for Case ID `{}`",
            callback.getEvent(),
            callback.getCaseDetails().getId()
        );
        return performStageRequest(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);
    }

    private ResponseEntity<PreSubmitCallbackResponse<T>> performStageRequest(
        PreSubmitCallbackStage callbackStage,
        Callback<T> callback
    ) {

        log.info(
            "Asylum Case CCD before `{}` event `{}` received for Case ID `{}`",
            callbackStage,
            callback.getEvent(),
            callback.getCaseDetails().getId()
        );

        PreSubmitCallbackResponse<T> callbackResponse =
            callbackDispatcher.handle(callbackStage, callback);

        log.info(
            "Asylum Case CCD After `{}` event `{}` handled for Case ID `{}`",
            callbackStage,
            callback.getEvent(),
            callback.getCaseDetails().getId()
        );

        //log callbackResponse.getData().toString();
        log.info(
            "Asylum Case CCD Data `{}`", callbackResponse.getData()
        );

        return ok(callbackResponse);
    }
}

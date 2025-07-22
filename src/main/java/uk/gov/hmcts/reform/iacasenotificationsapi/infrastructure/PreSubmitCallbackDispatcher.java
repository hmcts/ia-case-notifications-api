package uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure;

import static java.util.Objects.requireNonNull;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.CaseData;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.DispatchPriority;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.security.CcdEventAuthorizor;

@Slf4j
@Component
public class PreSubmitCallbackDispatcher<T extends CaseData> {

    private final CcdEventAuthorizor ccdEventAuthorizor;
    private final List<PreSubmitCallbackHandler<T>> sortedCallbackHandlers;

    public PreSubmitCallbackDispatcher(
        CcdEventAuthorizor ccdEventAuthorizor,
        List<PreSubmitCallbackHandler<T>> callbackHandlers
    ) {
        requireNonNull(ccdEventAuthorizor, "ccdEventAuthorizor must not be null");
        requireNonNull(callbackHandlers, "callbackHandlers must not be null");
        this.ccdEventAuthorizor = ccdEventAuthorizor;
        this.sortedCallbackHandlers = callbackHandlers.stream()
            // sorting handlers by handler class name
            .sorted(Comparator.comparing(h -> h.getClass().getSimpleName()))
            .collect(Collectors.toList());
    }

    public PreSubmitCallbackResponse<T> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<T> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        ccdEventAuthorizor.throwIfNotAuthorized(callback.getEvent());

        T caseData =
            callback
                .getCaseDetails()
                .getCaseData();

        log.info("CaseDetails for callback stage `{}` and event `{}`: {}",
            callbackStage,
            callback.getEvent(),
            callback.getCaseDetails());
        log.info("CaseData for callback stage `{}` and event `{}`: {}",
            callbackStage,
            callback.getEvent(),
            caseData);
 
        ExtendedAsylumCase<T> caseData3 = ExtendedAsylumCase.copyToExtended((AsylumCase) caseData);

        @SuppressWarnings("unchecked")
        PreSubmitCallbackResponse2<T> callbackResponse =
            new PreSubmitCallbackResponse2<>((T) caseData3);

        dispatchToHandlers(callbackStage, callback, sortedCallbackHandlers, callbackResponse, DispatchPriority.EARLIEST);
        dispatchToHandlers(callbackStage, callback, sortedCallbackHandlers, callbackResponse, DispatchPriority.EARLY);
        dispatchToHandlers(callbackStage, callback, sortedCallbackHandlers, callbackResponse, DispatchPriority.LATE);
        dispatchToHandlers(callbackStage, callback, sortedCallbackHandlers, callbackResponse, DispatchPriority.LATEST);

        log.info("Callback response after dispatching handlers for stage `{}` and event `{}`: {}",
            callbackStage,
            callback.getEvent(),
            callbackResponse);
    
        return callbackResponse;
    }

    private void dispatchToHandlers(
        PreSubmitCallbackStage callbackStage,
        Callback<T> callback,
        List<PreSubmitCallbackHandler<T>> callbackHandlers,
        PreSubmitCallbackResponse<T> callbackResponse,
        DispatchPriority dispatchPriority
    ) {
        for (PreSubmitCallbackHandler<T> callbackHandler : callbackHandlers) {

            if (callbackHandler.getDispatchPriority() == dispatchPriority) {

                Callback<T> callbackForHandler = new Callback<>(
                    new CaseDetails<>(
                        callback.getCaseDetails().getId(),
                        callback.getCaseDetails().getJurisdiction(),
                        callback.getCaseDetails().getState(),
                        callbackResponse.getData(),
                        callback.getCaseDetails().getCreatedDate()
                    ),
                    callback.getCaseDetailsBefore(),
                    callback.getEvent()
                );

                if (callbackHandler.canHandle(callbackStage, callbackForHandler)) {

                    PreSubmitCallbackResponse<T> callbackResponseFromHandler =
                        callbackHandler.handle(callbackStage, callbackForHandler);

                    callbackResponse.setData(callbackResponseFromHandler.getData());

                    if (!callbackResponseFromHandler.getErrors().isEmpty()) {
                        callbackResponse.addErrors(callbackResponseFromHandler.getErrors());
                    }
                }
            }
        }
    }

    private static class PreSubmitCallbackResponse2<T extends CaseData> extends PreSubmitCallbackResponse<T> {
        
        public PreSubmitCallbackResponse2(T caseData) {
            super(caseData);
        }
    }

    private static class ExtendedAsylumCase<T extends CaseData> extends AsylumCase {
        
        public static <T extends CaseData> ExtendedAsylumCase<T> copyToExtended(AsylumCase original) {
            ExtendedAsylumCase<T> extended = new ExtendedAsylumCase<>();
            // Copy all fields from original AsylumCase except isNabaEnabledOoc
            original.entrySet().stream()
                .filter(entry -> !"isNabaEnabledOoc".equals(entry.getKey()))
                .forEach(entry -> extended.put(entry.getKey(), entry.getValue()));
            //extended.put("stowaway", "defaultPassengerValue");
            return extended;
        }
    }
}

package uk.gov.hmcts.reform.iacasenotificationsapi.domain.handlers.presubmit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.function.BiPredicate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.handlers.ErrorHandler;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.NotificationGenerator;


@ExtendWith(MockitoExtension.class)
public class NotificationHandlerTest {

    @Mock
    Callback<AsylumCase> callback;
    @Mock
    CaseDetails<AsylumCase> caseDetails;
    @Mock
    AsylumCase asylumCase;
    @Mock
    NotificationGenerator notificationGenerator;
    @Mock
    BiPredicate<PreSubmitCallbackStage, Callback<AsylumCase>> canHandle;
    @Mock
    ErrorHandler<AsylumCase> errorHandler;

    private final PreSubmitCallbackStage callbackStage = PreSubmitCallbackStage.ABOUT_TO_SUBMIT;
    private NotificationHandler notificationHandler;

    @BeforeEach
    public void setup() {

        notificationHandler = new NotificationHandler(canHandle, Collections.singletonList(notificationGenerator));
    }

    @Test
    public void should_generate_notification_when_event_can_be_handled() {
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(canHandle.test(callbackStage, callback)).thenReturn(true);
        PreSubmitCallbackResponse<AsylumCase> response = notificationHandler.handle(callbackStage, callback);

        assertEquals(asylumCase, response.getData());
        verify(notificationGenerator).generate(callback);
    }

    @Test
    public void should_not_generate_notification_when_cannot_handle_event() {
        when(canHandle.test(callbackStage, callback)).thenReturn(false);

        IllegalStateException exception =
assertThrows(IllegalStateException.class, () -> notificationHandler.handle(callbackStage, callback))
            ;
assertEquals("Cannot handle callback", exception.getMessage());

        verifyNoInteractions(notificationGenerator);
    }

    @Test
    public void should_return_false_when_cannot_handle_event() {
        when(canHandle.test(callbackStage, callback)).thenReturn(false);

        assertFalse(notificationHandler.canHandle(callbackStage, callback));
    }

    @Test
    public void should_throw_exception_when_callback_stage_is_null() {
        NullPointerException exception =
assertThrows(NullPointerException.class, () -> notificationHandler.canHandle(null, callback))
            ;
assertEquals("callbackStage must not be null", exception.getMessage());
    }

    @Test
    public void should_throw_exception_when_callback_is_null() {
        NullPointerException exception =
assertThrows(NullPointerException.class, () -> notificationHandler.canHandle(callbackStage, null))
            ;
assertEquals("callback must not be null", exception.getMessage());
    }

    @Test
    public void should_catch_exception_and_invoke_error_handler() {
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(canHandle.test(callbackStage, callback)).thenReturn(true);
        String message = "exception happened";
        Throwable exception = new RuntimeException(message);
        doThrow(exception).when(notificationGenerator).generate(callback);
        notificationHandler =
            new NotificationHandler(canHandle, Collections.singletonList(notificationGenerator), errorHandler);

        notificationHandler.handle(callbackStage, callback);

        verify(errorHandler).accept(callback, exception);
    }

    @Test
    public void should_re_throw_exception_from_generator() {

        when(canHandle.test(callbackStage, callback)).thenReturn(true);
        String message = "exception happened";
        doThrow(new RuntimeException(message)).when(notificationGenerator).generate(callback);
        notificationHandler = new NotificationHandler(canHandle, Collections.singletonList(notificationGenerator));

        RuntimeException exception =
assertThrows(RuntimeException.class, () -> notificationHandler.handle(callbackStage, callback))
            ;
assertEquals(message, exception.getMessage());
    }
}

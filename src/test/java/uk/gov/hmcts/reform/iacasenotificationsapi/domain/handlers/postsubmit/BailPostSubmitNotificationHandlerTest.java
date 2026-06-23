package uk.gov.hmcts.reform.iacasenotificationsapi.domain.handlers.postsubmit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Optional;
import java.util.function.BiPredicate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.Message;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.PostSubmitCallbackStage;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.handlers.ErrorHandler;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.BailNotificationGenerator;

@ExtendWith(MockitoExtension.class)
class BailPostSubmitNotificationHandlerTest {

    private final PostSubmitCallbackStage callbackStage = PostSubmitCallbackStage.CCD_SUBMITTED;
    private final Message expectedMessage = new Message("success", "success");
    @Mock
    Callback<BailCase> callback;
    @Mock
    CaseDetails<BailCase> caseDetails;
    @Mock
    BailCase bailCase;
    @Mock
    BailNotificationGenerator bailNotificationGenerator;
    @Mock
    BiPredicate<PostSubmitCallbackStage, Callback<BailCase>> canHandle;
    @Mock
    ErrorHandler<BailCase> errorHandler;
    private BailPostSubmitNotificationHandler notificationHandler;

    @BeforeEach
    void setUp() {
        notificationHandler = new BailPostSubmitNotificationHandler(canHandle, Collections.singletonList(bailNotificationGenerator));
    }

    @Test
    void should_generate_notification_when_event_can_be_handled() {

        when(canHandle.test(callbackStage, callback)).thenReturn(true);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(bailCase);
        when(bailNotificationGenerator.getSuccessMessage()).thenReturn(expectedMessage);
        PostSubmitCallbackResponse response = notificationHandler.handle(callbackStage, callback);

        assertEquals("success", response.getConfirmationHeader().get());
        assertEquals(bailCase.toString(), response.getConfirmationBody().get());
        verify(bailNotificationGenerator).generate(callback);
    }

    @Test
    void should_return_default_confirmation_when_no_custom_message_is_given() {

        when(canHandle.test(callbackStage, callback)).thenReturn(true);
        when(bailNotificationGenerator.getSuccessMessage()).thenReturn(new Message());
        PostSubmitCallbackResponse response = notificationHandler.handle(callbackStage, callback);

        assertEquals("success", response.getConfirmationHeader().get());
        assertEquals("success", response.getConfirmationBody().get());
        assertEquals(Optional.ofNullable("success"), response.getConfirmationHeader());
        assertEquals(Optional.ofNullable("success"), response.getConfirmationBody());
        verify(bailNotificationGenerator).generate(callback);
    }

    @Test
    void should_not_generate_notification_when_cannot_handle_event() {
        when(canHandle.test(callbackStage, callback)).thenReturn(false);

        IllegalStateException exception =
            assertThrows(IllegalStateException.class, () -> notificationHandler.handle(callbackStage, callback));
        assertEquals("Cannot handle callback", exception.getMessage());

        verifyNoInteractions(bailNotificationGenerator);
    }

    @Test
    void should_return_false_when_cannot_handle_event() {
        when(canHandle.test(callbackStage, callback)).thenReturn(false);

        assertFalse(notificationHandler.canHandle(callbackStage, callback));
    }

    @Test
    void should_throw_exception_when_callback_stage_is_null() {
        NullPointerException exception =
            assertThrows(NullPointerException.class, () -> notificationHandler.canHandle(null, callback));
        assertEquals("callbackStage must not be null", exception.getMessage());
    }

    @Test
    void should_throw_exception_when_callback_is_null() {
        NullPointerException exception =
            assertThrows(NullPointerException.class, () -> notificationHandler.canHandle(callbackStage, null));
        assertEquals("callback must not be null", exception.getMessage());
    }

    @Test
    void should_catch_exception_and_invoke_error_handler() {
        when(canHandle.test(callbackStage, callback)).thenReturn(true);
        String message = "exception happened";
        Throwable exception = new RuntimeException(message);
        doThrow(exception).when(bailNotificationGenerator).generate(callback);
        notificationHandler =
            new BailPostSubmitNotificationHandler(canHandle, Collections.singletonList(bailNotificationGenerator), errorHandler);
        notificationHandler.handle(callbackStage, callback);

        verify(errorHandler).accept(callback, exception);
    }

    @Test
    void should_re_throw_exception_from_generator() {

        when(canHandle.test(callbackStage, callback)).thenReturn(true);
        String message = "exception happened";
        doThrow(new RuntimeException(message)).when(bailNotificationGenerator).generate(callback);
        notificationHandler = new BailPostSubmitNotificationHandler(canHandle, Collections.singletonList(bailNotificationGenerator));

        RuntimeException exception =
            assertThrows(RuntimeException.class, () -> notificationHandler.handle(callbackStage, callback));
        assertEquals(message, exception.getMessage());
    }
}

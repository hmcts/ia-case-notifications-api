package uk.gov.hmcts.reform.iacasenotificationsapi.domain.handlers.postsubmit;


import static org.assertj.core.api.Assertions.assertThatThrownBy;
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

    @Mock
    Callback<BailCase> callback;
    @Mock
    CaseDetails<BailCase> caseDetails;
    @Mock
    BailCase bailCase;
    @Mock
    BailNotificationGenerator notificationGenerator;
    @Mock
    BiPredicate<PostSubmitCallbackStage, Callback<BailCase>> canHandle;
    @Mock
    ErrorHandler<BailCase> errorHandler;

    private PostSubmitCallbackStage callbackStage = PostSubmitCallbackStage.CCD_SUBMITTED;
    private BailPostSubmitNotificationHandler notificationHandler;
    private Message expectedMessage = new Message("success", "success");

    @BeforeEach
    void setUp() {
        notificationHandler = new BailPostSubmitNotificationHandler(canHandle, Collections.singletonList(notificationGenerator));
    }

    @Test
    void should_generate_notification_when_event_can_be_handled() {

        when(canHandle.test(callbackStage, callback)).thenReturn(true);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(bailCase);
        when(notificationGenerator.getSuccessMessage()).thenReturn(expectedMessage);
        PostSubmitCallbackResponse response = notificationHandler.handle(callbackStage, callback);

        assertEquals("success", response.getConfirmationHeader().get());
        assertEquals(bailCase.toString(), response.getConfirmationBody().get());
        verify(notificationGenerator).generate(callback);
    }

    @Test
    void should_return_default_confirmation_when_no_custom_message_is_given() {

        when(canHandle.test(callbackStage, callback)).thenReturn(true);
        when(notificationGenerator.getSuccessMessage()).thenReturn(new Message());
        PostSubmitCallbackResponse response = notificationHandler.handle(callbackStage, callback);

        assertEquals("success", response.getConfirmationHeader().get());
        assertEquals("success", response.getConfirmationBody().get());
        assertEquals(Optional.ofNullable("success"), response.getConfirmationHeader());
        assertEquals(Optional.ofNullable("success"), response.getConfirmationBody());
        verify(notificationGenerator).generate(callback);
    }

    @Test
    void should_not_generate_notification_when_cannot_handle_event() {
        when(canHandle.test(callbackStage, callback)).thenReturn(false);

        assertThatThrownBy(() -> notificationHandler.handle(callbackStage, callback))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("Cannot handle callback");

        verifyNoInteractions(notificationGenerator);
    }

    @Test
    void should_return_false_when_cannot_handle_event() {
        when(canHandle.test(callbackStage, callback)).thenReturn(false);

        assertEquals(false, notificationHandler.canHandle(callbackStage, callback));
    }

    @Test
    void should_throw_exception_when_callback_stage_is_null() {
        assertThatThrownBy(() -> notificationHandler.canHandle(null, callback))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("callbackStage must not be null");
    }

    @Test
    void should_throw_exception_when_callback_is_null() {
        assertThatThrownBy(() -> notificationHandler.canHandle(callbackStage, null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("callback must not be null");
    }

    @Test
    void should_catch_exception_and_invoke_error_handler() {
        when(canHandle.test(callbackStage, callback)).thenReturn(true);
        String message = "exception happened";
        Throwable exception = new RuntimeException(message);
        doThrow(exception).when(notificationGenerator).generate(callback);
        notificationHandler =
            new BailPostSubmitNotificationHandler(canHandle, Collections.singletonList(notificationGenerator), errorHandler);

        notificationHandler.handle(callbackStage, callback);

        verify(errorHandler).accept(callback, exception);
    }

    @Test
    void should_re_throw_exception_from_generator() {

        when(canHandle.test(callbackStage, callback)).thenReturn(true);
        String message = "exception happened";
        doThrow(new RuntimeException(message)).when(notificationGenerator).generate(callback);
        notificationHandler = new BailPostSubmitNotificationHandler(canHandle, Collections.singletonList(notificationGenerator));

        assertThatThrownBy(() -> notificationHandler.handle(callbackStage, callback))
            .isExactlyInstanceOf(RuntimeException.class)
            .hasMessage(message);
    }
}

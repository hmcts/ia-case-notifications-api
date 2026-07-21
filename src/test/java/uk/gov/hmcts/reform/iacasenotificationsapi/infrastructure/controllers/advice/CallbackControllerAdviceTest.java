package uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.controllers.advice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.RequiredFieldMissingException;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.UnrecoverableException;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.clients.NotificationServiceResponseException;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.controllers.advice.model.ErrorResponse;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.security.idam.IdentityManagerResponseException;

@ExtendWith(MockitoExtension.class)
class CallbackControllerAdviceTest {

    @Mock
    private ErrorResponseBuilder errorResponseBuilder;

    @Mock
    private HttpServletRequest request;

    @Mock
    private BindingResult bindingResult;

    @Mock
    private HttpInputMessage httpInputMessage;

    private MethodParameter methodParameter;

    private CallbackControllerAdvice callbackControllerAdvice;

    @BeforeEach
    void setUp() throws NoSuchMethodException {
        callbackControllerAdvice = new CallbackControllerAdvice(errorResponseBuilder);
        Method method = getClass().getDeclaredMethod("dummyMethod", String.class);
        methodParameter = new MethodParameter(method, 0);
    }

    @SuppressWarnings("unused")
    private void dummyMethod(String param) {
        // dummy method for creating MethodParameter
    }

    @Test
    void should_handle_required_field_missing_exception() {
        RequiredFieldMissingException exception = new RequiredFieldMissingException("Field is missing");
        ErrorResponse expectedResponse = buildErrorResponse(ErrorCode.REQUIRED_FIELD_MISSING);

        when(errorResponseBuilder.build(eq(ErrorCode.REQUIRED_FIELD_MISSING), eq(request), eq("Field is missing")))
            .thenReturn(expectedResponse);

        ResponseEntity<ErrorResponse> response = callbackControllerAdvice
            .handleRequiredFieldMissingException(request, exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(ErrorCode.REQUIRED_FIELD_MISSING.getCode(), response.getBody().getErrorCode());
        verify(errorResponseBuilder).logError(eq(exception), eq(ErrorCode.REQUIRED_FIELD_MISSING), eq(request));
    }

    @Test
    void should_handle_illegal_state_exception() {
        IllegalStateException exception = new IllegalStateException("Invalid state");
        ErrorResponse expectedResponse = buildErrorResponse(ErrorCode.BAD_REQUEST);

        when(errorResponseBuilder.build(eq(ErrorCode.BAD_REQUEST), eq(request), eq("Invalid state")))
            .thenReturn(expectedResponse);

        ResponseEntity<ErrorResponse> response = callbackControllerAdvice
            .handleIllegalArgumentException(request, exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(ErrorCode.BAD_REQUEST.getCode(), response.getBody().getErrorCode());
        verify(errorResponseBuilder).logError(eq(exception), eq(ErrorCode.BAD_REQUEST), eq(request));
    }

    @Test
    void should_handle_illegal_argument_exception() {
        IllegalArgumentException exception = new IllegalArgumentException("Invalid argument");
        ErrorResponse expectedResponse = buildErrorResponse(ErrorCode.BAD_REQUEST);

        when(errorResponseBuilder.build(eq(ErrorCode.BAD_REQUEST), eq(request), eq("Invalid argument")))
            .thenReturn(expectedResponse);

        ResponseEntity<ErrorResponse> response = callbackControllerAdvice
            .handleIllegalArgumentException(request, exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(errorResponseBuilder).logError(eq(exception), eq(ErrorCode.BAD_REQUEST), eq(request));
    }

    @Test
    void should_handle_method_argument_not_valid_exception() {
        FieldError fieldError = new FieldError("object", "fieldName", "must not be null");
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(methodParameter, bindingResult);
        ErrorResponse expectedResponse = buildErrorResponse(ErrorCode.VALIDATION_ERROR);

        when(errorResponseBuilder.buildWithFieldErrors(eq(ErrorCode.VALIDATION_ERROR), eq(request), any()))
            .thenReturn(expectedResponse);

        ResponseEntity<ErrorResponse> response = callbackControllerAdvice
            .handleMethodArgumentNotValidException(request, exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(ErrorCode.VALIDATION_ERROR.getCode(), response.getBody().getErrorCode());
        verify(errorResponseBuilder).logError(eq(exception), eq(ErrorCode.VALIDATION_ERROR), eq(request));
    }

    @Test
    void should_handle_http_message_not_readable_exception() {
        HttpMessageNotReadableException exception =
            new HttpMessageNotReadableException("Malformed JSON", httpInputMessage);
        ErrorResponse expectedResponse = buildErrorResponse(ErrorCode.MALFORMED_REQUEST);

        when(errorResponseBuilder.build(eq(ErrorCode.MALFORMED_REQUEST), eq(request), eq(null)))
            .thenReturn(expectedResponse);

        ResponseEntity<ErrorResponse> response = callbackControllerAdvice
            .handleHttpMessageNotReadableException(request, exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(ErrorCode.MALFORMED_REQUEST.getCode(), response.getBody().getErrorCode());
        verify(errorResponseBuilder).logError(eq(exception), eq(ErrorCode.MALFORMED_REQUEST), eq(request));
    }

    @Test
    void should_handle_authentication_exception() {
        BadCredentialsException exception = new BadCredentialsException("Invalid credentials");
        ErrorResponse expectedResponse = buildErrorResponse(ErrorCode.UNAUTHORIZED);

        when(errorResponseBuilder.build(eq(ErrorCode.UNAUTHORIZED), eq(request), eq(null)))
            .thenReturn(expectedResponse);

        ResponseEntity<ErrorResponse> response = callbackControllerAdvice
            .handleAuthenticationException(request, exception);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(ErrorCode.UNAUTHORIZED.getCode(), response.getBody().getErrorCode());
        verify(errorResponseBuilder).logError(eq(exception), eq(ErrorCode.UNAUTHORIZED), eq(request));
    }

    @Test
    void should_handle_access_denied_exception() {
        AccessDeniedException exception = new AccessDeniedException("Access denied");
        ErrorResponse expectedResponse = buildErrorResponse(ErrorCode.ACCESS_DENIED);

        when(errorResponseBuilder.build(eq(ErrorCode.ACCESS_DENIED), eq(request), eq(null)))
            .thenReturn(expectedResponse);

        ResponseEntity<ErrorResponse> response = callbackControllerAdvice
            .handleAccessDeniedException(request, exception);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(ErrorCode.ACCESS_DENIED.getCode(), response.getBody().getErrorCode());
        verify(errorResponseBuilder).logError(eq(exception), eq(ErrorCode.ACCESS_DENIED), eq(request));
    }

    @Test
    void should_handle_no_resource_found_exception() {
        NoResourceFoundException exception = new NoResourceFoundException(null, "/unknown");
        ErrorResponse expectedResponse = buildErrorResponse(ErrorCode.NOT_FOUND);

        when(errorResponseBuilder.build(eq(ErrorCode.NOT_FOUND), eq(request), eq(null)))
            .thenReturn(expectedResponse);

        ResponseEntity<ErrorResponse> response = callbackControllerAdvice
            .handleNoResourceFoundException(request, exception);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(ErrorCode.NOT_FOUND.getCode(), response.getBody().getErrorCode());
        verify(errorResponseBuilder).logError(eq(exception), eq(ErrorCode.NOT_FOUND), eq(request));
    }

    @Test
    void should_handle_http_request_method_not_supported_exception() {
        HttpRequestMethodNotSupportedException exception = new HttpRequestMethodNotSupportedException("DELETE");
        ErrorResponse expectedResponse = buildErrorResponse(ErrorCode.METHOD_NOT_ALLOWED);

        when(errorResponseBuilder.build(eq(ErrorCode.METHOD_NOT_ALLOWED), eq(request), eq(null)))
            .thenReturn(expectedResponse);

        ResponseEntity<ErrorResponse> response = callbackControllerAdvice
            .handleHttpRequestMethodNotSupportedException(request, exception);

        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(ErrorCode.METHOD_NOT_ALLOWED.getCode(), response.getBody().getErrorCode());
        verify(errorResponseBuilder).logError(eq(exception), eq(ErrorCode.METHOD_NOT_ALLOWED), eq(request));
    }

    @Test
    void should_handle_notification_service_response_exception() {
        NotificationServiceResponseException exception =
            new NotificationServiceResponseException("Service error", new RuntimeException());
        ErrorResponse expectedResponse = buildErrorResponse(ErrorCode.NOTIFICATION_SERVICE_ERROR);

        when(errorResponseBuilder.build(eq(ErrorCode.NOTIFICATION_SERVICE_ERROR), eq(request), eq(null)))
            .thenReturn(expectedResponse);

        ResponseEntity<ErrorResponse> response = callbackControllerAdvice
            .handleNotificationServiceException(request, exception);

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(ErrorCode.NOTIFICATION_SERVICE_ERROR.getCode(), response.getBody().getErrorCode());
        verify(errorResponseBuilder).logError(eq(exception), eq(ErrorCode.NOTIFICATION_SERVICE_ERROR), eq(request));
    }

    @Test
    void should_handle_identity_manager_response_exception() {
        IdentityManagerResponseException exception =
            new IdentityManagerResponseException("Identity error", new RuntimeException());
        ErrorResponse expectedResponse = buildErrorResponse(ErrorCode.IDENTITY_SERVICE_ERROR);

        when(errorResponseBuilder.build(eq(ErrorCode.IDENTITY_SERVICE_ERROR), eq(request), eq(null)))
            .thenReturn(expectedResponse);

        ResponseEntity<ErrorResponse> response = callbackControllerAdvice
            .handleIdentityManagerException(request, exception);

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(ErrorCode.IDENTITY_SERVICE_ERROR.getCode(), response.getBody().getErrorCode());
        verify(errorResponseBuilder).logError(eq(exception), eq(ErrorCode.IDENTITY_SERVICE_ERROR), eq(request));
    }

    @Test
    void should_handle_unrecoverable_exception() {
        UnrecoverableException exception = new UnrecoverableException("Unrecoverable", new RuntimeException());
        ErrorResponse expectedResponse = buildErrorResponse(ErrorCode.INTERNAL_ERROR);

        when(errorResponseBuilder.build(eq(ErrorCode.INTERNAL_ERROR), eq(request), eq(null)))
            .thenReturn(expectedResponse);

        ResponseEntity<ErrorResponse> response = callbackControllerAdvice
            .handleUnrecoverableException(request, exception);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(ErrorCode.INTERNAL_ERROR.getCode(), response.getBody().getErrorCode());
        verify(errorResponseBuilder).logError(eq(exception), eq(ErrorCode.INTERNAL_ERROR), eq(request));
    }

    @Test
    void should_handle_generic_exception() {
        Exception exception = new Exception("Unexpected error");
        ErrorResponse expectedResponse = buildErrorResponse(ErrorCode.INTERNAL_ERROR);

        when(errorResponseBuilder.build(eq(ErrorCode.INTERNAL_ERROR), eq(request), eq(null)))
            .thenReturn(expectedResponse);

        ResponseEntity<ErrorResponse> response = callbackControllerAdvice
            .handleGenericException(request, exception);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(ErrorCode.INTERNAL_ERROR.getCode(), response.getBody().getErrorCode());
        verify(errorResponseBuilder).logError(eq(exception), eq(ErrorCode.INTERNAL_ERROR), eq(request));
    }

    private ErrorResponse buildErrorResponse(ErrorCode errorCode) {
        return ErrorResponse.builder()
            .errorCode(errorCode.getCode())
            .message(errorCode.getDefaultMessage())
            .build();
    }
}

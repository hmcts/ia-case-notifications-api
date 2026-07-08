package uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.controllers.advice;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.RequiredFieldMissingException;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.UnrecoverableException;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.clients.NotificationServiceResponseException;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.controllers.advice.model.ErrorResponse;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.security.idam.IdentityManagerResponseException;

@Slf4j
@ControllerAdvice(basePackages = "uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.controllers")
@RequestMapping(produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class CallbackControllerAdvice {

    private final ErrorResponseBuilder errorResponseBuilder;

    @ExceptionHandler(RequiredFieldMissingException.class)
    protected ResponseEntity<ErrorResponse> handleRequiredFieldMissingException(
            HttpServletRequest request,
            RequiredFieldMissingException ex
    ) {
        errorResponseBuilder.logError(ex, ErrorCode.REQUIRED_FIELD_MISSING, request);
        ErrorResponse response = errorResponseBuilder.build(
            ErrorCode.REQUIRED_FIELD_MISSING, request, ex.getMessage());
        return new ResponseEntity<>(response, ErrorCode.REQUIRED_FIELD_MISSING.getHttpStatus());
    }

    @ExceptionHandler({IllegalStateException.class, IllegalArgumentException.class})
    protected ResponseEntity<ErrorResponse> handleIllegalArgumentException(
            HttpServletRequest request,
            RuntimeException ex
    ) {
        errorResponseBuilder.logError(ex, ErrorCode.BAD_REQUEST, request);
        ErrorResponse response = errorResponseBuilder.build(
            ErrorCode.BAD_REQUEST, request, ex.getMessage());
        return new ResponseEntity<>(response, ErrorCode.BAD_REQUEST.getHttpStatus());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(
            HttpServletRequest request,
            MethodArgumentNotValidException ex
    ) {
        List<ErrorResponse.FieldError> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> ErrorResponse.FieldError.builder()
                        .field(error.getField())
                        .message(error.getDefaultMessage())
                        .build())
                .toList();
        errorResponseBuilder.logError(ex, ErrorCode.VALIDATION_ERROR, request);
        ErrorResponse response = errorResponseBuilder.buildWithFieldErrors(
            ErrorCode.VALIDATION_ERROR, request, fieldErrors);
        return new ResponseEntity<>(response, ErrorCode.VALIDATION_ERROR.getHttpStatus());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    protected ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(
            HttpServletRequest request,
            HttpMessageNotReadableException ex
    ) {
        errorResponseBuilder.logError(ex, ErrorCode.MALFORMED_REQUEST, request);
        ErrorResponse response = errorResponseBuilder.build(
            ErrorCode.MALFORMED_REQUEST, request, null);
        return new ResponseEntity<>(response, ErrorCode.MALFORMED_REQUEST.getHttpStatus());
    }

    @ExceptionHandler(AuthenticationException.class)
    protected ResponseEntity<ErrorResponse> handleAuthenticationException(
            HttpServletRequest request,
            AuthenticationException ex
    ) {
        errorResponseBuilder.logError(ex, ErrorCode.UNAUTHORIZED, request);
        ErrorResponse response = errorResponseBuilder.build(
            ErrorCode.UNAUTHORIZED, request, null);
        return new ResponseEntity<>(response, ErrorCode.UNAUTHORIZED.getHttpStatus());
    }

    @ExceptionHandler(AccessDeniedException.class)
    protected ResponseEntity<ErrorResponse> handleAccessDeniedException(
            HttpServletRequest request,
            AccessDeniedException ex
    ) {
        errorResponseBuilder.logError(ex, ErrorCode.ACCESS_DENIED, request);
        ErrorResponse response = errorResponseBuilder.build(
            ErrorCode.ACCESS_DENIED, request, null);
        return new ResponseEntity<>(response, ErrorCode.ACCESS_DENIED.getHttpStatus());
    }

    @ExceptionHandler(NoResourceFoundException.class)
    protected ResponseEntity<ErrorResponse> handleNoResourceFoundException(
            HttpServletRequest request,
            NoResourceFoundException ex
    ) {
        errorResponseBuilder.logError(ex, ErrorCode.NOT_FOUND, request);
        ErrorResponse response = errorResponseBuilder.build(
            ErrorCode.NOT_FOUND, request, null);
        return new ResponseEntity<>(response, ErrorCode.NOT_FOUND.getHttpStatus());
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    protected ResponseEntity<ErrorResponse> handleHttpRequestMethodNotSupportedException(
            HttpServletRequest request,
            HttpRequestMethodNotSupportedException ex
    ) {
        errorResponseBuilder.logError(ex, ErrorCode.METHOD_NOT_ALLOWED, request);
        ErrorResponse response = errorResponseBuilder.build(
            ErrorCode.METHOD_NOT_ALLOWED, request, null);
        return new ResponseEntity<>(response, ErrorCode.METHOD_NOT_ALLOWED.getHttpStatus());
    }

    @ExceptionHandler(NotificationServiceResponseException.class)
    protected ResponseEntity<ErrorResponse> handleNotificationServiceException(
            HttpServletRequest request,
            NotificationServiceResponseException ex
    ) {
        errorResponseBuilder.logError(ex, ErrorCode.NOTIFICATION_SERVICE_ERROR, request);
        ErrorResponse response = errorResponseBuilder.build(
            ErrorCode.NOTIFICATION_SERVICE_ERROR, request, null);
        return new ResponseEntity<>(response, ErrorCode.NOTIFICATION_SERVICE_ERROR.getHttpStatus());
    }

    @ExceptionHandler(IdentityManagerResponseException.class)
    protected ResponseEntity<ErrorResponse> handleIdentityManagerException(
            HttpServletRequest request,
            IdentityManagerResponseException ex
    ) {
        errorResponseBuilder.logError(ex, ErrorCode.IDENTITY_SERVICE_ERROR, request);
        ErrorResponse response = errorResponseBuilder.build(
            ErrorCode.IDENTITY_SERVICE_ERROR, request, null);
        return new ResponseEntity<>(response, ErrorCode.IDENTITY_SERVICE_ERROR.getHttpStatus());
    }

    @ExceptionHandler(UnrecoverableException.class)
    protected ResponseEntity<ErrorResponse> handleUnrecoverableException(
            HttpServletRequest request,
            UnrecoverableException ex
    ) {
        errorResponseBuilder.logError(ex, ErrorCode.INTERNAL_ERROR, request);
        ErrorResponse response = errorResponseBuilder.build(
            ErrorCode.INTERNAL_ERROR, request, null);
        return new ResponseEntity<>(response, ErrorCode.INTERNAL_ERROR.getHttpStatus());
    }

    @ExceptionHandler(Exception.class)
    protected ResponseEntity<ErrorResponse> handleGenericException(
            HttpServletRequest request,
            Exception ex
    ) {
        errorResponseBuilder.logError(ex, ErrorCode.INTERNAL_ERROR, request);
        ErrorResponse response = errorResponseBuilder.build(
            ErrorCode.INTERNAL_ERROR, request, null);
        return new ResponseEntity<>(response, ErrorCode.INTERNAL_ERROR.getHttpStatus());
    }
}

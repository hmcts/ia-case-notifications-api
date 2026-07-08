package uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.controllers.advice;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // Client errors (4xx)
    BAD_REQUEST("BAD_REQUEST", HttpStatus.BAD_REQUEST, "Invalid request"),
    VALIDATION_ERROR("VALIDATION_ERROR", HttpStatus.BAD_REQUEST, "Validation failed"),
    REQUIRED_FIELD_MISSING("REQUIRED_FIELD_MISSING", HttpStatus.BAD_REQUEST, "Required field is missing"),
    MALFORMED_REQUEST("MALFORMED_REQUEST", HttpStatus.BAD_REQUEST, "Malformed request body"),
    UNAUTHORIZED("UNAUTHORIZED", HttpStatus.UNAUTHORIZED, "Authentication required"),
    ACCESS_DENIED("ACCESS_DENIED", HttpStatus.FORBIDDEN, "Access denied"),
    NOT_FOUND("NOT_FOUND", HttpStatus.NOT_FOUND, "Resource not found"),
    METHOD_NOT_ALLOWED("METHOD_NOT_ALLOWED", HttpStatus.METHOD_NOT_ALLOWED, "Method not allowed"),

    // Server errors (5xx)
    INTERNAL_ERROR("INTERNAL_ERROR", HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred"),
    NOTIFICATION_SERVICE_ERROR("NOTIFICATION_SERVICE_ERROR", HttpStatus.SERVICE_UNAVAILABLE, "Notification service unavailable"),
    IDENTITY_SERVICE_ERROR("IDENTITY_SERVICE_ERROR", HttpStatus.SERVICE_UNAVAILABLE, "Identity service unavailable"),
    SERVICE_UNAVAILABLE("SERVICE_UNAVAILABLE", HttpStatus.SERVICE_UNAVAILABLE, "Service temporarily unavailable");

    private final String code;
    private final HttpStatus httpStatus;
    private final String defaultMessage;

}

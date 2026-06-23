package uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.controllers.advice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.controllers.advice.model.ErrorResponse;

@ExtendWith(MockitoExtension.class)
class ErrorResponseBuilderTest {

    private static final String TEST_CORRELATION_ID = "test-correlation-id";
    private static final String TEST_REQUEST_URI = "/asylum/ccdAboutToSubmit";
    private static final String TEST_CCD_CASE_ID = "1234567890";

    @Mock
    private HttpServletRequest request;

    @Mock
    private RequestAttributes requestAttributes;

    private ErrorResponseBuilder errorResponseBuilder;

    @BeforeEach
    void setUp() {
        errorResponseBuilder = new ErrorResponseBuilder();
        MDC.put(CorrelationIdFilter.CORRELATION_ID_MDC_KEY, TEST_CORRELATION_ID);
        when(request.getRequestURI()).thenReturn(TEST_REQUEST_URI);
    }

    @AfterEach
    void tearDown() {
        MDC.clear();
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void should_build_error_response_with_custom_message() {
        String customMessage = "Custom error message";

        ErrorResponse response = errorResponseBuilder.build(ErrorCode.BAD_REQUEST, request, customMessage);

        assertNotNull(response);
        assertEquals(ErrorCode.BAD_REQUEST.getCode(), response.getErrorCode());
        assertEquals(customMessage, response.getMessage());
        assertEquals(TEST_CORRELATION_ID, response.getRequestId());
        assertEquals(TEST_REQUEST_URI, response.getPath());
        assertNotNull(response.getTimestamp());
        assertNull(response.getFieldErrors());
    }

    @Test
    void should_build_error_response_with_default_message_when_custom_message_is_null() {
        ErrorResponse response = errorResponseBuilder.build(ErrorCode.INTERNAL_ERROR, request, null);

        assertNotNull(response);
        assertEquals(ErrorCode.INTERNAL_ERROR.getCode(), response.getErrorCode());
        assertEquals(ErrorCode.INTERNAL_ERROR.getDefaultMessage(), response.getMessage());
        assertEquals(TEST_CORRELATION_ID, response.getRequestId());
        assertEquals(TEST_REQUEST_URI, response.getPath());
    }

    @Test
    void should_build_error_response_with_field_errors() {
        List<ErrorResponse.FieldError> fieldErrors = List.of(
            ErrorResponse.FieldError.builder().field("field1").message("must not be null").build(),
            ErrorResponse.FieldError.builder().field("field2").message("must be positive").build()
        );

        ErrorResponse response = errorResponseBuilder.buildWithFieldErrors(
            ErrorCode.VALIDATION_ERROR, request, fieldErrors);

        assertNotNull(response);
        assertEquals(ErrorCode.VALIDATION_ERROR.getCode(), response.getErrorCode());
        assertEquals(ErrorCode.VALIDATION_ERROR.getDefaultMessage(), response.getMessage());
        assertEquals(TEST_CORRELATION_ID, response.getRequestId());
        assertEquals(TEST_REQUEST_URI, response.getPath());
        assertNotNull(response.getFieldErrors());
        assertEquals(2, response.getFieldErrors().size());
        assertEquals("field1", response.getFieldErrors().get(0).getField());
        assertEquals("must not be null", response.getFieldErrors().get(0).getMessage());
    }

    @Test
    void should_log_error_with_ccd_case_id_from_mdc() {
        MDC.put(CorrelationIdFilter.CCD_CASE_ID_MDC_KEY, TEST_CCD_CASE_ID);
        Exception exception = new RuntimeException("Test error");

        // This should not throw - verifying logging works with MDC
        errorResponseBuilder.logError(exception, ErrorCode.INTERNAL_ERROR, request);
    }

    @Test
    void should_log_error_with_ccd_case_id_from_request_attributes_when_not_in_mdc() {
        RequestContextHolder.setRequestAttributes(requestAttributes);
        when(requestAttributes.getAttribute("CCDCaseId", RequestAttributes.SCOPE_REQUEST))
            .thenReturn(TEST_CCD_CASE_ID);
        Exception exception = new RuntimeException("Test error");

        // This should not throw - verifying logging works with request attributes fallback
        errorResponseBuilder.logError(exception, ErrorCode.INTERNAL_ERROR, request);
    }

    @Test
    void should_log_error_with_unknown_ccd_case_id_when_not_available() {
        RequestContextHolder.setRequestAttributes(requestAttributes);
        when(requestAttributes.getAttribute("CCDCaseId", RequestAttributes.SCOPE_REQUEST))
            .thenReturn(null);
        Exception exception = new RuntimeException("Test error");

        // This should not throw - verifying logging works with null CCDCaseId
        errorResponseBuilder.logError(exception, ErrorCode.INTERNAL_ERROR, request);
    }

    @Test
    void should_log_error_with_unknown_ccd_case_id_when_no_request_attributes() {
        RequestContextHolder.resetRequestAttributes();
        Exception exception = new RuntimeException("Test error");

        // This should not throw - verifying logging works without request attributes
        errorResponseBuilder.logError(exception, ErrorCode.INTERNAL_ERROR, request);
    }

    @Test
    void should_log_error_with_abbreviated_stack_trace() {
        MDC.put(CorrelationIdFilter.CCD_CASE_ID_MDC_KEY, TEST_CCD_CASE_ID);
        Exception rootCause = new IllegalArgumentException("Root cause");
        Exception exception = new RuntimeException("Wrapper exception", rootCause);

        // This should not throw - verifying logging works with nested exception
        errorResponseBuilder.logError(exception, ErrorCode.INTERNAL_ERROR, request);
    }
}

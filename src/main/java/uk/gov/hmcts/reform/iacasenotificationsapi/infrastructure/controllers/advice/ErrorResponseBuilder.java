package uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.controllers.advice;

import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.controllers.advice.model.ErrorResponse;

@Slf4j
@Service
public class ErrorResponseBuilder {

    private static final String UNKNOWN = "unknown";
    private static final int STACK_TRACE_INITIAL_LINES = 5;

    public ErrorResponse build(ErrorCode errorCode, HttpServletRequest request, String customMessage) {
        return ErrorResponse.builder()
                .errorCode(errorCode.getCode())
                .message(customMessage != null ? customMessage : errorCode.getDefaultMessage())
                .timestamp(Instant.now())
                .requestId(MDC.get(CorrelationIdFilter.CORRELATION_ID_MDC_KEY))
                .path(request.getRequestURI())
                .build();
    }

    public ErrorResponse buildWithFieldErrors(ErrorCode errorCode,
                                              HttpServletRequest request,
                                              List<ErrorResponse.FieldError> fieldErrors) {
        return ErrorResponse.builder()
                .errorCode(errorCode.getCode())
                .message(errorCode.getDefaultMessage())
                .timestamp(Instant.now())
                .requestId(MDC.get(CorrelationIdFilter.CORRELATION_ID_MDC_KEY))
                .path(request.getRequestURI())
                .fieldErrors(fieldErrors)
                .build();
    }

    public void logError(Exception ex, ErrorCode errorCode, HttpServletRequest request) {
        String ccdCaseId = getCcdCaseId();
        String abbreviatedStackTrace = getAbbreviatedStackTrace(ex);
        log.error("Error [{}] for CCDCaseId: {}, path: {}, correlationId: {}\n{}",
                errorCode.getCode(),
                ccdCaseId,
                request.getRequestURI(),
                MDC.get(CorrelationIdFilter.CORRELATION_ID_MDC_KEY),
                abbreviatedStackTrace);
    }

    private String getCcdCaseId() {
        String ccdCaseId = MDC.get(CorrelationIdFilter.CCD_CASE_ID_MDC_KEY);
        if (ccdCaseId != null) {
            return ccdCaseId;
        }
        RequestAttributes attrs = RequestContextHolder.getRequestAttributes();
        if (attrs != null) {
            Object id = attrs.getAttribute("CCDCaseId", RequestAttributes.SCOPE_REQUEST);
            return id != null ? id.toString() : UNKNOWN;
        }
        return UNKNOWN;
    }

    private String getAbbreviatedStackTrace(Exception ex) {
        String[] trace = ExceptionUtils.getRootCauseStackTrace(ex);
        StringBuilder sb = new StringBuilder();
        String lastLine = "";
        String continuationLine = "        ...";
        for (int i = 0; i < trace.length; i++) {
            if (i < STACK_TRACE_INITIAL_LINES || trace[i].contains("uk.gov.hmcts.reform")) {
                lastLine = trace[i];
                sb.append(lastLine).append("\n");
            } else if (!lastLine.equals(continuationLine)) {
                lastLine = continuationLine;
                sb.append(lastLine).append("\n");
            }
        }
        return sb.toString();
    }
}

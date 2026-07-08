package uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.controllers.advice;

import org.slf4j.MDC;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdviceAdapter;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;

import java.lang.reflect.Type;

@ControllerAdvice
public class CallbackRequestAdapter extends RequestBodyAdviceAdapter {

    @Override
    public boolean supports(MethodParameter methodParameter, Type targetType,
                            Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    public Object afterBodyRead(Object body, HttpInputMessage inputMessage, MethodParameter parameter,
                                Type targetType, Class<? extends HttpMessageConverter<?>> converterType) {

        if (body instanceof Callback<?> callback) {
            String caseId = String.valueOf(callback.getCaseDetails().getId());

            RequestContextHolder.currentRequestAttributes()
                    .setAttribute("CCDCaseId", caseId, RequestAttributes.SCOPE_REQUEST);

            // Set in MDC for logging pattern
            MDC.put(CorrelationIdFilter.CCD_CASE_ID_MDC_KEY, caseId);
        }

        return body;
    }
}

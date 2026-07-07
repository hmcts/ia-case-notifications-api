package uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.controllers.advice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

import java.lang.reflect.Type;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;
import org.springframework.core.MethodParameter;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;

@ExtendWith(MockitoExtension.class)
class CallbackRequestAdapterTest {

    private static final Long TEST_CASE_ID = 1234567890L;

    @Mock
    private MethodParameter methodParameter;

    @Mock
    private Type type;

    @Mock
    private Callback<AsylumCase> callback;

    @Mock
    private CaseDetails<AsylumCase> caseDetails;

    private CallbackRequestAdapter callbackRequestAdapter;

    @BeforeEach
    void setUp() {
        callbackRequestAdapter = new CallbackRequestAdapter();
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    @AfterEach
    void tearDown() {
        MDC.clear();
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void should_return_true_for_supports() {
        assertEquals(true, callbackRequestAdapter.supports(methodParameter, type, null));
    }

    @Test
    void should_extract_ccd_case_id_and_set_in_request_attributes() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getId()).thenReturn(TEST_CASE_ID);

        Object result = callbackRequestAdapter.afterBodyRead(callback, null, null, null, null);

        assertEquals(callback, result);

        RequestAttributes attrs = RequestContextHolder.currentRequestAttributes();
        assertEquals(String.valueOf(TEST_CASE_ID), attrs.getAttribute("CCDCaseId", RequestAttributes.SCOPE_REQUEST));
    }

    @Test
    void should_set_ccd_case_id_in_mdc() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getId()).thenReturn(TEST_CASE_ID);

        assertNull(MDC.get(CorrelationIdFilter.CCD_CASE_ID_MDC_KEY));

        callbackRequestAdapter.afterBodyRead(callback, null, null, null, null);

        assertEquals(String.valueOf(TEST_CASE_ID), MDC.get(CorrelationIdFilter.CCD_CASE_ID_MDC_KEY));
    }
}

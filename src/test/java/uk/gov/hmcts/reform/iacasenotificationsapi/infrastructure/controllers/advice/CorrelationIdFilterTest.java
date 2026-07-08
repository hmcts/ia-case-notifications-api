package uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.controllers.advice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import java.io.IOException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

@ExtendWith(MockitoExtension.class)
class CorrelationIdFilterTest {

    private static final String TEST_CORRELATION_ID = "test-correlation-id-123";

    @Mock
    private FilterChain filterChain;

    private CorrelationIdFilter correlationIdFilter;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        correlationIdFilter = new CorrelationIdFilter();
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
    }

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    @Test
    void should_use_correlation_id_from_request_header() throws ServletException, IOException {
        request.addHeader(CorrelationIdFilter.CORRELATION_ID_HEADER, TEST_CORRELATION_ID);

        correlationIdFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertEquals(TEST_CORRELATION_ID, response.getHeader(CorrelationIdFilter.CORRELATION_ID_HEADER));
    }

    @Test
    void should_generate_correlation_id_when_not_provided() throws ServletException, IOException {
        correlationIdFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        String generatedCorrelationId = response.getHeader(CorrelationIdFilter.CORRELATION_ID_HEADER);
        assertNotNull(generatedCorrelationId);
    }

    @Test
    void should_generate_correlation_id_when_header_is_blank() throws ServletException, IOException {
        request.addHeader(CorrelationIdFilter.CORRELATION_ID_HEADER, "   ");

        correlationIdFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        String generatedCorrelationId = response.getHeader(CorrelationIdFilter.CORRELATION_ID_HEADER);
        assertNotNull(generatedCorrelationId);
    }

    @Test
    void should_clear_mdc_after_filter_chain_completes() throws ServletException, IOException {
        request.addHeader(CorrelationIdFilter.CORRELATION_ID_HEADER, TEST_CORRELATION_ID);
        MDC.put(CorrelationIdFilter.CCD_CASE_ID_MDC_KEY, "12345");

        correlationIdFilter.doFilterInternal(request, response, filterChain);

        assertNull(MDC.get(CorrelationIdFilter.CORRELATION_ID_MDC_KEY));
        assertNull(MDC.get(CorrelationIdFilter.CCD_CASE_ID_MDC_KEY));
    }

    @Test
    void should_clear_mdc_even_when_filter_chain_throws_exception() throws ServletException, IOException {
        request.addHeader(CorrelationIdFilter.CORRELATION_ID_HEADER, TEST_CORRELATION_ID);

        org.mockito.Mockito.doThrow(new ServletException("Test exception"))
            .when(filterChain).doFilter(request, response);

        try {
            correlationIdFilter.doFilterInternal(request, response, filterChain);
        } catch (ServletException e) {
            // Expected exception
        }

        assertNull(MDC.get(CorrelationIdFilter.CORRELATION_ID_MDC_KEY));
        assertNull(MDC.get(CorrelationIdFilter.CCD_CASE_ID_MDC_KEY));
    }
}

package com.amtpilot.common.web;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import jakarta.servlet.ServletException;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TraceIdFilterTest {

    private final TraceIdFilter filter = new TraceIdFilter();

    @Test
    void addsTheSameTraceIdToRequestResponseAndMdc() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicReference<String> traceIdSeenByApplication = new AtomicReference<>();

        filter.doFilter(request, response, (servletRequest, servletResponse) -> {
            String traceId = (String) servletRequest.getAttribute(TraceIdFilter.TRACE_ID_ATTRIBUTE);
            traceIdSeenByApplication.set(traceId);

            assertThat(MDC.get(TraceIdFilter.MDC_KEY)).isEqualTo(traceId);
        });

        String traceId = traceIdSeenByApplication.get();
        assertThat(traceId).isNotBlank();
        assertThat(response.getHeader(TraceIdFilter.TRACE_ID_HEADER)).isEqualTo(traceId);
        assertThat(UUID.fromString(traceId).toString()).isEqualTo(traceId);
        assertThat(MDC.get(TraceIdFilter.MDC_KEY)).isNull();
    }

    @Test
    void clearsMdcWhenRequestProcessingFails() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertThatThrownBy(() -> filter.doFilter(request, response, (servletRequest, servletResponse) -> {
            throw new ServletException("Test failure");
        })).isInstanceOf(ServletException.class);

        assertThat(MDC.get(TraceIdFilter.MDC_KEY)).isNull();
    }
}

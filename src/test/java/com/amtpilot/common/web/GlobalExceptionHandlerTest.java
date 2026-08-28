package com.amtpilot.common.web;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new TestController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .addFilters(new TraceIdFilter())
                .build();
    }

    @Test
    void returnsMalformedJsonError() throws Exception {
        MvcResult result = mockMvc.perform(post("/test/validation")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":"))
                .andExpect(status().isBadRequest())
                .andExpect(header().exists(TraceIdFilter.TRACE_ID_HEADER))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("MALFORMED_JSON"))
                .andExpect(jsonPath("$.error.message")
                        .value("Request body contains invalid JSON"))
                .andReturn();

        assertTraceIdMatchesResponseHeader(result);
    }

    @Test
    void returnsStructuredValidationError() throws Exception {
        MvcResult result = mockMvc.perform(post("/test/validation")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(header().exists(TraceIdFilter.TRACE_ID_HEADER))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.error.fieldErrors.name").value("must not be blank"))
                .andReturn();

        assertTraceIdMatchesResponseHeader(result);
    }

    @Test
    void hidesUnexpectedExceptionDetails() throws Exception {
        MvcResult result = mockMvc.perform(get("/test/failure"))
                .andExpect(status().isInternalServerError())
                .andExpect(header().exists(TraceIdFilter.TRACE_ID_HEADER))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("INTERNAL_ERROR"))
                .andExpect(jsonPath("$.error.message").value("An unexpected error occurred"))
                .andExpect(content().string(not(containsString("Sensitive internal detail"))))
                .andReturn();

        assertTraceIdMatchesResponseHeader(result);
    }

    private void assertTraceIdMatchesResponseHeader(MvcResult result) throws Exception {
        String traceId = result.getResponse().getHeader(TraceIdFilter.TRACE_ID_HEADER);
        assertThat(traceId).isNotBlank();
        assertThat(result.getResponse().getContentAsString())
                .contains("\"traceId\":\"" + traceId + "\"");
    }

    @RestController
    private static class TestController {

        @PostMapping("/test/validation")
        void validate(@Valid @RequestBody TestRequest request) {
        }

        @GetMapping("/test/failure")
        void fail() {
            throw new IllegalStateException("Sensitive internal detail");
        }
    }

    private record TestRequest(@NotBlank String name) {
    }
}

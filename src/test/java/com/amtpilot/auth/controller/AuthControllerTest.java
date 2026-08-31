package com.amtpilot.auth.controller;

import java.time.Instant;
import java.util.UUID;

import com.amtpilot.auth.exception.EmailAlreadyExistsException;
import com.amtpilot.auth.service.JwtService;
import com.amtpilot.common.web.GlobalExceptionHandler;
import com.amtpilot.common.web.TraceIdFilter;
import com.amtpilot.entity.User;
import com.amtpilot.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private JwtService jwtService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new AuthController(userService, jwtService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .addFilters(new TraceIdFilter())
                .build();
    }

    @Test
    void registersUserAndReturnsCreatedResponse() throws Exception {
        UUID userId = UUID.randomUUID();
        Instant createdAt = Instant.parse("2026-08-28T10:15:30Z");
        User user = mock(User.class);

        when(user.getId()).thenReturn(userId);
        when(user.getEmail()).thenReturn("student@example.com");
        when(user.getCreatedAt()).thenReturn(createdAt);
        when(userService.register("student@example.com", "strongPassword123"))
                .thenReturn(user);

        MvcResult result = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "student@example.com",
                                  "password": "strongPassword123"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().exists(TraceIdFilter.TRACE_ID_HEADER))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(userId.toString()))
                .andExpect(jsonPath("$.data.email").value("student@example.com"))
                .andExpect(jsonPath("$.data.createdAt").value(createdAt.toString()))
                .andExpect(jsonPath("$.data.passwordHash").doesNotExist())
                .andReturn();

        assertTraceIdMatchesResponseHeader(result);
    }

    @Test
    void rejectsInvalidRegistrationRequest() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "invalid-email",
                                  "password": "short"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.error.fieldErrors.email").value("Email must be valid"))
                .andExpect(jsonPath("$.error.fieldErrors.password")
                        .value("Password must be at least 8 characters"));

        verifyNoInteractions(userService);
    }

    @Test
    void returnsConflictWhenEmailAlreadyExists() throws Exception {
        when(userService.register("student@example.com", "strongPassword123"))
                .thenThrow(new EmailAlreadyExistsException());

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "student@example.com",
                                  "password": "strongPassword123"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("EMAIL_ALREADY_EXISTS"))
                .andExpect(jsonPath("$.error.message")
                        .value("An account with this email already exists"));
    }

    @Test
    void logsInUserAndReturnsJwtResponse() throws Exception {
        User user = mock(User.class);

        when(userService.authenticate(
                "student@example.com",
                "strongPassword123"))
                .thenReturn(user);
        when(jwtService.generateToken(user))
                .thenReturn("header.payload.signature");
        when(jwtService.getExpirationSeconds())
                .thenReturn(3600L);

        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "student@example.com",
                                  "password": "strongPassword123"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(header().exists(TraceIdFilter.TRACE_ID_HEADER))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken")
                        .value("header.payload.signature"))
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.data.expiresIn").value(3600))
                .andReturn();

        assertTraceIdMatchesResponseHeader(result);
    }

    private void assertTraceIdMatchesResponseHeader(MvcResult result) throws Exception {
        String traceId = result.getResponse().getHeader(TraceIdFilter.TRACE_ID_HEADER);
        assertThat(traceId).isNotBlank();
        assertThat(result.getResponse().getContentAsString())
                .contains("\"traceId\":\"" + traceId + "\"");
    }
}

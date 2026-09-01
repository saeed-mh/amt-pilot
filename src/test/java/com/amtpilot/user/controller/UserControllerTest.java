package com.amtpilot.user.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;

import com.amtpilot.common.web.ApiResponse;
import com.amtpilot.entity.User;
import com.amtpilot.enums.Role;
import com.amtpilot.service.UserService;
import com.amtpilot.user.dto.UserProfileResponse;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    @Test
    void returnsCurrentUserProfileFromJwtSubject() {
        UUID userId = UUID.randomUUID();
        Instant createdAt = Instant.parse("2026-09-01T10:00:00Z");

        Jwt jwt = mock(Jwt.class);
        User user = mock(User.class);

        when(jwt.getSubject()).thenReturn(userId.toString());
        when(userService.getById(userId)).thenReturn(user);

        when(user.getId()).thenReturn(userId);
        when(user.getEmail()).thenReturn("student@example.com");
        when(user.getPreferredLanguage()).thenReturn("en");
        when(user.getCity()).thenReturn("Berlin");
        when(user.getCountryOfOrigin()).thenReturn("Iran");
        when(user.getUserType()).thenReturn("STUDENT");
        when(user.getTimezone()).thenReturn("Europe/Berlin");
        when(user.getRole()).thenReturn(Role.USER);
        when(user.getCreatedAt()).thenReturn(createdAt);

        ResponseEntity<ApiResponse<UserProfileResponse>> response =
                userController.getCurrentUser(jwt, "trace-123");

        UserProfileResponse expectedProfile = new UserProfileResponse(
                userId,
                "student@example.com",
                "en",
                "Berlin",
                "Iran",
                "STUDENT",
                "Europe/Berlin",
                Role.USER,
                createdAt);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().success()).isTrue();
        assertThat(response.getBody().data()).isEqualTo(expectedProfile);
        assertThat(response.getBody().error()).isNull();
        assertThat(response.getBody().traceId()).isEqualTo("trace-123");

        verify(userService).getById(userId);
    }
}
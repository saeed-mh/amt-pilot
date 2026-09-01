package com.amtpilot.user.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.amtpilot.common.web.ApiResponse;
import com.amtpilot.common.web.TraceIdFilter;
import com.amtpilot.entity.User;
import com.amtpilot.service.UserService;
import com.amtpilot.user.dto.UserProfileResponse;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getCurrentUser(
            @AuthenticationPrincipal Jwt jwt,
            @RequestAttribute(TraceIdFilter.TRACE_ID_ATTRIBUTE) String traceId) {

        UUID userId = UUID.fromString(jwt.getSubject());
        User user = userService.getById(userId);

        UserProfileResponse response = new UserProfileResponse(
                user.getId(),
                user.getEmail(),
                user.getPreferredLanguage(),
                user.getCity(),
                user.getCountryOfOrigin(),
                user.getUserType(),
                user.getTimezone(),
                user.getRole(),
                user.getCreatedAt());

        return ResponseEntity.ok(
                ApiResponse.success(response, traceId));
    }

}

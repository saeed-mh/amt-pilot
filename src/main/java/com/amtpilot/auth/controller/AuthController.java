package com.amtpilot.auth.controller;

import com.amtpilot.auth.dto.LoginRequest;
import com.amtpilot.auth.dto.LoginResponse;
import com.amtpilot.auth.dto.RegisterRequest;
import com.amtpilot.auth.dto.RegisterResponse;
import com.amtpilot.auth.service.JwtService;
import com.amtpilot.common.web.ApiResponse;
import com.amtpilot.common.web.TraceIdFilter;
import com.amtpilot.entity.User;
import com.amtpilot.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final UserService userService;
    private final JwtService jwtService;

    public AuthController(UserService userService, JwtService jwtService) {
        this.userService = userService;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<RegisterResponse>> register(
            @Valid @RequestBody RegisterRequest request,
            @RequestAttribute(TraceIdFilter.TRACE_ID_ATTRIBUTE) String traceId) {

        User user = userService.register(
                request.getEmail(),
                request.getPassword());

        RegisterResponse response = new RegisterResponse(
                user.getId(),
                user.getEmail(),
                user.getCreatedAt());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, traceId));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request,
            @RequestAttribute(TraceIdFilter.TRACE_ID_ATTRIBUTE) String traceId) {
        User user = userService.authenticate(
                request.getEmail(),
                request.getPassword());

        String token = jwtService.generateToken(user);

        LoginResponse response = new LoginResponse(
                token,
                "Bearer",
                jwtService.getExpirationSeconds());

        return ResponseEntity.ok(ApiResponse.success(response, traceId));
    }
}

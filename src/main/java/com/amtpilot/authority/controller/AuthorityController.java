package com.amtpilot.authority.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.amtpilot.authority.dto.AuthorityResponse;
import com.amtpilot.authority.service.AuthorityService;
import com.amtpilot.common.web.ApiResponse;
import com.amtpilot.common.web.TraceIdFilter;
import com.amtpilot.entity.Authority;

@RestController
@RequestMapping("/api/v1/authorities")
public class AuthorityController {

    private final AuthorityService authorityService;

    public AuthorityController(AuthorityService authorityService) {
        this.authorityService = authorityService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<AuthorityResponse>>> getAuthorities(
            @RequestParam(defaultValue = "Dortmund") String city,
            @RequestAttribute(TraceIdFilter.TRACE_ID_ATTRIBUTE) String traceId) {

        List<AuthorityResponse> responses = authorityService.findByCity(city)
                .stream()
                .map(this::toResponse)
                .toList();

        return ResponseEntity.ok(
                ApiResponse.success(responses, traceId));
    }

    private AuthorityResponse toResponse(Authority authority) {
        return new AuthorityResponse(
                authority.getId(),
                authority.getName(),
                authority.getAuthorityType(),
                authority.getCity(),
                authority.getOfficialUrl(),
                authority.getContactUrl());
    }
}
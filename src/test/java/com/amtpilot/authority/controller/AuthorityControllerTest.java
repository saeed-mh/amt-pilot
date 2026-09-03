package com.amtpilot.authority.controller;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.amtpilot.authority.dto.AuthorityResponse;
import com.amtpilot.authority.service.AuthorityService;
import com.amtpilot.common.web.ApiResponse;
import com.amtpilot.entity.Authority;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthorityControllerTest {

    @Mock
    private AuthorityService authorityService;

    @InjectMocks
    private AuthorityController authorityController;

    @Test
    void returnsAuthoritiesForCity() {
        UUID authorityId = UUID.randomUUID();
        Authority authority = mock(Authority.class);

        when(authorityService.findByCity("Dortmund"))
                .thenReturn(List.of(authority));

        when(authority.getId()).thenReturn(authorityId);
        when(authority.getName()).thenReturn("Immigration Office");
        when(authority.getAuthorityType()).thenReturn("IMMIGRATION");
        when(authority.getCity()).thenReturn("Dortmund");
        when(authority.getOfficialUrl())
                .thenReturn("https://example.com/immigration");
        when(authority.getContactUrl()).thenReturn(null);

        ResponseEntity<ApiResponse<List<AuthorityResponse>>> response =
                authorityController.getAuthorities(
                        "Dortmund",
                        "trace-123");

        AuthorityResponse expectedAuthority = new AuthorityResponse(
                authorityId,
                "Immigration Office",
                "IMMIGRATION",
                "Dortmund",
                "https://example.com/immigration",
                null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().success()).isTrue();
        assertThat(response.getBody().data())
                .containsExactly(expectedAuthority);
        assertThat(response.getBody().error()).isNull();
        assertThat(response.getBody().traceId()).isEqualTo("trace-123");

        verify(authorityService).findByCity("Dortmund");
    }
}
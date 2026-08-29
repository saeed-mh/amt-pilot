package com.amtpilot.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.UUID;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import com.amtpilot.entity.User;
import com.amtpilot.enums.Role;

class JwtServiceTest {

    @Test
    void shouldGenerateTokenWithUserClaims() {
        SecretKey secretKey = new SecretKeySpec(
                "test-secret-key-with-at-least-32-characters"
                        .getBytes(StandardCharsets.UTF_8),
                "HmacSHA256");

        JwtEncoder jwtEncoder =
                NimbusJwtEncoder.withSecretKey(secretKey).build();

        JwtDecoder jwtDecoder =
                NimbusJwtDecoder.withSecretKey(secretKey).build();

        JwtService jwtService = new JwtService(jwtEncoder, 3600);

        UUID userId = UUID.randomUUID();
        User user = mock(User.class);

        when(user.getId()).thenReturn(userId);
        when(user.getEmail()).thenReturn("user@example.com");
        when(user.getRole()).thenReturn(Role.USER);

        String token = jwtService.generateToken(user);
        Jwt decodedToken = jwtDecoder.decode(token);

        assertThat(token).isNotBlank();
        assertThat(decodedToken.getIssuer().toString())
                .isEqualTo("https://amtpilot.local");
        assertThat(decodedToken.getSubject())
                .isEqualTo(userId.toString());
        assertThat(decodedToken.getClaimAsString("email"))
                .isEqualTo("user@example.com");
        assertThat(decodedToken.getClaimAsString("role"))
                .isEqualTo("USER");
        assertThat(Duration.between(
                decodedToken.getIssuedAt(),
                decodedToken.getExpiresAt()))
                .isEqualTo(Duration.ofHours(1));
    }
}
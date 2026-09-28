package com.amtpilot.common.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

class SecurityConfigurationTest {

    @Test
    void allowsPutRequestsFromTheFrontend() {
        SecurityConfiguration securityConfiguration =
                new SecurityConfiguration();
        CorsConfigurationSource source =
                securityConfiguration.corsConfigurationSource();
        MockHttpServletRequest request =
                new MockHttpServletRequest(
                        "OPTIONS",
                        "/api/v1/applications/example/advice/answers");

        CorsConfiguration configuration =
                source.getCorsConfiguration(request);

        assertThat(configuration).isNotNull();
        assertThat(configuration.getAllowedOrigins())
                .contains("http://localhost:5173");
        assertThat(configuration.getAllowedMethods())
                .contains("PUT");
    }
}

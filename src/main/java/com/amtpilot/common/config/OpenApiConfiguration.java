package com.amtpilot.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration
public class OpenApiConfiguration {

	private static final String SECURITY_SCHEME_NAME = "bearerAuth";

	@Bean
	OpenAPI amtPilotOpenApi() {
		return new OpenAPI()
				.info(new Info()
						.title("AmtPilot API")
						.version("v1")
						.description(
								"Backend API for the German bureaucracy copilot. Not legal advice."))
				.addSecurityItem(
						new SecurityRequirement()
								.addList(SECURITY_SCHEME_NAME))
				.components(
						new Components()
								.addSecuritySchemes(
										SECURITY_SCHEME_NAME,
										new SecurityScheme()
												.type(SecurityScheme.Type.HTTP)
												.scheme("bearer")
												.bearerFormat("JWT")));
	}
}
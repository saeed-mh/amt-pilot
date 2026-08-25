package com.amtpilot.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfiguration {

	@Bean
	OpenAPI amtPilotOpenApi() {
		return new OpenAPI().info(new Info()
				.title("AmtPilot API")
				.version("v1")
				.description("Backend API for the German bureaucracy copilot. Not legal advice."));
	}

}

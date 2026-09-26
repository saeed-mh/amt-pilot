package com.amtpilot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class AmtPilotApplication {

	public static void main(String[] args) {
		SpringApplication.run(AmtPilotApplication.class, args);
	}
}
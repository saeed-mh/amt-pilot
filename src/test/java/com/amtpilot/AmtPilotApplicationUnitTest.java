package com.amtpilot;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import static org.assertj.core.api.Assertions.assertThat;

class AmtPilotApplicationUnitTest {

	@Test
	void applicationHasSpringBootEntryPoint() {
		assertThat(AmtPilotApplication.class).hasAnnotation(SpringBootApplication.class);
	}

}

package com.amtpilot;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers(disabledWithoutDocker = true)
class PostgresMigrationIntegrationTest {

	private static final DockerImageName PGVECTOR_IMAGE = DockerImageName
			.parse("pgvector/pgvector:0.8.6-pg17")
			.asCompatibleSubstituteFor("postgres");

	@Container
	@ServiceConnection
	static final PostgreSQLContainer postgres = new PostgreSQLContainer(PGVECTOR_IMAGE);

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Test
	void flywayCreatesTheCoreTables() {
		Integer tableCount = jdbcTemplate.queryForObject("""
				SELECT COUNT(*)
				FROM information_schema.tables
				WHERE table_schema = 'public'
				  AND table_name IN ('app_user', 'authority', 'official_source',
				                     'process_definition', 'requirement_definition',
				                     'application', 'audit_event')
				""", Integer.class);

		assertThat(tableCount).isEqualTo(7);
	}

}

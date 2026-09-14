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

	@Test
	void flywaySeedsAddressRegistrationRequirements() {
		Integer requirementCount = jdbcTemplate.queryForObject("""
				SELECT COUNT(*)
				FROM requirement_definition requirement
				JOIN process_definition process
				  ON process.id = requirement.process_id
				WHERE process.code = 'ADDRESS_REGISTRATION'
				""", Integer.class);

		assertThat(requirementCount).isEqualTo(3);
	}

	@Test
	void flywaySeedsCoreDortmundProcessCatalog() {
		Integer authorityCount = jdbcTemplate.queryForObject("""
				SELECT COUNT(*)
				FROM authority
				WHERE city = 'Dortmund'
				""", Integer.class);

		Integer processCount = jdbcTemplate.queryForObject("""
				SELECT COUNT(*)
				FROM process_definition
				WHERE city = 'Dortmund'
				  AND active = TRUE
				""", Integer.class);

		Integer sourceCount = jdbcTemplate.queryForObject("""
				SELECT COUNT(*)
				FROM official_source
				WHERE city = 'Dortmund'
				""", Integer.class);

		Integer requirementCount = jdbcTemplate.queryForObject("""
				SELECT COUNT(*)
				FROM requirement_definition requirement
				JOIN process_definition process
				  ON process.id = requirement.process_id
				WHERE process.city = 'Dortmund'
				""", Integer.class);

		Integer processesWithoutRequirements = jdbcTemplate.queryForObject("""
				SELECT COUNT(*)
				FROM process_definition process
				WHERE process.city = 'Dortmund'
				  AND process.active = TRUE
				  AND NOT EXISTS (
				      SELECT 1
				      FROM requirement_definition requirement
				      WHERE requirement.process_id = process.id
				  )
				""", Integer.class);

		Integer residencePermitRequirementCount = jdbcTemplate.queryForObject("""
				SELECT COUNT(*)
				FROM requirement_definition requirement
				JOIN process_definition process
				  ON process.id = requirement.process_id
				WHERE process.code = 'RESIDENCE_PERMIT_EXTENSION'
				""", Integer.class);

		assertThat(authorityCount).isEqualTo(3);
		assertThat(processCount).isEqualTo(8);
		assertThat(sourceCount).isEqualTo(10);
		assertThat(requirementCount).isEqualTo(38);
		assertThat(processesWithoutRequirements).isZero();
		assertThat(residencePermitRequirementCount).isEqualTo(7);
	}

}

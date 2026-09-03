package com.amtpilot.repository;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import com.amtpilot.entity.Authority;
import com.amtpilot.entity.ProcessDefinition;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers(disabledWithoutDocker = true)
@Transactional
class ProcessDefinitionRepositoryIntegrationTest {

    private static final DockerImageName PGVECTOR_IMAGE = DockerImageName
            .parse("pgvector/pgvector:0.8.6-pg17")
            .asCompatibleSubstituteFor("postgres");

    @Container
    @ServiceConnection
    static final PostgreSQLContainer postgres =
            new PostgreSQLContainer(PGVECTOR_IMAGE);

    @Autowired
    private AuthorityRepository authorityRepository;

    @Autowired
    private ProcessDefinitionRepository processDefinitionRepository;

    @Test
    void findsActiveProcessesByCityAndCode() {
        // Arrange: create authorities needed by the processes
        Authority dortmundAuthority = new Authority(
                "Dortmund Immigration Office",
                "IMMIGRATION",
                "Dortmund",
                "https://example.com/dortmund",
                null);

        Authority cologneAuthority = new Authority(
                "Cologne Immigration Office",
                "IMMIGRATION",
                "Cologne",
                "https://example.com/cologne",
                null);

        authorityRepository.saveAllAndFlush(
                List.of(dortmundAuthority, cologneAuthority));

        // Arrange: create three processes
        ProcessDefinition residenceExtension = new ProcessDefinition(
                dortmundAuthority,
                "DO_STUDENT_RESIDENCE_EXTENSION",
                "Student Residence Permit Extension",
                "Dortmund",
                "IMMIGRATION");

        ProcessDefinition addressRegistration = new ProcessDefinition(
                dortmundAuthority,
                "DO_ADDRESS_REGISTRATION",
                "Address Registration",
                "Dortmund",
                "REGISTRATION");

        ProcessDefinition cologneRegistration = new ProcessDefinition(
                cologneAuthority,
                "CGN_ADDRESS_REGISTRATION",
                "Cologne Address Registration",
                "Cologne",
                "REGISTRATION");

        processDefinitionRepository.saveAllAndFlush(
                List.of(
                        residenceExtension,
                        addressRegistration,
                        cologneRegistration));

        // Act: search for active Dortmund processes
        List<ProcessDefinition> result =
                processDefinitionRepository
                        .findByCityIgnoreCaseAndActiveTrueOrderByTitleAsc(
                                "dOrTmUnD");

        // Assert: only Dortmund results, ordered by title
        assertThat(result)
                .extracting(ProcessDefinition::getTitle)
                .containsExactly(
                        "Address Registration",
                        "Student Residence Permit Extension");

        // Assert: Hibernate generated the defaults and metadata
        assertThat(result)
                .allMatch(process -> process.getId() != null)
                .allMatch(process -> process.getCreatedAt() != null)
                .allMatch(process -> process.getUpdatedAt() != null)
                .allMatch(process -> process.getVersion() == 1)
                .allMatch(ProcessDefinition::isActive);

        // Act: search using the unique process code
        Optional<ProcessDefinition> foundProcess =
                processDefinitionRepository.findByCode(
                        "DO_STUDENT_RESIDENCE_EXTENSION");

        // Assert: the correct process was found
        assertThat(foundProcess).isPresent();
        assertThat(foundProcess.orElseThrow().getTitle())
                .isEqualTo("Student Residence Permit Extension");
    }
}
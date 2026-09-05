package com.amtpilot.repository;

import java.util.List;

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
import com.amtpilot.entity.OfficialSource;
import com.amtpilot.entity.ProcessDefinition;
import com.amtpilot.entity.RequirementDefinition;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers(disabledWithoutDocker = true)
@Transactional
class RequirementDefinitionRepositoryIntegrationTest {

    private static final DockerImageName PGVECTOR_IMAGE = DockerImageName
            .parse("pgvector/pgvector:0.8.6-pg17")
            .asCompatibleSubstituteFor("postgres");

    @Container
    @ServiceConnection
    static final PostgreSQLContainer postgres = new PostgreSQLContainer(PGVECTOR_IMAGE);

    @Autowired
    private AuthorityRepository authorityRepository;

    @Autowired
    private OfficialSourceRepository officialSourceRepository;

    @Autowired
    private ProcessDefinitionRepository processDefinitionRepository;

    @Autowired
    private RequirementDefinitionRepository requirementRepository;

    @Test
    void findsRequirementsByProcessAndOrdersByTitle() {
        Authority authority = new Authority(
                "Dortmund Immigration Office",
                "IMMIGRATION",
                "Dortmund",
                "https://example.com/dortmund",
                null);

        authorityRepository.saveAndFlush(authority);

        OfficialSource source = new OfficialSource(
                authority,
                "https://example.com/dortmund/requirements",
                "Dortmund Residence Requirements",
                "Dortmund",
                "en");

        officialSourceRepository.saveAndFlush(source);

        ProcessDefinition residenceProcess = new ProcessDefinition(
                authority,
                "DO_STUDENT_RESIDENCE_EXTENSION",
                "Student Residence Permit Extension",
                "Dortmund",
                "IMMIGRATION");

        ProcessDefinition addressProcess = new ProcessDefinition(
                authority,
                "DO_ADDRESS_REGISTRATION",
                "Address Registration",
                "Dortmund",
                "REGISTRATION");

        processDefinitionRepository.saveAllAndFlush(
                List.of(residenceProcess, addressProcess));

        RequirementDefinition passport = new RequirementDefinition(
                residenceProcess,
                source,
                "PASSPORT",
                "Passport",
                true);

        RequirementDefinition enrollmentCertificate = new RequirementDefinition(
                residenceProcess,
                source,
                "ENROLLMENT_CERTIFICATE",
                "Enrollment Certificate",
                true);

        RequirementDefinition addressDocument = new RequirementDefinition(
                addressProcess,
                source,
                "ADDRESS_DOCUMENT",
                "Address Document",
                true);

        requirementRepository.saveAllAndFlush(
                List.of(
                        passport,
                        enrollmentCertificate,
                        addressDocument));

        List<RequirementDefinition> result = requirementRepository.findByProcessIdOrderByTitleAsc(
                residenceProcess.getId());

        assertThat(result)
                .extracting(RequirementDefinition::getTitle)
                .containsExactly(
                        "Enrollment Certificate",
                        "Passport");

        assertThat(result)
                .allMatch(requirement -> requirement.getId() != null)
                .allMatch(requirement -> requirement.getCreatedAt() != null)
                .allMatch(requirement -> requirement.getUpdatedAt() != null)
                .allMatch(requirement -> requirement.getVersion() == 1)
                .allMatch(RequirementDefinition::isRequired)
                .allMatch(requirement -> requirement.getProcess().getId()
                        .equals(residenceProcess.getId()))
                .allMatch(requirement -> requirement.getSource().getId()
                        .equals(source.getId()));
    }
}
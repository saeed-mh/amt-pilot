package com.amtpilot.repository;

import java.util.Comparator;
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

import com.amtpilot.entity.Application;
import com.amtpilot.entity.Authority;
import com.amtpilot.entity.ProcessDefinition;
import com.amtpilot.entity.User;
import com.amtpilot.enums.ApplicationStatus;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers(disabledWithoutDocker = true)
@Transactional
class ApplicationRepositoryIntegrationTest {

    private static final DockerImageName PGVECTOR_IMAGE = DockerImageName
            .parse("pgvector/pgvector:0.8.6-pg17")
            .asCompatibleSubstituteFor("postgres");

    @Container
    @ServiceConnection
    static final PostgreSQLContainer postgres = new PostgreSQLContainer(PGVECTOR_IMAGE);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthorityRepository authorityRepository;

    @Autowired
    private ProcessDefinitionRepository processDefinitionRepository;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Test
    void findsOnlyApplicationsOwnedByUser() {
        User mina = new User(
                "mina@example.com",
                "hashed-password");

        User alex = new User(
                "alex@example.com",
                "hashed-password");

        userRepository.saveAllAndFlush(List.of(mina, alex));

        Authority authority = new Authority(
                "Dortmund City Office",
                "CITY_ADMINISTRATION",
                "Dortmund",
                "https://example.com/dortmund",
                null);

        authorityRepository.saveAndFlush(authority);

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

        Application minaResidenceApplication = new Application(mina, residenceProcess);

        minaResidenceApplication.changeStatus(
                ApplicationStatus.ACTION_REQUIRED);
        minaResidenceApplication.updateCompleteness(50);

        Application minaAddressApplication = new Application(mina, addressProcess);

        Application alexResidenceApplication = new Application(alex, residenceProcess);

        applicationRepository.saveAllAndFlush(
                List.of(
                        minaResidenceApplication,
                        minaAddressApplication,
                        alexResidenceApplication));

        List<Application> minaApplications = applicationRepository
                .findByUserIdOrderByCreatedAtDesc(mina.getId());

        assertThat(minaApplications)
                .hasSize(2)
                .allMatch(application -> application.getUser().getId()
                        .equals(mina.getId()))
                .isSortedAccordingTo(
                        Comparator.comparing(Application::getCreatedAt)
                                .reversed());

        assertThat(applicationRepository.findByIdAndUserId(
                minaResidenceApplication.getId(),
                mina.getId()))
                .hasValueSatisfying(application -> {
                    assertThat(application.getStatus())
                            .isEqualTo(
                                    ApplicationStatus.ACTION_REQUIRED);
                    assertThat(application.getCompleteness())
                            .isEqualTo((short) 50);
                    assertThat(application.getCreatedAt()).isNotNull();
                    assertThat(application.getUpdatedAt()).isNotNull();
                });

        assertThat(applicationRepository.findByIdAndUserId(
                minaResidenceApplication.getId(),
                alex.getId()))
                .isEmpty();

        assertThat(minaAddressApplication.getStatus())
                .isEqualTo(ApplicationStatus.DRAFT);
        assertThat(minaAddressApplication.getCompleteness())
                .isZero();
    }
}
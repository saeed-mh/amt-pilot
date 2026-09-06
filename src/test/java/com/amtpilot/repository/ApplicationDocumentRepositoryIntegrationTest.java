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

import com.amtpilot.entity.Application;
import com.amtpilot.entity.ApplicationDocument;
import com.amtpilot.entity.ProcessDefinition;
import com.amtpilot.entity.User;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers(disabledWithoutDocker = true)
@Transactional
class ApplicationDocumentRepositoryIntegrationTest {

    private static final DockerImageName PGVECTOR_IMAGE = DockerImageName
            .parse("pgvector/pgvector:0.8.6-pg17")
            .asCompatibleSubstituteFor("postgres");

    @Container
    @ServiceConnection
    static final PostgreSQLContainer postgres = new PostgreSQLContainer(PGVECTOR_IMAGE);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProcessDefinitionRepository processRepository;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private ApplicationDocumentRepository documentRepository;

    @Test
    void findsDocumentsForApplicationAndChecksOwnership() {
        User mina = new User(
                "mina@example.com",
                "hashed-password");

        User alex = new User(
                "alex@example.com",
                "hashed-password");

        userRepository.saveAllAndFlush(List.of(mina, alex));

        ProcessDefinition process = new ProcessDefinition(
                null,
                "ADDRESS_REGISTRATION",
                "Address Registration",
                "Dortmund",
                "REGISTRATION");

        processRepository.saveAndFlush(process);

        Application minaApplication = new Application(mina, process);

        Application alexApplication = new Application(alex, process);

        applicationRepository.saveAllAndFlush(
                List.of(minaApplication, alexApplication));

        ApplicationDocument passport = new ApplicationDocument(
                minaApplication,
                null,
                "passport.pdf",
                "application/pdf",
                1024,
                "uploads/mina/passport.pdf");

        ApplicationDocument registrationForm = new ApplicationDocument(
                minaApplication,
                null,
                "registration-form.pdf",
                "application/pdf",
                2048,
                "uploads/mina/registration-form.pdf");

        ApplicationDocument alexPassport = new ApplicationDocument(
                alexApplication,
                null,
                "passport.pdf",
                "application/pdf",
                1500,
                "uploads/alex/passport.pdf");

        documentRepository.saveAllAndFlush(
                List.of(
                        passport,
                        registrationForm,
                        alexPassport));

        List<ApplicationDocument> result = documentRepository
                .findByApplicationIdOrderByCreatedAtDesc(
                        minaApplication.getId());

        assertThat(result)
                .hasSize(2)
                .extracting(ApplicationDocument::getOriginalFilename)
                .containsExactlyInAnyOrder(
                        "passport.pdf",
                        "registration-form.pdf");

        assertThat(documentRepository
                .findByIdAndApplicationUserId(
                        passport.getId(),
                        mina.getId()))
                .hasValueSatisfying(document -> {
                    assertThat(document.getContentType())
                            .isEqualTo("application/pdf");
                    assertThat(document.getSizeBytes())
                            .isEqualTo(1024);
                    assertThat(document.getCreatedAt())
                            .isNotNull();
                });

        assertThat(documentRepository
                .findByIdAndApplicationUserId(
                        passport.getId(),
                        alex.getId()))
                .isEmpty();
    }
}
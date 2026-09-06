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
import com.amtpilot.entity.ApplicationChecklistItem;
import com.amtpilot.entity.Authority;
import com.amtpilot.entity.OfficialSource;
import com.amtpilot.entity.ProcessDefinition;
import com.amtpilot.entity.RequirementDefinition;
import com.amtpilot.entity.User;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers(disabledWithoutDocker = true)
@Transactional
class ApplicationChecklistItemRepositoryIntegrationTest {

    private static final DockerImageName PGVECTOR_IMAGE =
            DockerImageName
                    .parse("pgvector/pgvector:0.8.6-pg17")
                    .asCompatibleSubstituteFor("postgres");

    @Container
    @ServiceConnection
    static final PostgreSQLContainer postgres =
            new PostgreSQLContainer(PGVECTOR_IMAGE);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthorityRepository authorityRepository;

    @Autowired
    private OfficialSourceRepository officialSourceRepository;

    @Autowired
    private ProcessDefinitionRepository processRepository;

    @Autowired
    private RequirementDefinitionRepository requirementRepository;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private ApplicationChecklistItemRepository checklistRepository;

    @Test
    void findsChecklistItemsForApplicationAndChecksOwnership() {
        User mina = new User(
                "mina@example.com",
                "hashed-password");

        User alex = new User(
                "alex@example.com",
                "hashed-password");

        userRepository.saveAllAndFlush(List.of(mina, alex));

        Authority authority = new Authority(
                "Dortmund Citizens Office",
                "CITIZENS_OFFICE",
                "Dortmund",
                "https://example.com/dortmund",
                null);

        authorityRepository.saveAndFlush(authority);

        OfficialSource source = new OfficialSource(
                authority,
                "https://example.com/address-registration",
                "Address Registration Requirements",
                "Dortmund",
                "en");

        officialSourceRepository.saveAndFlush(source);

        ProcessDefinition process = new ProcessDefinition(
                authority,
                "ADDRESS_REGISTRATION",
                "Address Registration",
                "Dortmund",
                "REGISTRATION");

        processRepository.saveAndFlush(process);

        RequirementDefinition passport = new RequirementDefinition(
                process,
                source,
                "PASSPORT",
                "Passport",
                true);

        RequirementDefinition registrationForm =
                new RequirementDefinition(
                        process,
                        source,
                        "REGISTRATION_FORM",
                        "Address Registration Form",
                        true);

        requirementRepository.saveAllAndFlush(
                List.of(passport, registrationForm));

        Application minaApplication =
                new Application(mina, process);

        Application alexApplication =
                new Application(alex, process);

        applicationRepository.saveAllAndFlush(
                List.of(minaApplication, alexApplication));

        ApplicationChecklistItem minaPassport =
                new ApplicationChecklistItem(
                        minaApplication,
                        passport);

        minaPassport.updateCompleted(true);

        ApplicationChecklistItem minaRegistrationForm =
                new ApplicationChecklistItem(
                        minaApplication,
                        registrationForm);

        ApplicationChecklistItem alexPassport =
                new ApplicationChecklistItem(
                        alexApplication,
                        passport);

        checklistRepository.saveAllAndFlush(
                List.of(
                        minaPassport,
                        minaRegistrationForm,
                        alexPassport));

        List<ApplicationChecklistItem> result =
                checklistRepository
                        .findByApplicationIdOrderByRequirementTitleAsc(
                                minaApplication.getId());

        assertThat(result)
                .hasSize(2)
                .extracting(item ->
                        item.getRequirement().getTitle())
                .containsExactly(
                        "Address Registration Form",
                        "Passport");

        assertThat(result)
                .allMatch(item ->
                        item.getApplication().getId()
                                .equals(minaApplication.getId()));

        assertThat(checklistRepository
                .findByIdAndApplicationUserId(
                        minaPassport.getId(),
                        mina.getId()))
                .hasValueSatisfying(item -> {
                    assertThat(item.isCompleted()).isTrue();
                    assertThat(item.getCreatedAt()).isNotNull();
                    assertThat(item.getUpdatedAt()).isNotNull();
                });

        assertThat(checklistRepository
                .findByIdAndApplicationUserId(
                        minaPassport.getId(),
                        alex.getId()))
                .isEmpty();
    }
}
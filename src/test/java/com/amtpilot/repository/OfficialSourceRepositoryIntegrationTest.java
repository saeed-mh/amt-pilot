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

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers(disabledWithoutDocker = true)
@Transactional
class OfficialSourceRepositoryIntegrationTest {

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
    private OfficialSourceRepository officialSourceRepository;

    @Test
    void findsSourcesByCityIgnoringCaseAndOrdersByTitle() {
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

        OfficialSource residencePermit = new OfficialSource(
                dortmundAuthority,
                "https://example.com/dortmund/residence",
                "Residence Permit",
                "Dortmund",
                "en");

        OfficialSource cityRegistration = new OfficialSource(
                dortmundAuthority,
                "https://example.com/dortmund/registration",
                "City Registration",
                "Dortmund",
                "en");

        OfficialSource cologneSource = new OfficialSource(
                cologneAuthority,
                "https://example.com/cologne/registration",
                "Cologne Registration",
                "Cologne",
                "en");

        officialSourceRepository.saveAllAndFlush(
                List.of(residencePermit, cityRegistration, cologneSource));

        List<OfficialSource> result =
                officialSourceRepository
                        .findByCityIgnoreCaseOrderByTitleAsc("dOrTmUnD");

        assertThat(result)
                .extracting(OfficialSource::getTitle)
                .containsExactly("City Registration", "Residence Permit");

        assertThat(result)
                .allMatch(source -> source.getId() != null)
                .allMatch(source -> source.getCreatedAt() != null)
                .allMatch(source -> source.getUpdatedAt() != null)
                .allMatch(source -> source.getStatus().equals("NEEDS_REVIEW"))
                .allMatch(source ->
                        source.getAuthority().getId()
                                .equals(dortmundAuthority.getId()));
    }
}
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

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers(disabledWithoutDocker = true)
@Transactional
class AuthorityRepositoryIntegrationTest {

    private static final DockerImageName PGVECTOR_IMAGE = DockerImageName
            .parse("pgvector/pgvector:0.8.6-pg17")
            .asCompatibleSubstituteFor("postgres");

    @Container
    @ServiceConnection
    static final PostgreSQLContainer postgres =
            new PostgreSQLContainer(PGVECTOR_IMAGE);

    @Autowired
    private AuthorityRepository authorityRepository;

    @Test
    void findsAuthoritiesByCityIgnoringCaseAndOrdersByName() {
        Authority immigrationOffice = new Authority(
                "Immigration Office",
                "IMMIGRATION",
                "Dortmund",
                "https://example.com/immigration",
                null);

        Authority citizensOffice = new Authority(
                "Citizens Office",
                "CITIZENS_SERVICE",
                "Dortmund",
                "https://example.com/citizens",
                "https://example.com/citizens/contact");

        Authority cologneOffice = new Authority(
                "Cologne Office",
                "CITIZENS_SERVICE",
                "Cologne",
                "https://example.com/cologne",
                null);

        authorityRepository.saveAllAndFlush(
                List.of(immigrationOffice, citizensOffice, cologneOffice));

        List<Authority> result =
                authorityRepository.findByCityIgnoreCaseOrderByNameAsc("dOrTmUnD");

        assertThat(result)
                .extracting(Authority::getName)
                .containsExactly("Citizens Office", "Immigration Office");

        assertThat(result)
                .allMatch(authority -> authority.getId() != null)
                .allMatch(authority -> authority.getCreatedAt() != null)
                .allMatch(authority -> authority.getUpdatedAt() != null);
    }
}
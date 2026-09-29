package com.amtpilot.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import com.amtpilot.process.dto.ProcessGuideChunk;

@SpringBootTest
@Testcontainers(disabledWithoutDocker = true)
@Transactional
class ProcessGuideChunkRepositoryIntegrationTest {

    private static final DockerImageName PGVECTOR_IMAGE = DockerImageName
            .parse("pgvector/pgvector:0.8.6-pg17")
            .asCompatibleSubstituteFor("postgres");

    @Container
    @ServiceConnection
    static final PostgreSQLContainer postgres =
            new PostgreSQLContainer(PGVECTOR_IMAGE);

    @Autowired
    private ProcessGuideChunkRepository chunkRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void storesEmbeddingsAndReturnsNearestGuideChunk() {
        UUID processId = jdbcTemplate.queryForObject("""
                SELECT id
                FROM process_definition
                WHERE code = 'ADDRESS_REGISTRATION'
                """, UUID.class);

        List<ProcessGuideChunk> chunks = chunkRepository.findNeedingEmbedding(
                processId,
                "en",
                "test-model");

        ProcessGuideChunk closest = chunks.getFirst();
        ProcessGuideChunk farther = chunks.get(1);
        chunkRepository.updateEmbedding(
                closest.id(),
                vector(1.0, 0.0),
                "test-model");
        chunkRepository.updateEmbedding(
                farther.id(),
                vector(0.0, 1.0),
                "test-model");

        List<ProcessGuideChunk> result = chunkRepository.findNearest(
                processId,
                "en",
                "test-model",
                vector(0.9, 0.1),
                2);

        assertThat(result)
                .extracting(ProcessGuideChunk::id)
                .containsExactly(closest.id(), farther.id());
        assertThat(chunkRepository.hasIndexedChunks(
                processId,
                "en",
                "test-model"))
                .isTrue();
    }

    private String vector(double first, double second) {
        return IntStream.range(0, 768)
                .mapToObj(index -> {
                    if (index == 0) {
                        return String.valueOf(first);
                    }
                    if (index == 1) {
                        return String.valueOf(second);
                    }
                    return "0.0";
                })
                .collect(Collectors.joining(",", "[", "]"));
    }
}

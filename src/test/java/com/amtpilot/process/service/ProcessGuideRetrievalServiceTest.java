package com.amtpilot.process.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.amtpilot.ai.client.AiEmbeddingClient;
import com.amtpilot.ai.dto.AiEmbeddingResponse;
import com.amtpilot.ai.exception.AiServiceUnavailableException;
import com.amtpilot.process.dto.ProcessGuideChunk;
import com.amtpilot.repository.ProcessGuideChunkRepository;

@ExtendWith(MockitoExtension.class)
class ProcessGuideRetrievalServiceTest {

    @Mock
    private ProcessGuideChunkRepository chunkRepository;

    @Mock
    private AiEmbeddingClient embeddingClient;

    private ProcessGuideRetrievalService retrievalService;

    @BeforeEach
    void setUp() {
        retrievalService = new ProcessGuideRetrievalService(
                chunkRepository,
                embeddingClient,
                "test-embedding-model",
                3,
                2);
    }

    @Test
    void indexesMissingChunksAndReturnsNearestOfficialContext() {
        UUID processId = UUID.randomUUID();
        ProcessGuideChunk chunk = chunk("Register within two weeks.");

        when(chunkRepository.findNeedingEmbedding(
                processId,
                "en",
                "test-embedding-model"))
                .thenReturn(List.of(chunk));
        when(embeddingClient.embed(
                List.of(chunk.content()),
                "RETRIEVAL_DOCUMENT"))
                .thenReturn(response(List.of(List.of(1.0, 0.0, 0.0))));
        when(embeddingClient.embed(
                List.of("What is the deadline?"),
                "RETRIEVAL_QUERY"))
                .thenReturn(response(List.of(List.of(0.9, 0.1, 0.0))));
        when(chunkRepository.findNearest(
                eq(processId),
                eq("en"),
                eq("test-embedding-model"),
                anyString(),
                eq(2)))
                .thenReturn(List.of(chunk));

        List<ProcessGuideChunk> result = retrievalService.retrieve(
                processId,
                "What is the deadline?");

        assertThat(result).containsExactly(chunk);
        verify(chunkRepository).updateEmbedding(
                chunk.id(),
                "[1.0,0.0,0.0]",
                "test-embedding-model");
    }

    @Test
    void skipsAiCallWhenTheProcessHasNoGuideChunks() {
        UUID processId = UUID.randomUUID();
        when(chunkRepository.findNeedingEmbedding(
                processId,
                "en",
                "test-embedding-model"))
                .thenReturn(List.of());
        when(chunkRepository.hasIndexedChunks(
                processId,
                "en",
                "test-embedding-model"))
                .thenReturn(false);

        assertThat(retrievalService.retrieve(processId, "query")).isEmpty();
    }

    @Test
    void rejectsEmbeddingResponsesWithTheWrongDimension() {
        UUID processId = UUID.randomUUID();
        ProcessGuideChunk chunk = chunk("Register within two weeks.");
        when(chunkRepository.findNeedingEmbedding(
                processId,
                "en",
                "test-embedding-model"))
                .thenReturn(List.of(chunk));
        when(embeddingClient.embed(
                List.of(chunk.content()),
                "RETRIEVAL_DOCUMENT"))
                .thenReturn(new AiEmbeddingResponse(
                        "test-embedding-model",
                        2,
                        List.of(List.of(1.0, 0.0))));

        assertThatThrownBy(() -> retrievalService.retrieve(processId, "query"))
                .isInstanceOf(AiServiceUnavailableException.class)
                .hasMessage("AI embedding service returned an invalid response");
    }

    private AiEmbeddingResponse response(List<List<Double>> embeddings) {
        return new AiEmbeddingResponse(
                "test-embedding-model",
                3,
                embeddings);
    }

    private ProcessGuideChunk chunk(String content) {
        return new ProcessGuideChunk(
                UUID.randomUUID(),
                "deadline",
                content,
                "Official registration guide",
                "https://example.test/registration",
                LocalDate.of(2026, 9, 27));
    }
}

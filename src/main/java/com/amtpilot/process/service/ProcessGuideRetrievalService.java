package com.amtpilot.process.service;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.amtpilot.ai.client.AiEmbeddingClient;
import com.amtpilot.ai.dto.AiEmbeddingResponse;
import com.amtpilot.ai.exception.AiServiceUnavailableException;
import com.amtpilot.process.dto.ProcessGuideChunk;
import com.amtpilot.repository.ProcessGuideChunkRepository;

@Service
public class ProcessGuideRetrievalService {

    private static final String LANGUAGE = "en";
    private static final String DOCUMENT_TASK = "RETRIEVAL_DOCUMENT";
    private static final String QUERY_TASK = "RETRIEVAL_QUERY";

    private final ProcessGuideChunkRepository chunkRepository;
    private final AiEmbeddingClient embeddingClient;
    private final String embeddingModel;
    private final int embeddingDimension;
    private final int resultLimit;

    public ProcessGuideRetrievalService(
            ProcessGuideChunkRepository chunkRepository,
            AiEmbeddingClient embeddingClient,
            @Value("${app.ai.embedding-model}") String embeddingModel,
            @Value("${app.ai.embedding-dimension}") int embeddingDimension,
            @Value("${app.ai.retrieval-limit}") int resultLimit) {

        this.chunkRepository = chunkRepository;
        this.embeddingClient = embeddingClient;
        this.embeddingModel = embeddingModel;
        this.embeddingDimension = embeddingDimension;
        this.resultLimit = resultLimit;
    }

    @Transactional
    public List<ProcessGuideChunk> retrieve(
            UUID processId,
            String query) {

        List<ProcessGuideChunk> chunksToIndex = chunkRepository
                .findNeedingEmbedding(
                        processId,
                        LANGUAGE,
                        embeddingModel);

        if (!chunksToIndex.isEmpty()) {
            AiEmbeddingResponse response = embeddingClient.embed(
                    chunksToIndex.stream()
                            .map(ProcessGuideChunk::content)
                            .toList(),
                    DOCUMENT_TASK);

            validateResponse(response, chunksToIndex.size());

            for (int index = 0; index < chunksToIndex.size(); index++) {
                chunkRepository.updateEmbedding(
                        chunksToIndex.get(index).id(),
                        toVector(response.embeddings().get(index)),
                        embeddingModel);
            }
        }

        if (chunksToIndex.isEmpty()
                && !chunkRepository.hasIndexedChunks(
                        processId,
                        LANGUAGE,
                        embeddingModel)) {
            return List.of();
        }

        AiEmbeddingResponse queryResponse = embeddingClient.embed(
                List.of(query),
                QUERY_TASK);
        validateResponse(queryResponse, 1);

        return chunkRepository.findNearest(
                processId,
                LANGUAGE,
                embeddingModel,
                toVector(queryResponse.embeddings().getFirst()),
                resultLimit);
    }

    private void validateResponse(
            AiEmbeddingResponse response,
            int expectedCount) {

        if (!embeddingModel.equals(response.model())
                || response.dimension() != embeddingDimension
                || response.embeddings() == null
                || response.embeddings().size() != expectedCount
                || response.embeddings().stream()
                        .anyMatch(vector -> vector == null
                                || vector.size() != embeddingDimension
                                || vector.stream().anyMatch(
                                        value -> value == null
                                                || !Double.isFinite(value)))) {
            throw new AiServiceUnavailableException(
                    "AI embedding service returned an invalid response");
        }
    }

    private String toVector(List<Double> values) {
        return values.stream()
                .map(String::valueOf)
                .collect(java.util.stream.Collectors.joining(",", "[", "]"));
    }

}

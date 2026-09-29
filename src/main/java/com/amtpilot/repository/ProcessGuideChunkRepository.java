package com.amtpilot.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import com.amtpilot.process.dto.ProcessGuideChunk;

@Repository
public class ProcessGuideChunkRepository {

    private final JdbcClient jdbcClient;

    public ProcessGuideChunkRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public List<ProcessGuideChunk> findNeedingEmbedding(
            UUID processId,
            String language,
            String embeddingModel) {

        return jdbcClient.sql("""
                SELECT id, section, content, source_title,
                       source_url, verified_at
                FROM process_guide_chunk
                WHERE process_id = :processId
                  AND language = :language
                  AND (embedding IS NULL
                       OR embedding_model <> :embeddingModel)
                ORDER BY section, chunk_index
                """)
                .param("processId", processId)
                .param("language", language)
                .param("embeddingModel", embeddingModel)
                .query(ProcessGuideChunk.class)
                .list();
    }

    public void updateEmbedding(
            UUID chunkId,
            String embedding,
            String embeddingModel) {

        jdbcClient.sql("""
                UPDATE process_guide_chunk
                SET embedding = CAST(:embedding AS vector),
                    embedding_model = :embeddingModel,
                    updated_at = CURRENT_TIMESTAMP
                WHERE id = :chunkId
                """)
                .param("embedding", embedding)
                .param("embeddingModel", embeddingModel)
                .param("chunkId", chunkId)
                .update();
    }

    public boolean hasIndexedChunks(
            UUID processId,
            String language,
            String embeddingModel) {

        Integer count = jdbcClient.sql("""
                SELECT COUNT(*)
                FROM process_guide_chunk
                WHERE process_id = :processId
                  AND language = :language
                  AND embedding_model = :embeddingModel
                  AND embedding IS NOT NULL
                """)
                .param("processId", processId)
                .param("language", language)
                .param("embeddingModel", embeddingModel)
                .query(Integer.class)
                .single();

        return count > 0;
    }

    public List<ProcessGuideChunk> findNearest(
            UUID processId,
            String language,
            String embeddingModel,
            String queryEmbedding,
            int limit) {

        return jdbcClient.sql("""
                SELECT id, section, content, source_title,
                       source_url, verified_at
                FROM process_guide_chunk
                WHERE process_id = :processId
                  AND language = :language
                  AND embedding_model = :embeddingModel
                  AND embedding IS NOT NULL
                ORDER BY embedding <=> CAST(:queryEmbedding AS vector)
                LIMIT :limit
                """)
                .param("processId", processId)
                .param("language", language)
                .param("embeddingModel", embeddingModel)
                .param("queryEmbedding", queryEmbedding)
                .param("limit", limit)
                .query(ProcessGuideChunk.class)
                .list();
    }
}

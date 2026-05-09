package com.iamportfolio.ai.rag;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RagDocumentRepository extends JpaRepository<RagDocument, Long> {

    Optional<RagDocument> findFirstBySourcePathAndSourceHash(String sourcePath, String sourceHash);

    void deleteBySourcePath(String sourcePath);

    /**
     * Top-K full-text retrieval using Postgres tsvector + ts_rank_cd.
     * to_tsquery('english', :query) is parsed by Postgres so callers
     * pass already-OR'd terms (the loader builds them from the user prompt).
     */
    @Query(value = """
            SELECT id, source_path, source_hash, chunk_index, content,
                   ts_rank_cd(search_vector, to_tsquery('english', :query)) AS rank
            FROM rag_documents
            WHERE search_vector @@ to_tsquery('english', :query)
            ORDER BY rank DESC
            LIMIT :limit
            """, nativeQuery = true)
    List<Object[]> searchTopK(@Param("query") String tsQuery, @Param("limit") int limit);
}

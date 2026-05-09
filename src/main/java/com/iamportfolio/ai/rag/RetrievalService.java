package com.iamportfolio.ai.rag;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Top-K retrieval over the RAG corpus using Postgres full-text search.
 * Returned snippets are passed verbatim to the assistant as context;
 * source_path is preserved so the answer can cite where each fact came from.
 */
@Service
public class RetrievalService {

    @Autowired
    private RagDocumentRepository repository;

    public List<RetrievedChunk> retrieve(String userQuery, int k) {
        if (userQuery == null || userQuery.isBlank()) return List.of();
        String tsQuery = toTsQuery(userQuery);
        if (tsQuery.isBlank()) return List.of();

        List<Object[]> rows = repository.searchTopK(tsQuery, k);
        return rows.stream()
                .map(r -> new RetrievedChunk(
                        ((Number) r[0]).longValue(),
                        (String) r[1],
                        ((Number) r[3]).intValue(),
                        (String) r[4],
                        ((Number) r[5]).doubleValue()
                ))
                .toList();
    }

    /**
     * Turns a free-form question into a Postgres tsquery: "What is SoD?"
     * -> "what | sod" (stripping stopwords is delegated to to_tsquery).
     * Keeps things permissive (OR) so partial matches still surface.
     */
    static String toTsQuery(String input) {
        return Arrays.stream(input.toLowerCase().split("[^\\p{L}\\p{N}]+"))
                .filter(s -> s.length() >= 2)
                .map(s -> s.replace(":", "").replace("&", "").replace("|", ""))
                .filter(s -> !s.isBlank())
                .collect(Collectors.joining(" | "));
    }

    public record RetrievedChunk(Long id, String sourcePath, int chunkIndex, String content, double rank) {}
}

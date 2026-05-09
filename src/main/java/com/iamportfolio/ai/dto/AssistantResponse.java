package com.iamportfolio.ai.dto;

import java.util.List;

public record AssistantResponse(
        String answer,
        List<SourceCitation> sources,
        String model,
        Usage usage
) {
    public record SourceCitation(String sourcePath, int chunkIndex, double rank) {}

    /** Mirrors AnthropicClient.Usage so the UI can show cache effectiveness. */
    public record Usage(
            Integer inputTokens,
            Integer outputTokens,
            Integer cacheCreationInputTokens,
            Integer cacheReadInputTokens
    ) {}
}

package com.iamportfolio.ai.service;

import com.iamportfolio.ai.client.AnthropicClient;
import com.iamportfolio.ai.dto.AssistantResponse;
import com.iamportfolio.ai.rag.RetrievalService;
import com.iamportfolio.ai.rag.RetrievalService.RetrievedChunk;
import com.iamportfolio.common.audit.Auditable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Orchestrates retrieval + Claude. The system prompt is marked
 * cacheable so repeated questions over the same corpus benefit from
 * Anthropic's prompt caching (visible as cache_read_input_tokens in the
 * response Usage).
 */
@Service
public class AssistantService {

    private static final int TOP_K = 5;

    private static final String SYSTEM_TEMPLATE = """
            You are the IAM Portfolio assistant for an Identity & Access
            Management platform. Answer the user's question using ONLY the
            internal documents in CONTEXT below. If the answer isn't in the
            context, say "I don't know based on the internal docs" — do not
            improvise.

            When you cite a fact, append (source: <filename>) inline. Keep
            answers concise (5 sentences max unless the user asks for more).

            CONTEXT:
            %s
            """;

    @Autowired
    private RetrievalService retrieval;

    @Autowired
    private AnthropicClient anthropic;

    @Auditable(value = "AI_ASSISTANT_QUERY", resourceType = "AI")
    public AssistantResponse ask(String question) {
        List<RetrievedChunk> chunks = retrieval.retrieve(question, TOP_K);
        String context = chunks.isEmpty()
                ? "(no internal documents matched the question)"
                : chunks.stream()
                    .map(c -> "--- " + c.sourcePath() + " (chunk " + c.chunkIndex() + ") ---\n" + c.content())
                    .collect(Collectors.joining("\n\n"));

        String system = SYSTEM_TEMPLATE.formatted(context);
        AnthropicClient.AnthropicResponse claude = anthropic.complete(question, system, true);

        List<AssistantResponse.SourceCitation> citations = chunks.stream()
                .map(c -> new AssistantResponse.SourceCitation(c.sourcePath(), c.chunkIndex(), c.rank()))
                .toList();

        AssistantResponse.Usage usage = claude.usage() == null ? null
                : new AssistantResponse.Usage(
                        claude.usage().input_tokens(),
                        claude.usage().output_tokens(),
                        claude.usage().cache_creation_input_tokens(),
                        claude.usage().cache_read_input_tokens()
                );

        return new AssistantResponse(claude.text(), citations, claude.model(), usage);
    }
}

package com.iamportfolio.ai.client;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import jakarta.annotation.PostConstruct;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Thin client around Anthropic's Messages API.
 * <p>
 * Why hand-rolled instead of the official SDK: we want fine control over
 * <b>prompt caching</b> headers + cache_control breakpoints (cuts the cost
 * of repeated RAG context dramatically) and we already have WebClient on
 * the classpath for SCIM outbound. The official SDK would add another
 * dependency without giving us anything we don't already have.
 *
 * <p>Anthropic-Version pinned to the dated header recommended in the
 * docs; bump it explicitly when adopting a new model family.
 */
@Component
public class AnthropicClient {

    private static final Logger logger = LoggerFactory.getLogger(AnthropicClient.class);
    private static final String API_VERSION = "2023-06-01";

    @Value("${app.ai.anthropic.api-key:}")
    private String apiKey;

    @Value("${app.ai.anthropic.base-url:https://api.anthropic.com}")
    private String baseUrl;

    @Value("${app.ai.anthropic.model:claude-opus-4-5}")
    private String model;

    @Value("${app.ai.anthropic.max-tokens:1024}")
    private int maxTokens;

    private WebClient webClient;

    @PostConstruct
    void init() {
        if (apiKey == null || apiKey.isBlank()) {
            logger.warn("ANTHROPIC_API_KEY is not set — /api/ai/assistant calls will fail. " +
                    "Set ANTHROPIC_API_KEY in .env to enable the assistant.");
        }
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("x-api-key", apiKey == null ? "" : apiKey)
                .defaultHeader("anthropic-version", API_VERSION)
                .defaultHeader("content-type", "application/json")
                .build();
        logger.info("AnthropicClient ready (model={}, base={})", model, baseUrl);
    }

    /**
     * Send a Messages call with a system prompt that can be marked
     * cacheable. Pass {@code cacheableSystem=true} when the system text
     * is large + reused (e.g., a RAG corpus prefix) so subsequent calls
     * can hit the prompt cache.
     */
    public AnthropicResponse complete(String userPrompt, String systemPrompt, boolean cacheableSystem) {
        Map<String, Object> body = new HashMap<>();
        body.put("model", model);
        body.put("max_tokens", maxTokens);

        if (systemPrompt != null && !systemPrompt.isBlank()) {
            if (cacheableSystem) {
                Map<String, Object> sysBlock = new HashMap<>();
                sysBlock.put("type", "text");
                sysBlock.put("text", systemPrompt);
                sysBlock.put("cache_control", Map.of("type", "ephemeral"));
                body.put("system", List.of(sysBlock));
            } else {
                body.put("system", systemPrompt);
            }
        }
        body.put("messages", List.of(Map.of(
                "role", "user",
                "content", userPrompt
        )));

        try {
            return webClient.post()
                    .uri("/v1/messages")
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(AnthropicResponse.class)
                    .block(Duration.ofSeconds(60));
        } catch (Exception e) {
            throw new AnthropicException("Anthropic call failed: " + e.getMessage(), e);
        }
    }

    public String getModel() {
        return model;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record AnthropicResponse(
            String id,
            String model,
            String role,
            String stop_reason,
            List<ContentBlock> content,
            Usage usage
    ) {
        public String text() {
            if (content == null || content.isEmpty()) return "";
            return content.stream()
                    .filter(c -> "text".equals(c.type()))
                    .map(ContentBlock::text)
                    .reduce("", String::concat);
        }
    }

    public record ContentBlock(String type, String text) {}

    public record Usage(
            Integer input_tokens,
            Integer output_tokens,
            Integer cache_creation_input_tokens,
            Integer cache_read_input_tokens
    ) {}
}

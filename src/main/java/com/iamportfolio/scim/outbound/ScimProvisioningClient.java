package com.iamportfolio.scim.outbound;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import jakarta.annotation.PostConstruct;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Calls the external SCIM connector (the Python FastAPI service in
 * scim-connector/) when lifecycle events fire on the IdP side.
 * <p>
 * Retries with exponential backoff: at most 3 attempts, 500ms initial
 * delay, multiplier 2.0. Failures bubble up so the orchestrator can
 * record a FAILURE provisioning_event for replay.
 */
@Component
public class ScimProvisioningClient {

    private static final Logger logger = LoggerFactory.getLogger(ScimProvisioningClient.class);

    @Value("${app.connector.base-url:http://localhost:9000}")
    private String connectorBaseUrl;

    private WebClient webClient;

    @PostConstruct
    void init() {
        this.webClient = WebClient.builder()
                .baseUrl(connectorBaseUrl)
                .build();
        logger.info("ScimProvisioningClient targeting {}", connectorBaseUrl);
    }

    @Retryable(
            retryFor = { WebClientResponseException.class, RuntimeException.class },
            maxAttempts = 3,
            backoff = @Backoff(delay = 500, multiplier = 2.0)
    )
    public void provision(String externalId, String userName, String email, boolean active) {
        Map<String, Object> body = new HashMap<>();
        body.put("userName", userName);
        body.put("email", email);
        body.put("active", active);
        body.put("externalId", externalId);

        webClient.post()
                .uri("/connector/users/{id}/sync", externalId)
                .bodyValue(body)
                .retrieve()
                .toBodilessEntity()
                .block(Duration.ofSeconds(10));
    }

    @Retryable(
            retryFor = { WebClientResponseException.class, RuntimeException.class },
            maxAttempts = 3,
            backoff = @Backoff(delay = 500, multiplier = 2.0)
    )
    public void deprovision(String externalId) {
        webClient.delete()
                .uri("/connector/users/{id}", externalId)
                .retrieve()
                .toBodilessEntity()
                .block(Duration.ofSeconds(10));
    }
}

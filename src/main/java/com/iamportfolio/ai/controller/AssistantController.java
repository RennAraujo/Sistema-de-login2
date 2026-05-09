package com.iamportfolio.ai.controller;

import com.iamportfolio.ai.client.AnthropicException;
import com.iamportfolio.ai.dto.AssistantRequest;
import com.iamportfolio.ai.dto.AssistantResponse;
import com.iamportfolio.ai.service.AssistantService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Map;

/**
 * IAM Assistant — RAG over internal IAM policies / runbooks, answered
 * by Claude. Authenticated callers only (any role); the system prompt
 * forces grounded answers + citations.
 */
@RestController
@RequestMapping("/api/ai/assistant")
public class AssistantController {

    @Autowired
    private AssistantService service;

    @PostMapping
    public AssistantResponse ask(@Valid @RequestBody AssistantRequest request) {
        return service.ask(request.question());
    }

    @ExceptionHandler(AnthropicException.class)
    public ResponseEntity<Map<String, String>> upstreamFailure(AnthropicException e) {
        return ResponseEntity.status(502).body(Map.of(
                "error", "Assistant unavailable",
                "detail", e.getMessage()
        ));
    }
}

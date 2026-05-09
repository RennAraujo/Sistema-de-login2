package com.iamportfolio.oauth2.controller;

import com.iamportfolio.common.audit.Auditable;
import com.iamportfolio.oauth2.dto.OAuth2ClientCreateRequest;
import com.iamportfolio.oauth2.dto.OAuth2ClientResponse;
import com.iamportfolio.oauth2.service.OAuth2ClientAdminService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * OAuth2 client (relying party) registration and inspection.
 * Gated by oauth2:client:manage / ROLE_ADMIN in SecurityConfig.
 *
 * The plain client_secret is returned ONCE in the POST response —
 * subsequent GETs hide it because what's stored is the bcrypt hash.
 */
@RestController
@RequestMapping("/api/oauth2/clients")
public class OAuth2ClientAdminController {

    @Autowired
    private OAuth2ClientAdminService service;

    @PostMapping
    @Auditable(value = "OAUTH2_CLIENT_CREATE", resourceType = "OAUTH2_CLIENT")
    public OAuth2ClientResponse create(@Valid @RequestBody OAuth2ClientCreateRequest request) {
        return service.register(request);
    }

    @GetMapping("/{id}")
    public OAuth2ClientResponse getById(@PathVariable String id) {
        return service.get(id);
    }

    @GetMapping("/by-client-id/{clientId}")
    public OAuth2ClientResponse getByClientId(@PathVariable String clientId) {
        return service.getByClientId(clientId);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> notFound(IllegalArgumentException e) {
        return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> conflict(IllegalStateException e) {
        return ResponseEntity.status(409).body(Map.of("error", e.getMessage()));
    }
}

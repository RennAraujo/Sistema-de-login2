package com.iamportfolio.oauth2.dto;

import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;

import java.time.Instant;
import java.util.Set;
import java.util.stream.Collectors;

public record OAuth2ClientResponse(
        String id,
        String clientId,
        String clientName,
        Set<String> grantTypes,
        Set<String> scopes,
        Set<String> redirectUris,
        Instant clientIdIssuedAt,
        String clientSecret
) {
    /**
     * @param plainSecret null on read endpoints, populated only on the create
     *                    response so the caller can copy it once.
     */
    public static OAuth2ClientResponse from(RegisteredClient c, String plainSecret) {
        return new OAuth2ClientResponse(
                c.getId(),
                c.getClientId(),
                c.getClientName(),
                c.getAuthorizationGrantTypes().stream()
                        .map(AuthorizationGrantType::getValue).collect(Collectors.toSet()),
                c.getScopes(),
                c.getRedirectUris(),
                c.getClientIdIssuedAt(),
                plainSecret
        );
    }
}

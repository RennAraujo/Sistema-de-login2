package com.iamportfolio.oauth2.service;

import com.iamportfolio.oauth2.dto.OAuth2ClientCreateRequest;
import com.iamportfolio.oauth2.dto.OAuth2ClientResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

/**
 * Wraps RegisteredClientRepository so the REST admin layer can stay free of
 * Spring Authorization Server internals (grant-type strings, password
 * encoding, etc).
 */
@Service
public class OAuth2ClientAdminService {

    private static final PasswordEncoder ENCODER = PasswordEncoderFactories.createDelegatingPasswordEncoder();

    @Autowired
    private RegisteredClientRepository clients;

    public OAuth2ClientResponse register(OAuth2ClientCreateRequest req) {
        if (clients.findByClientId(req.clientId()) != null) {
            throw new IllegalStateException("Client already exists: " + req.clientId());
        }

        String plainSecret = UUID.randomUUID().toString().replace("-", "");
        RegisteredClient.Builder builder = RegisteredClient.withId(UUID.randomUUID().toString())
                .clientId(req.clientId())
                .clientSecret(ENCODER.encode(plainSecret))
                .clientName(req.clientName())
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
                .clientSettings(ClientSettings.builder().requireAuthorizationConsent(req.requireConsent()).build())
                .tokenSettings(TokenSettings.builder()
                        .accessTokenTimeToLive(Duration.ofMinutes(15))
                        .refreshTokenTimeToLive(Duration.ofDays(30))
                        .build());

        req.grantTypes().forEach(g -> builder.authorizationGrantType(new AuthorizationGrantType(g)));
        req.scopes().forEach(builder::scope);

        // OIDC clients should always carry the openid scope; ensure it.
        if (req.scopes().stream().noneMatch(OidcScopes.OPENID::equals)) {
            builder.scope(OidcScopes.OPENID);
        }
        if (req.redirectUris() != null) {
            req.redirectUris().forEach(builder::redirectUri);
        }

        RegisteredClient client = builder.build();
        clients.save(client);
        return OAuth2ClientResponse.from(client, plainSecret);
    }

    public OAuth2ClientResponse get(String id) {
        RegisteredClient c = clients.findById(id);
        if (c == null) throw new IllegalArgumentException("Client not found: " + id);
        return OAuth2ClientResponse.from(c, null);
    }

    public OAuth2ClientResponse getByClientId(String clientId) {
        RegisteredClient c = clients.findByClientId(clientId);
        if (c == null) throw new IllegalArgumentException("Client not found: " + clientId);
        return OAuth2ClientResponse.from(c, null);
    }
}

-- V7: Seed demo OAuth2 client used by Swagger / quickstart guides.
--
-- Secret is stored as {noop} for the demo so the README can show a literal
-- value ("demo-secret"). For any real client created via /api/oauth2/clients
-- the OAuth2ClientAdminService bcrypts the secret and returns the plain
-- value once in the POST response.
--
-- Settings JSON columns mirror what RegisteredClient.builder().build()
-- serialises so JdbcRegisteredClientRepository can read them back.

INSERT INTO oauth2_registered_client (
    id, client_id, client_id_issued_at,
    client_secret, client_secret_expires_at, client_name,
    client_authentication_methods, authorization_grant_types,
    redirect_uris, post_logout_redirect_uris,
    scopes, client_settings, token_settings
) VALUES (
    'demo-client-uuid-0000-0000-000000000001',
    'demo-client',
    CURRENT_TIMESTAMP,
    '{noop}demo-secret',
    NULL,
    'IAM Portfolio Demo Client',
    'client_secret_basic,client_secret_post',
    'authorization_code,refresh_token,client_credentials',
    'http://localhost:8080/login/oauth2/code/demo,http://127.0.0.1:8080/login/oauth2/code/demo',
    NULL,
    'openid,profile,email,scim:provision',
    '{"@class":"java.util.Collections$UnmodifiableMap","settings.client.require-proof-key":false,"settings.client.require-authorization-consent":true}',
    '{"@class":"java.util.Collections$UnmodifiableMap","settings.token.reuse-refresh-tokens":true,"settings.token.id-token-signature-algorithm":["org.springframework.security.oauth2.jose.jws.SignatureAlgorithm","RS256"],"settings.token.access-token-time-to-live":["java.time.Duration",900.000000000],"settings.token.access-token-format":{"@class":"org.springframework.security.oauth2.server.authorization.settings.OAuth2TokenFormat","value":"self-contained"},"settings.token.refresh-token-time-to-live":["java.time.Duration",2592000.000000000],"settings.token.authorization-code-time-to-live":["java.time.Duration",300.000000000],"settings.token.device-code-time-to-live":["java.time.Duration",300.000000000]}'
);

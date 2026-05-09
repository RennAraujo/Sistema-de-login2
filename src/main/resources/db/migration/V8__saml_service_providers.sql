-- V8: SAML 2.0 Service Provider registry.
-- Each row is a relying party (SP) the IdP will issue signed assertions
-- for. signing_cert_pem is the SP's public X.509 cert used to verify
-- AuthnRequest signatures (when present).

CREATE TABLE saml_service_providers (
    id                 BIGSERIAL PRIMARY KEY,
    entity_id          VARCHAR(512) NOT NULL UNIQUE,
    name               VARCHAR(200) NOT NULL,
    acs_url            VARCHAR(2048) NOT NULL,
    name_id_format     VARCHAR(255),
    signing_cert_pem   TEXT,
    enabled            BOOLEAN NOT NULL DEFAULT TRUE,
    created_at         TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at         TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_saml_sp_entity_id ON saml_service_providers (entity_id);

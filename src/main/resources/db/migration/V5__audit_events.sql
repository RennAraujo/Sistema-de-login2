-- V5: Immutable audit log.
-- Append-only by convention (no UPDATE/DELETE in code; review/forensic only).
-- jsonb details lets each action attach its own structured payload without
-- schema changes; correlation_id ties events together across a single request.

CREATE TABLE audit_events (
    id              BIGSERIAL    PRIMARY KEY,
    timestamp       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actor           VARCHAR(255),
    actor_ip        VARCHAR(64),
    action          VARCHAR(128) NOT NULL,
    resource_type   VARCHAR(64),
    resource_id     VARCHAR(128),
    outcome         VARCHAR(16)  NOT NULL,
    details         JSONB,
    correlation_id  VARCHAR(64)
);

CREATE INDEX idx_audit_timestamp       ON audit_events (timestamp DESC);
CREATE INDEX idx_audit_actor           ON audit_events (actor);
CREATE INDEX idx_audit_action          ON audit_events (action);
CREATE INDEX idx_audit_correlation     ON audit_events (correlation_id);

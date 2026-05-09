-- V9: outbound provisioning audit trail.
-- Each outbound call to a downstream connector lands here so we can:
--   * replay failed events
--   * answer "what did we provision and when" questions
--   * inspect the request/response payloads after the fact

CREATE TABLE provisioning_events (
    id               BIGSERIAL    PRIMARY KEY,
    user_id          BIGINT       REFERENCES users (id) ON DELETE SET NULL,
    external_id      VARCHAR(128),
    operation        VARCHAR(32)  NOT NULL,    -- CREATE | UPDATE | DELETE
    target           VARCHAR(128) NOT NULL,    -- connector identifier (e.g. "scim-connector")
    outcome          VARCHAR(16)  NOT NULL,    -- SUCCESS | FAILURE
    http_status      INTEGER,
    error_message    TEXT,
    attempted_at     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    correlation_id   VARCHAR(64)
);

CREATE INDEX idx_provisioning_events_user_id     ON provisioning_events (user_id);
CREATE INDEX idx_provisioning_events_outcome     ON provisioning_events (outcome);
CREATE INDEX idx_provisioning_events_attempted   ON provisioning_events (attempted_at DESC);

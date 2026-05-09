-- V11: Governance — access reviews + Separation of Duties (SoD).
--
-- access_reviews are quarterly campaigns; access_review_items track each
-- role assignment that needs sign-off.
-- sod_rules pin pairs of roles that must NOT coexist on the same user.
-- sod_violations records the offending assignments detected by the
-- scheduled checker (one row per (user, rule) violation per detection).

CREATE TABLE access_reviews (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(200) NOT NULL,
    description TEXT,
    status      VARCHAR(32)  NOT NULL DEFAULT 'OPEN',  -- OPEN | CLOSED
    started_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    closed_at   TIMESTAMP
);

CREATE TABLE access_review_items (
    id                    BIGSERIAL PRIMARY KEY,
    access_review_id      BIGINT NOT NULL REFERENCES access_reviews (id) ON DELETE CASCADE,
    user_id               BIGINT NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    role_id               BIGINT NOT NULL REFERENCES roles (id) ON DELETE CASCADE,
    decision              VARCHAR(16) NOT NULL DEFAULT 'PENDING', -- PENDING | KEEP | REVOKE
    decided_by            VARCHAR(200),
    decided_at            TIMESTAMP,
    CONSTRAINT uk_review_user_role UNIQUE (access_review_id, user_id, role_id)
);

CREATE TABLE sod_rules (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(200) NOT NULL UNIQUE,
    role_a_id   BIGINT NOT NULL REFERENCES roles (id) ON DELETE CASCADE,
    role_b_id  BIGINT NOT NULL REFERENCES roles (id) ON DELETE CASCADE,
    description TEXT,
    enabled     BOOLEAN NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_sod_distinct_roles CHECK (role_a_id <> role_b_id)
);

CREATE TABLE sod_violations (
    id            BIGSERIAL PRIMARY KEY,
    sod_rule_id   BIGINT NOT NULL REFERENCES sod_rules (id) ON DELETE CASCADE,
    user_id       BIGINT NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    detected_at   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    resolved_at   TIMESTAMP
);

CREATE INDEX idx_review_items_pending ON access_review_items (access_review_id) WHERE decision = 'PENDING';
CREATE INDEX idx_sod_violations_open  ON sod_violations (resolved_at) WHERE resolved_at IS NULL;

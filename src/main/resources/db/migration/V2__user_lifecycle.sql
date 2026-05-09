-- V2: Identity lifecycle (Joiner-Mover-Leaver) and HR-style attributes.
--
-- Adds the columns the LifecycleService and SCIM mapper will use.
-- Existing users (none expected at this point) get ACTIVE so login keeps working.

ALTER TABLE users
    ADD COLUMN lifecycle_state    VARCHAR(32)  NOT NULL DEFAULT 'ACTIVE',
    ADD COLUMN hire_date          DATE,
    ADD COLUMN termination_date   DATE,
    ADD COLUMN department         VARCHAR(100),
    ADD COLUMN external_id        VARCHAR(128) UNIQUE,
    ADD COLUMN manager_id         BIGINT;

ALTER TABLE users
    ADD CONSTRAINT fk_users_manager
        FOREIGN KEY (manager_id) REFERENCES users (id) ON DELETE SET NULL;

CREATE INDEX idx_users_lifecycle_state ON users (lifecycle_state);
CREATE INDEX idx_users_external_id     ON users (external_id);
CREATE INDEX idx_users_manager_id      ON users (manager_id);

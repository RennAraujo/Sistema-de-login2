-- V3: Role-Based Access Control schema.
--
-- Permissions are atomic capabilities (e.g. "identity:write").
-- Roles bundle permissions and act as Spring Security authorities (their
--   name is what shows up as ROLE_* in the security context).
-- Groups give a user a bundle of roles (departmental / team-based grants).
-- RoleAssignment is the direct user-role link with audit metadata
--   (assigned_by, expires_at) — group-mediated grants live in group_roles.

CREATE TABLE permissions (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(128) NOT NULL UNIQUE,
    description VARCHAR(255)
);

CREATE TABLE roles (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(64) NOT NULL UNIQUE,
    description VARCHAR(255)
);

CREATE TABLE role_permissions (
    role_id       BIGINT NOT NULL REFERENCES roles (id)       ON DELETE CASCADE,
    permission_id BIGINT NOT NULL REFERENCES permissions (id) ON DELETE CASCADE,
    PRIMARY KEY (role_id, permission_id)
);

CREATE TABLE groups (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255)
);

CREATE TABLE group_roles (
    group_id BIGINT NOT NULL REFERENCES groups (id) ON DELETE CASCADE,
    role_id  BIGINT NOT NULL REFERENCES roles (id)  ON DELETE CASCADE,
    PRIMARY KEY (group_id, role_id)
);

CREATE TABLE user_groups (
    user_id  BIGINT NOT NULL REFERENCES users (id)  ON DELETE CASCADE,
    group_id BIGINT NOT NULL REFERENCES groups (id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, group_id)
);

CREATE TABLE role_assignments (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT      NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    role_id     BIGINT      NOT NULL REFERENCES roles (id) ON DELETE CASCADE,
    assigned_at TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    assigned_by VARCHAR(100),
    expires_at  TIMESTAMP,
    CONSTRAINT uk_role_assignment UNIQUE (user_id, role_id)
);

CREATE INDEX idx_role_assignments_user      ON role_assignments (user_id);
CREATE INDEX idx_role_assignments_role      ON role_assignments (role_id);
CREATE INDEX idx_role_assignments_expires   ON role_assignments (expires_at);

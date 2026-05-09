-- V1: Baseline schema for the iam-portfolio refactor.
-- Mirrors the original JPA-managed users table so existing AuthService and
-- TwoFactorService keep working unchanged. Future migrations will extend it
-- with lifecycle, rbac and audit tables.
--
-- Nullability matches the JPA entity exactly (com.login.model.User) so that
-- spring.jpa.hibernate.ddl-auto=validate accepts the schema on startup.

CREATE TABLE users (
    id                            BIGSERIAL PRIMARY KEY,
    username                      VARCHAR(50)  NOT NULL UNIQUE,
    email                         VARCHAR(255) NOT NULL UNIQUE,
    password                      VARCHAR(255) NOT NULL,
    first_name                    VARCHAR(100),
    last_name                     VARCHAR(100),
    is_enabled                    BOOLEAN      DEFAULT TRUE,
    is_account_non_expired        BOOLEAN      DEFAULT TRUE,
    is_account_non_locked         BOOLEAN      DEFAULT TRUE,
    is_credentials_non_expired    BOOLEAN      DEFAULT TRUE,
    two_factor_enabled            BOOLEAN      DEFAULT FALSE,
    two_factor_secret             VARCHAR(255),
    backup_codes                  TEXT,
    created_at                    TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    updated_at                    TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    last_login                    TIMESTAMP
);

CREATE INDEX idx_users_email     ON users (email);
CREATE INDEX idx_users_username  ON users (username);

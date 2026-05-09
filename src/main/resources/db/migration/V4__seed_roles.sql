-- V4: Seed the baseline RBAC catalog used by the rest of the platform.
-- These names are referenced by SecurityConfig, the OAuth2 client admin,
-- the SCIM provisioning scope and the audit endpoints.

INSERT INTO permissions (name, description) VALUES
    ('identity:read',         'Read users, groups and lifecycle state'),
    ('identity:write',        'Create/update users, manage memberships and lifecycle transitions'),
    ('audit:read',            'Read the immutable audit log'),
    ('oauth2:client:manage',  'Register and manage OAuth2 clients'),
    ('saml:sp:manage',        'Register and manage SAML service providers'),
    ('scim:provision',        'Call SCIM 2.0 endpoints to provision identities'),
    ('governance:manage',     'Manage policies, access reviews and SoD rules');

INSERT INTO roles (name, description) VALUES
    ('ROLE_USER',             'Default role for any authenticated end user'),
    ('ROLE_ADMIN',            'Full administrative access (super-user)'),
    ('ROLE_AUDITOR',          'Read-only access to audit log and reports'),
    ('ROLE_IDENTITY_MANAGER', 'Manage identities, groups and lifecycle');

-- ROLE_USER: nothing beyond authentication.
-- ROLE_AUDITOR: audit:read.
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p
WHERE r.name = 'ROLE_AUDITOR' AND p.name = 'audit:read';

-- ROLE_IDENTITY_MANAGER: identity:read/write + scim:provision.
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p
WHERE r.name = 'ROLE_IDENTITY_MANAGER'
  AND p.name IN ('identity:read', 'identity:write', 'scim:provision');

-- ROLE_ADMIN: every permission in the catalog.
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p
WHERE r.name = 'ROLE_ADMIN';

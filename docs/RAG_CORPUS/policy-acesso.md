# Access Policy

The IAM Portfolio platform follows the principle of least privilege:
every user starts with ROLE_USER and earns additional roles only when
business need is documented and approved.

## How access is granted

- Direct grants via /api/rbac/role-assignments require the requester
  to hold ROLE_IDENTITY_MANAGER (or ROLE_ADMIN).
- Group-mediated grants are preferred: assign the user to a group
  (e.g. group_engineering) and let the group's role membership
  propagate. This keeps audit reasoning simple ("user X has role Y
  because they're in group Z").
- Time-boxed access uses the expires_at field on role_assignments;
  the assignment automatically stops counting after that timestamp,
  no nightly job required.

## Required reviews

- Every quarter, all role assignments older than 90 days must be
  re-confirmed by the user's manager via the access reviews dashboard
  at /api/governance/access-reviews. Unconfirmed assignments are
  revoked automatically at the end of the review window.
- Privileged roles (ROLE_ADMIN, ROLE_IDENTITY_MANAGER) trigger a
  monthly review instead of quarterly.

## Emergency access

The break-glass account `emergency-admin` is normally OFFBOARDED.
Activating it requires two ROLE_ADMIN approvals and notifies the
security team via the audit pipeline. Every action taken under this
account is flagged in audit_events with actor=emergency-admin so
post-incident review can attribute every step.

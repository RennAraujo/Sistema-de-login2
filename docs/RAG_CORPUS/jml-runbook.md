# Joiner-Mover-Leaver Runbook

The IAM Portfolio platform tracks every identity through five lifecycle
states: PENDING_APPROVAL, ACTIVE, SUSPENDED, OFFBOARDED, DELETED.
Transitions are managed by LifecycleService and trigger downstream
SCIM provisioning automatically.

## Joiner (new hire)

1. HR posts a user-create request to /api/identity/users via the SCIM
   2.0 endpoint /scim/v2/Users.
2. The user starts in PENDING_APPROVAL when the
   app.identity.require-approval flag is true; otherwise ACTIVE.
3. An identity manager calls
   POST /api/identity/users/{id}/approve to move the user to ACTIVE.
4. ProvisioningOrchestrator publishes the change to the SCIM connector,
   which creates the user in every downstream app subscribed to the
   IdP.

## Mover (role change)

1. POST /api/rbac/role-assignments with the new role and an optional
   expires_at for time-boxed access.
2. Effective authorities are recomputed at the user's next login;
   tokens issued before the change keep their old authorities until
   they expire.
3. SoD rules are re-evaluated; if the new role triggers a violation
   the assignment is rolled back and an audit_event is recorded.

## Leaver (offboarding)

1. POST /api/identity/users/{id}/offboard.
2. lifecycle_state becomes OFFBOARDED, termination_date is stamped
   automatically, and active sessions are revoked.
3. ProvisioningOrchestrator issues DELETE on the SCIM connector,
   which removes the user downstream.
4. After the GDPR retention period (180 days), the row is moved to
   DELETED and any PII fields are nulled.

## Re-hire

A previously OFFBOARDED user may be reactivated only by recreating
the account; the old row stays archived for audit. The new row
inherits the previous external_id so downstream apps can correlate
the two if they need to.

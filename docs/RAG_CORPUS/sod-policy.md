# Separation of Duties (SoD) Policy

To prevent collusion, fraud, and accidental privilege escalation, the
following role pairs are mutually exclusive — no single user may hold
both roles at the same time:

- ROLE_ADMIN ⊥ ROLE_AUDITOR  
  Auditors cannot also be administrators. Allowing it would let one
  person both grant access and review their own grants.

- ROLE_IDENTITY_MANAGER ⊥ ROLE_AUDITOR  
  Identity managers create users, groups, and role assignments; the
  auditor reviews those events. Combining them defeats the purpose.

- ROLE_DEVELOPER ⊥ ROLE_DEPLOYER  
  (Reserved for the deployment pipeline.) The person who writes the
  code cannot be the same person who promotes it to production
  without a peer review override.

The SodCheckJob runs every 30 minutes and emits an audit_event with
action=SOD_VIOLATION_DETECTED for any account holding an exclusive
pair. Violations must be remediated within 7 calendar days; after that,
the account is automatically suspended pending manager review.

To request a temporary exception (rare), open a governance ticket
referencing the business justification; the exception is recorded in
sod_exceptions with an expiry date.

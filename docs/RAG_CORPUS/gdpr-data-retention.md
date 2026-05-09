# GDPR / LGPD Data Retention

Personal data captured by the IAM Portfolio platform is retained for
the minimum period required to fulfil its purpose, in line with GDPR
Art. 5(1)(e) and LGPD Art. 16.

| Data category                      | Retention                       |
| ---------------------------------- | ------------------------------- |
| User profile (name, email, dept)   | While account is ACTIVE         |
| 2FA secrets, backup codes          | While 2FA is enabled            |
| Audit log entries                  | 7 years (SOX compliance)        |
| Provisioning events                | 2 years                         |
| OAuth2 authorizations + refresh    | Until token expiry + 7 days     |
| Password history (for re-use rule) | 5 most recent only              |

After OFFBOARDING, the user row stays for 180 days in case of re-hire,
then PII fields are nulled (username and external_id remain so audit
joins still work). Audit_events survive the user — they reference
actor strings, not foreign keys, by design.

## Subject Access Requests

A data subject can request export or deletion via
POST /api/identity/users/{id}/data-export. The endpoint dumps every
row referencing the user across users, role_assignments,
provisioning_events, audit_events, and oauth2_authorization, and
returns a signed JSON file. Deletion (right to be forgotten) is
fulfilled by hard-deleting the user row and overwriting referenced
audit_events.actor with a hash.

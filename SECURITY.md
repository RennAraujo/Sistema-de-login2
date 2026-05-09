# Security Policy

## Reporting a vulnerability

This is a portfolio project, but reports are still welcome. Open a
**private security advisory** on GitHub
([Security tab](https://github.com/RennAraujo/Sistema-de-login2/security/advisories))
rather than a public issue. Expect a response within 7 days.

Please do not open issues for vulnerabilities that:

- only apply to the in-memory dev keys (JWKS / SAML signing) — they're
  documented as ephemeral and meant to be replaced in production.
- only manifest with `JWT_SECRET` left at the local fallback value.

## Defaults to be aware of

| Default                              | Production action                      |
| ------------------------------------ | -------------------------------------- |
| `JWT_SECRET` dev fallback (local profile) | Set a random value ≥32 chars in env |
| In-memory JWKS RSA key (regenerated) | Bind-mount a JKS keystore               |
| In-memory SAML signing cert + key    | Bind-mount a JKS keystore               |
| Demo OAuth2 client `demo-client / demo-secret` (`{noop}`) | Delete and re-create through `/api/oauth2/clients` so secret is bcrypted |
| `WantAuthnRequestsSigned="false"` in SAML metadata | Flip to true and verify SP signatures |

## Audit + traceability

Every security-relevant action lands in `audit_events` with actor,
source IP, outcome, and a request-scoped correlation_id that's also
present in the structured logs (LogstashEncoder JSON). For incident
review, query both with the same id.

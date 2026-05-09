# Operations Runbook

Quick troubleshooting + recovery for common issues. For day-to-day
quickstart see the [README](../README.md).

## Common issues

### App fails to start: `password authentication failed for user "iam"`

A local PostgreSQL instance (Windows service `postgresql-x64-13` is the
classic culprit) is binding port 5432 and the app is connecting to that
instead of the docker-compose Postgres on 5433.

```bash
# Verify what's on 5432
docker ps --filter publish=5432
# Kill the local service or change POSTGRES_PORT in .env (default 5433)
```

### `mvn spring-boot:run` errors about `JWT_SECRET`

`application-local.yml` provides a dev fallback so this only happens
outside the local profile. To run any other profile from the shell:

```bash
export JWT_SECRET="changeMeToALongRandomStringOfAtLeast32Characters!!"
mvn spring-boot:run -Dspring-boot.run.profiles=docker
```

### Flyway: "Validate failed: Migration checksum mismatch"

A migration file was edited after it was applied. Restore the original
content (git history) or, in development only:

```bash
docker exec iam-portfolio-postgres psql -U iam -d iam_portfolio \
  -c "DELETE FROM flyway_schema_history WHERE version = '<n>';"
# Then drop the offending tables manually and re-run mvn spring-boot:run.
```

### `/oauth2/token` returns `invalid_client` for the demo client

`SecurityConfig.passwordEncoder()` must be a `DelegatingPasswordEncoder`
so the `{noop}` prefix on the seeded client_secret is honored. If the
bean was changed to a raw `BCryptPasswordEncoder`, Spring AS will reject
the literal `{noop}demo-secret` value. Fix: revert to
`PasswordEncoderFactories.createDelegatingPasswordEncoder()`.

### SAML metadata cached by the SP

The IdP's signing key is regenerated every restart in this build.
After a restart the SP's cached IdP metadata still has the *old* cert
and signature verification fails. Re-import the metadata XML, or in
production, mount a long-lived keystore (see [ARCHITECTURE.md](ARCHITECTURE.md)
"Trade-offs").

### IAM Assistant returns 502

`ANTHROPIC_API_KEY` isn't set. The startup log warns about it; set the
key in `.env` and restart the app.

## Reset everything

```bash
docker compose down -v       # WARNING: deletes the Postgres volume
docker compose up --build
```

## Where things live in the DB

```sql
\dt           -- list tables
SELECT version, description FROM flyway_schema_history ORDER BY installed_rank;

-- Lifecycle audit trail for one user
SELECT timestamp, action, outcome, details
FROM audit_events
WHERE actor = 'admin' OR resource_id = '42'
ORDER BY timestamp DESC LIMIT 50;

-- Outbound provisioning replay
SELECT operation, target, outcome, http_status, error_message, attempted_at
FROM provisioning_events ORDER BY attempted_at DESC LIMIT 20;

-- Open SoD violations
SELECT v.id, r.name, u.username, v.detected_at
FROM sod_violations v
JOIN sod_rules r ON r.id = v.sod_rule_id
JOIN users u ON u.id = v.user_id
WHERE v.resolved_at IS NULL;
```

# Contributing

## Branching

`main` is always green. Feature work happens on
`type/<short-slug>` branches (e.g. `feat/saml-encryption`,
`fix/audit-correlation`). PRs target `main` and require CI to pass.

## Conventional Commits

Every commit message follows
[Conventional Commits](https://www.conventionalcommits.org/) in
**English**:

```
type(scope): subject in imperative mood

Optional body explaining the *why*. Hard-wrap at ~72 chars.
```

Allowed scopes (mirror the bounded contexts):

`auth | identity | rbac | oauth2 | saml | scim | audit | ai |
governance | infra | chore | docs | test | ci | frontend`

Examples:

```
feat(scim): expose scim 2.0 server endpoints (Users, Groups)
fix(auth): block login when lifecycle_state != ACTIVE
docs: rewrite README with job-spec mapping table
chore(ci): pin actions/setup-java to v4
```

## Local development loop

```bash
docker compose up -d postgres
mvn spring-boot:run
# (separate shell) cd scim-connector && uvicorn app.main:app --reload
```

Hot-reload tip: pair `spring-boot-devtools` with IntelliJ "Build project
automatically" + `Ctrl+Shift+F9` for a quick incremental restart.

## Tests

- Unit tests live next to the code they exercise.
- Integration tests extend `AbstractPostgresIT` and pull a real Postgres
  via Testcontainers.
- The Python connector has its own pytest suite under
  `scim-connector/tests`.

`mvn verify` runs both Java suites locally; the GitHub Actions workflow
runs all three jobs (Java, Python, docker-build) on every PR.

## Adding a new context

1. Create `com.iamportfolio.<context>.{model,repository,service,controller,dto}`.
2. If you need new tables: drop a `V{n}__<snake_case>.sql` migration —
   never edit a previously-applied one.
3. If the new endpoint is sensitive, gate it in
   `SecurityConfig` with the appropriate authority and consider adding
   `@Auditable` to the service method.
4. Add an integration test extending `AbstractPostgresIT`.

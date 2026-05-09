# IAM Portfolio

[![CI](https://github.com/RennAraujo/Sistema-de-login2/actions/workflows/ci.yml/badge.svg)](https://github.com/RennAraujo/Sistema-de-login2/actions/workflows/ci.yml)
![Java](https://img.shields.io/badge/Java-17-blue)
![Spring%20Boot](https://img.shields.io/badge/Spring%20Boot-3.2-brightgreen)
![Postgres](https://img.shields.io/badge/PostgreSQL-16-336791)
![Python](https://img.shields.io/badge/Python-3.12-3776AB)

> Production-shaped Identity & Access Management portfolio:
> SSO (OIDC + SAML2), joiner-mover-leaver lifecycle, SCIM 2.0
> provisioning, immutable auditing, separation-of-duties governance,
> and a RAG-powered assistant over internal IAM policies.

## What's inside

```
┌──────────────────── Frontend (vanilla HTML/CSS/JS) ────────────────────┐
│ Login • Custom OAuth2 consent screen • IAM Assistant chat (RAG)        │
└───────────────────────────────┬────────────────────────────────────────┘
                                │ JWT / session
┌───────────────────────────────▼────────────────────────────────────────┐
│  iam-portfolio  (Spring Boot 3.2 / Java 17)                             │
│                                                                         │
│  ┌──────────┬─────────────┬──────────┬────────────┬──────────────────┐  │
│  │  AuthN   │  OAuth2 AS  │  SAML2   │   RBAC     │   Lifecycle      │  │
│  │ JWT/2FA  │   (OIDC)    │   IdP    │ R/P/Group  │  (JML)           │  │
│  ├──────────┼─────────────┼──────────┼────────────┼──────────────────┤  │
│  │  Audit   │  SCIM 2.0   │ Govern.  │ Actuator   │   AI / RAG       │  │
│  │ append-  │ in + out    │  SoD     │ Prometheus │  (Claude)        │  │
│  │  only    │ provision   │ checker  │            │                  │  │
│  └──────────┴─────────────┴──────────┴────────────┴──────────────────┘  │
└───────────┬──────────────────┬──────────────────┬───────────────────────┘
            │ SCIM outbound    │ JDBC             │ HTTPS
┌───────────▼─────────┐  ┌─────▼──────┐    ┌──────▼────────┐
│  scim-connector     │  │ PostgreSQL │    │  Claude API   │
│  (FastAPI / Python) │  │  + Flyway  │    │  (Anthropic)  │
└─────────────────────┘  └────────────┘    └───────────────┘
```

## Job-spec ↔ code map

| Requirement                                            | Where to look                                                        |
| ------------------------------------------------------ | -------------------------------------------------------------------- |
| Java, Spring Boot, escalável                           | All `src/main/java/com/iamportfolio/**`                              |
| **OAuth2 / OIDC**                                      | [oauth2/config/AuthorizationServerConfig](src/main/java/com/iamportfolio/oauth2/config/AuthorizationServerConfig.java), V6/V7 migrations, [/consent](src/main/java/com/iamportfolio/oauth2/controller/ConsentController.java) |
| **SAML 2.0**                                           | [saml/controller/SamlIdpController](src/main/java/com/iamportfolio/saml/controller/SamlIdpController.java), [SamlAssertionBuilder](src/main/java/com/iamportfolio/saml/service/SamlAssertionBuilder.java), [SAML_QUICKSTART](docs/SAML_QUICKSTART.md) |
| **Joiner-Mover-Leaver**                                | [identity/service/LifecycleService](src/main/java/com/iamportfolio/identity/service/LifecycleService.java), [JoinerMoverLeaverOrchestrator](src/main/java/com/iamportfolio/identity/service/JoinerMoverLeaverOrchestrator.java) |
| **Provisionamento (SCIM 2.0)**                         | [scim/server/](src/main/java/com/iamportfolio/scim/server/), [scim/outbound/](src/main/java/com/iamportfolio/scim/outbound/) |
| **Conectores de identidade**                           | [scim-connector/](scim-connector/) (Python FastAPI)                  |
| **Governança / SoD / compliance**                      | [governance/service/SodCheckJob](src/main/java/com/iamportfolio/governance/service/SodCheckJob.java) |
| **Auditoria imutável**                                 | [common/audit/](src/main/java/com/iamportfolio/common/audit/), V5 migration, /api/audit/events |
| **Python diferencial**                                 | [scim-connector/app/](scim-connector/app/)                           |
| **LLMs + RAG (Claude)**                                | [ai/service/AssistantService](src/main/java/com/iamportfolio/ai/service/AssistantService.java), [ai/rag/](src/main/java/com/iamportfolio/ai/rag/), [docs/RAG_CORPUS](docs/RAG_CORPUS/) |
| **Alta disponibilidade / observability**               | [logback-spring.xml](src/main/resources/logback-spring.xml), `/actuator/health\|info\|prometheus` |

## Quickstart

Pre-reqs: Docker + Docker Compose, Java 17, Maven 3.6+ (only for the
`mvn` quickstart; full container quickstart only needs Docker).

```bash
git clone https://github.com/RennAraujo/Sistema-de-login2.git
cd Sistema-de-login2
cp .env.example .env

# Bring everything up — Postgres, Java IdP, Python SCIM connector
docker compose up --build
```

Then open:

- http://localhost:8080 — frontend
- http://localhost:8080/swagger-ui.html — every endpoint, grouped by tag
- http://localhost:8080/.well-known/openid-configuration — OIDC metadata
- http://localhost:8080/saml2/idp/metadata — SAML IdP metadata
- http://localhost:8080/actuator/health — liveness / readiness
- http://localhost:9000/health — connector
- http://localhost:8080/assistant.html — IAM assistant (needs `ANTHROPIC_API_KEY` in `.env`)

To run only the database in Docker and the Java app via Maven (faster
inner loop):

```bash
docker compose up -d postgres
mvn spring-boot:run    # uses the "local" profile + safe dev fallbacks
```

## 5-minute demo flow

1. **Register** a user via Swagger (`POST /api/auth/register`) and copy
   the JWT.
2. **Promote yourself** to `ROLE_ADMIN` (one-liner SQL —
   `INSERT INTO role_assignments (user_id, role_id, assigned_by) ...`).
3. **Get an OAuth2 token** for the demo client:
   ```bash
   curl -u demo-client:demo-secret \
        -d 'grant_type=client_credentials&scope=scim:provision' \
        http://localhost:8080/oauth2/token
   ```
4. **Walk the SCIM endpoint** with that token:
   ```bash
   curl -H "Authorization: Bearer $TOKEN" http://localhost:8080/scim/v2/Users
   ```
5. **Trigger lifecycle**: approve / suspend / offboard a user and watch
   `/api/audit/events` and `provisioning_events` in Postgres.
6. **Ask the assistant**: open `/assistant.html` and try
   *"What is the SoD policy?"* — the response cites
   `sod-policy.md`.

## Tech stack

| Layer                    | Tech                                              |
| ------------------------ | ------------------------------------------------- |
| Language                 | Java 17, Python 3.12                              |
| Framework                | Spring Boot 3.2 + Spring Security 6.2             |
| OAuth2 / OIDC            | Spring Authorization Server 1.2                   |
| SAML 2.0                 | OpenSAML-free; JDK XMLDSig + BouncyCastle for cert|
| Persistence              | Spring Data JPA, Hibernate 6.4                    |
| Database                 | PostgreSQL 16 + Flyway migrations                 |
| 2FA                      | samstevens TOTP + ZXing QR                        |
| Connector                | FastAPI + httpx + aiosqlite                       |
| AI                       | Anthropic Messages API (claude-opus-4-5) + RAG    |
| Tests                    | JUnit 5 + MockMvc + Testcontainers + pytest       |
| Container                | Docker + Docker Compose v2                        |
| CI                       | GitHub Actions                                    |
| Observability            | Actuator + Micrometer (Prometheus) + Logback JSON |

## Project structure

```
.
├── src/main/java/com/iamportfolio/
│   ├── auth/        AuthN: JWT, 2FA, login/register
│   ├── identity/    Users, lifecycle (joiner-mover-leaver)
│   ├── rbac/        Role / Permission / Group / RoleAssignment
│   ├── oauth2/      Spring Authorization Server config + client admin
│   ├── saml/        SAML 2.0 IdP + SP registry
│   ├── scim/        SCIM 2.0 server (in) + outbound provisioning client
│   ├── audit/       Audit log query API
│   ├── ai/          Claude client + RAG pipeline + /api/ai/assistant
│   ├── governance/  Access reviews + SoD rules + scheduled check
│   ├── common/      AOP audit, correlation-id filter
│   └── config/      SecurityConfig + Swagger
├── src/main/resources/
│   ├── db/migration/V1..V11_*.sql   Flyway schema history
│   ├── application*.yml             config + profiles
│   ├── logback-spring.xml           dev console + JSON for docker
│   └── static/                      vanilla frontend
├── scim-connector/                  Python FastAPI microservice
├── docs/RAG_CORPUS/                 5 internal IAM policy docs (RAG)
├── docs/SAML_QUICKSTART.md          how to register an SP
├── Dockerfile                       multi-stage app image
├── docker-compose.yml               full stack (postgres+app+connector)
└── .github/workflows/ci.yml         build + tests + image build
```

## Tests

```bash
# Java
mvn verify        # unit + integration (Testcontainers spins up Postgres)

# Python
cd scim-connector && pytest tests/
```

CI runs both on every push/PR (Ubuntu runners, Docker available
out-of-the-box).

## Configuration

Every secret + per-env value lives in environment variables; defaults
are dev-friendly so `mvn spring-boot:run` works without exporting
anything. The full list is in [.env.example](.env.example).

| Variable                        | Default                  | Notes                                          |
| ------------------------------- | ------------------------ | ---------------------------------------------- |
| `POSTGRES_PORT`                 | `5433`                   | Avoids clash with local Postgres on 5432       |
| `JWT_SECRET`                    | dev-only fallback        | **Required in production**, ≥32 chars         |
| `APP_OAUTH2_ISSUER_URI`         | `http://localhost:8080`  | Must match public URL behind a proxy           |
| `APP_CONNECTOR_BASE_URL`        | `http://localhost:9000`  | `http://scim-connector:9000` inside compose    |
| `ANTHROPIC_API_KEY`             | (empty)                  | Required only for `/api/ai/assistant`          |

## License

Developed for technical evaluation and demonstration.

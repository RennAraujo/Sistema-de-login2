# SCIM Connector

A small FastAPI service that pulls users from the IAM Portfolio's SCIM
2.0 endpoint and keeps a local SQLite mirror. It stands in for a
downstream corporate app the IdP provisions into.

The connector demonstrates two flows for the portfolio:

1. **Pull**: `/connector/sync` calls `GET /scim/v2/Users` on the IdP
   (with a Bearer token obtained via `client_credentials`) and reconciles
   the local shadow store, returning a delta (`new`, `updated`, `removed`).
2. **Push**: the IdP itself calls `POST /connector/users/{id}/sync` and
   `DELETE /connector/users/{id}` when a `LifecycleEvent` fires (added in
   commit 5.3).

## Quickstart

```bash
cd scim-connector
cp .env.example .env
python -m venv .venv && source .venv/bin/activate   # or .venv\Scripts\activate on Windows
pip install -r requirements.txt
uvicorn app.main:app --reload --port 9000
```

With the IAM Portfolio running on `:8080` and the demo OAuth2 client
seeded:

```bash
curl http://localhost:9000/connector/sync
# { "pulled": 1, "new": 1, "updated": 0, "removed": 0, "sample_users": ["audittest1"] }
```

## Tests

```bash
pytest tests/
```

The tests mock the IdP with `respx` so they don't need a running Java
side and run in CI without Docker.

## Docker

```bash
docker build -t iam-portfolio-scim-connector .
docker run -p 9000:9000 --env-file .env iam-portfolio-scim-connector
```

The connector is added as a service in the root `docker-compose.yml` in
commit 8.1 so `docker compose up` brings the whole stack online at once.

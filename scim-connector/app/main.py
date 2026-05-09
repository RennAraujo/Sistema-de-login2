"""SCIM Connector — small FastAPI service that pulls users from the IAM
Portfolio's SCIM 2.0 endpoint and keeps a local SQLite mirror.

This stands in for a downstream corporate app that the IdP provisions
into. Commit 5.3 lets the IdP push to /connector/users/{id}/sync as
joiner-mover-leaver events fire in the Java side."""
from __future__ import annotations

from fastapi import FastAPI, HTTPException, Response
from pydantic import BaseModel

from . import db
from .models import SyncResult
from .scim_client import scim_client
from .settings import settings

app = FastAPI(
    title="SCIM Connector",
    description="External SCIM 2.0 consumer + provisioning sink for the IAM Portfolio",
    version="0.1.0",
)


@app.get("/health")
async def health() -> dict:
    return {"status": "ok", "iam_base_url": settings.iam_base_url}


@app.get("/connector/users")
async def list_users() -> list[dict]:
    return [u.model_dump() for u in await scim_client.list_users()]


@app.post("/connector/sync", response_model=SyncResult)
async def sync() -> SyncResult:
    """Pull every user from the IdP and reconcile the local shadow store."""
    try:
        users = await scim_client.list_users()
    except Exception as e:
        raise HTTPException(status_code=502, detail=f"Failed to reach IdP SCIM: {e}")

    payloads = [u.model_dump() for u in users]
    new, updated = await db.upsert_users(payloads)
    removed = await db.remove_missing([u.id for u in users])
    sample = [u.userName for u in users[:5]]
    return SyncResult(
        pulled=len(users), new=new, updated=updated,
        removed=removed, sample_users=sample,
    )


class ProvisionPayload(BaseModel):
    userName: str
    email: str | None = None
    active: bool = True
    externalId: str | None = None


@app.post("/connector/users/{external_id}/sync")
async def provision_user(external_id: str, payload: ProvisionPayload) -> dict:
    """Inbound webhook from the IdP: create or update a single user.
    Used by commit 5.3 when a LifecycleEvent fires."""
    user = {
        "id": external_id,
        "userName": payload.userName,
        "externalId": payload.externalId or external_id,
        "emails": [{"value": payload.email, "primary": True}] if payload.email else [],
        "active": payload.active,
    }
    new, updated = await db.upsert_users([user])
    return {"created": new == 1, "updated": updated == 1, "externalId": external_id}


@app.delete("/connector/users/{external_id}", status_code=204, response_class=Response)
async def deprovision_user(external_id: str):
    """Inbound webhook from the IdP: drop a user (offboarding).

    Declared with response_class=Response (no body) because FastAPI 0.111+
    refuses to attach a JSON body to a 204 response — this signals "no
    content" explicitly so the framework doesn't try to serialize a return
    value.
    """
    # Easiest is to drop everyone NOT in the seen set; here we want only
    # this id removed, so build the seen set as "everyone except external_id".
    current = await db.all_user_names()
    keep = [name for name in current if name != external_id]
    await db.remove_missing(keep)
    return Response(status_code=204)

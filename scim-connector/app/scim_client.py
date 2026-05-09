"""Thin async client around the IAM Portfolio's SCIM 2.0 server."""
from __future__ import annotations
from typing import List

import httpx

from .auth import token_manager
from .models import ScimUser
from .settings import settings


class ScimClient:
    SCIM_CONTENT_TYPE = "application/scim+json"

    async def _headers(self) -> dict:
        token = await token_manager.get()
        return {
            "Authorization": f"Bearer {token}",
            "Accept": self.SCIM_CONTENT_TYPE,
            "Content-Type": self.SCIM_CONTENT_TYPE,
        }

    async def list_users(self) -> List[ScimUser]:
        url = f"{settings.iam_base_url.rstrip('/')}/scim/v2/Users"
        async with httpx.AsyncClient(timeout=15.0) as client:
            response = await client.get(url, headers=await self._headers())
            response.raise_for_status()
            payload = response.json()
        return [ScimUser.model_validate(item) for item in payload.get("Resources", [])]

    async def delete_user(self, user_id: str) -> None:
        url = f"{settings.iam_base_url.rstrip('/')}/scim/v2/Users/{user_id}"
        async with httpx.AsyncClient(timeout=15.0) as client:
            response = await client.delete(url, headers=await self._headers())
            response.raise_for_status()


scim_client = ScimClient()

"""Cached OAuth2 client_credentials token client for the IAM Portfolio AS."""
from __future__ import annotations
import time
from dataclasses import dataclass
from typing import Optional

import httpx

from .settings import settings


@dataclass
class _CachedToken:
    value: str
    expires_at: float  # epoch seconds


class TokenManager:
    """Fetches a Bearer token via client_credentials and caches it until 30s
    before it would expire. Thread-safety isn't required here — uvicorn calls
    handlers on a single asyncio loop."""

    def __init__(self) -> None:
        self._cached: Optional[_CachedToken] = None

    async def get(self) -> str:
        if self._cached and self._cached.expires_at - time.time() > 30:
            return self._cached.value

        token_url = f"{settings.iam_base_url.rstrip('/')}/oauth2/token"
        async with httpx.AsyncClient(timeout=10.0) as client:
            response = await client.post(
                token_url,
                auth=(settings.iam_client_id, settings.iam_client_secret),
                data={
                    "grant_type": "client_credentials",
                    "scope": "scim:provision",
                },
            )
            response.raise_for_status()
            payload = response.json()

        self._cached = _CachedToken(
            value=payload["access_token"],
            expires_at=time.time() + int(payload.get("expires_in", 900)),
        )
        return self._cached.value


token_manager = TokenManager()

"""Tiny SQLite shadow store of the users we last synced from the IdP.

Exists so we can compute deltas (new / updated / removed) and demonstrate
end-to-end lifecycle propagation: when the IdP offboards a user, the
next sync should mark them as removed locally."""
from __future__ import annotations
from typing import Iterable, List

import aiosqlite

from .settings import settings

_SCHEMA = """
CREATE TABLE IF NOT EXISTS shadow_users (
    scim_id     TEXT PRIMARY KEY,
    user_name   TEXT NOT NULL,
    email       TEXT,
    active      INTEGER NOT NULL DEFAULT 1,
    raw_json    TEXT NOT NULL,
    last_synced TEXT NOT NULL DEFAULT (datetime('now'))
);
"""


async def _connect() -> aiosqlite.Connection:
    conn = await aiosqlite.connect(settings.scim_shadow_db_path)
    await conn.executescript(_SCHEMA)
    await conn.commit()
    return conn


async def all_user_names() -> List[str]:
    async with await _connect() as conn:
        async with conn.execute("SELECT user_name FROM shadow_users ORDER BY user_name") as cur:
            return [row[0] async for row in cur]


async def upsert_users(users: Iterable[dict]) -> tuple[int, int]:
    """Returns (new_count, updated_count)."""
    new_count = updated = 0
    async with await _connect() as conn:
        for u in users:
            scim_id = u["id"]
            async with conn.execute("SELECT 1 FROM shadow_users WHERE scim_id = ?", (scim_id,)) as cur:
                exists = (await cur.fetchone()) is not None
            await conn.execute(
                """
                INSERT INTO shadow_users (scim_id, user_name, email, active, raw_json, last_synced)
                VALUES (?, ?, ?, ?, ?, datetime('now'))
                ON CONFLICT(scim_id) DO UPDATE SET
                    user_name = excluded.user_name,
                    email = excluded.email,
                    active = excluded.active,
                    raw_json = excluded.raw_json,
                    last_synced = datetime('now')
                """,
                (
                    scim_id,
                    u["userName"],
                    (u.get("emails") or [{}])[0].get("value"),
                    1 if u.get("active", True) else 0,
                    str(u),
                ),
            )
            if exists:
                updated += 1
            else:
                new_count += 1
        await conn.commit()
    return new_count, updated


async def remove_missing(seen_ids: Iterable[str]) -> int:
    """Drops shadow rows whose SCIM IDs are not in the set we just pulled."""
    seen = list(seen_ids)
    placeholders = ",".join("?" * len(seen)) if seen else "''"
    async with await _connect() as conn:
        cur = await conn.execute(
            f"DELETE FROM shadow_users WHERE scim_id NOT IN ({placeholders})",
            seen,
        )
        await conn.commit()
        return cur.rowcount or 0

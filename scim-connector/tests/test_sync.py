"""Smoke tests for the connector. We mock the IdP's SCIM endpoint with
respx so the suite stays self-contained and runs in CI without Docker."""
from __future__ import annotations
import os

import pytest
import respx

os.environ["IAM_BASE_URL"] = "http://idp.test"

from app.scim_client import scim_client  # noqa: E402


@pytest.mark.asyncio
async def test_list_users_maps_response():
    with respx.mock:
        respx.post("http://idp.test/oauth2/token").respond(
            200, json={"access_token": "test-token", "expires_in": 900}
        )
        respx.get("http://idp.test/scim/v2/Users").respond(
            200, json={
                "schemas": ["urn:ietf:params:scim:api:messages:2.0:ListResponse"],
                "totalResults": 1,
                "Resources": [
                    {
                        "id": "42",
                        "userName": "alice",
                        "active": True,
                        "emails": [{"value": "alice@example.com", "primary": True}],
                    }
                ],
            }
        )
        users = await scim_client.list_users()

    assert len(users) == 1
    assert users[0].userName == "alice"
    assert users[0].emails[0].value == "alice@example.com"


def test_models_validate_minimal_user():
    """Pydantic mapping smoke check — guards against accidental schema drift."""
    from app.models import ScimUser

    user = ScimUser.model_validate({
        "id": "1",
        "userName": "alice",
        "active": True,
        "emails": [{"value": "alice@example.com", "primary": True}],
    })
    assert user.userName == "alice"
    assert user.emails[0].primary is True

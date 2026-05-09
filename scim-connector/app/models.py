"""Pydantic models mirroring the SCIM 2.0 User resource we consume."""
from typing import List, Optional
from pydantic import BaseModel, Field


class ScimEmail(BaseModel):
    value: str
    type: Optional[str] = None
    primary: bool = False


class ScimName(BaseModel):
    givenName: Optional[str] = None
    familyName: Optional[str] = None
    formatted: Optional[str] = None


class ScimUser(BaseModel):
    id: str
    userName: str
    externalId: Optional[str] = None
    name: Optional[ScimName] = None
    emails: List[ScimEmail] = Field(default_factory=list)
    active: bool = True


class ScimListResponse(BaseModel):
    schemas: List[str]
    totalResults: int
    startIndex: int = 1
    itemsPerPage: int = 0
    Resources: List[dict] = Field(default_factory=list)


class SyncResult(BaseModel):
    """Diff snapshot returned by /connector/sync."""
    pulled: int
    new: int
    updated: int
    removed: int
    sample_users: List[str] = Field(default_factory=list)

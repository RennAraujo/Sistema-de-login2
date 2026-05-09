"""Connector configuration loaded from environment variables (and .env)."""
from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    iam_base_url: str = "http://localhost:8080"
    iam_client_id: str = "demo-client"
    iam_client_secret: str = "demo-secret"
    scim_shadow_db_path: str = "./connector.db"
    port: int = 9000

    model_config = SettingsConfigDict(env_file=".env", case_sensitive=False)


settings = Settings()

from functools import lru_cache
from pathlib import Path

from pydantic import SecretStr
from pydantic_settings import BaseSettings, SettingsConfigDict

PROJECT_ROOT = Path(__file__).resolve().parents[2]


class Settings(BaseSettings):
    google_api_key: SecretStr
    google_model: str = "gemini-3.5-flash-lite"
    google_embedding_model: str = "gemini-embedding-001"
    embedding_dimension: int = 768
    ocr_enabled: bool = True
    ocr_command: str = "ocrmypdf"
    ocr_languages: str = "deu+eng"
    ocr_tessdata_prefix: str | None = None
    ocr_timeout_seconds: int = 120
    ocr_max_pages: int = 20
    ocr_min_text_characters: int = 20

    model_config = SettingsConfigDict(
        env_file=PROJECT_ROOT / ".env",
        env_file_encoding="utf-8",
        extra="ignore",
    )


@lru_cache
def get_settings() -> Settings:
    return Settings()

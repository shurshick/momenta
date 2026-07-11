import json
import logging
from datetime import datetime, timedelta, timezone
from typing import Any

import httpx
from pydantic import ValidationError
from sqlalchemy.ext.asyncio import AsyncSession

from app.config import settings
from app.schemas.app_update import AppLatestResponse
from app.services.setting_service import get_setting, set_setting

logger = logging.getLogger(__name__)

ANDROID_UPDATE_CACHE_KEY = "app_update_android_latest_json"
ANDROID_UPDATE_FETCHED_AT_KEY = "app_update_android_latest_fetched_at"


async def get_latest_android_update(db: AsyncSession) -> AppLatestResponse:
    cached = await _get_cached_update(db)
    if cached and not await _is_cache_expired(db):
        return cached

    try:
        latest = await fetch_latest_android_update_from_github()
    except Exception:
        logger.warning("Android update metadata refresh failed", exc_info=True)
        if cached:
            return cached
        raise

    await _store_cached_update(db, latest)
    return latest


async def fetch_latest_android_update_from_github() -> AppLatestResponse:
    if settings.app_update_source != "github":
        raise RuntimeError(f"Unsupported app update source: {settings.app_update_source}")

    headers = {
        "Accept": "application/vnd.github+json",
        "User-Agent": f"Momenta/{settings.app_version}",
    }
    if settings.app_update_github_token:
        headers["Authorization"] = f"Bearer {settings.app_update_github_token}"

    timeout = httpx.Timeout(settings.app_update_request_timeout_seconds)
    async with httpx.AsyncClient(timeout=timeout, headers=headers, follow_redirects=True) as client:
        releases = await _load_releases(client)
        for release in releases:
            if release.get("draft") or release.get("prerelease"):
                continue
            asset_url = _find_metadata_asset_url(release)
            if not asset_url:
                continue
            response = await client.get(asset_url)
            response.raise_for_status()
            return _parse_metadata(response.json())

    raise RuntimeError(
        f"No release with {settings.app_update_metadata_asset_name} found in "
        f"{settings.app_update_github_repo}"
    )


async def _load_releases(client: httpx.AsyncClient) -> list[dict[str, Any]]:
    url = (
        f"{settings.app_update_github_api_base_url.rstrip('/')}/repos/"
        f"{settings.app_update_github_repo}/releases"
    )
    response = await client.get(url, params={"per_page": 30})
    response.raise_for_status()
    releases = response.json()
    if not isinstance(releases, list):
        raise RuntimeError("GitHub releases response is not a list")
    return releases


def _find_metadata_asset_url(release: dict[str, Any]) -> str | None:
    assets = release.get("assets")
    if not isinstance(assets, list):
        return None
    for asset in assets:
        if not isinstance(asset, dict):
            continue
        if asset.get("name") != settings.app_update_metadata_asset_name:
            continue
        url = asset.get("browser_download_url")
        return url if isinstance(url, str) and url else None
    return None


def _parse_metadata(data: Any) -> AppLatestResponse:
    try:
        return AppLatestResponse.model_validate(data)
    except ValidationError as exc:
        raise RuntimeError("Android update metadata is invalid") from exc


async def _get_cached_update(db: AsyncSession) -> AppLatestResponse | None:
    raw = await get_setting(db, ANDROID_UPDATE_CACHE_KEY)
    if not raw:
        return None
    try:
        return _parse_metadata(json.loads(raw))
    except (json.JSONDecodeError, RuntimeError):
        logger.warning("Stored Android update metadata is invalid", exc_info=True)
        return None


async def _is_cache_expired(db: AsyncSession) -> bool:
    raw = await get_setting(db, ANDROID_UPDATE_FETCHED_AT_KEY)
    if not raw:
        return True
    try:
        fetched_at = datetime.fromisoformat(raw)
    except ValueError:
        return True
    if fetched_at.tzinfo is None:
        fetched_at = fetched_at.replace(tzinfo=timezone.utc)
    ttl = max(settings.app_update_cache_ttl_seconds, 0)
    return datetime.now(timezone.utc) - fetched_at >= timedelta(seconds=ttl)


async def _store_cached_update(db: AsyncSession, metadata: AppLatestResponse) -> None:
    await set_setting(
        db,
        ANDROID_UPDATE_CACHE_KEY,
        metadata.model_dump_json(exclude_none=True),
    )
    await set_setting(
        db,
        ANDROID_UPDATE_FETCHED_AT_KEY,
        datetime.now(timezone.utc).isoformat(),
    )

from fastapi import APIRouter

from app.config import settings
from app.schemas.app_update import AppLatestResponse

router = APIRouter(prefix="/api/v1/app", tags=["app"])


@router.get("/latest", response_model=AppLatestResponse)
async def get_latest_android_app() -> AppLatestResponse:
    return AppLatestResponse(
        app_name=settings.app_latest_android_app_name,
        package_name=settings.app_latest_android_package_name,
        platform="android",
        channel=settings.app_latest_android_channel,
        version_name=settings.app_latest_android_version_name,
        version_code=settings.app_latest_android_version_code,
        min_supported_version_code=settings.app_min_supported_android_version_code,
        mandatory=settings.app_latest_android_mandatory,
        apk_url=settings.app_latest_android_apk_url,
        apk_sha256=settings.app_latest_android_apk_sha256 or None,
        apk_size_bytes=settings.app_latest_android_apk_size_bytes,
        release_url=settings.app_latest_android_release_url or None,
        release_notes=settings.app_latest_android_release_note_list,
        published_at=settings.app_latest_android_published_at,
    )

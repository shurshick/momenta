from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]

REMOVED_MANUAL_FIELDS = (
    "APP_LATEST_ANDROID_VERSION_NAME",
    "APP_LATEST_ANDROID_VERSION_CODE",
    "APP_LATEST_ANDROID_APK_URL",
    "APP_LATEST_ANDROID_APK_SHA256",
    "APP_LATEST_ANDROID_APK_SIZE_BYTES",
    "APP_LATEST_ANDROID_RELEASE_URL",
    "APP_LATEST_ANDROID_RELEASE_NOTES",
    "APP_LATEST_ANDROID_PUBLISHED_AT",
)


def main() -> None:
    config_text = (ROOT / "app/config.py").read_text(encoding="utf-8")
    workflow_text = (ROOT / ".github/workflows/android-publish.yml").read_text(
        encoding="utf-8"
    )

    require("app_update_github_repo" in config_text, "Missing GitHub update source config")
    require("app_update_metadata_asset_name" in config_text, "Missing metadata asset config")
    require("android-update.json" in workflow_text, "Android workflow must publish metadata JSON")
    require("sha256sum" in workflow_text, "Android workflow must calculate APK sha256")
    require("stat -c%s" in workflow_text, "Android workflow must calculate APK size")
    require("versionName" in workflow_text, "Android workflow must read versionName")
    require("versionCode" in workflow_text, "Android workflow must read versionCode")
    require("assembleProdRelease" in workflow_text, "Android workflow must build prod release")
    require("verifyInstallableProdReleaseApk" in workflow_text, "Missing prod release verification")
    require("app-prod-release.apk" in workflow_text, "Release APK must have the production name")

    for path in (
        ROOT / "deploy/truenas/docker-compose.truenas.yml",
        ROOT / "docs/API.md",
        ROOT / "docs/DEPLOY_TRUENAS.md",
    ):
        text = path.read_text(encoding="utf-8")
        for field in REMOVED_MANUAL_FIELDS:
            require(field not in text, f"{path}: manual APK env still present: {field}")

    print("android update metadata automation ok")


def require(condition: bool, message: str) -> None:
    if not condition:
        raise SystemExit(message)


if __name__ == "__main__":
    main()

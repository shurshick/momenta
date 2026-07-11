import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]


def main() -> None:
    config_text = (ROOT / "app/config.py").read_text(encoding="utf-8")
    version = string_value(config_text, "app_latest_android_version_name")
    version_code = int_value(config_text, "app_latest_android_version_code")
    sha256 = string_value(config_text, "app_latest_android_apk_sha256")
    size = int_value(config_text, "app_latest_android_apk_size_bytes")
    apk_url = string_value(config_text, "app_latest_android_apk_url")
    release_url = string_value(config_text, "app_latest_android_release_url")

    require(re.fullmatch(r"\d+\.\d+\.\d+", version) is not None, "Bad Android version_name")
    require(isinstance(version_code, int) and version_code > 0, "Bad Android version_code")
    require(re.fullmatch(r"[0-9a-f]{64}", sha256 or "") is not None, "Bad APK sha256")
    require(isinstance(size, int) and size > 0, "Bad APK size")
    require(apk_url.endswith("/app-prod-debug.apk"), "APK URL must point to app-prod-debug.apk")
    require(f"/download/v{version}/" in apk_url, "APK URL tag does not match version_name")
    require(release_url.endswith(f"/v{version}"), "Release URL tag does not match version_name")

    expected = {
        "version_name": version,
        "version_code": str(version_code),
        "apk_url": apk_url,
        "apk_sha256": sha256,
        "apk_size_bytes": str(size),
        "release_url": release_url,
    }
    for path in (
        ROOT / "deploy/truenas/docker-compose.truenas.yml",
        ROOT / "docs/API.md",
        ROOT / "docs/DEPLOY_TRUENAS.md",
    ):
        text = path.read_text(encoding="utf-8")
        for label, value in expected.items():
            require(value in text, f"{path}: missing {label}={value}")

    gradle = (ROOT / "android/app/build.gradle.kts").read_text(encoding="utf-8")
    require(f'versionCode = {version_code}' in gradle, "Gradle versionCode mismatch")
    require(f'versionName = "{version}"' in gradle, "Gradle versionName mismatch")

    print(f"android update metadata ok: {version} ({version_code})")


def require(condition: bool, message: str) -> None:
    if not condition:
        raise SystemExit(message)


def string_value(text: str, name: str) -> str:
    match = re.search(rf'{name}: [^\n=]+=\s*"([^"]+)"', text)
    if match is None:
        match = re.search(rf'{name}: [^\n=]+=\s*\(\s*"([^"]+)"\s*\)', text)
    require(match is not None, f"Missing config value: {name}")
    return match.group(1)


def int_value(text: str, name: str) -> int:
    match = re.search(rf"{name}: .*?= (\d+)", text)
    require(match is not None, f"Missing config value: {name}")
    return int(match.group(1))


if __name__ == "__main__":
    main()

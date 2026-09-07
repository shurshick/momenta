import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]


def read(path: str) -> str:
    return (ROOT / path).read_text(encoding="utf-8")


def match(pattern: str, path: str) -> str:
    found = re.search(pattern, read(path), re.MULTILINE)
    if not found:
        raise SystemExit(f"Version marker not found in {path}")
    return found.group(1)


server_version = match(r'RELEASE_VERSION = "([^"]+)"', "app/version.py")
pyproject_version = match(r'^version = "([^"]+)"', "pyproject.toml")
android_version = match(r'versionName = "([^"]+)"', "android/app/build.gradle.kts")
android_code = int(match(r"versionCode = (\d+)", "android/app/build.gradle.kts"))

if len({server_version, pyproject_version, android_version}) != 1:
    raise SystemExit(
        "Version mismatch: "
        f"server={server_version}, pyproject={pyproject_version}, android={android_version}"
    )

required_markers = {
    "README.md": (f"v{server_version}", f"momenta:{server_version}"),
    "deploy/truenas/docker-compose.truenas.yml": (f"momenta:{server_version}",),
}
for path, markers in required_markers.items():
    content = read(path)
    missing = [marker for marker in markers if marker not in content]
    if missing:
        raise SystemExit(f"Stale release reference in {path}: missing {missing}")

if android_code < 1:
    raise SystemExit("Android versionCode must be positive")

print(f"OK: {server_version}, Android versionCode {android_code}")

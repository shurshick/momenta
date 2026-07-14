from pathlib import Path

from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
CANONICAL_LOGO = ROOT / "assets" / "brand" / "momenta-logo-official.png"
ANDROID_LOGO = (
    ROOT
    / "android"
    / "app"
    / "src"
    / "main"
    / "res"
    / "drawable-nodpi"
    / "momenta_logo_official.png"
)


def main() -> int:
    if not CANONICAL_LOGO.exists() or not ANDROID_LOGO.exists():
        raise SystemExit("Official logo is missing")
    if CANONICAL_LOGO.read_bytes() != ANDROID_LOGO.read_bytes():
        raise SystemExit("Android logo differs from the canonical brand asset")

    with Image.open(CANONICAL_LOGO) as image:
        if image.format != "PNG" or image.mode != "RGBA":
            raise SystemExit("Official logo must be an RGBA PNG")
        if image.size != (1254, 1254):
            raise SystemExit(f"Unexpected official logo size: {image.size}")

        alpha = image.getchannel("A")
        if alpha.getextrema() != (0, 255):
            raise SystemExit("Official logo must contain transparent and opaque pixels")
        corners = (
            alpha.getpixel((0, 0)),
            alpha.getpixel((image.width - 1, 0)),
            alpha.getpixel((0, image.height - 1)),
            alpha.getpixel((image.width - 1, image.height - 1)),
        )
        if any(corners):
            raise SystemExit(f"Official logo corners must be transparent: {corners}")

    print("brand assets check passed")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())

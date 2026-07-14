from __future__ import annotations

import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
SKIP_DIRS = {
    ".git",
    ".gradle",
    ".idea",
    ".venv",
    "__pycache__",
    "build",
    "node_modules",
}
TEXT_SUFFIXES = {
    ".gradle",
    ".ini",
    ".json",
    ".kts",
    ".kt",
    ".md",
    ".py",
    ".sh",
    ".toml",
    ".txt",
    ".xml",
    ".yaml",
    ".yml",
}
TEXT_NAMES = {
    ".editorconfig",
    ".gitignore",
    "Dockerfile",
}
MOJIBAKE_PATTERNS = (
    "Рњ",
    "Рќ",
    "Рџ",
    "Рћ",
    "РЎ",
    "РЈ",
    "Р’",
    "Р“",
    "Р”",
    "Р›",
    "Р¤",
    "Р—",
    "Рљ",
    "Р°",
    "Р±",
    "РІ",
    "Рі",
    "Рґ",
    "Рµ",
    "Рё",
    "Р№",
    "Рє",
    "Р»",
    "Рј",
    "РЅ",
    "Рѕ",
    "Рї",
    "СЃ",
    "С‚",
    "СЊ",
    "С‹",
    "СЂ",
    "СЋ",
    "СЏ",
    "С‡",
    "С€",
    "С‰",
    "С†",
    "С…",
    "СЉ",
    "СЌ",
    "вЂ",
    "Гђ",
    "Г‘",
)
MOJIBAKE_NOISE = set("РСЃЊЉЌЋЏЂЃЄЅІЇЈљњџ™“”„…†‡€")


def cyrillic_count(text: str) -> int:
    return sum("А" <= char <= "я" or char in "Ёё" for char in text)


def looks_like_cp1251_mojibake(line: str) -> bool:
    try:
        restored = line.encode("cp1251").decode("utf-8")
    except UnicodeError:
        return False
    if restored == line:
        return False
    noise_count = sum(char in MOJIBAKE_NOISE for char in line)
    return noise_count >= 2 and cyrillic_count(restored) >= 3


def has_mojibake(line: str) -> bool:
    return any(pattern in line for pattern in MOJIBAKE_PATTERNS) or looks_like_cp1251_mojibake(line)


def should_skip(path: Path) -> bool:
    return any(part in SKIP_DIRS for part in path.relative_to(ROOT).parts)


def is_text_file(path: Path) -> bool:
    return path.name in TEXT_NAMES or path.suffix in TEXT_SUFFIXES


def read_text(path: Path) -> tuple[str | None, bool]:
    try:
        data = path.read_bytes()
    except OSError:
        return None, False
    if b"\0" in data[:4096]:
        return None, False
    if not is_text_file(path):
        try:
            data.decode("utf-8")
        except UnicodeDecodeError:
            return None, False
    try:
        return data.decode("utf-8"), False
    except UnicodeDecodeError as exc:
        rel = path.relative_to(ROOT)
        print(f"{rel}: invalid utf-8: {exc}", file=sys.stderr)
        return None, True


def main() -> int:
    failures: list[tuple[Path, int, str]] = []
    invalid_utf8 = False

    self_path = Path(__file__).resolve()

    for path in sorted(ROOT.rglob("*")):
        if path.is_dir() or should_skip(path):
            continue
        if path.resolve() == self_path:
            continue
        text, is_invalid_utf8 = read_text(path)
        if is_invalid_utf8:
            invalid_utf8 = True
            continue
        if text is None:
            continue
        for line_no, line in enumerate(text.splitlines(), start=1):
            if has_mojibake(line):
                failures.append((path.relative_to(ROOT), line_no, line.strip()))

    for rel, line_no, line in failures:
        print(f"{rel}:{line_no}: {line[:180]}")

    if failures or invalid_utf8:
        print(
            f"mojibake check failed: {len(failures)} suspicious line(s)",
            file=sys.stderr,
        )
        return 1

    print("mojibake check passed")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())

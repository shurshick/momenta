import argparse
import asyncio
from dataclasses import asdict, dataclass

from sqlalchemy import select

from app.db import async_session_factory
from app.models.post import Post
from app.models.user import User
from app.services.counter_service import CounterService


@dataclass
class RepairCountersResult:
    dry_run: bool
    posts_checked: int = 0
    posts_changed: int = 0
    users_checked: int = 0
    users_with_activity: int = 0


async def repair_counters(dry_run: bool = False) -> RepairCountersResult:
    async with async_session_factory() as db:
        result = await repair_counters_for_session(db, dry_run=dry_run)
    return result


async def repair_counters_for_session(db, dry_run: bool = False) -> RepairCountersResult:
    result = RepairCountersResult(dry_run=dry_run)
    counters = CounterService(db)
    posts = list((await db.execute(select(Post))).scalars().all())
    for post in posts:
        result.posts_checked += 1
        before = (post.likes_count, post.comments_count, post.reports_count)
        values = await counters.sync_post_counts(post)
        after = (values.likes_count, values.comments_count, values.reports_count)
        if before != after:
            result.posts_changed += 1

    users = list((await db.execute(select(User))).scalars().all())
    for user in users:
        result.users_checked += 1
        user_values = await counters.user_counter_values(user.id)
        if user_values.moments_count or user_values.likes_count:
            result.users_with_activity += 1

    if dry_run:
        await db.rollback()
    else:
        await db.commit()
    return result


async def _main_async(argv: list[str] | None = None) -> int:
    parser = argparse.ArgumentParser(prog="python -m app.cli")
    subparsers = parser.add_subparsers(dest="command", required=True)
    repair_parser = subparsers.add_parser("repair-counters")
    repair_parser.add_argument("--dry-run", action="store_true")
    args = parser.parse_args(argv)

    if args.command == "repair-counters":
        result = await repair_counters(dry_run=args.dry_run)
        for key, value in asdict(result).items():
            print(f"{key}={value}")
        return 0
    return 1


def main(argv: list[str] | None = None) -> int:
    return asyncio.run(_main_async(argv))


if __name__ == "__main__":
    raise SystemExit(main())

from fastapi import APIRouter
from app.api.v1 import health, auth, challenges, posts, feed, reactions, reports, users

router = APIRouter()
router.include_router(health.router)
router.include_router(auth.router)
router.include_router(challenges.router)
router.include_router(posts.router)
router.include_router(feed.router)
router.include_router(reactions.router)
router.include_router(reports.router)
router.include_router(users.router)

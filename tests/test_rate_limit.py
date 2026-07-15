import pytest

from app.services.rate_limit_service import check_rate_limit


@pytest.mark.asyncio
async def test_rate_limit_hashes_identifier_and_reports_retry_after(monkeypatch):
    calls = []

    class FakeRedis:
        async def eval(self, script, number_of_keys, key, window_seconds):
            calls.append((number_of_keys, key, window_seconds))
            return [4, 23]

    async def fake_redis():
        return FakeRedis()

    monkeypatch.setattr("app.services.rate_limit_service.get_redis", fake_redis)
    result = await check_rate_limit("login", "192.0.2.10", limit=3, window_seconds=60)

    assert result.allowed is False
    assert result.retry_after_seconds == 23
    assert calls[0][0] == 1
    assert calls[0][1].startswith("rate:login:")
    assert "192.0.2.10" not in calls[0][1]
    assert calls[0][2] == 60

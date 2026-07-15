FROM python:3.12-slim

WORKDIR /app

RUN apt-get update && apt-get install -y --no-install-recommends \
    gcc libpq-dev curl && \
    rm -rf /var/lib/apt/lists/*

COPY . .

RUN pip install --no-cache-dir . && \
    pip install --no-cache-dir uvicorn[standard] gunicorn

RUN useradd -m -u 1000 appuser && chown -R appuser:appuser /app
USER appuser

EXPOSE 8000

HEALTHCHECK --interval=30s --timeout=10s --retries=5 \
    CMD curl -f http://localhost:8000/health || exit 1

CMD ["sh", "scripts/start_api.sh"]

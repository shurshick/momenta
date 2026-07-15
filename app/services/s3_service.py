import asyncio
from datetime import date
from typing import BinaryIO

import boto3
from botocore.client import Config

from app.config import settings

s3_client = None


def get_s3():
    global s3_client
    if s3_client is None:
        s3_client = boto3.client(
            "s3",
            endpoint_url=settings.s3_endpoint,
            aws_access_key_id=settings.s3_access_key,
            aws_secret_access_key=settings.s3_secret_key,
            region_name=settings.s3_region,
            config=Config(signature_version="s3v4"),
            verify=settings.s3_secure,
        )
    return s3_client


def ensure_bucket():
    s3 = get_s3()
    bucket = settings.s3_bucket
    try:
        s3.head_bucket(Bucket=bucket)
    except Exception:
        s3.create_bucket(Bucket=bucket)


def upload_fileobj(fileobj: BinaryIO, object_key: str, content_type: str) -> str:
    s3 = get_s3()
    bucket = settings.s3_bucket
    s3.upload_fileobj(
        fileobj, bucket, object_key, ExtraArgs={"ContentType": content_type, "ACL": "public-read"}
    )
    return f"{settings.s3_public_endpoint}/{bucket}/{object_key}"


async def upload_fileobj_async(fileobj: BinaryIO, object_key: str, content_type: str) -> str:
    return await asyncio.to_thread(upload_fileobj, fileobj, object_key, content_type)


def delete_object(object_key: str):
    s3 = get_s3()
    bucket = settings.s3_bucket
    s3.delete_object(Bucket=bucket, Key=object_key)


async def delete_object_async(object_key: str) -> None:
    await asyncio.to_thread(delete_object, object_key)


def make_object_key(d: date, post_id: str, variant: str, ext: str) -> str:
    return f"media/{d.year:04d}/{d.month:02d}/{d.day:02d}/{variant}/{post_id}.{ext}"

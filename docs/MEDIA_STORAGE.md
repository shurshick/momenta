# Media Storage

## S3 Structure

```
media/
  yyyy/
    mm/
      dd/
        original/{post_id}.{ext}
        preview/{post_id}.webp
        thumb/{post_id}.webp
```

## Processing

- Original: stored as uploaded
- Preview: max 1440px, WebP, quality 85
- Thumb: max 400px, WebP, quality 75

## Rules

- Feed returns preview URL, never original
- Thumb used for grid/miniature views
- MIME type validated server-side
- File name from user is never used as object key
- Worker processes async after upload

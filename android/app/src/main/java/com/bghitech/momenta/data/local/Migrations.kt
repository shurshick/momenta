package com.bghitech.momenta.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE cached_posts_v5 (
                id TEXT NOT NULL PRIMARY KEY,
                username TEXT NOT NULL,
                displayName TEXT,
                avatarUrl TEXT,
                avatarKey TEXT,
                challengeDate TEXT NOT NULL,
                mediaType TEXT NOT NULL,
                previewUrl TEXT NOT NULL,
                thumbUrl TEXT,
                caption TEXT,
                country TEXT,
                city TEXT,
                likesCount INTEGER NOT NULL,
                commentsCount INTEGER NOT NULL,
                viewsCount INTEGER NOT NULL,
                createdAt TEXT NOT NULL,
                isLiked INTEGER NOT NULL,
                isMine INTEGER NOT NULL,
                canDelete INTEGER NOT NULL,
                syncState TEXT NOT NULL,
                cachedAt INTEGER NOT NULL
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            INSERT INTO cached_posts_v5 (
                id, username, displayName, avatarUrl, avatarKey, challengeDate, mediaType,
                previewUrl, thumbUrl, caption, country, city, likesCount, commentsCount,
                viewsCount, createdAt, isLiked, isMine, canDelete, syncState, cachedAt
            )
            SELECT
                id, username, displayName, avatarUrl, avatarKey, challengeDate, mediaType,
                previewUrl, thumbUrl, caption, country, city, likesCount, commentsCount,
                viewsCount, createdAt, isLiked, isMine, canDelete, 'remote', cachedAt
            FROM cached_posts
            """.trimIndent()
        )
        db.execSQL("DROP TABLE cached_posts")
        db.execSQL("ALTER TABLE cached_posts_v5 RENAME TO cached_posts")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_cached_posts_challengeDate_createdAt ON cached_posts(challengeDate, createdAt)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_cached_posts_challengeDate_cachedAt ON cached_posts(challengeDate, cachedAt)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_cached_posts_isLiked ON cached_posts(isLiked)")
    }
}

val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // v5 cache had no account owner, so carrying it forward could expose another user's feed.
        db.execSQL("DROP TABLE IF EXISTS cached_posts")
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS cached_posts (
                accountId TEXT NOT NULL,
                id TEXT NOT NULL,
                username TEXT NOT NULL,
                displayName TEXT,
                avatarUrl TEXT,
                avatarKey TEXT,
                challengeDate TEXT NOT NULL,
                mediaType TEXT NOT NULL,
                previewUrl TEXT NOT NULL,
                thumbUrl TEXT,
                caption TEXT,
                country TEXT,
                city TEXT,
                likesCount INTEGER NOT NULL,
                commentsCount INTEGER NOT NULL,
                viewsCount INTEGER NOT NULL,
                createdAt TEXT NOT NULL,
                isLiked INTEGER NOT NULL,
                isBookmarked INTEGER NOT NULL,
                bookmarkedAt TEXT,
                isMine INTEGER NOT NULL,
                canDelete INTEGER NOT NULL,
                syncState TEXT NOT NULL,
                cachedAt INTEGER NOT NULL,
                PRIMARY KEY(accountId, id)
            )
            """.trimIndent()
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS index_cached_posts_accountId_challengeDate_createdAt ON cached_posts(accountId, challengeDate, createdAt)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_cached_posts_accountId_challengeDate_cachedAt ON cached_posts(accountId, challengeDate, cachedAt)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_cached_posts_accountId_isLiked ON cached_posts(accountId, isLiked)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_cached_posts_accountId_isBookmarked_bookmarkedAt ON cached_posts(accountId, isBookmarked, bookmarkedAt)")
    }
}

val MIGRATION_6_7 = object : Migration(6, 7) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // The old challenge row contained account-specific state without an account key.
        db.execSQL("DROP TABLE IF EXISTS cached_challenge")
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS cached_challenge (
                accountId TEXT NOT NULL,
                id TEXT NOT NULL,
                date TEXT NOT NULL,
                title TEXT NOT NULL,
                description TEXT,
                prompt TEXT,
                source TEXT NOT NULL,
                endsAt TEXT,
                userPosted INTEGER NOT NULL,
                participantsCount INTEGER NOT NULL,
                cachedAt INTEGER NOT NULL,
                PRIMARY KEY(accountId, id)
            )
            """.trimIndent()
        )
        db.execSQL(
            "CREATE INDEX IF NOT EXISTS index_cached_challenge_accountId_date " +
                "ON cached_challenge(accountId, date)"
        )

        // Upload queue was not wired into the product before v7, so no valid jobs exist to preserve.
        db.execSQL("DROP TABLE IF EXISTS upload_queue")
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS upload_queue (
                localId TEXT NOT NULL PRIMARY KEY,
                accountId TEXT NOT NULL,
                challengeId TEXT NOT NULL,
                challengeDate TEXT NOT NULL,
                filePath TEXT NOT NULL,
                caption TEXT,
                country TEXT,
                city TEXT,
                mediaType TEXT NOT NULL,
                status TEXT NOT NULL,
                retryCount INTEGER NOT NULL,
                createdAt INTEGER NOT NULL,
                updatedAt INTEGER NOT NULL
            )
            """.trimIndent()
        )
        db.execSQL(
            "CREATE INDEX IF NOT EXISTS index_upload_queue_accountId_status_createdAt " +
                "ON upload_queue(accountId, status, createdAt)"
        )
        db.execSQL(
            "CREATE INDEX IF NOT EXISTS index_upload_queue_accountId_challengeDate " +
                "ON upload_queue(accountId, challengeDate)"
        )
    }
}

package com.bghitech.momenta.data.local.entity

import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "cached_posts",
    primaryKeys = ["accountId", "id"],
    indices = [
        Index(value = ["accountId", "challengeDate", "createdAt"]),
        Index(value = ["accountId", "challengeDate", "cachedAt"]),
        Index(value = ["accountId", "isLiked"]),
        Index(value = ["accountId", "isBookmarked", "bookmarkedAt"])
    ]
)
data class CachedPostEntity(
    val accountId: String,
    val id: String,
    val username: String,
    val displayName: String?,
    val avatarUrl: String?,
    val avatarKey: String?,
    val challengeDate: String,
    val mediaType: String,
    val previewUrl: String,
    val thumbUrl: String?,
    val caption: String?,
    val country: String?,
    val city: String?,
    val likesCount: Int,
    val commentsCount: Int,
    val viewsCount: Int,
    val createdAt: String,
    val isLiked: Boolean,
    val isBookmarked: Boolean,
    val bookmarkedAt: String?,
    val isMine: Boolean,
    val canDelete: Boolean,
    val syncState: String = "remote",
    val cachedAt: Long = System.currentTimeMillis()
)

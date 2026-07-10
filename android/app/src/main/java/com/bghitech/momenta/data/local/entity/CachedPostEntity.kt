package com.bghitech.momenta.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "cached_posts",
    indices = [
        Index(value = ["challengeDate", "createdAt"]),
        Index(value = ["challengeDate", "cachedAt"]),
        Index(value = ["isLiked"])
    ]
)
data class CachedPostEntity(
    @PrimaryKey val id: String,
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
    val isMine: Boolean,
    val canDelete: Boolean,
    val cachedAt: Long = System.currentTimeMillis()
)

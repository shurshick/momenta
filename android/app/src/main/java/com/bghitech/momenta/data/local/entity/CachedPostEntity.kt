package com.bghitech.momenta.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cached_posts")
data class CachedPostEntity(
    @PrimaryKey val id: String,
    val username: String,
    val displayName: String?,
    val avatarUrl: String?,
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
    val cachedAt: Long = System.currentTimeMillis()
)

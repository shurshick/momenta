package com.bghitech.momenta.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProfileDto(
    val id: String,
    val username: String,
    @SerialName("display_name")
    val displayName: String? = null,
    @SerialName("avatar_url")
    val avatarUrl: String? = null,
    @SerialName("avatar_key")
    val avatarKey: String? = null,
    val bio: String? = null,
    val country: String? = null,
    val city: String? = null,
    val locale: String = "ru",
    @SerialName("moments_count")
    val momentsCount: Int = 0,
    @SerialName("streak_count")
    val streakCount: Int = 0,
    @SerialName("likes_count")
    val likesCount: Int = 0,
    @SerialName("recent_posts")
    val recentPosts: List<RecentPostDto> = emptyList(),
    @SerialName("created_at")
    val createdAt: String? = null,
    @SerialName("last_seen_at")
    val lastSeenAt: String? = null
)

@Serializable
data class RecentPostDto(
    val id: String,
    @SerialName("preview_url")
    val previewUrl: String? = null,
    @SerialName("thumb_url")
    val thumbUrl: String? = null,
    @SerialName("created_at")
    val createdAt: String? = null
)

@Serializable
data class AvatarOptionDto(
    val key: String
)

@Serializable
data class AvatarListResponseDto(
    val items: List<AvatarOptionDto> = emptyList()
)

@Serializable
data class UpdateAvatarRequestDto(
    @SerialName("avatar_key")
    val avatarKey: String
)

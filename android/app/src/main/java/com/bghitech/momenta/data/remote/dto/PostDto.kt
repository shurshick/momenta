package com.bghitech.momenta.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PostDto(
    val id: String,
    val user: UserDto,
    @SerialName("media_type")
    val mediaType: String = "image",
    @SerialName("preview_url")
    val previewUrl: String,
    @SerialName("thumb_url")
    val thumbUrl: String? = null,
    val caption: String? = null,
    val country: String? = null,
    val city: String? = null,
    @SerialName("likes_count")
    val likesCount: Int = 0,
    @SerialName("comments_count")
    val commentsCount: Int = 0,
    @SerialName("views_count")
    val viewsCount: Int = 0,
    @SerialName("created_at")
    val createdAt: String,
    @SerialName("is_liked")
    val isLiked: Boolean = false
)

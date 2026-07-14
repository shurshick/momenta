package com.bghitech.momenta.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreatePostResponseDto(
    val id: String,
    val status: String
)

@Serializable
data class PostDto(
    val id: String,
    val user: UserDto? = null,
    @SerialName("media_type")
    val mediaType: String = "image",
    @SerialName("preview_url")
    val previewUrl: String? = null,
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
    @SerialName("challenge_date")
    val challengeDate: String? = null,
    @SerialName("created_at")
    val createdAt: String? = null,
    @SerialName("is_liked")
    val isLiked: Boolean = false,
    @SerialName("is_bookmarked")
    val isBookmarked: Boolean = false,
    @SerialName("bookmarked_at")
    val bookmarkedAt: String? = null,
    @SerialName("is_mine")
    val isMine: Boolean = false,
    @SerialName("can_delete")
    val canDelete: Boolean = false
)

@Serializable
data class BestMomentResponseDto(
    val post: PostDto? = null
)

@Serializable
data class CommentDto(
    val id: String,
    @SerialName("post_id")
    val postId: String,
    val user: UserDto,
    val text: String,
    @SerialName("created_at")
    val createdAt: String? = null,
    @SerialName("is_mine")
    val isMine: Boolean = false,
    @SerialName("can_delete")
    val canDelete: Boolean = false
)

@Serializable
data class CommentListResponseDto(
    val items: List<CommentDto> = emptyList()
)

@Serializable
data class CreateCommentRequestDto(
    val text: String
)

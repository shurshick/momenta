package com.bghitech.momenta.domain.model

data class Post(
    val id: String,
    val user: User,
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
    val isLiked: Boolean = false,
    val isMine: Boolean = false,
    val canDelete: Boolean = false
)

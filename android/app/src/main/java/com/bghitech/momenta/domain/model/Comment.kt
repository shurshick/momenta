package com.bghitech.momenta.domain.model

data class Comment(
    val id: String,
    val postId: String,
    val user: User,
    val text: String,
    val createdAt: String,
    val isMine: Boolean,
    val canDelete: Boolean
)

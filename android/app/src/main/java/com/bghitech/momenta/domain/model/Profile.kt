package com.bghitech.momenta.domain.model

data class Profile(
    val id: String,
    val username: String,
    val displayName: String?,
    val avatarUrl: String?,
    val bio: String?,
    val momentsCount: Int,
    val streakCount: Int,
    val likesCount: Int,
    val recentPosts: List<Post>
)

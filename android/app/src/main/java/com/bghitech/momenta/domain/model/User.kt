package com.bghitech.momenta.domain.model

data class User(
    val id: String,
    val username: String,
    val displayName: String?,
    val avatarUrl: String?,
    val avatarKey: String?,
    val email: String?
)

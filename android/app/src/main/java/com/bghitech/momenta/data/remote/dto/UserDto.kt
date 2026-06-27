package com.bghitech.momenta.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserDto(
    val id: String,
    val username: String,
    @SerialName("display_name")
    val displayName: String? = null,
    @SerialName("avatar_url")
    val avatarUrl: String? = null,
    @SerialName("avatar_key")
    val avatarKey: String? = null,
    val email: String? = null
)

@Serializable
data class UserListResponseDto(
    val items: List<UserDto> = emptyList()
)

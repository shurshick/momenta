package com.bghitech.momenta.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UpdateProfileRequestDto(
    @SerialName("display_name")
    val displayName: String? = null,
    val bio: String? = null
)

package com.bghitech.momenta.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FeedResponseDto(
    val items: List<PostDto>,
    @SerialName("next_cursor")
    val nextCursor: String? = null
)

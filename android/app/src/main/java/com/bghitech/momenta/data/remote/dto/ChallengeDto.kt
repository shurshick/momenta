package com.bghitech.momenta.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ChallengeDto(
    val id: String,
    @SerialName("challenge_date")
    val date: String,
    @SerialName("title_ru")
    val title: String,
    @SerialName("description_ru")
    val description: String? = null,
    @SerialName("ends_at")
    val endsAt: String? = null,
    @SerialName("user_posted")
    val userPosted: Boolean = false,
    @SerialName("participants_count")
    val participantsCount: Int = 0
)

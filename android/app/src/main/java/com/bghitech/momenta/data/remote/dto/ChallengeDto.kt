package com.bghitech.momenta.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ChallengeDto(
    val id: String,
    @SerialName("date")
    val date: String,
    val title: String,
    val description: String? = null,
    val prompt: String? = null,
    val source: String = "manual",
    @SerialName("ends_at")
    val endsAt: String? = null,
    @SerialName("user_posted")
    val userPosted: Boolean = false,
    @SerialName("participants_count")
    val participantsCount: Int = 0
)

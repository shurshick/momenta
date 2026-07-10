package com.bghitech.momenta.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ChallengeDto(
    val id: String,
    @SerialName("date")
    val date: String? = null,
    @SerialName("challenge_date")
    val challengeDate: String? = null,
    val title: String? = null,
    @SerialName("title_ru")
    val titleRu: String? = null,
    val description: String? = null,
    @SerialName("description_ru")
    val descriptionRu: String? = null,
    val prompt: String? = null,
    @SerialName("prompt_ru")
    val promptRu: String? = null,
    val source: String = "manual",
    @SerialName("ends_at")
    val endsAt: String? = null,
    @SerialName("user_posted")
    val userPosted: Boolean = false,
    @SerialName("participants_count")
    val participantsCount: Int = 0
)

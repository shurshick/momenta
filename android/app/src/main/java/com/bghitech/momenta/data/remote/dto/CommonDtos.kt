package com.bghitech.momenta.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ApiMessageDto(
    val message: String = "",
    val status: String = "ok",
    val liked: Boolean? = null,
    @SerialName("likes_count")
    val likesCount: Int? = null
)

@Serializable
data class HealthDto(
    val status: String = ""
)

@Serializable
data class ReadyDto(
    val ready: Boolean = false
)

@Serializable
data class ReportRequestDto(
    val reason: String = "Неуместное содержание"
)

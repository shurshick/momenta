package com.bghitech.momenta.domain.model

data class Challenge(
    val id: String,
    val date: String,
    val title: String,
    val description: String?,
    val endsAt: String,
    val userPosted: Boolean,
    val participantsCount: Int
)

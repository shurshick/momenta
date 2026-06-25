package com.bghitech.momenta.domain.model

data class AuthToken(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String
)

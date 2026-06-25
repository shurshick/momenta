package com.bghitech.momenta.domain.repository

import com.bghitech.momenta.core.common.AppResult
import com.bghitech.momenta.domain.model.AuthToken
import com.bghitech.momenta.domain.model.User

interface AuthRepository {
    suspend fun login(usernameOrEmail: String, password: String): AppResult<AuthToken>
    suspend fun register(username: String, email: String, password: String): AppResult<AuthToken>
    suspend fun refreshToken(): AppResult<AuthToken>
    suspend fun logout()
    suspend fun getMe(): AppResult<User>
    suspend fun isLoggedIn(): Boolean
    fun observeAuthState(): kotlinx.coroutines.flow.Flow<Boolean>
}

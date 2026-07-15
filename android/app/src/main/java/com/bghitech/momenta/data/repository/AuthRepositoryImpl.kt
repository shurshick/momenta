package com.bghitech.momenta.data.repository

import com.bghitech.momenta.core.common.AppError
import com.bghitech.momenta.core.common.AppResult
import com.bghitech.momenta.core.common.safeApiCall
import com.bghitech.momenta.core.datastore.TokenStore
import com.bghitech.momenta.core.upload.UploadManager
import com.bghitech.momenta.data.mapper.toDomain
import com.bghitech.momenta.data.remote.MomentaApi
import com.bghitech.momenta.data.remote.dto.LoginRequest
import com.bghitech.momenta.data.remote.dto.RefreshRequest
import com.bghitech.momenta.data.remote.dto.RegisterRequest
import com.bghitech.momenta.domain.model.AuthToken
import com.bghitech.momenta.domain.model.User
import com.bghitech.momenta.domain.repository.AuthRepository
import com.bghitech.momenta.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val api: MomentaApi,
    private val tokenStore: TokenStore,
    private val profileRepository: ProfileRepository,
    private val uploadManager: UploadManager
) : AuthRepository {

    override suspend fun login(usernameOrEmail: String, password: String): AppResult<AuthToken> {
        return safeApiCall {
            val response = api.login(LoginRequest(usernameOrEmail, password))
            tokenStore.saveTokens(response.accessToken, response.refreshToken, response.user.id, response.user.username)
            profileRepository.clearCache()
            resumePendingUploads(response.user.id)
            AuthToken(response.accessToken, response.refreshToken, response.tokenType)
        }
    }

    override suspend fun register(username: String, email: String, password: String): AppResult<AuthToken> {
        return safeApiCall {
            val response = api.register(RegisterRequest(username, email, password))
            tokenStore.saveTokens(response.accessToken, response.refreshToken, response.user.id, response.user.username)
            profileRepository.clearCache()
            resumePendingUploads(response.user.id)
            AuthToken(response.accessToken, response.refreshToken, response.tokenType)
        }
    }

    override suspend fun refreshToken(): AppResult<AuthToken> {
        val refreshToken = tokenStore.getRefreshToken() ?: return AppResult.Error(AppError.Unauthorized)
        val result = safeApiCall {
            val response = api.refresh(RefreshRequest(refreshToken))
            tokenStore.saveTokens(response.accessToken, response.refreshToken, "", "")
            AuthToken(response.accessToken, response.refreshToken, response.tokenType)
        }
        if (result is AppResult.Error) {
            tokenStore.clearTokens()
            profileRepository.clearCache()
        }
        return result
    }

    override suspend fun logout() {
        tokenStore.getUserIdSync()?.let(uploadManager::cancelUploads)
        try { api.logout() } catch (_: Exception) { }
        tokenStore.clearTokens()
        profileRepository.clearCache()
    }

    override suspend fun getMe(): AppResult<User> {
        return safeApiCall {
            api.getMe().toDomain()
        }
    }

    override suspend fun isLoggedIn(): Boolean = tokenStore.getAccessToken() != null

    override fun observeAuthState(): Flow<Boolean> = tokenStore.observeToken().map { it != null }

    private suspend fun resumePendingUploads(userId: String) {
        try {
            uploadManager.resumePendingUploads(userId)
        } catch (_: Exception) {
            // A stale local queue must never turn a valid login into an auth failure.
        }
    }
}

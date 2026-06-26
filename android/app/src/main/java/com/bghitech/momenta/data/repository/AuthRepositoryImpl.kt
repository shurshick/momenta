package com.bghitech.momenta.data.repository

import com.bghitech.momenta.core.common.AppError
import com.bghitech.momenta.core.common.AppResult
import com.bghitech.momenta.core.datastore.TokenStore
import com.bghitech.momenta.data.mapper.toDomain
import com.bghitech.momenta.data.remote.MomentaApi
import com.bghitech.momenta.data.remote.dto.LoginRequest
import com.bghitech.momenta.data.remote.dto.RefreshRequest
import com.bghitech.momenta.data.remote.dto.RegisterRequest
import com.bghitech.momenta.domain.model.AuthToken
import com.bghitech.momenta.domain.model.User
import com.bghitech.momenta.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val api: MomentaApi,
    private val tokenStore: TokenStore
) : AuthRepository {

    override suspend fun login(usernameOrEmail: String, password: String): AppResult<AuthToken> {
        return try {
            val response = api.login(LoginRequest(usernameOrEmail, password))
            tokenStore.saveTokens(response.accessToken, response.refreshToken, response.user.id, response.user.username)
            AppResult.Success(AuthToken(response.accessToken, response.refreshToken, response.tokenType))
        } catch (e: Exception) {
            AppResult.Error(mapError(e))
        }
    }

    override suspend fun register(username: String, email: String, password: String): AppResult<AuthToken> {
        return try {
            val response = api.register(RegisterRequest(username, email, password))
            tokenStore.saveTokens(response.accessToken, response.refreshToken, response.user.id, response.user.username)
            AppResult.Success(AuthToken(response.accessToken, response.refreshToken, response.tokenType))
        } catch (e: Exception) {
            AppResult.Error(mapError(e))
        }
    }

    override suspend fun refreshToken(): AppResult<AuthToken> {
        return try {
            val refreshToken = tokenStore.getRefreshToken() ?: return AppResult.Error(AppError.Unauthorized)
            val response = api.refresh(RefreshRequest(refreshToken))
            tokenStore.saveTokens(response.accessToken, response.refreshToken, "", "")
            AppResult.Success(AuthToken(response.accessToken, response.refreshToken, response.tokenType))
        } catch (e: Exception) {
            tokenStore.clearTokens()
            AppResult.Error(mapError(e))
        }
    }

    override suspend fun logout() {
        try { api.logout() } catch (_: Exception) { }
        tokenStore.clearTokens()
    }

    override suspend fun getMe(): AppResult<User> {
        return try {
            val user = api.getMe()
            AppResult.Success(user.toDomain())
        } catch (e: Exception) {
            AppResult.Error(mapError(e))
        }
    }

    override suspend fun isLoggedIn(): Boolean = tokenStore.getAccessToken() != null

    override fun observeAuthState(): Flow<Boolean> = tokenStore.observeToken().map { it != null }

    private fun mapError(e: Exception): AppError {
        return when (e) {
            is HttpException -> {
                val serverMessage = try {
                    e.response()?.errorBody()?.string()?.let { body ->
                        kotlinx.serialization.json.Json { ignoreUnknownKeys = true }
                            .decodeFromString<Map<String, String>>(body)["detail"]
                    }
                } catch (_: Exception) { null }

                when (e.code()) {
                    401 -> AppError.Unauthorized
                    409 -> AppError.Validation(serverMessage ?: "Данные уже заняты")
                    in 400..499 -> AppError.Validation(serverMessage ?: "Ошибка запроса")
                    in 500..599 -> AppError.Server
                    else -> AppError.Unknown(serverMessage ?: e.message())
                }
            }
            is IOException -> {
                if (e.message?.contains("timeout") == true || e.message?.contains("Unable to resolve host") == true) {
                    AppError.Network
                } else {
                    AppError.Unknown(e.message)
                }
            }
            else -> AppError.Unknown(e.message)
        }
    }
}

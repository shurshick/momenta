package com.bghitech.momenta.data.remote.interceptor

import com.bghitech.momenta.BuildConfig
import com.bghitech.momenta.core.datastore.AuthTokenProvider
import com.bghitech.momenta.core.datastore.TokenStore
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.Route
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthInterceptor @Inject constructor(
    private val tokenStore: TokenStore,
    private val tokenProvider: AuthTokenProvider
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        if (!shouldAttachAuth(originalRequest)) {
            return chain.proceed(originalRequest)
        }

        val accessToken = tokenProvider.accessToken() ?: runBlocking { tokenStore.getAccessToken() }

        val request = if (accessToken != null) {
            originalRequest.newBuilder()
                .header("Authorization", "Bearer $accessToken")
                .build()
        } else {
            originalRequest
        }

        return chain.proceed(request)
    }

    private fun shouldAttachAuth(request: Request): Boolean {
        val path = request.url.encodedPath
        if (!path.startsWith("/api/v1/")) return false
        return path !in PublicPaths
    }

    private companion object {
        val PublicPaths = setOf(
            "/api/v1/auth/login",
            "/api/v1/auth/register",
            "/api/v1/auth/refresh",
        )
    }
}

@Singleton
class TokenAuthenticator @Inject constructor(
    private val tokenStore: TokenStore,
    private val tokenProvider: AuthTokenProvider
) : Authenticator {
    private val refreshLock = Any()
    private val refreshClient = OkHttpClient()

    override fun authenticate(route: Route?, response: Response): Request? {
        if (response.request.url.encodedPath == "/api/v1/auth/refresh") return null
        if (response.request.header("Authorization") == null) return null
        if (responseCount(response) >= 2) return null

        val requestToken = response.request.header("Authorization")?.removePrefix("Bearer ")
        val cachedToken = tokenProvider.accessToken() ?: runBlocking { tokenStore.getAccessToken() }
        if (!cachedToken.isNullOrBlank() && cachedToken != requestToken) {
            return response.request.newBuilder()
                .header("Authorization", "Bearer $cachedToken")
                .build()
        }

        val newAccessToken = synchronized(refreshLock) {
            val currentToken = tokenProvider.accessToken() ?: runBlocking { tokenStore.getAccessToken() }
            if (!currentToken.isNullOrBlank() && currentToken != requestToken) {
                currentToken
            } else {
                refreshAccessToken()
            }
        }

        if (newAccessToken.isNullOrBlank()) return null

        return response.request.newBuilder()
            .header("Authorization", "Bearer $newAccessToken")
            .build()
    }

    private fun refreshAccessToken(): String? {
        val refreshToken = tokenProvider.refreshToken() ?: runBlocking { tokenStore.getRefreshToken() }
        if (refreshToken.isNullOrBlank()) {
            runBlocking { tokenStore.clearTokens() }
            return null
        }

        return try {
            val baseUrl = BuildConfig.DEFAULT_SERVER_URL.trimEnd('/')
            val body = JSONObject()
                .put("refresh_token", refreshToken)
                .toString()
                .toRequestBody(JsonMediaType)
            val request = Request.Builder()
                .url("$baseUrl/api/v1/auth/refresh")
                .post(body)
                .build()

            refreshClient.newCall(request).execute().use { refreshResponse ->
                if (!refreshResponse.isSuccessful) {
                    runBlocking { tokenStore.clearTokens() }
                    return null
                }

                val responseBody = refreshResponse.body?.string().orEmpty()
                val json = JSONObject(responseBody)
                val accessToken = json.optString("access_token")
                val newRefreshToken = json.optString("refresh_token")
                if (accessToken.isBlank() || newRefreshToken.isBlank()) {
                    runBlocking { tokenStore.clearTokens() }
                    return null
                }
                runBlocking { tokenStore.saveTokens(accessToken, newRefreshToken, "", "") }
                accessToken
            }
        } catch (_: Exception) {
            runBlocking { tokenStore.clearTokens() }
            null
        }
    }

    private fun responseCount(response: Response): Int {
        var count = 1
        var priorResponse = response.priorResponse
        while (priorResponse != null) {
            count++
            priorResponse = priorResponse.priorResponse
        }
        return count
    }

    private companion object {
        val JsonMediaType = "application/json".toMediaType()
    }
}

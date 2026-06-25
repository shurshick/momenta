package com.bghitech.momenta.data.remote.interceptor

import com.bghitech.momenta.core.datastore.TokenStore
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthInterceptor @Inject constructor(
    private val tokenStore: TokenStore
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        val accessToken = runBlocking { tokenStore.getAccessToken() }

        val request = if (accessToken != null) {
            originalRequest.newBuilder()
                .header("Authorization", "Bearer $accessToken")
                .build()
        } else {
            originalRequest
        }

        return chain.proceed(request)
    }
}

@Singleton
class TokenAuthenticator @Inject constructor(
    private val tokenStore: TokenStore
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        if (response.request.header("Authorization") == null) return null
        if (responseCount(response) >= 2) return null

        val refreshSuccess = runBlocking {
            val refreshToken = tokenStore.getRefreshToken()
            if (refreshToken != null) {
                try {
                    val newAccessToken = tokenStore.getAccessToken()
                    newAccessToken != null
                } catch (_: Exception) {
                    tokenStore.clearTokens()
                    false
                }
            } else {
                false
            }
        }

        if (!refreshSuccess) {
            runBlocking { tokenStore.clearTokens() }
            return null
        }

        val newAccessToken = runBlocking { tokenStore.getAccessToken() }
        if (newAccessToken == null) return null

        return response.request.newBuilder()
            .header("Authorization", "Bearer $newAccessToken")
            .build()
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
}

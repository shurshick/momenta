package com.bghitech.momenta.core.datastore

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "momenta_prefs")

@Singleton
class TokenStore @Inject constructor(
    @ApplicationContext private val context: Context,
    private val tokenProvider: AuthTokenProvider
) {
    private object Keys {
        val ACCESS_TOKEN = stringPreferencesKey("access_token")
        val REFRESH_TOKEN = stringPreferencesKey("refresh_token")
        val USER_ID = stringPreferencesKey("user_id")
        val USERNAME = stringPreferencesKey("username")
        val SERVER_URL = stringPreferencesKey("server_url")
        val FIRST_LAUNCH_COMPLETED = booleanPreferencesKey("first_launch_completed")
        val LOGGING_ENABLED = booleanPreferencesKey("logging_enabled")
    }

    suspend fun saveTokens(accessToken: String, refreshToken: String, userId: String, username: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.ACCESS_TOKEN] = accessToken
            prefs[Keys.REFRESH_TOKEN] = refreshToken
            if (userId.isNotBlank()) prefs[Keys.USER_ID] = userId
            if (username.isNotBlank()) prefs[Keys.USERNAME] = username
        }
        tokenProvider.update(accessToken, refreshToken)
    }

    suspend fun getAccessToken(): String? {
        tokenProvider.accessToken()?.let { return it }
        loadTokensIntoProvider()
        return tokenProvider.accessToken()
    }

    suspend fun getRefreshToken(): String? {
        tokenProvider.refreshToken()?.let { return it }
        loadTokensIntoProvider()
        return tokenProvider.refreshToken()
    }

    private suspend fun loadTokensIntoProvider() {
        val prefs = context.dataStore.data.first()
        tokenProvider.update(prefs[Keys.ACCESS_TOKEN], prefs[Keys.REFRESH_TOKEN])
    }

    suspend fun clearTokens() {
        context.dataStore.edit { prefs ->
            prefs.remove(Keys.ACCESS_TOKEN)
            prefs.remove(Keys.REFRESH_TOKEN)
            prefs.remove(Keys.USER_ID)
            prefs.remove(Keys.USERNAME)
        }
        tokenProvider.clear()
    }

    fun observeToken(): Flow<String?> {
        return context.dataStore.data.map { it[Keys.ACCESS_TOKEN] }
    }

    fun getServerUrl(): Flow<String> {
        return context.dataStore.data.map { it[Keys.SERVER_URL] ?: "https://momenta.bghitech.ru" }
    }

    suspend fun setServerUrl(url: String) {
        context.dataStore.edit { it[Keys.SERVER_URL] = url }
    }

    fun getLoggingEnabled(): Flow<Boolean> {
        return context.dataStore.data.map { it[Keys.LOGGING_ENABLED] ?: true }
    }

    suspend fun setLoggingEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.LOGGING_ENABLED] = enabled }
    }

    suspend fun isFirstLaunchCompleted(): Boolean {
        return context.dataStore.data.map { it[Keys.FIRST_LAUNCH_COMPLETED] ?: false }.first()
    }

    suspend fun setFirstLaunchCompleted() {
        context.dataStore.edit { it[Keys.FIRST_LAUNCH_COMPLETED] = true }
    }
}

package com.bghitech.momenta.core.datastore

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
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
    private val tokenPrefs = context.getSharedPreferences("momenta_auth_tokens", Context.MODE_PRIVATE)
    private val tokenState = MutableStateFlow<String?>(null)

    private object Keys {
        val ACCESS_TOKEN = stringPreferencesKey("access_token")
        val REFRESH_TOKEN = stringPreferencesKey("refresh_token")
        val USER_ID = stringPreferencesKey("user_id")
        val USERNAME = stringPreferencesKey("username")
        val SERVER_URL = stringPreferencesKey("server_url")
        val FIRST_LAUNCH_COMPLETED = booleanPreferencesKey("first_launch_completed")
        val LOGGING_ENABLED = booleanPreferencesKey("logging_enabled")

        const val ACCESS_TOKEN_NAME = "access_token"
        const val REFRESH_TOKEN_NAME = "refresh_token"
        const val USER_ID_NAME = "user_id"
        const val USERNAME_NAME = "username"
        const val TOKENS_CLEARED_NAME = "tokens_cleared"
    }

    init {
        loadSyncTokensIntoProvider()
    }

    suspend fun warmUp() {
        if (!tokenProvider.accessToken().isNullOrBlank()) return
        if (tokenPrefs.getBoolean(Keys.TOKENS_CLEARED_NAME, false)) return

        val prefs = context.dataStore.data.first()
        val accessToken = prefs[Keys.ACCESS_TOKEN]
        val refreshToken = prefs[Keys.REFRESH_TOKEN]
        if (!accessToken.isNullOrBlank() && !refreshToken.isNullOrBlank()) {
            saveTokensSync(
                accessToken = accessToken,
                refreshToken = refreshToken,
                userId = prefs[Keys.USER_ID].orEmpty(),
                username = prefs[Keys.USERNAME].orEmpty()
            )
        }
    }

    suspend fun saveTokens(accessToken: String, refreshToken: String, userId: String, username: String) {
        saveTokensSync(accessToken, refreshToken, userId, username)
    }

    fun saveTokensSync(accessToken: String, refreshToken: String, userId: String, username: String) {
        val editor = tokenPrefs.edit()
            .putString(Keys.ACCESS_TOKEN_NAME, accessToken)
            .putString(Keys.REFRESH_TOKEN_NAME, refreshToken)
            .remove(Keys.TOKENS_CLEARED_NAME)
        if (userId.isNotBlank()) editor.putString(Keys.USER_ID_NAME, userId)
        if (username.isNotBlank()) editor.putString(Keys.USERNAME_NAME, username)
        editor.apply()
        tokenProvider.update(accessToken, refreshToken)
        tokenState.value = accessToken
    }

    suspend fun getAccessToken(): String? {
        tokenProvider.accessToken()?.let { return it }
        loadSyncTokensIntoProvider()
        return tokenProvider.accessToken()
    }

    suspend fun getRefreshToken(): String? {
        tokenProvider.refreshToken()?.let { return it }
        loadSyncTokensIntoProvider()
        return tokenProvider.refreshToken()
    }

    suspend fun clearTokens() {
        clearTokensSync()
    }

    fun clearTokensSync() {
        tokenPrefs.edit()
            .remove(Keys.ACCESS_TOKEN_NAME)
            .remove(Keys.REFRESH_TOKEN_NAME)
            .remove(Keys.USER_ID_NAME)
            .remove(Keys.USERNAME_NAME)
            .putBoolean(Keys.TOKENS_CLEARED_NAME, true)
            .apply()
        tokenProvider.clear()
        tokenState.value = null
    }

    fun observeToken(): Flow<String?> = tokenState

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

    private fun loadSyncTokensIntoProvider() {
        val accessToken = tokenPrefs.getString(Keys.ACCESS_TOKEN_NAME, null)
        val refreshToken = tokenPrefs.getString(Keys.REFRESH_TOKEN_NAME, null)
        tokenProvider.update(accessToken, refreshToken)
        tokenState.value = accessToken
    }
}

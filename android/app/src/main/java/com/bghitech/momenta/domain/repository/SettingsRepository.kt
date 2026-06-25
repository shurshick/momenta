package com.bghitech.momenta.domain.repository

import com.bghitech.momenta.core.common.AppResult
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun getServerUrl(): Flow<String>
    suspend fun setServerUrl(url: String)
    suspend fun checkConnection(): AppResult<Unit>
    fun getLoggingEnabled(): Flow<Boolean>
    suspend fun setLoggingEnabled(enabled: Boolean)
    suspend fun clearAllData()
}

package com.bghitech.momenta.data.repository

import com.bghitech.momenta.core.common.AppResult
import com.bghitech.momenta.core.common.safeApiCall
import com.bghitech.momenta.core.datastore.TokenStore
import com.bghitech.momenta.core.upload.UploadManager
import com.bghitech.momenta.data.remote.MomentaApi
import com.bghitech.momenta.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    private val tokenStore: TokenStore,
    private val api: MomentaApi,
    private val uploadManager: UploadManager
) : SettingsRepository {

    override fun getServerUrl(): Flow<String> = tokenStore.getServerUrl()

    override suspend fun setServerUrl(url: String) {
        tokenStore.setServerUrl(url)
    }

    override suspend fun checkConnection(): AppResult<Unit> {
        return safeApiCall {
            api.health()
            api.ready()
            Unit
        }
    }

    override suspend fun clearAllData() {
        tokenStore.getUserIdSync()?.let(uploadManager::cancelUploads)
        tokenStore.clearTokens()
    }
}

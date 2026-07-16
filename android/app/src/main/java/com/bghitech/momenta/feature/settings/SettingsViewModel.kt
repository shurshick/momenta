package com.bghitech.momenta.feature.settings

import android.content.Context
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bghitech.momenta.BuildConfig
import com.bghitech.momenta.core.datastore.TokenStore
import com.bghitech.momenta.core.upload.UploadManager
import com.bghitech.momenta.data.local.dao.ChallengeDao
import com.bghitech.momenta.data.local.dao.PostDao
import com.bghitech.momenta.data.local.dao.UploadQueueDao
import com.bghitech.momenta.data.local.entity.UploadQueueEntity
import com.bghitech.momenta.domain.repository.ProfileRepository
import com.bghitech.momenta.domain.usecase.LogoutUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class SettingsUiState(
    val uploads: List<UploadQueueEntity> = emptyList(),
    val cacheSizeBytes: Long = 0,
    val isClearingCache: Boolean = false,
    val message: String? = null
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val logoutUseCase: LogoutUseCase,
    private val tokenStore: TokenStore,
    private val uploadQueueDao: UploadQueueDao,
    private val postDao: PostDao,
    private val challengeDao: ChallengeDao,
    private val uploadManager: UploadManager,
    private val profileRepository: ProfileRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val accountId = tokenStore.getUserIdSync().orEmpty()
    private val _state = MutableStateFlow(SettingsUiState())
    val state: StateFlow<SettingsUiState> = _state.asStateFlow()

    init {
        if (accountId.isNotBlank()) {
            viewModelScope.launch {
                uploadQueueDao.observePendingForAccount(accountId).collect { uploads ->
                    _state.update { it.copy(uploads = uploads) }
                }
            }
        }
        refreshCacheSize()
    }

    fun retryUpload(localId: String) {
        val upload = _state.value.uploads.firstOrNull { it.localId == localId } ?: return
        viewModelScope.launch {
            uploadQueueDao.updateStatus(localId, "pending")
            uploadManager.enqueueUpload(upload.accountId, localId)
            _state.update { it.copy(message = "Публикация снова поставлена в очередь") }
        }
    }

    fun deleteUpload(localId: String) {
        val upload = _state.value.uploads.firstOrNull { it.localId == localId } ?: return
        viewModelScope.launch {
            uploadManager.cancelUpload(upload.accountId, localId)
            uploadQueueDao.deleteById(localId)
            postDao.deleteById(upload.accountId, localId)
            withContext(Dispatchers.IO) {
                val source = File(upload.filePath)
                if (isInsideAppCache(source)) source.delete()
            }
            refreshCacheSize()
            _state.update { it.copy(message = "Публикация удалена из очереди") }
        }
    }

    fun clearCache() {
        if (_state.value.isClearingCache) return
        viewModelScope.launch {
            _state.update { it.copy(isClearingCache = true) }
            val protectedPaths = uploadQueueDao.getPendingForAccount(accountId)
                .mapNotNull { runCatching { File(it.filePath).canonicalPath }.getOrNull() }
                .toSet()
            withContext(Dispatchers.IO) {
                clearCacheDirectory(context.cacheDir, protectedPaths)
                context.externalCacheDir?.let { clearCacheDirectory(it, protectedPaths) }
                context.getSharedPreferences("momenta_profile_cache", Context.MODE_PRIVATE)
                    .edit()
                    .remove("profile_$accountId")
                    .apply()
            }
            if (accountId.isNotBlank()) {
                postDao.clearDisposableCache(accountId)
                challengeDao.clearForAccount(accountId)
            }
            profileRepository.clearCache()
            val size = cacheSize()
            _state.update {
                it.copy(
                    cacheSizeBytes = size,
                    isClearingCache = false,
                    message = "Кэш очищен"
                )
            }
        }
    }

    fun diagnosticsText(): String = buildString {
        appendLine("Момент ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})")
        appendLine("Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})")
        appendLine("Устройство: ${Build.MANUFACTURER} ${Build.MODEL}")
        appendLine("Ожидающих публикаций: ${_state.value.uploads.size}")
        append("Кэш: ${_state.value.cacheSizeBytes} байт")
    }

    fun consumeMessage() {
        _state.update { it.copy(message = null) }
    }

    suspend fun logout() {
        logoutUseCase()
    }

    private fun refreshCacheSize() {
        viewModelScope.launch {
            _state.update { it.copy(cacheSizeBytes = cacheSize()) }
        }
    }

    private suspend fun cacheSize(): Long = withContext(Dispatchers.IO) {
        cacheDirectorySize(context.cacheDir) +
            (context.externalCacheDir?.let(::cacheDirectorySize) ?: 0L)
    }

    private fun isInsideAppCache(file: File): Boolean {
        val path = runCatching { file.canonicalPath }.getOrNull() ?: return false
        val roots = listOfNotNull(context.cacheDir, context.externalCacheDir)
        return roots.any { root ->
            val rootPath = runCatching { root.canonicalPath }.getOrNull() ?: return@any false
            path == rootPath || path.startsWith("$rootPath${File.separator}")
        }
    }
}

internal fun cacheDirectorySize(file: File): Long {
    if (!file.exists()) return 0
    if (file.isFile) return file.length()
    return file.listFiles()?.sumOf(::cacheDirectorySize) ?: 0
}

internal fun clearCacheDirectory(directory: File, protectedPaths: Set<String>) {
    directory.listFiles()?.forEach { file ->
        val canonical = runCatching { file.canonicalPath }.getOrNull() ?: return@forEach
        if (canonical in protectedPaths) return@forEach
        if (file.isDirectory) {
            clearCacheDirectory(file, protectedPaths)
            if (file.listFiles().isNullOrEmpty()) file.delete()
        } else {
            file.delete()
        }
    }
}

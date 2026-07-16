package com.bghitech.momenta.feature.settings

import android.content.Context
import android.content.SharedPreferences
import com.bghitech.momenta.core.datastore.TokenStore
import com.bghitech.momenta.core.upload.UploadManager
import com.bghitech.momenta.data.local.dao.ChallengeDao
import com.bghitech.momenta.data.local.dao.PostDao
import com.bghitech.momenta.data.local.dao.UploadQueueDao
import com.bghitech.momenta.data.local.entity.UploadQueueEntity
import com.bghitech.momenta.domain.repository.ProfileRepository
import com.bghitech.momenta.domain.usecase.LogoutUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {
    @get:Rule
    val temporaryFolder = TemporaryFolder()

    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun queuedUploadsAreShownAndCanBeRetried() = runTest(dispatcher) {
        val upload = queuedUpload(File(temporaryFolder.root, "pending.jpg"))
        val queue = MutableStateFlow(listOf(upload))
        val dependencies = dependencies(queue)
        val viewModel = dependencies.viewModel()
        advanceUntilIdle()

        assertEquals(listOf(upload), viewModel.state.value.uploads)

        viewModel.retryUpload(upload.localId)
        advanceUntilIdle()

        coVerify { dependencies.uploadQueueDao.updateStatus(upload.localId, "pending", any()) }
        verify { dependencies.uploadManager.enqueueUpload(upload.accountId, upload.localId) }
    }

    @Test
    fun clearingCachePreservesQueuedSourceFile() {
        val queuedFile = File(temporaryFolder.root, "queued.jpg").apply { writeText("keep") }
        val disposableFile = File(temporaryFolder.root, "preview.tmp").apply { writeText("remove") }
        val protectedPaths = setOf(queuedFile.canonicalPath)

        clearCacheDirectory(temporaryFolder.root, protectedPaths)

        assertTrue(queuedFile.exists())
        assertFalse(disposableFile.exists())
        assertEquals(queuedFile.length(), cacheDirectorySize(temporaryFolder.root))
    }

    private fun dependencies(queue: MutableStateFlow<List<UploadQueueEntity>>): Dependencies {
        val context = mockk<Context>()
        val preferences = mockk<SharedPreferences>(relaxed = true)
        every { context.cacheDir } returns temporaryFolder.root
        every { context.externalCacheDir } returns null
        every { context.getSharedPreferences(any(), any()) } returns preferences

        val tokenStore = mockk<TokenStore>()
        every { tokenStore.getUserIdSync() } returns "user-1"

        val uploadQueueDao = mockk<UploadQueueDao>(relaxed = true)
        every { uploadQueueDao.observePendingForAccount("user-1") } returns queue

        return Dependencies(
            context = context,
            tokenStore = tokenStore,
            uploadQueueDao = uploadQueueDao,
            postDao = mockk(relaxed = true),
            challengeDao = mockk(relaxed = true),
            uploadManager = mockk(relaxed = true),
            profileRepository = mockk(relaxed = true),
            logoutUseCase = mockk(relaxed = true)
        )
    }

    private fun queuedUpload(file: File) = UploadQueueEntity(
        localId = "local-1",
        accountId = "user-1",
        challengeId = "challenge-1",
        challengeDate = "2026-07-16",
        filePath = file.absolutePath,
        caption = "Test",
        country = null,
        city = null,
        mediaType = "image",
        status = "failed"
    )

    private data class Dependencies(
        val context: Context,
        val tokenStore: TokenStore,
        val uploadQueueDao: UploadQueueDao,
        val postDao: PostDao,
        val challengeDao: ChallengeDao,
        val uploadManager: UploadManager,
        val profileRepository: ProfileRepository,
        val logoutUseCase: LogoutUseCase
    ) {
        fun viewModel() = SettingsViewModel(
            logoutUseCase = logoutUseCase,
            tokenStore = tokenStore,
            uploadQueueDao = uploadQueueDao,
            postDao = postDao,
            challengeDao = challengeDao,
            uploadManager = uploadManager,
            profileRepository = profileRepository,
            context = context
        )
    }
}

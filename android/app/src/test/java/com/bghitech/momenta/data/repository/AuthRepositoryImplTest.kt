package com.bghitech.momenta.data.repository

import com.bghitech.momenta.core.datastore.TokenStore
import com.bghitech.momenta.core.upload.UploadManager
import com.bghitech.momenta.data.remote.MomentaApi
import com.bghitech.momenta.domain.repository.ProfileRepository
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import java.io.IOException
import kotlinx.coroutines.test.runTest
import org.junit.Test

class AuthRepositoryImplTest {

    @Test
    fun logoutClearsLocalSessionWhenApiCallFails() = runTest {
        val api = mockk<MomentaApi>()
        val tokenStore = mockk<TokenStore>()
        val uploadManager = mockk<UploadManager>()
        val profileRepository = mockk<ProfileRepository>()
        coEvery { api.logout() } throws IOException("offline")
        coEvery { tokenStore.clearTokens() } just Runs
        every { tokenStore.getUserIdSync() } returns "user-1"
        every { uploadManager.cancelUploads("user-1") } just Runs
        coEvery { profileRepository.clearCache() } just Runs
        val repository = AuthRepositoryImpl(api, tokenStore, profileRepository, uploadManager)

        repository.logout()

        coVerify(exactly = 1) { tokenStore.clearTokens() }
        coVerify(exactly = 1) { profileRepository.clearCache() }
    }
}

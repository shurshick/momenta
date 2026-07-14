package com.bghitech.momenta.data.repository

import com.bghitech.momenta.core.common.AppError
import com.bghitech.momenta.core.common.AppResult
import com.bghitech.momenta.core.datastore.TokenStore
import com.bghitech.momenta.data.local.dao.ChallengeDao
import com.bghitech.momenta.data.local.dao.PostDao
import com.bghitech.momenta.data.remote.MomentaApi
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import java.io.IOException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FeedTodayRepositoryTest {

    @Test
    fun challengeNetworkErrorIsNotMaskedByCache() = runTest {
        val api = mockk<MomentaApi>()
        coEvery { api.getTodayChallenge() } throws IOException("offline")
        val repository = ChallengeRepositoryImpl(api, mockk<ChallengeDao>())

        val result = repository.getTodayChallenge()

        assertTrue(result is AppResult.Error)
        assertEquals(AppError.Network, (result as AppResult.Error).error)
    }

    @Test
    fun bestMomentNetworkErrorIsNotMaskedByCache() = runTest {
        val api = mockk<MomentaApi>()
        coEvery { api.getBestMoment() } throws IOException("offline")
        val tokenStore = mockk<TokenStore>()
        every { tokenStore.getUserIdSync() } returns "test-user"
        val repository = FeedRepositoryImpl(api, mockk<PostDao>(), tokenStore)

        val result = repository.getBestMoment()

        assertTrue(result is AppResult.Error)
        assertEquals(AppError.Network, (result as AppResult.Error).error)
    }
}

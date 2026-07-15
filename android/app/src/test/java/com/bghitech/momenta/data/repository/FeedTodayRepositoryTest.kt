package com.bghitech.momenta.data.repository

import com.bghitech.momenta.core.common.AppError
import com.bghitech.momenta.core.common.AppResult
import com.bghitech.momenta.core.datastore.TokenStore
import com.bghitech.momenta.data.local.dao.ChallengeDao
import com.bghitech.momenta.data.local.dao.PostDao
import com.bghitech.momenta.data.local.entity.CachedChallengeEntity
import com.bghitech.momenta.data.remote.MomentaApi
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.coVerify
import io.mockk.verify
import java.io.IOException
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.flow.first
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FeedTodayRepositoryTest {

    @Test
    fun challengeNetworkErrorIsNotMaskedByCache() = runTest {
        val api = mockk<MomentaApi>()
        coEvery { api.getTodayChallenge() } throws IOException("offline")
        val repository = ChallengeRepositoryImpl(api, mockk<ChallengeDao>(), mockk<TokenStore>())

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

    @Test
    fun challengeCacheIsReadOnlyForCurrentAccount() = runTest {
        val api = mockk<MomentaApi>()
        val dao = mockk<ChallengeDao>()
        val tokenStore = mockk<TokenStore>()
        every { tokenStore.getUserIdSync() } returns "account-a"
        coEvery { dao.getChallengeByDate("account-a", any()) } returns CachedChallengeEntity(
            accountId = "account-a",
            id = "challenge-1",
            date = "2026-07-15",
            title = "Today",
            description = null,
            endsAt = null,
            userPosted = true,
            participantsCount = 1
        )
        val repository = ChallengeRepositoryImpl(api, dao, tokenStore)

        repository.getCachedChallenge()

        coVerify(exactly = 1) { dao.getChallengeByDate("account-a", any()) }
    }

    @Test
    fun feedCacheIsNotOpenedWithoutAccountId() = runTest {
        val api = mockk<MomentaApi>()
        val dao = mockk<PostDao>()
        val tokenStore = mockk<TokenStore>()
        every { tokenStore.getUserIdSync() } returns null
        val repository = FeedRepositoryImpl(api, dao, tokenStore)

        assertTrue(repository.observeTodayFeed().first().isEmpty())
        verify(exactly = 0) { dao.observePostsByChallengeDate(any(), any()) }
    }
}

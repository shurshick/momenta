package com.bghitech.momenta.feature.profile

import com.bghitech.momenta.core.common.AppError
import com.bghitech.momenta.core.common.AppResult
import com.bghitech.momenta.domain.model.Profile
import com.bghitech.momenta.domain.repository.FeedRepository
import com.bghitech.momenta.domain.repository.PostRepository
import com.bghitech.momenta.domain.repository.ProfileRepository
import com.bghitech.momenta.domain.usecase.GetMyProfileUseCase
import com.bghitech.momenta.domain.usecase.LogoutUseCase
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {
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
    fun cachedProfileIsShownWhenNetworkIsOffline() = runTest(dispatcher) {
        val cached = Profile(
            id = "user-1",
            username = "cached-user",
            displayName = "Cached User",
            avatarUrl = null,
            avatarKey = "avatar_01",
            bio = "Offline profile",
            momentsCount = 7,
            streakCount = 3,
            likesCount = 11,
            recentPosts = emptyList()
        )
        val getProfile = mockk<GetMyProfileUseCase>()
        val profileRepository = mockk<ProfileRepository>()
        val feedRepository = mockk<FeedRepository>()
        coEvery { getProfile.getCached() } returns cached
        coEvery { getProfile.invoke() } returns AppResult.Error(AppError.Network)
        every { feedRepository.observeBookmarks() } returns flowOf(emptyList())
        coEvery { feedRepository.syncBookmarks() } returns AppResult.Error(AppError.Network)

        val viewModel = ProfileViewModel(
            getMyProfileUseCase = getProfile,
            profileRepository = profileRepository,
            feedRepository = feedRepository,
            postRepository = mockk<PostRepository>(),
            logoutUseCase = mockk<LogoutUseCase>()
        )
        advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertTrue(state.isOffline)
        assertEquals("@cached-user", state.username)
        assertEquals("Cached User", state.displayName)
        assertEquals(7, state.momentsCount)
        assertNull(state.error)
    }
}

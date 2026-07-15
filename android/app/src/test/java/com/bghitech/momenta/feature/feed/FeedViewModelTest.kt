package com.bghitech.momenta.feature.feed

import com.bghitech.momenta.core.common.AppResult
import com.bghitech.momenta.domain.model.Post
import com.bghitech.momenta.domain.model.User
import com.bghitech.momenta.domain.repository.FeedRepository
import com.bghitech.momenta.domain.repository.PostRepository
import com.bghitech.momenta.domain.repository.ProfileRepository
import com.bghitech.momenta.domain.usecase.GetTodayFeedUseCase
import com.bghitech.momenta.domain.usecase.LikePostUseCase
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
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
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FeedViewModelTest {
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
    fun feedStoreUpdatesDoNotReplaceServerUserSuggestions() = runTest(dispatcher) {
        val todayPosts = MutableStateFlow(emptyList<Post>())
        val feedRepository = mockk<FeedRepository>()
        val getTodayFeed = mockk<GetTodayFeedUseCase>()
        val allUsers = listOf(user("1", "alice"), user("2", "bob"))
        every { feedRepository.observeTodayFeed() } returns todayPosts
        coEvery { getTodayFeed.invoke(any(), any()) } returns AppResult.Success(emptyList())
        coEvery { feedRepository.getUserSuggestions() } returns AppResult.Success(allUsers)

        val viewModel = FeedViewModel(
            getTodayFeedUseCase = getTodayFeed,
            likePostUseCase = mockk<LikePostUseCase>(),
            feedRepository = feedRepository,
            postRepository = mockk<PostRepository>(),
            profileRepository = mockk<ProfileRepository>()
        )
        advanceUntilIdle()
        assertEquals(allUsers, viewModel.state.value.suggestedUsers)

        todayPosts.value = listOf(post(allUsers.first()))
        advanceUntilIdle()

        assertEquals(allUsers, viewModel.state.value.suggestedUsers)
    }

    private fun user(id: String, username: String) = User(
        id = id,
        username = username,
        displayName = username.replaceFirstChar(Char::uppercaseChar),
        avatarUrl = null,
        avatarKey = null,
        email = null
    )

    private fun post(user: User) = Post(
        id = "post-${user.id}",
        user = user,
        mediaType = "image",
        previewUrl = "https://example.test/post.jpg",
        thumbUrl = null,
        caption = null,
        country = null,
        city = null,
        likesCount = 0,
        commentsCount = 0,
        viewsCount = 0,
        challengeDate = "2026-07-15",
        createdAt = "2026-07-15T08:00:00Z"
    )
}

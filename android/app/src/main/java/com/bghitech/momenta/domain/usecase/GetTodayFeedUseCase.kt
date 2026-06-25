package com.bghitech.momenta.domain.usecase

import com.bghitech.momenta.core.common.AppResult
import com.bghitech.momenta.domain.model.Post
import com.bghitech.momenta.domain.repository.FeedRepository
import javax.inject.Inject

class GetTodayFeedUseCase @Inject constructor(
    private val feedRepository: FeedRepository
) {
    suspend operator fun invoke(cursor: String? = null, limit: Int = 20): AppResult<List<Post>> {
        val result = feedRepository.getTodayFeed(cursor, limit)
        if (result is AppResult.Success && cursor == null) {
            feedRepository.cacheFeed(result.data)
        }
        return result
    }

    suspend fun getCached(): List<Post> = feedRepository.getCachedFeed()
    fun getNextCursor(): String? = null // handled in repo
}

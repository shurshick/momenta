package com.bghitech.momenta.domain.repository

import com.bghitech.momenta.core.common.AppResult
import com.bghitech.momenta.domain.model.Post
import com.bghitech.momenta.domain.model.User

interface FeedRepository {
    suspend fun getTodayFeed(cursor: String?, limit: Int): AppResult<List<Post>>
    suspend fun getBestMoment(): AppResult<Post?>
    suspend fun getNextCursor(): String?
    suspend fun getCachedFeed(): List<Post>
    suspend fun cacheFeed(posts: List<Post>)
    suspend fun replaceCachedFeed(posts: List<Post>)
    suspend fun getUserSuggestions(): AppResult<List<User>>
}

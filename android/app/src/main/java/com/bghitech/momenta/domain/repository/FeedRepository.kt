package com.bghitech.momenta.domain.repository

import com.bghitech.momenta.core.common.AppResult
import com.bghitech.momenta.domain.model.Post
import com.bghitech.momenta.domain.model.User
import kotlinx.coroutines.flow.Flow

interface FeedRepository {
    fun observeTodayFeed(): Flow<List<Post>>
    fun observeBookmarks(): Flow<List<Post>>
    suspend fun getTodayFeed(cursor: String?, limit: Int): AppResult<List<Post>>
    suspend fun getBestMoment(): AppResult<Post?>
    suspend fun getNextCursor(): String?
    suspend fun getCachedFeed(): List<Post>
    suspend fun cacheFeed(posts: List<Post>)
    suspend fun replaceCachedFeed(posts: List<Post>)
    suspend fun upsertLocalPost(post: Post)
    suspend fun replaceLocalPost(localId: String, post: Post)
    suspend fun removeLocalPost(postId: String)
    suspend fun updateCachedPost(post: Post)
    suspend fun syncBookmarks(cursor: String? = null): AppResult<String?>
    suspend fun getUserSuggestions(): AppResult<List<User>>
}

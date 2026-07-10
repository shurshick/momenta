package com.bghitech.momenta.data.repository

import com.bghitech.momenta.core.common.AppResult
import com.bghitech.momenta.core.common.safeApiCall
import com.bghitech.momenta.data.local.dao.PostDao
import com.bghitech.momenta.data.mapper.*
import com.bghitech.momenta.data.remote.MomentaApi
import com.bghitech.momenta.core.util.AppDateUtils
import com.bghitech.momenta.domain.model.Post
import com.bghitech.momenta.domain.model.User
import com.bghitech.momenta.domain.repository.FeedRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FeedRepositoryImpl @Inject constructor(
    private val api: MomentaApi,
    private val postDao: PostDao
) : FeedRepository {

    private var nextCursor: String? = null

    override suspend fun getTodayFeed(cursor: String?, limit: Int): AppResult<List<Post>> {
        return safeApiCall {
            val response = api.getTodayFeed(cursor, limit)
            nextCursor = response.nextCursor
            response.items.map { it.toDomain() }.todayOnly()
        }
    }

    override suspend fun getBestMoment(): AppResult<Post?> {
        return when (val result = safeApiCall { api.getBestMoment().post?.toDomain() }) {
            is AppResult.Success -> result
            is AppResult.Error -> {
                val cached = getCachedFeed()
                AppResult.Success(cached.sortedByDescending { it.likesCount }.take(10).randomOrNull())
            }
        }
    }

    override suspend fun getNextCursor(): String? = nextCursor

    override suspend fun getCachedFeed(): List<Post> {
        return postDao.getPostsByChallengeDate(AppDateUtils.todayKey()).map { it.toDomain() }.todayOnly()
    }

    override suspend fun cacheFeed(posts: List<Post>) {
        postDao.insertPosts(posts.todayOnly().map { it.toCachedEntity() })
    }

    override suspend fun replaceCachedFeed(posts: List<Post>) {
        postDao.clearAll()
        postDao.insertPosts(posts.todayOnly().map { it.toCachedEntity() })
    }

    override suspend fun getUserSuggestions(): AppResult<List<User>> {
        return when (val result = safeApiCall { api.getUserSuggestions().items.map { it.toDomain() } }) {
            is AppResult.Success -> result
            is AppResult.Error -> AppResult.Success(getCachedFeed().activeUsers())
        }
    }

    private fun List<Post>.todayOnly(): List<Post> {
        val today = AppDateUtils.todayKey()
        return filter { it.challengeDate == today }
    }

    private fun List<Post>.activeUsers(): List<User> =
        groupBy { it.user.username }
            .values
            .sortedWith(
                compareByDescending<List<Post>> { it.size }
                    .thenByDescending { posts -> posts.maxOfOrNull { it.createdAt }.orEmpty() }
            )
            .map { it.first().user }
            .filter { it.username.isNotBlank() }
            .take(20)
}

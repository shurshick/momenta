package com.bghitech.momenta.data.repository

import com.bghitech.momenta.core.common.AppError
import com.bghitech.momenta.core.common.AppResult
import com.bghitech.momenta.data.local.dao.PostDao
import com.bghitech.momenta.data.mapper.*
import com.bghitech.momenta.data.remote.MomentaApi
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
        return try {
            val response = api.getTodayFeed(cursor, limit)
            nextCursor = response.nextCursor
            AppResult.Success(response.items.map { it.toDomain() })
        } catch (e: Exception) {
            if (cursor == null) {
                val cached = getCachedFeed()
                if (cached.isNotEmpty()) AppResult.Success(cached)
                else AppResult.Error(AppError.Network)
            } else {
                AppResult.Error(AppError.Network)
            }
        }
    }

    override suspend fun getBestMoment(): AppResult<Post?> {
        return try {
            AppResult.Success(api.getBestMoment().post?.toDomain())
        } catch (e: Exception) {
            val cached = getCachedFeed()
            AppResult.Success(cached.sortedByDescending { it.likesCount }.take(10).randomOrNull())
        }
    }

    override suspend fun getNextCursor(): String? = nextCursor

    override suspend fun getCachedFeed(): List<Post> {
        return postDao.getAllPosts().map { it.toDomain() }
    }

    override suspend fun cacheFeed(posts: List<Post>) {
        postDao.insertPosts(posts.map { it.toCachedEntity() })
    }

    override suspend fun replaceCachedFeed(posts: List<Post>) {
        postDao.clearAll()
        postDao.insertPosts(posts.map { it.toCachedEntity() })
    }

    override suspend fun getUserSuggestions(): AppResult<List<User>> {
        return try {
            AppResult.Success(api.getUserSuggestions().items.map { it.toDomain() })
        } catch (e: Exception) {
            AppResult.Success(getCachedFeed().map { it.user }.distinctBy { it.username }.take(20))
        }
    }
}

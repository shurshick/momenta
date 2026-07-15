package com.bghitech.momenta.data.repository

import com.bghitech.momenta.core.common.AppResult
import com.bghitech.momenta.core.common.safeApiCall
import com.bghitech.momenta.core.datastore.TokenStore
import com.bghitech.momenta.data.local.dao.PostDao
import com.bghitech.momenta.data.mapper.*
import com.bghitech.momenta.data.remote.MomentaApi
import com.bghitech.momenta.core.util.AppDateUtils
import com.bghitech.momenta.domain.model.Post
import com.bghitech.momenta.domain.model.User
import com.bghitech.momenta.domain.repository.FeedRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

@Singleton
class FeedRepositoryImpl @Inject constructor(
    private val api: MomentaApi,
    private val postDao: PostDao,
    private val tokenStore: TokenStore
) : FeedRepository {

    private var nextCursor: String? = null

    override fun observeTodayFeed(): Flow<List<Post>> {
        val accountId = accountId() ?: return flowOf(emptyList())
        return postDao.observePostsByChallengeDate(accountId, AppDateUtils.todayKey())
            .map { entities -> entities.map { it.toDomain() }.todayOnly() }
    }

    override fun observeBookmarks(): Flow<List<Post>> {
        val accountId = accountId() ?: return flowOf(emptyList())
        return postDao.observeBookmarks(accountId).map { entities -> entities.map { it.toDomain() } }
    }

    override suspend fun getTodayFeed(cursor: String?, limit: Int): AppResult<List<Post>> {
        return safeApiCall {
            val response = api.getTodayFeed(cursor, limit)
            nextCursor = response.nextCursor
            val posts = response.items.map { it.toDomain() }.todayOnly()
            if (cursor == null) {
                replaceCachedFeed(posts)
            } else {
                cacheFeed(posts)
            }
            posts
        }
    }

    override suspend fun getBestMoment(): AppResult<Post?> =
        safeApiCall {
            api.getBestMoment().post?.toDomain()?.takeIf {
                it.challengeDate == AppDateUtils.todayKey()
            }
        }

    override suspend fun getNextCursor(): String? = nextCursor

    override suspend fun getCachedFeed(): List<Post> {
        val accountId = accountId() ?: return emptyList()
        return postDao.getPostsByChallengeDate(accountId, AppDateUtils.todayKey()).map { it.toDomain() }.todayOnly()
    }

    override suspend fun cacheFeed(posts: List<Post>) {
        val accountId = accountId() ?: return
        postDao.insertPosts(posts.todayOnly().map { it.toCachedEntity(accountId) })
    }

    override suspend fun replaceCachedFeed(posts: List<Post>) {
        val accountId = accountId() ?: return
        val today = AppDateUtils.todayKey()
        postDao.replaceRemotePosts(accountId, today, posts.todayOnly().map { it.toCachedEntity(accountId) })
    }

    override suspend fun upsertLocalPost(post: Post) {
        val accountId = accountId() ?: return
        postDao.insertPost(post.toCachedEntity(accountId).copy(syncState = "pending"))
    }

    override suspend fun replaceLocalPost(localId: String, post: Post) {
        val accountId = accountId() ?: return
        postDao.replacePostId(accountId, localId, post.toCachedEntity(accountId).copy(syncState = "uploaded"))
    }

    override suspend fun removeLocalPost(postId: String) {
        val accountId = accountId() ?: return
        postDao.deleteById(accountId, postId)
    }

    override suspend fun updateCachedPost(post: Post) {
        val accountId = accountId() ?: return
        postDao.insertPost(post.toCachedEntity(accountId))
    }

    override suspend fun syncBookmarks(cursor: String?): AppResult<String?> = safeApiCall {
        val response = api.getBookmarks(cursor = cursor, limit = 30)
        val posts = response.items.map { it.toDomain() }
        accountId()?.let { accountId ->
            val entities = posts.map { it.toCachedEntity(accountId) }
            if (cursor == null) {
                postDao.replaceBookmarks(accountId, entities)
            } else {
                postDao.insertPosts(entities)
            }
        }
        response.nextCursor
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

    private fun accountId(): String? = tokenStore.getUserIdSync()?.takeIf { it.isNotBlank() }

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

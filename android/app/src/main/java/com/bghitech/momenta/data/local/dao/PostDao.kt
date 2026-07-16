package com.bghitech.momenta.data.local.dao

import androidx.room.*
import com.bghitech.momenta.data.local.entity.CachedPostEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PostDao {
    @Query("SELECT * FROM cached_posts WHERE accountId = :accountId AND id = :postId LIMIT 1")
    suspend fun getById(accountId: String, postId: String): CachedPostEntity?

    @Query("SELECT * FROM cached_posts WHERE accountId = :accountId ORDER BY createdAt DESC, cachedAt DESC")
    suspend fun getAllPosts(accountId: String): List<CachedPostEntity>

    @Query("SELECT * FROM cached_posts WHERE accountId = :accountId AND challengeDate = :challengeDate ORDER BY createdAt DESC, cachedAt DESC")
    suspend fun getPostsByChallengeDate(accountId: String, challengeDate: String): List<CachedPostEntity>

    @Query("SELECT * FROM cached_posts WHERE accountId = :accountId AND challengeDate = :challengeDate ORDER BY createdAt DESC, cachedAt DESC")
    fun observePostsByChallengeDate(accountId: String, challengeDate: String): Flow<List<CachedPostEntity>>

    @Query("SELECT * FROM cached_posts WHERE accountId = :accountId AND isBookmarked = 1 ORDER BY bookmarkedAt DESC, cachedAt DESC")
    fun observeBookmarks(accountId: String): Flow<List<CachedPostEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPosts(posts: List<CachedPostEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPost(post: CachedPostEntity)

    @Query("DELETE FROM cached_posts WHERE accountId = :accountId AND id = :postId")
    suspend fun deleteById(accountId: String, postId: String)

    @Query("DELETE FROM cached_posts WHERE accountId = :accountId AND challengeDate = :challengeDate AND syncState = 'remote' AND isBookmarked = 0")
    suspend fun clearRemoteByChallengeDate(accountId: String, challengeDate: String)

    @Query("UPDATE cached_posts SET isBookmarked = 0, bookmarkedAt = NULL WHERE accountId = :accountId")
    suspend fun clearBookmarkFlags(accountId: String)

    @Transaction
    suspend fun replaceRemotePosts(accountId: String, challengeDate: String, posts: List<CachedPostEntity>) {
        clearRemoteByChallengeDate(accountId, challengeDate)
        insertPosts(posts)
    }

    @Transaction
    suspend fun replacePostId(accountId: String, oldId: String, post: CachedPostEntity) {
        deleteById(accountId, oldId)
        insertPost(post)
    }

    @Transaction
    suspend fun replaceBookmarks(accountId: String, posts: List<CachedPostEntity>) {
        clearBookmarkFlags(accountId)
        insertPosts(posts)
    }

    @Query("DELETE FROM cached_posts")
    suspend fun clearAll()

    @Query("DELETE FROM cached_posts WHERE accountId = :accountId AND syncState = 'remote' AND isBookmarked = 0")
    suspend fun clearDisposableCache(accountId: String)
}

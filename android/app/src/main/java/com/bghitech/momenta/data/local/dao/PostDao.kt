package com.bghitech.momenta.data.local.dao

import androidx.room.*
import com.bghitech.momenta.data.local.entity.CachedPostEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PostDao {
    @Query("SELECT * FROM cached_posts ORDER BY cachedAt DESC")
    suspend fun getAllPosts(): List<CachedPostEntity>

    @Query("SELECT * FROM cached_posts WHERE challengeDate = :challengeDate ORDER BY cachedAt DESC")
    suspend fun getPostsByChallengeDate(challengeDate: String): List<CachedPostEntity>

    @Query("SELECT * FROM cached_posts WHERE challengeDate = :challengeDate ORDER BY cachedAt DESC")
    fun observePostsByChallengeDate(challengeDate: String): Flow<List<CachedPostEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPosts(posts: List<CachedPostEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPost(post: CachedPostEntity)

    @Query("DELETE FROM cached_posts WHERE id = :postId")
    suspend fun deleteById(postId: String)

    @Query("DELETE FROM cached_posts WHERE challengeDate = :challengeDate AND syncState = 'remote'")
    suspend fun clearRemoteByChallengeDate(challengeDate: String)

    @Transaction
    suspend fun replaceRemotePosts(challengeDate: String, posts: List<CachedPostEntity>) {
        clearRemoteByChallengeDate(challengeDate)
        insertPosts(posts)
    }

    @Transaction
    suspend fun replacePostId(oldId: String, post: CachedPostEntity) {
        deleteById(oldId)
        insertPost(post)
    }

    @Query("DELETE FROM cached_posts")
    suspend fun clearAll()
}

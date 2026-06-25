package com.bghitech.momenta.data.local.dao

import androidx.room.*
import com.bghitech.momenta.data.local.entity.CachedPostEntity

@Dao
interface PostDao {
    @Query("SELECT * FROM cached_posts ORDER BY cachedAt DESC")
    suspend fun getAllPosts(): List<CachedPostEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPosts(posts: List<CachedPostEntity>)

    @Query("DELETE FROM cached_posts")
    suspend fun clearAll()
}

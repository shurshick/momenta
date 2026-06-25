package com.bghitech.momenta.data.local.dao

import androidx.room.*
import com.bghitech.momenta.data.local.entity.CachedChallengeEntity

@Dao
interface ChallengeDao {
    @Query("SELECT * FROM cached_challenge ORDER BY cachedAt DESC LIMIT 1")
    suspend fun getLatestChallenge(): CachedChallengeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChallenge(challenge: CachedChallengeEntity)

    @Query("DELETE FROM cached_challenge")
    suspend fun clearAll()
}

package com.bghitech.momenta.data.local.dao

import androidx.room.*
import com.bghitech.momenta.data.local.entity.CachedChallengeEntity

@Dao
interface ChallengeDao {
    @Query("SELECT * FROM cached_challenge WHERE accountId = :accountId AND date = :date LIMIT 1")
    suspend fun getChallengeByDate(accountId: String, date: String): CachedChallengeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChallenge(challenge: CachedChallengeEntity)

    @Query("DELETE FROM cached_challenge WHERE accountId = :accountId")
    suspend fun clearForAccount(accountId: String)
}

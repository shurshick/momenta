package com.bghitech.momenta.data.local.dao

import androidx.room.*
import com.bghitech.momenta.data.local.entity.UploadQueueEntity

@Dao
interface UploadQueueDao {
    @Query("SELECT * FROM upload_queue WHERE localId = :localId LIMIT 1")
    suspend fun getById(localId: String): UploadQueueEntity?

    @Query("SELECT * FROM upload_queue WHERE accountId = :accountId AND status IN ('pending', 'uploading', 'failed') ORDER BY createdAt ASC")
    suspend fun getPendingForAccount(accountId: String): List<UploadQueueEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: UploadQueueEntity)

    @Update
    suspend fun update(entity: UploadQueueEntity)

    @Delete
    suspend fun delete(entity: UploadQueueEntity)

    @Query("UPDATE upload_queue SET status = :status, updatedAt = :updatedAt WHERE localId = :localId")
    suspend fun updateStatus(localId: String, status: String, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE upload_queue SET status = :status, retryCount = retryCount + 1, updatedAt = :updatedAt WHERE localId = :localId")
    suspend fun markFailed(localId: String, status: String = "failed", updatedAt: Long = System.currentTimeMillis())

    @Query("DELETE FROM upload_queue WHERE localId = :localId")
    suspend fun deleteById(localId: String)
}

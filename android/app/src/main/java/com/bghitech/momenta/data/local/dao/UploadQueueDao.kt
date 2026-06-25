package com.bghitech.momenta.data.local.dao

import androidx.room.*
import com.bghitech.momenta.data.local.entity.UploadQueueEntity

@Dao
interface UploadQueueDao {
    @Query("SELECT * FROM upload_queue WHERE status = :status ORDER BY createdAt ASC")
    suspend fun getByStatus(status: String): List<UploadQueueEntity>

    @Query("SELECT * FROM upload_queue ORDER BY createdAt DESC")
    suspend fun getAll(): List<UploadQueueEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: UploadQueueEntity)

    @Update
    suspend fun update(entity: UploadQueueEntity)

    @Delete
    suspend fun delete(entity: UploadQueueEntity)

    @Query("UPDATE upload_queue SET status = :status, retryCount = retryCount + 1, updatedAt = :updatedAt WHERE localId = :localId")
    suspend fun markFailed(localId: String, status: String = "failed", updatedAt: Long = System.currentTimeMillis())
}

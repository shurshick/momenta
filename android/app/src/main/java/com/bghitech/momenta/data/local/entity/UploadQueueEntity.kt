package com.bghitech.momenta.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "upload_queue",
    indices = [
        Index(value = ["accountId", "status", "createdAt"]),
        Index(value = ["accountId", "challengeDate"])
    ]
)
data class UploadQueueEntity(
    @PrimaryKey val localId: String,
    val accountId: String,
    val challengeId: String,
    val challengeDate: String,
    val filePath: String,
    val caption: String?,
    val country: String?,
    val city: String?,
    val mediaType: String,
    val status: String = "pending",
    val retryCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

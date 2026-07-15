package com.bghitech.momenta.data.local.entity

import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "cached_challenge",
    primaryKeys = ["accountId", "id"],
    indices = [Index(value = ["accountId", "date"])]
)
data class CachedChallengeEntity(
    val accountId: String,
    val id: String,
    val date: String,
    val title: String,
    val description: String?,
    val prompt: String? = null,
    val source: String = "manual",
    val endsAt: String?,
    val userPosted: Boolean,
    val participantsCount: Int,
    val cachedAt: Long = System.currentTimeMillis()
)

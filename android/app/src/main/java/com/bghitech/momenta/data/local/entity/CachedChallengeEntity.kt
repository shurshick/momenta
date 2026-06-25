package com.bghitech.momenta.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cached_challenge")
data class CachedChallengeEntity(
    @PrimaryKey val id: String,
    val date: String,
    val title: String,
    val description: String?,
    val endsAt: String?,
    val userPosted: Boolean,
    val participantsCount: Int,
    val cachedAt: Long = System.currentTimeMillis()
)

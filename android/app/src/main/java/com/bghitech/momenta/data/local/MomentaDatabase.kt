package com.bghitech.momenta.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.bghitech.momenta.data.local.entity.CachedChallengeEntity
import com.bghitech.momenta.data.local.entity.CachedPostEntity
import com.bghitech.momenta.data.local.entity.UploadQueueEntity
import com.bghitech.momenta.data.local.dao.ChallengeDao
import com.bghitech.momenta.data.local.dao.PostDao
import com.bghitech.momenta.data.local.dao.UploadQueueDao

@Database(
    entities = [
        CachedChallengeEntity::class,
        CachedPostEntity::class,
        UploadQueueEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class MomentaDatabase : RoomDatabase() {
    abstract fun challengeDao(): ChallengeDao
    abstract fun postDao(): PostDao
    abstract fun uploadQueueDao(): UploadQueueDao
}

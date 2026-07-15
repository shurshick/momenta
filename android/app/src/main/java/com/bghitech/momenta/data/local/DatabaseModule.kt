package com.bghitech.momenta.data.local

import android.content.Context
import androidx.room.Room
import com.bghitech.momenta.data.local.dao.ChallengeDao
import com.bghitech.momenta.data.local.dao.PostDao
import com.bghitech.momenta.data.local.dao.UploadQueueDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): MomentaDatabase {
        return Room.databaseBuilder(
            context,
            MomentaDatabase::class.java,
            "momenta_database"
        )
            .addMigrations(MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7)
            .build()
    }

    @Provides fun provideChallengeDao(db: MomentaDatabase): ChallengeDao = db.challengeDao()
    @Provides fun providePostDao(db: MomentaDatabase): PostDao = db.postDao()
    @Provides fun provideUploadQueueDao(db: MomentaDatabase): UploadQueueDao = db.uploadQueueDao()
}

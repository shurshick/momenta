package com.bghitech.momenta.data.repository

import com.bghitech.momenta.core.common.AppError
import com.bghitech.momenta.core.common.AppResult
import com.bghitech.momenta.data.local.dao.ChallengeDao
import com.bghitech.momenta.data.mapper.*
import com.bghitech.momenta.data.remote.MomentaApi
import com.bghitech.momenta.domain.model.Challenge
import com.bghitech.momenta.domain.repository.ChallengeRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChallengeRepositoryImpl @Inject constructor(
    private val api: MomentaApi,
    private val challengeDao: ChallengeDao
) : ChallengeRepository {

    override suspend fun getTodayChallenge(): AppResult<Challenge> {
        return try {
            val dto = api.getTodayChallenge()
            AppResult.Success(dto.toDomain())
        } catch (e: Exception) {
            val cached = getCachedChallenge()
            if (cached != null) AppResult.Success(cached)
            else AppResult.Error(AppError.Network)
        }
    }

    override suspend fun getCachedChallenge(): Challenge? {
        return challengeDao.getLatestChallenge()?.toDomain()
    }

    override suspend fun cacheChallenge(challenge: Challenge) {
        challengeDao.insertChallenge(challenge.toCachedEntity())
    }
}

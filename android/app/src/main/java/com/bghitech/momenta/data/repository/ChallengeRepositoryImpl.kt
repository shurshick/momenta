package com.bghitech.momenta.data.repository

import com.bghitech.momenta.core.common.AppResult
import com.bghitech.momenta.core.common.safeApiCall
import com.bghitech.momenta.core.util.AppDateUtils
import com.bghitech.momenta.core.datastore.TokenStore
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
    private val challengeDao: ChallengeDao,
    private val tokenStore: TokenStore
) : ChallengeRepository {

    override suspend fun getTodayChallenge(): AppResult<Challenge> =
        safeApiCall { api.getTodayChallenge().toDomain() }

    override suspend fun getCachedChallenge(): Challenge? {
        val accountId = tokenStore.getUserIdSync()?.takeIf { it.isNotBlank() } ?: return null
        return challengeDao.getChallengeByDate(accountId, AppDateUtils.todayKey())?.toDomain()
    }

    override suspend fun cacheChallenge(challenge: Challenge) {
        val accountId = tokenStore.getUserIdSync()?.takeIf { it.isNotBlank() } ?: return
        challengeDao.insertChallenge(challenge.toCachedEntity(accountId))
    }
}

package com.bghitech.momenta.domain.repository

import com.bghitech.momenta.core.common.AppResult
import com.bghitech.momenta.domain.model.Challenge

interface ChallengeRepository {
    suspend fun getTodayChallenge(): AppResult<Challenge>
    suspend fun getCachedChallenge(): Challenge?
    suspend fun cacheChallenge(challenge: Challenge)
}

package com.bghitech.momenta.domain.usecase

import com.bghitech.momenta.core.common.AppResult
import com.bghitech.momenta.domain.model.Challenge
import com.bghitech.momenta.domain.repository.ChallengeRepository
import javax.inject.Inject

class GetTodayChallengeUseCase @Inject constructor(
    private val challengeRepository: ChallengeRepository
) {
    suspend operator fun invoke(): AppResult<Challenge> {
        val result = challengeRepository.getTodayChallenge()
        if (result is AppResult.Success) {
            challengeRepository.cacheChallenge(result.data)
        }
        return result
    }

    suspend fun getCached(): Challenge? = challengeRepository.getCachedChallenge()
}

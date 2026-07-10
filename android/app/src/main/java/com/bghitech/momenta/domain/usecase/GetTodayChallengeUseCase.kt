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
            try {
                challengeRepository.cacheChallenge(result.data)
            } catch (_: Exception) {
                // Cache must not hide a valid challenge from the UI.
            }
        }
        return result
    }

    suspend fun getCached(): Challenge? = challengeRepository.getCachedChallenge()
}

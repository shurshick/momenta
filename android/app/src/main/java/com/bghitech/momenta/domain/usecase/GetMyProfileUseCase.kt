package com.bghitech.momenta.domain.usecase

import com.bghitech.momenta.core.common.AppResult
import com.bghitech.momenta.domain.model.Profile
import com.bghitech.momenta.domain.repository.ProfileRepository
import javax.inject.Inject

class GetMyProfileUseCase @Inject constructor(
    private val profileRepository: ProfileRepository
) {
    suspend operator fun invoke(): AppResult<Profile> {
        val result = profileRepository.getMyProfile()
        if (result is AppResult.Success) {
            profileRepository.cacheProfile(result.data)
        }
        return result
    }

    suspend fun getCached(): Profile? = profileRepository.getCachedProfile()
}

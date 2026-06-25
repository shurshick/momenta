package com.bghitech.momenta.domain.repository

import com.bghitech.momenta.core.common.AppResult
import com.bghitech.momenta.domain.model.Profile

interface ProfileRepository {
    suspend fun getMyProfile(): AppResult<Profile>
    suspend fun updateProfile(displayName: String?, bio: String?): AppResult<Profile>
    suspend fun getCachedProfile(): Profile?
    suspend fun cacheProfile(profile: Profile)
}

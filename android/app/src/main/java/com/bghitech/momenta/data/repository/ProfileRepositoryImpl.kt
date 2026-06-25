package com.bghitech.momenta.data.repository

import com.bghitech.momenta.core.common.AppError
import com.bghitech.momenta.core.common.AppResult
import com.bghitech.momenta.data.mapper.toDomain
import com.bghitech.momenta.data.remote.MomentaApi
import com.bghitech.momenta.data.remote.dto.UpdateProfileRequestDto
import com.bghitech.momenta.domain.model.Profile
import com.bghitech.momenta.domain.repository.ProfileRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProfileRepositoryImpl @Inject constructor(
    private val api: MomentaApi
) : ProfileRepository {

    private var cachedProfile: Profile? = null

    override suspend fun getMyProfile(): AppResult<Profile> {
        return try {
            val dto = api.getMyProfile()
            val profile = dto.toDomain()
            cachedProfile = profile
            AppResult.Success(profile)
        } catch (e: Exception) {
            val cached = getCachedProfile()
            if (cached != null) AppResult.Success(cached)
            else AppResult.Error(AppError.Network)
        }
    }

    override suspend fun updateProfile(displayName: String?, bio: String?): AppResult<Profile> {
        return try {
            val dto = api.updateMyProfile(UpdateProfileRequestDto(displayName, bio))
            val profile = dto.toDomain()
            cachedProfile = profile
            AppResult.Success(profile)
        } catch (e: Exception) {
            AppResult.Error(AppError.Unknown(e.message))
        }
    }

    override suspend fun getCachedProfile(): Profile? = cachedProfile

    override suspend fun cacheProfile(profile: Profile) {
        cachedProfile = profile
    }
}

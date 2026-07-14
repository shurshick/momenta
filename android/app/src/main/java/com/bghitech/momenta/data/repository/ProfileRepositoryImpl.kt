package com.bghitech.momenta.data.repository

import com.bghitech.momenta.core.common.AppError
import com.bghitech.momenta.core.common.AppResult
import com.bghitech.momenta.core.common.safeApiCall
import com.bghitech.momenta.data.mapper.toDomain
import com.bghitech.momenta.data.remote.MomentaApi
import com.bghitech.momenta.data.remote.dto.UpdateAvatarRequestDto
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
        return when (val result = safeApiCall {
            api.getMyProfile().toDomain().also { cachedProfile = it }
        }) {
            is AppResult.Success -> result
            is AppResult.Error -> {
                val cached = getCachedProfile()
                if (cached != null && result.error.canUseCachedProfile()) {
                    AppResult.Success(cached)
                } else {
                    result
                }
            }
        }
    }

    override suspend fun updateProfile(displayName: String?, bio: String?): AppResult<Profile> {
        return safeApiCall {
            api.updateMyProfile(UpdateProfileRequestDto(displayName, bio)).toDomain().also { cachedProfile = it }
        }
    }

    override suspend fun updateAvatar(avatarKey: String): AppResult<Profile> {
        return safeApiCall {
            api.updateMyAvatar(UpdateAvatarRequestDto(avatarKey)).toDomain().also { cachedProfile = it }
        }
    }

    override suspend fun getAvatars(): AppResult<List<String>> {
        return when (val result = safeApiCall { api.getAvatars().items.map { it.key } }) {
            is AppResult.Success -> result
            is AppResult.Error -> AppResult.Success((1..40).map { index -> "avatar_%02d".format(index) })
        }
    }

    override suspend fun getCachedProfile(): Profile? = cachedProfile

    override suspend fun cacheProfile(profile: Profile) {
        cachedProfile = profile
    }

    override suspend fun clearCache() {
        cachedProfile = null
    }

    private fun AppError.canUseCachedProfile(): Boolean =
        this == AppError.Network || this == AppError.Server
}

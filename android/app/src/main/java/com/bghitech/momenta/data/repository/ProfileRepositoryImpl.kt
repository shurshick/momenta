package com.bghitech.momenta.data.repository

import android.content.Context
import com.bghitech.momenta.core.common.AppResult
import com.bghitech.momenta.core.common.safeApiCall
import com.bghitech.momenta.core.datastore.TokenStore
import com.bghitech.momenta.data.mapper.toDomain
import com.bghitech.momenta.data.remote.MomentaApi
import com.bghitech.momenta.data.remote.dto.ProfileDto
import com.bghitech.momenta.data.remote.dto.RecentPostDto
import com.bghitech.momenta.data.remote.dto.UpdateAvatarRequestDto
import com.bghitech.momenta.data.remote.dto.UpdateProfileRequestDto
import com.bghitech.momenta.domain.model.Profile
import com.bghitech.momenta.domain.repository.ProfileRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProfileRepositoryImpl @Inject constructor(
    private val api: MomentaApi,
    @ApplicationContext context: Context,
    private val tokenStore: TokenStore
) : ProfileRepository {

    private var cachedProfile: Profile? = null
    private val cache = context.getSharedPreferences("momenta_profile_cache", Context.MODE_PRIVATE)
    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun getMyProfile(): AppResult<Profile> {
        return safeApiCall {
            api.getMyProfile().toDomain().also(::storeProfile)
        }
    }

    override suspend fun updateProfile(displayName: String?, bio: String?): AppResult<Profile> {
        return safeApiCall {
            api.updateMyProfile(UpdateProfileRequestDto(displayName, bio)).toDomain().also(::storeProfile)
        }
    }

    override suspend fun updateAvatar(avatarKey: String): AppResult<Profile> {
        return safeApiCall {
            api.updateMyAvatar(UpdateAvatarRequestDto(avatarKey)).toDomain().also(::storeProfile)
        }
    }

    override suspend fun getAvatars(): AppResult<List<String>> {
        return when (val result = safeApiCall { api.getAvatars().items.map { it.key } }) {
            is AppResult.Success -> result
            is AppResult.Error -> AppResult.Success((1..40).map { index -> "avatar_%02d".format(index) })
        }
    }

    override suspend fun getUserPosts(
        userId: String,
        cursor: String?
    ): AppResult<Pair<List<com.bghitech.momenta.domain.model.Post>, String?>> = safeApiCall {
        val response = api.getUserFeed(userId, cursor)
        response.items.map { it.toDomain() } to response.nextCursor
    }

    override suspend fun getCachedProfile(): Profile? {
        cachedProfile?.let { return it }
        val accountId = tokenStore.getUserIdSync()?.takeIf { it.isNotBlank() } ?: return null
        val encoded = cache.getString(cacheKey(accountId), null) ?: return null
        return runCatching { json.decodeFromString<ProfileDto>(encoded).toDomain() }
            .getOrNull()
            ?.also { cachedProfile = it }
    }

    override suspend fun cacheProfile(profile: Profile) {
        storeProfile(profile)
    }

    override suspend fun clearCache() {
        cachedProfile = null
    }

    private fun storeProfile(profile: Profile) {
        cachedProfile = profile
        cache.edit().putString(cacheKey(profile.id), json.encodeToString(profile.toCacheDto())).apply()
    }

    private fun cacheKey(accountId: String) = "profile_$accountId"

    private fun Profile.toCacheDto() = ProfileDto(
        id = id,
        username = username,
        displayName = displayName,
        avatarUrl = avatarUrl,
        avatarKey = avatarKey,
        bio = bio,
        momentsCount = momentsCount,
        streakCount = streakCount,
        likesCount = likesCount,
        recentPosts = recentPosts.map { post ->
            RecentPostDto(
                id = post.id,
                previewUrl = post.previewUrl,
                thumbUrl = post.thumbUrl,
                createdAt = post.createdAt
            )
        }
    )
}

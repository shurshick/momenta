package com.bghitech.momenta.data.repository

import com.bghitech.momenta.core.common.AppError
import com.bghitech.momenta.core.common.AppResult
import com.bghitech.momenta.data.mapper.toDomain
import com.bghitech.momenta.data.remote.MomentaApi
import com.bghitech.momenta.data.remote.dto.ReportRequestDto
import com.bghitech.momenta.domain.model.Post
import com.bghitech.momenta.domain.repository.PostRepository
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PostRepositoryImpl @Inject constructor(
    private val api: MomentaApi
) : PostRepository {

    override suspend fun uploadPost(
        challengeId: String,
        mediaFile: File,
        caption: String?,
        country: String?,
        city: String?
    ): AppResult<Post> {
        return try {
            val challengeIdPart = challengeId.toRequestBody("text/plain".toMediaTypeOrNull())
            val captionPart = caption?.toRequestBody("text/plain".toMediaTypeOrNull())
            val countryPart = country?.toRequestBody("text/plain".toMediaTypeOrNull())
            val cityPart = city?.toRequestBody("text/plain".toMediaTypeOrNull())
            val mediaPart = MultipartBody.Part.createFormData(
                "media",
                mediaFile.name,
                mediaFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
            )
            val post = api.uploadPost(challengeIdPart, captionPart, countryPart, cityPart, mediaPart)
            AppResult.Success(post.toDomain())
        } catch (e: Exception) {
            when {
                e.message?.contains("409") == true -> AppResult.Error(AppError.Validation("Вы уже опубликовали момент сегодня"))
                else -> AppResult.Error(AppError.Unknown(e.message))
            }
        }
    }

    override suspend fun likePost(postId: String): AppResult<Unit> {
        return try {
            api.likePost(postId)
            AppResult.Success(Unit)
        } catch (e: Exception) {
            AppResult.Error(AppError.Unknown(e.message))
        }
    }

    override suspend fun unlikePost(postId: String): AppResult<Unit> {
        return try {
            api.unlikePost(postId)
            AppResult.Success(Unit)
        } catch (e: Exception) {
            AppResult.Error(AppError.Unknown(e.message))
        }
    }

    override suspend fun reportPost(postId: String, reason: String): AppResult<Unit> {
        return try {
            api.reportPost(postId, ReportRequestDto(reason))
            AppResult.Success(Unit)
        } catch (e: Exception) {
            AppResult.Error(AppError.Unknown(e.message))
        }
    }
}

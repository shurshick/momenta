package com.bghitech.momenta.data.repository

import com.bghitech.momenta.core.common.AppResult
import com.bghitech.momenta.core.common.safeApiCall
import com.bghitech.momenta.core.util.AppDateUtils
import com.bghitech.momenta.data.mapper.toDomain
import com.bghitech.momenta.data.remote.MomentaApi
import com.bghitech.momenta.data.remote.dto.CreateCommentRequestDto
import com.bghitech.momenta.data.remote.dto.ReportRequestDto
import com.bghitech.momenta.domain.model.Comment
import com.bghitech.momenta.domain.model.Post
import com.bghitech.momenta.domain.model.User
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
        return safeApiCall {
            val challengeIdPart = challengeId.toRequestBody("text/plain".toMediaTypeOrNull())
            val captionPart = caption?.toRequestBody("text/plain".toMediaTypeOrNull())
            val countryPart = country?.toRequestBody("text/plain".toMediaTypeOrNull())
            val cityPart = city?.toRequestBody("text/plain".toMediaTypeOrNull())
            val mediaPart = MultipartBody.Part.createFormData(
                "media",
                mediaFile.name,
                mediaFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
            )
            val response = api.uploadPost(challengeIdPart, captionPart, countryPart, cityPart, mediaPart)
            Post(
                id = response.id,
                user = User("", "", null, null, null, null),
                mediaType = "image",
                previewUrl = "",
                thumbUrl = null,
                caption = caption,
                country = country,
                city = city,
                likesCount = 0,
                commentsCount = 0,
                viewsCount = 0,
                challengeDate = AppDateUtils.todayKey(),
                createdAt = ""
            )
        }
    }

    override suspend fun likePost(postId: String): AppResult<Unit> {
        return safeApiCall {
            api.likePost(postId)
            Unit
        }
    }

    override suspend fun unlikePost(postId: String): AppResult<Unit> {
        return safeApiCall {
            api.unlikePost(postId)
            Unit
        }
    }

    override suspend fun reportPost(postId: String, reason: String): AppResult<Unit> {
        return safeApiCall {
            api.reportPost(postId, ReportRequestDto(reason))
            Unit
        }
    }

    override suspend fun deletePost(postId: String): AppResult<Unit> {
        return safeApiCall {
            api.deletePost(postId)
            Unit
        }
    }

    override suspend fun getComments(postId: String): AppResult<List<Comment>> {
        return safeApiCall {
            api.getComments(postId).items.map { it.toDomain() }
        }
    }

    override suspend fun createComment(postId: String, text: String): AppResult<Comment> {
        return safeApiCall {
            api.createComment(postId, CreateCommentRequestDto(text)).toDomain()
        }
    }

    override suspend fun deleteComment(postId: String, commentId: String): AppResult<Unit> {
        return safeApiCall {
            api.deleteComment(postId, commentId)
            Unit
        }
    }
}

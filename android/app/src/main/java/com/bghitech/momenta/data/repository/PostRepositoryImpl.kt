package com.bghitech.momenta.data.repository

import com.bghitech.momenta.core.common.AppError
import com.bghitech.momenta.core.common.AppResult
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
            val response = api.uploadPost(challengeIdPart, captionPart, countryPart, cityPart, mediaPart)
            AppResult.Success(Post(
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
                createdAt = ""
            ))
        } catch (e: Exception) {
            when (e) {
                is retrofit2.HttpException -> {
                    val serverMessage = try {
                        e.response()?.errorBody()?.string()?.let { body ->
                            kotlinx.serialization.json.Json.parseToJsonElement(body)
                                .let { it as? kotlinx.serialization.json.JsonObject }
                                ?.get("detail")?.let { (it as? kotlinx.serialization.json.JsonPrimitive)?.content }
                        }
                    } catch (_: Exception) { null }
                    when (e.code()) {
                        409 -> AppResult.Error(AppError.Validation(serverMessage ?: "Вы уже опубликовали момент сегодня"))
                        in 400..499 -> AppResult.Error(AppError.Validation(serverMessage ?: "Ошибка публикации"))
                        in 500..599 -> AppResult.Error(AppError.Server)
                        else -> AppResult.Error(AppError.Unknown(serverMessage ?: e.message()))
                    }
                }
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

    override suspend fun deletePost(postId: String): AppResult<Unit> {
        return try {
            api.deletePost(postId)
            AppResult.Success(Unit)
        } catch (e: Exception) {
            AppResult.Error(AppError.Unknown(e.message))
        }
    }

    override suspend fun getComments(postId: String): AppResult<List<Comment>> {
        return try {
            AppResult.Success(api.getComments(postId).items.map { it.toDomain() })
        } catch (e: Exception) {
            AppResult.Error(AppError.Unknown(e.message))
        }
    }

    override suspend fun createComment(postId: String, text: String): AppResult<Comment> {
        return try {
            AppResult.Success(api.createComment(postId, CreateCommentRequestDto(text)).toDomain())
        } catch (e: Exception) {
            AppResult.Error(AppError.Unknown(e.message))
        }
    }

    override suspend fun deleteComment(postId: String, commentId: String): AppResult<Unit> {
        return try {
            api.deleteComment(postId, commentId)
            AppResult.Success(Unit)
        } catch (e: Exception) {
            AppResult.Error(AppError.Unknown(e.message))
        }
    }
}

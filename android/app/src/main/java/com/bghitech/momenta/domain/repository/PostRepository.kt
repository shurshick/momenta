package com.bghitech.momenta.domain.repository

import com.bghitech.momenta.core.common.AppResult
import com.bghitech.momenta.domain.model.Comment
import com.bghitech.momenta.domain.model.Post
import java.io.File

interface PostRepository {
    suspend fun uploadPost(
        challengeId: String,
        mediaFile: File,
        caption: String?,
        country: String?,
        city: String?
    ): AppResult<Post>
    suspend fun likePost(postId: String): AppResult<Unit>
    suspend fun unlikePost(postId: String): AppResult<Unit>
    suspend fun reportPost(postId: String, reason: String): AppResult<Unit>
    suspend fun deletePost(postId: String): AppResult<Unit>
    suspend fun getComments(postId: String): AppResult<List<Comment>>
    suspend fun createComment(postId: String, text: String): AppResult<Comment>
    suspend fun deleteComment(postId: String, commentId: String): AppResult<Unit>
}

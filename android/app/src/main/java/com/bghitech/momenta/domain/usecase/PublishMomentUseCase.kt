package com.bghitech.momenta.domain.usecase

import com.bghitech.momenta.core.common.AppResult
import com.bghitech.momenta.domain.model.Post
import com.bghitech.momenta.domain.repository.PostRepository
import java.io.File
import javax.inject.Inject

class PublishMomentUseCase @Inject constructor(
    private val postRepository: PostRepository
) {
    suspend operator fun invoke(
        challengeId: String,
        mediaFile: File,
        caption: String?,
        country: String?,
        city: String?
    ): AppResult<Post> {
        return postRepository.uploadPost(challengeId, mediaFile, caption, country, city)
    }
}

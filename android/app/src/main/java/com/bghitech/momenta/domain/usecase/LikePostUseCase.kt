package com.bghitech.momenta.domain.usecase

import com.bghitech.momenta.core.common.AppResult
import com.bghitech.momenta.domain.repository.PostRepository
import javax.inject.Inject

class LikePostUseCase @Inject constructor(
    private val postRepository: PostRepository
) {
    suspend operator fun invoke(postId: String, liked: Boolean): AppResult<Unit> {
        return if (liked) postRepository.likePost(postId)
        else postRepository.unlikePost(postId)
    }
}

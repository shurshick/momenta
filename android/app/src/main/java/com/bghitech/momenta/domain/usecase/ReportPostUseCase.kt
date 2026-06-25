package com.bghitech.momenta.domain.usecase

import com.bghitech.momenta.core.common.AppResult
import com.bghitech.momenta.domain.repository.PostRepository
import javax.inject.Inject

class ReportPostUseCase @Inject constructor(
    private val postRepository: PostRepository
) {
    suspend operator fun invoke(postId: String, reason: String = "Неуместное содержание"): AppResult<Unit> {
        return postRepository.reportPost(postId, reason)
    }
}

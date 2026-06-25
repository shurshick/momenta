package com.bghitech.momenta.domain.usecase

import com.bghitech.momenta.core.common.AppResult
import com.bghitech.momenta.domain.model.AuthToken
import com.bghitech.momenta.domain.repository.AuthRepository
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(usernameOrEmail: String, password: String): AppResult<AuthToken> {
        return authRepository.login(usernameOrEmail, password)
    }
}

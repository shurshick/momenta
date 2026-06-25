package com.bghitech.momenta.domain.usecase

import com.bghitech.momenta.core.common.AppError
import com.bghitech.momenta.core.common.AppResult
import com.bghitech.momenta.domain.model.AuthToken
import com.bghitech.momenta.domain.repository.AuthRepository
import javax.inject.Inject

class RegisterUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(username: String, email: String, password: String): AppResult<AuthToken> {
        if (username.isBlank()) return AppResult.Error(AppError.Validation("Имя пользователя не может быть пустым"))
        if (email.isBlank()) return AppResult.Error(AppError.Validation("Email не может быть пустым"))
        if (password.length < 6) return AppResult.Error(AppError.Validation("Пароль должен быть не менее 6 символов"))
        return authRepository.register(username, email, password)
    }
}

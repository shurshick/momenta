package com.bghitech.momenta.domain.usecase

import com.bghitech.momenta.domain.repository.AuthRepository
import javax.inject.Inject

class CheckAuthUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): Boolean {
        return authRepository.isLoggedIn()
    }
}

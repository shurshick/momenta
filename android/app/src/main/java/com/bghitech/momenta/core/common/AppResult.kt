package com.bghitech.momenta.core.common

sealed interface AppResult<out T> {
    data class Success<T>(val data: T) : AppResult<T>
    data class Error(val error: AppError) : AppResult<Nothing>
}

sealed interface AppError {
    data object Network : AppError
    data object Unauthorized : AppError
    data object Server : AppError
    data class Validation(val message: String) : AppError
    data class Unknown(val message: String?) : AppError
}

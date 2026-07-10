package com.bghitech.momenta.core.common

sealed interface AppResult<out T> {
    data class Success<T>(val data: T) : AppResult<T>
    data class Error(val error: AppError) : AppResult<Nothing>
}

sealed interface AppError {
    data object Network : AppError
    data object Unauthorized : AppError
    data object Forbidden : AppError
    data object Server : AppError
    data object Parse : AppError
    data class BadRequest(val message: String) : AppError
    data class Conflict(val message: String) : AppError
    data class Validation(val message: String) : AppError
    data class Unknown(val message: String?) : AppError
}

fun AppError.userMessage(defaultMessage: String = "Произошла ошибка"): String {
    return when (this) {
        AppError.Network -> "Нет подключения к серверу"
        AppError.Unauthorized -> "Нужен вход в аккаунт"
        AppError.Forbidden -> "Недостаточно прав"
        AppError.Server -> "Ошибка сервера, попробуйте позже"
        AppError.Parse -> "Сервер вернул неожиданный ответ"
        is AppError.BadRequest -> message
        is AppError.Conflict -> message
        is AppError.Validation -> message
        is AppError.Unknown -> message ?: defaultMessage
    }
}

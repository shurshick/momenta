package com.bghitech.momenta.core.common

import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import retrofit2.HttpException
import java.io.IOException

suspend fun <T> safeApiCall(call: suspend () -> T): AppResult<T> {
    return try {
        AppResult.Success(call())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        AppResult.Error(e.toAppError())
    }
}

fun Throwable.toAppError(): AppError {
    return when (this) {
        is HttpException -> {
            val serverMessage = parseServerMessage()
            when (code()) {
                400 -> AppError.BadRequest(serverMessage ?: "Ошибка запроса")
                401 -> AppError.Unauthorized
                403 -> AppError.Forbidden
                409 -> AppError.Conflict(serverMessage ?: "Конфликт запроса")
                422 -> AppError.Validation(serverMessage ?: "Ошибка валидации")
                in 400..499 -> AppError.Validation(serverMessage ?: "Ошибка запроса")
                in 500..599 -> AppError.Server
                else -> AppError.Unknown(serverMessage ?: message())
            }
        }
        is IOException -> AppError.Network
        is SerializationException -> AppError.Parse
        else -> AppError.Unknown(message)
    }
}

private fun HttpException.parseServerMessage(): String? {
    val body = response()?.errorBody()?.string() ?: return null
    return try {
        val element = Json.parseToJsonElement(body)
        val root = element as? JsonObject ?: return null
        val detail = root["detail"]
        when (detail) {
            is JsonPrimitive -> detail.content
            is JsonObject -> (detail["message"] as? JsonPrimitive)?.content
            else -> null
        }
    } catch (_: Exception) {
        null
    }
}

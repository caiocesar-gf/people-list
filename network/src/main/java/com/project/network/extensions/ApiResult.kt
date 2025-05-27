package com.project.network.extensions

sealed class ApiResult<T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Error<T>(val message: String, val code: Int? = null) : ApiResult<T>()
    data class NetworkError<T>(val message: String) : ApiResult<T>()
    data class ParseError<T>(val message: String) : ApiResult<T>()
    data class CacheResult<T>(val data: T, val message: String = "Dados carregados do cache local") : ApiResult<T>()
}

fun <T> handleApiResult(
    result: ApiResult<T>,
    onSuccess: (T) -> Unit,
    onError: (String) -> Unit,
    onCache: ((T, String) -> Unit)? = null
) {
    when (result) {
        is ApiResult.Success -> onSuccess(result.data)
        is ApiResult.CacheResult -> {
            if (onCache != null) {
                onCache(result.data, result.message)
            } else {
                onSuccess(result.data)
            }
        }
        is ApiResult.Error -> {
            val message = when (result.code) {
                401 -> "Erro de autenticação (401)"
                500 -> "Erro interno do servidor (500)"
                else -> result.message
            }
            onError(message)
        }
        is ApiResult.NetworkError -> onError("Erro de conexão: ${result.message}")
        is ApiResult.ParseError -> onError("Erro de parsing: ${result.message}")
    }
}
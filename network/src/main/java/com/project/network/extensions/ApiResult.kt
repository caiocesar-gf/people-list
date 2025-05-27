package com.project.network.extensions

sealed class ApiResult<T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Error<T>(val message: String, val code: Int? = null) : ApiResult<T>()
    data class NetworkError<T>(val message: String) : ApiResult<T>()
    data class ParseError<T>(val message: String) : ApiResult<T>()
}
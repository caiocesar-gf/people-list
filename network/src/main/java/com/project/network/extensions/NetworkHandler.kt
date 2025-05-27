package com.project.network.extensions

import retrofit2.HttpException
import com.google.gson.JsonSyntaxException
import com.google.gson.JsonParseException
import java.io.IOException
import java.net.UnknownHostException
import java.net.SocketTimeoutException

object NetworkHandler {

    suspend fun <T> loadFromNetwork(call: suspend () -> T): ApiResult<T> {
        return try {
            ApiResult.Success(call())
        } catch (e: Exception) {
            when (e) {
                is HttpException -> {
                    ApiResult.Error(
                        message = getHttpErrorMessage(e.code()),
                        code = e.code()
                    )
                }
                is JsonSyntaxException, is JsonParseException -> {
                    ApiResult.ParseError("Erro ao processar resposta do servidor")
                }
                is UnknownHostException -> {
                    ApiResult.NetworkError("Sem conexão com a internet")
                }
                is SocketTimeoutException -> {
                    ApiResult.NetworkError("Tempo limite excedido")
                }
                is IOException -> {
                    ApiResult.NetworkError("Problema de conexão")
                }
                else -> {
                    ApiResult.Error(e.message ?: "Erro desconhecido")
                }
            }
        }
    }

    private fun getHttpErrorMessage(code: Int): String {
        return when (code) {
            400 -> "Requisição inválida"
            401 -> "Não autorizado"
            403 -> "Acesso negado"
            404 -> "Não encontrado"
            500 -> "Erro interno do servidor"
            502 -> "Servidor indisponível"
            503 -> "Serviço temporariamente indisponível"
            else -> "Erro no servidor (código $code)"
        }
    }
}
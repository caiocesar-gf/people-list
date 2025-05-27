package com.project.network.datasource

import android.content.Context
import com.project.core.User
import com.project.network.dto.toDomain
import com.project.network.extensions.ApiResult
import com.project.network.extensions.NetworkHandler
import com.project.network.service.UserService
import com.project.core.Utils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class RemoteUserDataSourceImpl(
    private val service: UserService,
    private val context: Context
) : RemoteUserDataSource {

    private var cachedUsers: List<User>? = null
    private var cachedUser: User? = null

    override fun getUsersWithResult(searchQuery: String): Flow<ApiResult<List<User>>> = flow {
        // Verifica se há conexão com a internet
        if (!Utils.hasInternetAccess(context)) {
            cachedUsers?.let { users ->
                val filteredUsers = if (searchQuery.isEmpty()) {
                    users
                } else {
                    users.filter { user ->
                        user.name.contains(searchQuery, ignoreCase = true) ||
                                user.email.contains(searchQuery, ignoreCase = true)
                    }
                }
                emit(ApiResult.CacheResult(filteredUsers, "Sem conexão - dados do cache local"))
                return@flow
            }

            emit(ApiResult.NetworkError("Sem conexão com a internet e nenhum dado em cache"))
            return@flow
        }

        val result = NetworkHandler.loadFromNetwork {
            service.getUsers()
                .map { it.toDomain() }
                .also { users ->
                    cachedUsers = users
                }
                .filter { user ->
                    if (searchQuery.isEmpty()) {
                        true
                    } else {
                        user.name.contains(searchQuery, ignoreCase = true) ||
                                user.email.contains(searchQuery, ignoreCase = true)
                    }
                }
        }

        if (result is ApiResult.NetworkError && cachedUsers != null) {
            val filteredUsers = cachedUsers!!.filter { user ->
                if (searchQuery.isEmpty()) {
                    true
                } else {
                    user.name.contains(searchQuery, ignoreCase = true) ||
                            user.email.contains(searchQuery, ignoreCase = true)
                }
            }
            emit(ApiResult.CacheResult(filteredUsers, "Erro de conexão - usando cache local"))
        } else {
            emit(result)
        }
    }

    override suspend fun getUserById(id: Int): ApiResult<User> {
        if (!Utils.isNetworkAvailable(context)) {
            cachedUsers?.find { it.id == id }?.let { user ->
                return ApiResult.CacheResult(user, "Sem conexão - dados do cache local")
            }

            if (cachedUser?.id == id) {
                return ApiResult.CacheResult(cachedUser!!, "Sem conexão - dados do cache local")
            }

            return ApiResult.NetworkError("Sem conexão com a internet e usuário não encontrado no cache")
        }

        val result = NetworkHandler.loadFromNetwork {
            service.getUserById(id).toDomain().also { user ->
                cachedUser = user
            }
        }

        if (result is ApiResult.NetworkError) {
            cachedUsers?.find { it.id == id }?.let { user ->
                return ApiResult.CacheResult(user, "Erro de conexão - usando cache local")
            }

            if (cachedUser?.id == id) {
                return ApiResult.CacheResult(cachedUser!!, "Erro de conexão - usando cache local")
            }
        }

        return result
    }
}
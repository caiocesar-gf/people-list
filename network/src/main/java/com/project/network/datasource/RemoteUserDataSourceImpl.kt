package com.project.network.datasource

import com.project.core.User
import com.project.network.dto.toDomain
import com.project.network.extensions.ApiResult
import com.project.network.extensions.NetworkHandler
import com.project.network.service.UserService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class RemoteUserDataSourceImpl(
    private val service: UserService
) : RemoteUserDataSource {

    override fun getUsersWithResult(searchQuery: String): Flow<ApiResult<List<User>>> = flow {
        val result = NetworkHandler.loadFromNetwork {
            service.getUsers()
                .map { it.toDomain() }
                .filter { user ->
                    if (searchQuery.isEmpty()) {
                        true
                    } else {
                        user.name.contains(searchQuery, ignoreCase = true) ||
                                user.email.contains(searchQuery, ignoreCase = true)
                    }
                }
        }
        emit(result)
    }

    override suspend fun getUserById(id: Int): ApiResult<User> {
        return NetworkHandler.loadFromNetwork {
            service.getUserById(id).toDomain()
        }
    }
}
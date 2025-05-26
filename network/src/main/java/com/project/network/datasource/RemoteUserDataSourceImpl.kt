package com.project.network.datasource

import com.project.core.User
import com.project.network.dto.toDomain
import com.project.network.service.UserService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RemoteUserDataSourceImpl(
    private val service: UserService
) : RemoteUserDataSource {

    override fun getUsers(): Flow<List<User>> = flow {
        val users = withContext(Dispatchers.IO) {
            service.getUsers().map { it.toDomain() }
        }

        emit(users)

    }.catch { e ->
        emit(emptyList())
    }
}
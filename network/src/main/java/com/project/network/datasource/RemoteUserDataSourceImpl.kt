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

    override fun getUsers(searchQuery: String): Flow<List<User>> = flow {
        val users = withContext(Dispatchers.IO) {
            service.getUsers()
                .map { it.toDomain() }
                .filter { user ->
                    if (searchQuery.isEmpty()) {
                        true
                    } else {
                        // Filtra apenas por NOME e E-MAIL
                        user.name.contains(searchQuery, ignoreCase = true) ||
                                user.email.contains(searchQuery, ignoreCase = true)
                    }
                }
        }

        emit(users)

    }.catch { e ->
        emit(emptyList())
    }
}
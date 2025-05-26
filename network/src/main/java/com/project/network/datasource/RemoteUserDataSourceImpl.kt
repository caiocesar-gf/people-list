package com.project.network.datasource

import com.project.core.User
import com.project.network.dto.toDomain
import com.project.network.service.UserService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RemoteUserDataSourceImpl(
    private val service: UserService
) : RemoteUserDataSource {

    override fun getUsers(query: String): Flow<List<User>> = flow {
        try {
            val users = withContext(Dispatchers.IO) {
                service.getUsers()
                    .map { it.toDomain() }
                    .filter {
                        if (query.isEmpty()) {
                            true
                        } else {
                            it.name.contains(query, ignoreCase = true) ||
                                    it.email.contains(query, ignoreCase = true) ||
                                    it.address.city.contains(query, ignoreCase = true)
                        }
                    }
            }
            emit(users)
        } catch (e: Exception) {
            emit(emptyList())
            throw e
        }
    }
}
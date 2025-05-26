package com.project.database.datasource

import com.project.database.UserDao
import com.project.database.entity.toDomain
import com.project.database.entity.toEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.project.core.User


class LocalUserDataSourceImpl(
    private val dao: UserDao
) : LocalUserDataSource {

    override fun getUsers(): Flow<List<User>> {
        return dao.getAllUsers().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun insertUsers(users: List<User>) = withContext(Dispatchers.IO) {
        dao.insertUsers(users.map { it.toEntity() })
    }

    override suspend fun deleteAllUsers() = withContext(Dispatchers.IO) {
        dao.deleteAllUsers()
    }

    override suspend fun getUserById(id: Int): User? = withContext(Dispatchers.IO) {
        dao.getUserById(id)?.toDomain()
    }
}
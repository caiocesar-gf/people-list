package com.project.database.datasource

import com.project.core.User
import kotlinx.coroutines.flow.Flow

interface LocalUserDataSource {
    fun getUsers(): Flow<List<User>>
    suspend fun insertUsers(users: List<User>)
    suspend fun deleteAllUsers()
    suspend fun getUserById(id: Int): User?
}

package com.project.peoplelist.domain.repository

import androidx.paging.PagingData
import com.project.core.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun getUsers(): Flow<PagingData<User>>
    suspend fun getUserById(id: Int): User?
    suspend fun clearCache()
    suspend fun refreshUsers()
}

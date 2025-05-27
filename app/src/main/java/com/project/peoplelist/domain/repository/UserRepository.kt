package com.project.peoplelist.domain.repository

import androidx.paging.PagingData
import com.project.core.User
import com.project.network.extensions.ApiResult
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun getUsers(
        searchQuery: String = "",
        onCacheUsed: ((String) -> Unit)? = null
    ): Flow<PagingData<User>>

    suspend fun getUserById(id: Int): ApiResult<User>
}
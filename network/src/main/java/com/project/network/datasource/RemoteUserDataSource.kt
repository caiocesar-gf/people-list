package com.project.network.datasource

import com.project.core.User
import com.project.network.extensions.ApiResult
import kotlinx.coroutines.flow.Flow


interface RemoteUserDataSource {
    fun getUsersWithResult(searchQuery: String = ""): Flow<ApiResult<List<User>>>
    suspend fun getUserById(id: Int): ApiResult<User>
}
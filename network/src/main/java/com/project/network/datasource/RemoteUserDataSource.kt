package com.project.network.datasource

import com.project.core.User
import kotlinx.coroutines.flow.Flow


interface RemoteUserDataSource {
    fun getUsers(searchQuery: String = ""): Flow<List<User>>
}
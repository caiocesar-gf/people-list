package com.project.peoplelist.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.project.core.User
import com.project.database.datasource.LocalUserDataSource
import com.project.network.datasource.RemoteUserDataSource
import com.project.network.extensions.ApiResult
import com.project.peoplelist.data.paging.UserPagingSource
import com.project.peoplelist.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow

class UserRepositoryImpl(
    private val remoteUserDataSource: RemoteUserDataSource,
    private val localUserDataSource: LocalUserDataSource
) : UserRepository {

    override fun getUsers(
        searchQuery: String,
        onCacheUsed: ((String) -> Unit)?
    ): Flow<PagingData<User>> {
        return Pager(
            config = PagingConfig(
                pageSize = 3,
                enablePlaceholders = false,
                prefetchDistance = 1
            ),
            pagingSourceFactory = {
                UserPagingSource(
                    remoteUserDataSource = remoteUserDataSource,
                    localUserDataSource = localUserDataSource,
                    searchQuery = searchQuery,
                    onCacheUsed = onCacheUsed
                )
            }
        ).flow
    }

    override suspend fun getUserById(id: Int): ApiResult<User> {
        return remoteUserDataSource.getUserById(id)
    }
}
package com.project.peoplelist.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.project.core.User
import com.project.database.datasource.LocalUserDataSource
import com.project.network.datasource.RemoteUserDataSource
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first

class UserPagingSource(
    private val remoteUserDataSource: RemoteUserDataSource,
    private val localUserDataSource: LocalUserDataSource,
    private val searchQuery: String = ""
) : PagingSource<Int, User>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, User> {
        val currentPage = params.key ?: 1

        return try {
            delay(800)

            val allUsers = if (searchQuery.isNotEmpty()) {
                remoteUserDataSource.getUsers(searchQuery).first()
            } else {
                val cachedUsers = try {
                    localUserDataSource.getUsers().first()
                } catch (e: Exception) {
                    emptyList()
                }

                if (cachedUsers.isNotEmpty()) {
                    try {
                        val freshUsers = remoteUserDataSource.getUsers("").first()
                        if (freshUsers.isNotEmpty() && freshUsers != cachedUsers) {
                            localUserDataSource.deleteAllUsers()
                            localUserDataSource.insertUsers(freshUsers)
                        }
                    } catch (networkError: Exception) {
                    }
                    cachedUsers
                } else {
                    val users = remoteUserDataSource.getUsers("").first()
                    if (users.isNotEmpty()) {
                        try {
                            localUserDataSource.deleteAllUsers()
                            localUserDataSource.insertUsers(users)
                        } catch (e: Exception) {
                        }
                    }
                    users
                }
            }

            paginateUsers(allUsers, currentPage)

        } catch (e: Exception) {
            try {
                val cachedUsers = localUserDataSource.getUsers().first()
                val filteredUsers = if (searchQuery.isNotEmpty()) {
                    cachedUsers.filter { user ->
                        user.name.contains(searchQuery, ignoreCase = true) ||
                                user.email.contains(searchQuery, ignoreCase = true)
                    }
                } else {
                    cachedUsers
                }

                if (filteredUsers.isNotEmpty()) {
                    paginateUsers(filteredUsers, currentPage)
                } else {
                    LoadResult.Error(e)
                }
            } catch (cacheError: Exception) {
                LoadResult.Error(e)
            }
        }
    }

    private fun paginateUsers(users: List<User>, currentPage: Int): LoadResult<Int, User> {
        val pageSize = 3
        val startIndex = (currentPage - 1) * pageSize
        val endIndex = (startIndex + pageSize).coerceAtMost(users.size)

        val pageUsers = if (startIndex < users.size) {
            users.subList(startIndex, endIndex)
        } else {
            emptyList()
        }

        return LoadResult.Page(
            data = pageUsers,
            prevKey = if (currentPage == 1) null else currentPage - 1,
            nextKey = if (endIndex >= users.size) null else currentPage + 1
        )
    }

    override fun getRefreshKey(state: PagingState<Int, User>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }
}
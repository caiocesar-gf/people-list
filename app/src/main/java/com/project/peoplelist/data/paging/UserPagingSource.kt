package com.project.peoplelist.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.project.core.User
import com.project.database.datasource.LocalUserDataSource
import com.project.network.datasource.RemoteUserDataSource
import com.project.network.extensions.ApiResult
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first

class UserPagingSource(
    private val remoteUserDataSource: RemoteUserDataSource,
    private val localUserDataSource: LocalUserDataSource,
    private val searchQuery: String = "",
    private val onCacheUsed: ((String) -> Unit)? = null
) : PagingSource<Int, User>() {

    private var allCachedUsers: List<User>? = null
    private var isUsingCache = false

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, User> {
        val currentPage = params.key ?: 1

        return try {
            delay(800)

            if (isUsingCache && allCachedUsers != null) {
                return paginateUsers(allCachedUsers!!, currentPage)
            }

            val allUsers = if (searchQuery.isNotEmpty()) {
                when (val result = remoteUserDataSource.getUsersWithResult(searchQuery).first()) {
                    is ApiResult.Success -> {
                        isUsingCache = false
                        result.data
                    }
                    is ApiResult.Error -> {
                        return tryLocalCacheWithSearch(searchQuery) ?: LoadResult.Error(Exception(result.message))
                    }
                    is ApiResult.ParseError -> return LoadResult.Error(Exception("Erro ao processar dados: ${result.message}"))
                    is ApiResult.NetworkError -> {
                        return tryLocalCacheWithSearch(searchQuery) ?: LoadResult.Error(Exception(result.message))
                    }
                    is ApiResult.CacheResult -> {
                        onCacheUsed?.invoke(result.message)
                        isUsingCache = true
                        allCachedUsers = result.data
                        result.data
                    }
                }
            } else {
                val cachedUsers = try {
                    localUserDataSource.getUsers().first()
                } catch (e: Exception) {
                    emptyList()
                }

                if (cachedUsers.isNotEmpty()) {
                    try {
                        when (val result = remoteUserDataSource.getUsersWithResult("").first()) {
                            is ApiResult.Success -> {
                                val freshUsers = result.data
                                if (freshUsers.isNotEmpty() && freshUsers != cachedUsers) {
                                    localUserDataSource.deleteAllUsers()
                                    localUserDataSource.insertUsers(freshUsers)
                                }
                                isUsingCache = false
                                freshUsers
                            }
                            is ApiResult.CacheResult -> {
                                onCacheUsed?.invoke(result.message)
                                isUsingCache = true
                                allCachedUsers = result.data
                                result.data
                            }
                            else -> {
                                isUsingCache = true
                                allCachedUsers = cachedUsers
                                if (currentPage == 1) {
                                    onCacheUsed?.invoke("Erro de conexão - usando dados salvos localmente")
                                }
                                cachedUsers
                            }
                        }
                    } catch (networkError: Exception) {
                        isUsingCache = true
                        allCachedUsers = cachedUsers
                        if (currentPage == 1) {
                            onCacheUsed?.invoke("Erro de conexão - usando dados salvos localmente")
                        }
                        cachedUsers
                    }
                } else {
                    when (val result = remoteUserDataSource.getUsersWithResult("").first()) {
                        is ApiResult.Success -> {
                            val users = result.data
                            if (users.isNotEmpty()) {
                                try {
                                    localUserDataSource.deleteAllUsers()
                                    localUserDataSource.insertUsers(users)
                                } catch (_: Exception) {
                                }
                            }
                            isUsingCache = false
                            users
                        }
                        is ApiResult.Error -> return LoadResult.Error(Exception(result.message))
                        is ApiResult.ParseError -> return LoadResult.Error(Exception("Erro ao processar dados: ${result.message}"))
                        is ApiResult.NetworkError -> return LoadResult.Error(Exception(result.message))
                        is ApiResult.CacheResult -> {
                            onCacheUsed?.invoke(result.message)
                            val users = result.data
                            isUsingCache = true
                            allCachedUsers = users
                            if (users.isNotEmpty()) {
                                try {
                                    localUserDataSource.deleteAllUsers()
                                    localUserDataSource.insertUsers(users)
                                } catch (_: Exception) {
                                }
                            }
                            users
                        }
                    }
                }
            }

            paginateUsers(allUsers, currentPage)

        } catch (e: Exception) {
            tryLocalCacheWithSearch(searchQuery) ?: LoadResult.Error(e)
        }
    }

    private suspend fun tryLocalCacheWithSearch(searchQuery: String): LoadResult<Int, User>? {
        return try {
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
                onCacheUsed?.invoke("Erro de conexão - usando dados salvos localmente")
                isUsingCache = true
                allCachedUsers = filteredUsers
                paginateUsers(filteredUsers, 1)
            } else {
                null
            }
        } catch (cacheError: Exception) {
            null
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
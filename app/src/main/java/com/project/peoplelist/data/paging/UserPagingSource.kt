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
    private val onCacheUsed: ((String) -> Unit)? = null // Callback para notificar uso de cache
) : PagingSource<Int, User>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, User> {
        val currentPage = params.key ?: 1

        return try {
            delay(800)

            val allUsers = if (searchQuery.isNotEmpty()) {
                when (val result = remoteUserDataSource.getUsersWithResult(searchQuery).first()) {
                    is ApiResult.Success -> result.data
                    is ApiResult.Error -> return LoadResult.Error(Exception(result.message))
                    is ApiResult.ParseError -> return LoadResult.Error(Exception("Erro ao processar dados: ${result.message}"))
                    is ApiResult.NetworkError -> return LoadResult.Error(Exception(result.message))
                    is ApiResult.CacheResult -> {
                        onCacheUsed?.invoke(result.message)
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
                                freshUsers
                            }
                            is ApiResult.CacheResult -> {
                                onCacheUsed?.invoke(result.message)
                                result.data
                            }
                            else -> {
                                cachedUsers
                            }
                        }
                    } catch (networkError: Exception) {
                        // Se houver erro de rede, usa cache local
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
                            users
                        }
                        is ApiResult.Error -> return LoadResult.Error(Exception(result.message))
                        is ApiResult.ParseError -> return LoadResult.Error(Exception("Erro ao processar dados: ${result.message}"))
                        is ApiResult.NetworkError -> return LoadResult.Error(Exception(result.message))
                        is ApiResult.CacheResult -> {
                            onCacheUsed?.invoke(result.message)
                            val users = result.data
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
                    // Notifica que está usando cache local devido a erro
                    onCacheUsed?.invoke("Erro de conexão - usando dados salvos localmente")
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
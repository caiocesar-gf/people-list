package com.project.peoplelist.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.project.core.User
import com.project.database.datasource.LocalUserDataSource
import com.project.network.datasource.RemoteUserDataSource
import kotlinx.coroutines.flow.first

class UserPagingSource(
    private val remoteUserDataSource: RemoteUserDataSource,
    private val localUserDataSource: LocalUserDataSource,
    private val searchQuery: String = ""
) : PagingSource<Int, User>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, User> {
        val currentPage = params.key ?: 1

        return try {
            val users = if (searchQuery.isEmpty()) {
                // Para busca sem filtro, tenta cache primeiro
                loadUsersWithCacheStrategy(params, currentPage)
            } else {
                // Para busca com filtro, sempre vai para remote
                loadUsersFromRemote(params, currentPage)
            }

            users
        } catch (e: Exception) {
            tryLoadFromCache(params, currentPage) ?: LoadResult.Error(e)
        }
    }

    private suspend fun loadUsersWithCacheStrategy(
        params: LoadParams<Int>,
        currentPage: Int
    ): LoadResult<Int, User> {
        return try {
            // Primeiro tenta carregar do cache local
            val cachedUsers = localUserDataSource.getUsers().first()

            if (cachedUsers.isNotEmpty()) {
                // Se tem dados no cache, usa eles
                paginateUsers(cachedUsers, params, currentPage)
            } else {
                // Se não tem cache, busca remote e salva no cache
                val remoteUsers = remoteUserDataSource.getUsers("").first()

                // Salva no cache para próximas buscas - APENAS se tiver dados
                if (remoteUsers.isNotEmpty()) {
                    try {
                        localUserDataSource.deleteAllUsers()
                        localUserDataSource.insertUsers(remoteUsers)
                    } catch (cacheException: Exception) {
                        // Se falhar ao salvar no cache, continua sem cache
                        // Não afeta o resultado principal
                    }
                }

                paginateUsers(remoteUsers, params, currentPage)
            }
        } catch (e: Exception) {
            throw e
        }
    }

    private suspend fun loadUsersFromRemote(
        params: LoadParams<Int>,
        currentPage: Int
    ): LoadResult<Int, User> {
        return try {
            val remoteUsers = remoteUserDataSource.getUsers(searchQuery).first()
            paginateUsers(remoteUsers, params, currentPage)
        } catch (e: Exception) {
            throw e
        }
    }

    private suspend fun tryLoadFromCache(
        params: LoadParams<Int>,
        currentPage: Int
    ): LoadResult<Int, User>? {
        return try {
            val cachedUsers = localUserDataSource.getUsers().first()
            if (cachedUsers.isNotEmpty()) {
                val filteredUsers = if (searchQuery.isNotEmpty()) {
                    cachedUsers.filter { user ->
                        user.name.contains(searchQuery, ignoreCase = true) ||
                                user.email.contains(searchQuery, ignoreCase = true) ||
                                user.address.city.contains(searchQuery, ignoreCase = true)
                    }
                } else {
                    cachedUsers
                }
                paginateUsers(filteredUsers, params, currentPage)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun paginateUsers(
        allUsers: List<User>,
        params: LoadParams<Int>,
        currentPage: Int
    ): LoadResult<Int, User> {
        val pageSize = params.loadSize
        val startIndex = (currentPage - 1) * pageSize
        val endIndex = (startIndex + pageSize).coerceAtMost(allUsers.size)

        val users = if (startIndex < allUsers.size) {
            allUsers.subList(startIndex, endIndex)
        } else {
            emptyList()
        }

        return LoadResult.Page(
            data = users,
            prevKey = if (currentPage == 1) null else currentPage - 1,
            nextKey = if (endIndex >= allUsers.size) null else currentPage + 1
        )
    }

    override fun getRefreshKey(state: PagingState<Int, User>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }
}
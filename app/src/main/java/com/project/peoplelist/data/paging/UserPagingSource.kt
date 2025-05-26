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
    private val localUserDataSource: LocalUserDataSource
) : PagingSource<Int, User>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, User> {
        val currentPage = params.key ?: 1

        return try {
            // Verifica cache primeiro
            val cachedUsers = try {
                localUserDataSource.getUsers().first()
            } catch (e: Exception) {
                emptyList()
            }

            // Se tem cache, use ele e tente atualizar em background
            if (cachedUsers.isNotEmpty()) {
                val result = paginateUsers(cachedUsers, currentPage)

                // Tenta atualizar cache em background (sem afetar resultado)
                try {
                    val freshUsers = remoteUserDataSource.getUsers().first()
                    if (freshUsers.isNotEmpty() && freshUsers != cachedUsers) {
                        localUserDataSource.deleteAllUsers()
                        localUserDataSource.insertUsers(freshUsers)
                    }
                } catch (networkError: Exception) {
                    // Falha de rede é silenciosa quando temos cache
                }

                return result
            }

            // Se não tem cache, busca da rede
            delay(800) // ✅ Delay para mostrar loading

            val allUsers = remoteUserDataSource.getUsers().first()

            // Salva no cache
            if (allUsers.isNotEmpty()) {
                try {
                    localUserDataSource.deleteAllUsers()
                    localUserDataSource.insertUsers(allUsers)
                } catch (e: Exception) {
                    // Erro no cache não afeta o resultado
                }
            }

            paginateUsers(allUsers, currentPage)

        } catch (e: Exception) {
            // Em caso de erro de rede, SEMPRE tenta carregar do cache
            try {
                val cachedUsers = localUserDataSource.getUsers().first()

                if (cachedUsers.isNotEmpty()) {
                    paginateUsers(cachedUsers, currentPage)
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
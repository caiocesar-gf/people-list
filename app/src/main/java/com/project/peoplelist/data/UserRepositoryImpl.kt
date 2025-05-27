package com.project.peoplelist.data

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
import kotlinx.coroutines.flow.first

class UserRepositoryImpl(
    private val remoteUserDataSource: RemoteUserDataSource,
    private val localUserDataSource: LocalUserDataSource
) : UserRepository {

    override fun getUsers(searchQuery: String): Flow<PagingData<User>> {
        return Pager(
            config = PagingConfig(
                pageSize = 3,
                enablePlaceholders = false,
                prefetchDistance = 1,
                initialLoadSize = 3,
                maxSize = 50
            ),
            pagingSourceFactory = {
                UserPagingSource(
                    remoteUserDataSource = remoteUserDataSource,
                    localUserDataSource = localUserDataSource,
                    searchQuery = searchQuery
                )
            }
        ).flow
    }

    override suspend fun getUserById(id: Int): User? {
        return try {
            val localUser = localUserDataSource.getUserById(id)

            if (localUser != null) {
                localUser
            } else {
                when (val result = remoteUserDataSource.getUserById(id)) {
                    is ApiResult.Success -> {
                        try {
                            localUserDataSource.insertUsers(listOf(result.data))
                        } catch (cacheException: Exception) {
                            // Se falhar ao salvar no cache, continua sem cache
                        }
                        result.data
                    }
                    is ApiResult.Error -> {
                        // Log: Erro HTTP ${result.code}: ${result.message}
                        null
                    }
                    is ApiResult.ParseError -> {
                        // Log: Erro de parsing: ${result.message}
                        null
                    }
                    is ApiResult.NetworkError -> {
                        // Log: Erro de rede: ${result.message}
                        null
                    }
                }
            }
        } catch (e: Exception) {
            try {
                localUserDataSource.getUserById(id)
            } catch (cacheException: Exception) {
                null
            }
        }
    }

    override suspend fun clearCache() {
        try {
            localUserDataSource.deleteAllUsers()
        } catch (e: Exception) {
            throw Exception("Erro ao limpar cache: ${e.message}", e)
        }
    }

    override suspend fun refreshUsers() {
        try {
            when (val result = remoteUserDataSource.getUsersWithResult("").first()) {
                is ApiResult.Success -> {
                    try {
                        localUserDataSource.deleteAllUsers()
                    } catch (deleteException: Exception) {
                    }

                    if (result.data.isNotEmpty()) {
                        localUserDataSource.insertUsers(result.data)
                    }
                }
                is ApiResult.Error -> {
                    throw Exception("Erro do servidor: ${result.message}")
                }
                is ApiResult.ParseError -> {
                    throw Exception("Erro ao processar dados: ${result.message}")
                }
                is ApiResult.NetworkError -> {
                    throw Exception("Erro de conexão: ${result.message}")
                }
            }
        } catch (e: Exception) {
            throw Exception("Erro ao atualizar dados: ${e.message}", e)
        }
    }
}
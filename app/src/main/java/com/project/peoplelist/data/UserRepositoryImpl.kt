package com.project.peoplelist.data

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.project.core.User
import com.project.database.datasource.LocalUserDataSource
import com.project.network.datasource.RemoteUserDataSource
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
                pageSize = 3,  // ✅ 3 usuários por página SEMPRE
                enablePlaceholders = false,
                prefetchDistance = 1,  // ✅ Menor para melhor controle
                initialLoadSize = 3,   // ✅ Primeira carga também 3
                maxSize = 50          // ✅ Limite máximo para performance
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
                try {
                    val allUsers = remoteUserDataSource.getUsers("").first()
                    val user = allUsers.find { it.id == id }

                    user?.let {
                        try {
                            localUserDataSource.insertUsers(listOf(it))
                        } catch (cacheException: Exception) {
                            // Se falhar ao salvar no cache, continua sem cache
                        }
                    }

                    user
                } catch (networkException: Exception) {
                    null
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
            val users = remoteUserDataSource.getUsers("").first()

            try {
                localUserDataSource.deleteAllUsers()
            } catch (deleteException: Exception) {
                // Se falhar ao deletar, continua
            }

            if (users.isNotEmpty()) {
                localUserDataSource.insertUsers(users)
            }
        } catch (e: Exception) {
            throw Exception("Erro ao atualizar dados: ${e.message}", e)
        }
    }
}

package com.project.peoplelist.domain.usecase

import androidx.paging.PagingData
import com.project.core.User
import com.project.peoplelist.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow


class GetUsersUseCase(
    private val userRepository: UserRepository
) {
    operator fun invoke(searchQuery: String = ""): Flow<PagingData<User>> {
        return userRepository.getUsers(searchQuery)
    }
}
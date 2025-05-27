package com.project.peoplelist.domain.usecase

import com.project.core.User
import com.project.network.extensions.ApiResult
import com.project.peoplelist.domain.repository.UserRepository

class GetUserByIdUseCase(
    private val userRepository: UserRepository
) {

    suspend operator fun invoke(id: Int): ApiResult<User> {
        return userRepository.getUserById(id)
    }
}
package com.project.peoplelist.domain.usecase

import com.project.core.User
import com.project.peoplelist.domain.repository.UserRepository

class GetUserByIdUseCase(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(userId: Int): User? {
        return userRepository.getUserById(userId)
    }
}
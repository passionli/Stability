package com.example.stability.domain.usecase

import com.example.stability.domain.model.User
import com.example.stability.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow

class GetCurrentUserUseCase(private val userRepository: UserRepository) {

    operator fun invoke(): Flow<User?> {
        return userRepository.getCurrentUser()
    }
}

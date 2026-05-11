package com.example.stability.domain.usecase

import com.example.stability.domain.repository.UserRepository

class LogoutUseCase(private val userRepository: UserRepository) {

    suspend operator fun invoke() {
        userRepository.logout()
        userRepository.clearUser()
    }
}

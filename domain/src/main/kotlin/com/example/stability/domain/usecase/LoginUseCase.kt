package com.example.stability.domain.usecase

import com.example.stability.domain.model.LoginResult
import com.example.stability.domain.model.User
import com.example.stability.domain.repository.UserRepository

class LoginUseCase(private val userRepository: UserRepository) {

    suspend operator fun invoke(username: String, password: String): LoginResult {
        return try {
            val user = userRepository.login(username, password)
            userRepository.saveUser(user)
            LoginResult.Success(user)
        } catch (e: Exception) {
            LoginResult.Error(e.message ?: "登录失败")
        }
    }
}

package com.example.stability.domain.model

sealed interface LoginResult {
    data class Success(val user: User) : LoginResult
    data class Error(val message: String) : LoginResult
}

package com.example.stability.domain.repository

import com.example.stability.domain.model.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    suspend fun login(username: String, password: String): User
    suspend fun logout()
    fun getCurrentUser(): Flow<User?>
    suspend fun saveUser(user: User)
    suspend fun clearUser()
}

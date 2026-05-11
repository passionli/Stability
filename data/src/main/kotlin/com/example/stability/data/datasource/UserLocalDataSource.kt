package com.example.stability.data.datasource

import com.example.stability.domain.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class UserLocalDataSource {

    private val userFlow = MutableStateFlow<User?>(null)
    private var cachedUser: User? = null

    fun getUser(): Flow<User?> = userFlow

    suspend fun saveUser(user: User) {
        cachedUser = user
        userFlow.value = user
    }

    suspend fun clearUser() {
        cachedUser = null
        userFlow.value = null
    }

    fun getCachedUser(): User? = cachedUser
}

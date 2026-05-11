package com.example.stability.data.datasource

import com.example.stability.domain.model.User
import kotlinx.coroutines.delay

class UserRemoteDataSource {

    private val mockUsers = mapOf(
        "admin" to "123456",
        "test" to "password"
    )

    suspend fun login(username: String, password: String): User {
        delay(1000)
        
        if (mockUsers[username] == password) {
            return User(
                id = "user_${System.currentTimeMillis()}",
                username = username,
                email = "$username@example.com",
                token = "token_${System.currentTimeMillis()}"
            )
        }
        
        throw IllegalArgumentException("用户名或密码错误")
    }

    suspend fun logout() {
        delay(500)
    }
}

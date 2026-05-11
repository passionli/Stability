package com.example.stability.data.repository

import com.example.stability.data.datasource.UserLocalDataSource
import com.example.stability.data.datasource.UserRemoteDataSource
import com.example.stability.domain.model.User
import com.example.stability.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow

class UserRepositoryImpl(
    private val remoteDataSource: UserRemoteDataSource,
    private val localDataSource: UserLocalDataSource
) : UserRepository {

    override suspend fun login(username: String, password: String): User {
        return remoteDataSource.login(username, password)
    }

    override suspend fun logout() {
        remoteDataSource.logout()
    }

    override fun getCurrentUser(): Flow<User?> {
        return localDataSource.getUser()
    }

    override suspend fun saveUser(user: User) {
        localDataSource.saveUser(user)
    }

    override suspend fun clearUser() {
        localDataSource.clearUser()
    }
}

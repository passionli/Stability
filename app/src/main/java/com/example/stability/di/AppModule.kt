package com.example.stability.di

import com.example.stability.data.datasource.UserLocalDataSource
import com.example.stability.data.datasource.UserRemoteDataSource
import com.example.stability.data.repository.UserRepositoryImpl
import com.example.stability.domain.repository.UserRepository
import com.example.stability.domain.usecase.GetCurrentUserUseCase
import com.example.stability.domain.usecase.LoginUseCase
import com.example.stability.domain.usecase.LogoutUseCase

object AppModule {

    private val userRemoteDataSource by lazy { UserRemoteDataSource() }
    private val userLocalDataSource by lazy { UserLocalDataSource() }
    private val userRepository by lazy { UserRepositoryImpl(userRemoteDataSource, userLocalDataSource) }

    val loginUseCase: LoginUseCase by lazy { LoginUseCase(userRepository) }
    val logoutUseCase: LogoutUseCase by lazy { LogoutUseCase(userRepository) }
    val getCurrentUserUseCase: GetCurrentUserUseCase by lazy { GetCurrentUserUseCase(userRepository) }
}

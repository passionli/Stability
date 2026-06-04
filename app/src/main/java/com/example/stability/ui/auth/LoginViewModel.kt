package com.example.stability.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stability.di.AppModule
import com.example.stability.domain.model.LoginResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {

    private val loginUseCase = AppModule.loginUseCase
    private val logoutUseCase = AppModule.logoutUseCase
    private val getCurrentUserUseCase = AppModule.getCurrentUserUseCase

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _username = MutableStateFlow("")
    val username: StateFlow<String> = _username.asStateFlow()

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    private val _usernameError = MutableStateFlow("")
    val usernameError: StateFlow<String> = _usernameError.asStateFlow()

    private val _passwordError = MutableStateFlow("")
    val passwordError: StateFlow<String> = _passwordError.asStateFlow()

    init {
        checkCurrentUser()
    }

    private fun checkCurrentUser() {
        viewModelScope.launch {
            getCurrentUserUseCase().collectLatest { user ->
                if (user != null) {
                    _uiState.value = LoginUiState.LoggedIn(user)
                }
            }
        }
    }

    fun onUsernameChange(value: String) {
        _username.value = value
        if (_usernameError.value.isNotEmpty()) {
            _usernameError.value = ""
        }
    }

    fun onPasswordChange(value: String) {
        _password.value = value
        if (_passwordError.value.isNotEmpty()) {
            _passwordError.value = ""
        }
    }

    fun login() {
        if (!validateInput()) {
            return
        }

        _uiState.value = LoginUiState.Loading

        viewModelScope.launch {
            val result = loginUseCase(_username.value, _password.value)
            when (result) {
                is LoginResult.Success -> {
                    _uiState.value = LoginUiState.LoggedIn(result.user)
                }
                is LoginResult.Error -> {
                    _uiState.value = LoginUiState.Error(result.message)
                }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            logoutUseCase()
            _uiState.value = LoginUiState.Idle
            _username.value = ""
            _password.value = ""
        }
    }

    private fun validateInput(): Boolean {
        val usernameValid = validateField(
            _username.value,
            minLength = 3,
            emptyError = "用户名不能为空",
            lengthError = "用户名至少3个字符"
        ) { _usernameError.value = it }

        val passwordValid = validateField(
            _password.value,
            minLength = 6,
            emptyError = "密码不能为空",
            lengthError = "密码至少6个字符"
        ) { _passwordError.value = it }

        return usernameValid && passwordValid
    }

    private fun validateField(
        value: String,
        minLength: Int,
        emptyError: String,
        lengthError: String,
        setError: (String) -> Unit
    ): Boolean = when {
        value.isEmpty() -> { setError(emptyError); false }
        value.length < minLength -> { setError(lengthError); false }
        else -> { setError(""); true }
    }

    fun clearError() {
        _uiState.value = LoginUiState.Idle
    }
}

sealed interface LoginUiState {
    object Idle : LoginUiState
    object Loading : LoginUiState
    data class LoggedIn(val user: com.example.stability.domain.model.User) : LoginUiState
    data class Error(val message: String) : LoginUiState
}

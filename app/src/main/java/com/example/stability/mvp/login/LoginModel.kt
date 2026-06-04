package com.example.stability.mvp.login

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * 登录 Model 实现
 * 设计原因：负责数据处理，与 Presenter 分离
 * 技术目的：模拟登录验证和会话管理
 */
class LoginModel : LoginContract.Model {
    
    companion object {
        private const val TAG = "LoginModel"
        private const val VALID_USERNAME = "admin"
        private const val VALID_PASSWORD = "123456"
    }
    
    private var sessionToken: String? = null
    
    /**
     * 执行登录（使用 Coroutines）
     */
    override fun login(username: String, password: String, callback: LoginContract.LoginCallback) {
        Log.d(TAG, "login: username=$username, password=$password")
        
        // 使用 Coroutines 替代手动线程
        GlobalScope.launch {
            try {
                // 模拟网络请求延迟
                val result = performLogin(username, password)
                when (result) {
                    is LoginResult.Success -> {
                        saveSession(result.token)
                        Log.d(TAG, "Login success, token: ${result.token}")
                        callback.onSuccess(result.token)
                    }
                    is LoginResult.Error -> {
                        Log.d(TAG, "Login failed: ${result.message}")
                        callback.onError(result.message)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Login error: ${e.message}")
                callback.onError("登录失败，请稍后重试")
            }
        }
    }
    
    /**
     * 执行登录验证（挂起函数）
     */
    private suspend fun performLogin(username: String, password: String): LoginResult = withContext(Dispatchers.IO) {
        // 模拟网络延迟
        delay(1000)
        
        // 验证用户名和密码
        when {
            username == VALID_USERNAME && password == VALID_PASSWORD -> {
                LoginResult.Success("token_${System.currentTimeMillis()}")
            }
            username != VALID_USERNAME -> {
                LoginResult.Error("用户名错误")
            }
            else -> {
                LoginResult.Error("密码错误")
            }
        }
    }
    
    /**
     * 登录结果密封类
     */
    private sealed class LoginResult {
        data class Success(val token: String) : LoginResult()
        data class Error(val message: String) : LoginResult()
    }
    
    override fun saveSession(token: String) {
        Log.d(TAG, "saveSession: $token")
        sessionToken = token
    }
    
    override fun getSession(): String? {
        Log.d(TAG, "getSession: $sessionToken")
        return sessionToken
    }
    
    override fun clearSession() {
        Log.d(TAG, "clearSession")
        sessionToken = null
    }
}
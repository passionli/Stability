package com.example.stability.mvp.login

import android.util.Log

/**
 * 登录 Model 实现
 * 设计原因：负责数据处理，与 Presenter 分离
 * 技术目的：模拟登录验证和会话管理
 */
class LoginModel : LoginContract.Model {
    
    companion object {
        private const val TAG = "LoginModel"
        // 模拟有效的用户名和密码
        private const val VALID_USERNAME = "admin"
        private const val VALID_PASSWORD = "123456"
    }
    
    // 模拟会话存储
    private var sessionToken: String? = null
    
    /**
     * 执行登录
     * 技术目的：模拟登录验证过程
     */
    override fun login(username: String, password: String, callback: LoginContract.LoginCallback) {
        Log.d(TAG, "login: username=$username, password=$password")
        
        // 模拟网络请求延迟
        Thread {
            try {
                Thread.sleep(1000) // 模拟网络延迟
                
                // 验证用户名和密码
                if (username == VALID_USERNAME && password == VALID_PASSWORD) {
                    // 生成模拟的 token
                    val token = "token_${System.currentTimeMillis()}"
                    saveSession(token)
                    Log.d(TAG, "Login success, token: $token")
                    callback.onSuccess(token)
                } else {
                    val errorMessage = if (username != VALID_USERNAME) {
                        "用户名错误"
                    } else {
                        "密码错误"
                    }
                    Log.d(TAG, "Login failed: $errorMessage")
                    callback.onError(errorMessage)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Login error: ${e.message}")
                callback.onError("登录失败，请稍后重试")
            }
        }.start()
    }
    
    /**
     * 保存会话信息
     * 技术目的：存储登录状态
     */
    override fun saveSession(token: String) {
        Log.d(TAG, "saveSession: $token")
        sessionToken = token
    }
    
    /**
     * 获取会话信息
     * 技术目的：检查是否已登录
     */
    override fun getSession(): String? {
        Log.d(TAG, "getSession: $sessionToken")
        return sessionToken
    }
    
    /**
     * 清除会话信息
     * 技术目的：退出登录时使用
     */
    override fun clearSession() {
        Log.d(TAG, "clearSession")
        sessionToken = null
    }
}
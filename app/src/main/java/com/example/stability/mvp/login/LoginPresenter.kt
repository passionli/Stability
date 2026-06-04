package com.example.stability.mvp.login

import android.util.Log

/**
 * 登录 Presenter 实现
 * 设计原因：负责业务逻辑，连接 View 和 Model
 * 技术目的：处理登录流程和业务逻辑
 */
class LoginPresenter(private val model: LoginContract.Model) : LoginContract.Presenter {
    
    companion object {
        private const val TAG = "LoginPresenter"
    }
    
    // View 引用
    private var view: LoginContract.View? = null
    
    /**
     * 绑定 View
     * 技术目的：建立 Presenter 和 View 的联系
     */
    override fun attachView(view: LoginContract.View) {
        Log.d(TAG, "attachView")
        this.view = view
    }
    
    /**
     * 解绑 View
     * 技术目的：防止内存泄漏，在 View 销毁时调用
     */
    override fun detachView() {
        Log.d(TAG, "detachView")
        this.view = null
    }
    
    /**
     * 执行登录操作
     * 技术目的：处理登录业务逻辑
     */
    override fun login(username: String, password: String) {
        Log.d(TAG, "login: username=$username, password=$password")
        
        // 验证输入
        if (!validateInput(username, password)) {
            return
        }
        
        // 显示加载状态
        view?.showLoading()
        
        // 调用 Model 执行登录
        model.login(username, password, object : LoginContract.LoginCallback {
            override fun onSuccess(token: String) {
                Log.d(TAG, "Login success")
                view?.hideLoading()
                view?.showLoginSuccess()
                view?.navigateToHome()
            }
            
            override fun onError(message: String) {
                Log.d(TAG, "Login error: $message")
                view?.hideLoading()
                view?.showLoginError(message)
            }
        })
    }
    
    /**
     * 检查会话状态
     * 技术目的：检查用户是否已登录
     */
    override fun checkSession() {
        Log.d(TAG, "checkSession")
        val token = model.getSession()
        if (token != null) {
            Log.d(TAG, "Session exists, navigating to home")
            view?.navigateToHome()
        }
    }
    
    /**
     * 验证输入
     * 技术目的：确保输入数据的有效性
     */
    private fun validateInput(username: String, password: String): Boolean {
        val usernameValidation = validateUsername(username)
        val passwordValidation = validatePassword(password)

        view?.showUsernameError(usernameValidation.errorMessage)
        view?.showPasswordError(passwordValidation.errorMessage)

        return usernameValidation.isValid && passwordValidation.isValid
    }

    /**
     * 验证结果数据类
     */
    private data class ValidationResult(val isValid: Boolean, val errorMessage: String)

    /**
     * 验证用户名
     */
    private fun validateUsername(username: String): ValidationResult = when {
        username.isEmpty() -> ValidationResult(false, "用户名不能为空")
        username.length < 3 -> ValidationResult(false, "用户名至少3个字符")
        else -> ValidationResult(true, "")
    }

    /**
     * 验证密码
     */
    private fun validatePassword(password: String): ValidationResult = when {
        password.isEmpty() -> ValidationResult(false, "密码不能为空")
        password.length < 6 -> ValidationResult(false, "密码至少6个字符")
        else -> ValidationResult(true, "")
    }
}
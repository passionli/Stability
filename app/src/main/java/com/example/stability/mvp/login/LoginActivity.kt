package com.example.stability.mvp.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.stability.R
import com.example.stability.mvp.login.MainActivity

/**
 * 登录 Activity
 * 设计原因：实现 View 接口，负责 UI 展示和用户交互
 * 技术目的：提供登录界面和用户操作处理
 */
class LoginActivity : AppCompatActivity(), LoginContract.View {
    
    companion object {
        private const val TAG = "LoginActivity"
    }
    
    private lateinit var presenter: LoginContract.Presenter
    private lateinit var etUsername: EditText
    private lateinit var etPassword: EditText
    private lateinit var tvUsernameError: TextView
    private lateinit var tvPasswordError: TextView
    private lateinit var progressBar: ProgressBar
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        Log.d(TAG, "onCreate")
        
        // 初始化视图
        initViews()
        
        // 创建 Presenter
        presenter = LoginPresenter(LoginModel())
        presenter.attachView(this)
        
        // 检查会话状态
        presenter.checkSession()
    }
    
    override fun onDestroy() {
        Log.d(TAG, "onDestroy")
        presenter.detachView()
        super.onDestroy()
    }
    
    /**
     * 初始化视图
     * 技术目的：获取 UI 控件引用
     */
    private fun initViews() {
        etUsername = findViewById(R.id.et_username)
        etPassword = findViewById(R.id.et_password)
        tvUsernameError = findViewById(R.id.tv_username_error)
        tvPasswordError = findViewById(R.id.tv_password_error)
        progressBar = findViewById(R.id.progress_bar)
        
        // 设置登录按钮点击事件
        findViewById<View>(R.id.btn_login).setOnClickListener {
            val username = etUsername.text.toString()
            val password = etPassword.text.toString()
            presenter.login(username, password)
        }
    }
    
    /**
     * 显示加载状态
     * 技术目的：在登录过程中显示加载动画
     */
    override fun showLoading() {
        Log.d(TAG, "showLoading")
        progressBar.visibility = View.VISIBLE
    }
    
    /**
     * 隐藏加载状态
     * 技术目的：登录完成后隐藏加载动画
     */
    override fun hideLoading() {
        Log.d(TAG, "hideLoading")
        progressBar.visibility = View.GONE
    }
    
    /**
     * 显示用户名错误
     * 技术目的：提示用户用户名输入错误
     */
    override fun showUsernameError(message: String) {
        Log.d(TAG, "showUsernameError: $message")
        tvUsernameError.text = message
        tvUsernameError.visibility = if (message.isEmpty()) View.GONE else View.VISIBLE
    }
    
    /**
     * 显示密码错误
     * 技术目的：提示用户密码输入错误
     */
    override fun showPasswordError(message: String) {
        Log.d(TAG, "showPasswordError: $message")
        tvPasswordError.text = message
        tvPasswordError.visibility = if (message.isEmpty()) View.GONE else View.VISIBLE
    }
    
    /**
     * 显示登录成功
     * 技术目的：提示用户登录成功
     */
    override fun showLoginSuccess() {
        Log.d(TAG, "showLoginSuccess")
        android.widget.Toast.makeText(this, "登录成功", android.widget.Toast.LENGTH_SHORT).show()
    }
    
    /**
     * 显示登录错误
     * 技术目的：提示用户登录失败的原因
     */
    override fun showLoginError(message: String) {
        Log.d(TAG, "showLoginError: $message")
        android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_SHORT).show()
    }
    
    /**
     * 跳转到主页
     * 技术目的：登录成功后导航到应用主页
     */
    override fun navigateToHome() {
        Log.d(TAG, "navigateToHome")
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }
}
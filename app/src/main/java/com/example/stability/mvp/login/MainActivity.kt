package com.example.stability.mvp.login

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.stability.R

/**
 * MVP 登录示例的主 Activity
 * 设计原因：提供登录成功后的主页界面
 * 技术目的：展示登录成功后的状态
 */
class MainActivity : AppCompatActivity() {
    
    companion object {
        private const val TAG = "MVP_MainActivity"
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mvp_main)
        Log.d(TAG, "onCreate")
        
        // 设置退出登录按钮点击事件
        findViewById<View>(R.id.btn_logout).setOnClickListener {
            logout()
        }
    }
    
    /**
     * 退出登录
     * 技术目的：清除会话信息并返回登录界面
     */
    private fun logout() {
        Log.d(TAG, "logout")
        // 清除会话信息
        val model = LoginModel()
        model.clearSession()
        
        // 返回登录界面
        val intent = android.content.Intent(this, LoginActivity::class.java)
        intent.flags = android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK or android.content.Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }
}
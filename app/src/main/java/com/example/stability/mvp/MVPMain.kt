package com.example.stability.mvp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.stability.mvp.basic.MVPBasicExample
import com.example.stability.mvp.intermediate.MVPIntermediateExample
import com.example.stability.mvp.advanced.MVPAdvancedExample
import com.example.stability.mvp.login.LoginActivity

/**
 * MVP 统一管理类
 * 用于管理和启动不同级别的 MVP 示例
 */
class MVPMain : AppCompatActivity() {
    
    companion object {
        private const val TAG = "MVP"
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        Log.d(TAG, "=== MVPMain.onCreate called ===")
        Log.d(TAG, "Thread ID: ${Thread.currentThread().id}")
        
        showMVPExamplesDialog()
    }
    
    /**
     * 显示 MVP 示例选择对话框
     * 设计原因：提供一个清晰的菜单让用户选择不同级别的 MVP 示例
     * 技术目的：通过 AlertDialog 显示选项列表
     */
    private fun showMVPExamplesDialog() {
        val examples = arrayOf(
            "初级 - MVP 基础概念",
            "中级 - MVP 登录示例",
            "高级 - MVP 完整实现",
            "实际 - 可运行的登录功能"
        )
        
        val builder = android.app.AlertDialog.Builder(this)
        builder.setTitle("MVP 架构示例")
        builder.setItems(examples) { dialog, which ->
            Log.d(TAG, "用户选择了 MVP 示例: $which")
            when (which) {
                0 -> {
                    Log.d(TAG, "启动初级 MVP 基础概念示例")
                    MVPBasicExample().start()
                    finish()
                }
                1 -> {
                    Log.d(TAG, "启动中级 MVP 登录示例")
                    MVPIntermediateExample().start()
                    finish()
                }
                2 -> {
                    Log.d(TAG, "启动高级 MVP 完整实现示例")
                    MVPAdvancedExample().start()
                    finish()
                }
                3 -> {
                    Log.d(TAG, "启动实际可运行的登录功能")
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                }
            }
        }
        builder.setOnCancelListener {
            Log.d(TAG, "用户取消了 MVP 示例选择")
            finish()
        }
        builder.show()
        
        Log.d(TAG, "=== MVPMain.onCreate completed ===")
    }
}

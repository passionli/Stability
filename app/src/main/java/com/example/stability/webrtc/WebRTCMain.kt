package com.example.stability.webrtc

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.stability.R
import com.example.stability.webrtc.basic.WebRTCBasicExample
import com.example.stability.webrtc.intermediate.WebRTCMediaStreamExample
import com.example.stability.webrtc.intermediate.WebRTCSignalingExample
import com.example.stability.webrtc.advanced.WebRTCAdvancedExample

/**
 * WebRTC 统一管理类
 * 用于管理和启动不同级别的 WebRTC 示例
 */
class WebRTCMain : AppCompatActivity() {
    
    companion object {
        private const val TAG = "WebRTC"
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        Log.d(TAG, "=== WebRTCMain.onCreate called ===")
        Log.d(TAG, "Thread ID: ${Thread.currentThread().id}")
        
        // 显示 WebRTC 示例选择对话框
        showWebRTCExamplesDialog()
    }
    
    /**
     * 显示 WebRTC 示例选择对话框
     * 设计原因：提供一个清晰的菜单让用户选择不同级别的 WebRTC 示例
     * 技术目的：通过 AlertDialog 显示选项列表
     */
    private fun showWebRTCExamplesDialog() {
        val examples = arrayOf(
            "初级 - WebRTC 基础概念",
            "中级 - 媒体流处理",
            "中级 - 信令服务",
            "高级 - P2P 连接示例"
        )
        
        val builder = android.app.AlertDialog.Builder(this)
        builder.setTitle("WebRTC 示例")
        builder.setItems(examples) { dialog, which ->
            Log.d(TAG, "用户选择了 WebRTC 示例: $which")
            when (which) {
                0 -> {
                    // 启动初级 WebRTC 示例
                    Log.d(TAG, "启动初级 WebRTC 基础概念示例")
                    WebRTCBasicExample().start()
                    finish()
                }
                1 -> {
                    // 启动中级媒体流示例
                    Log.d(TAG, "启动中级媒体流处理示例")
                    WebRTCMediaStreamExample().start()
                    finish()
                }
                2 -> {
                    // 启动中级信令示例
                    Log.d(TAG, "启动中级信令服务示例")
                    WebRTCSignalingExample().start()
                    finish()
                }
                3 -> {
                    // 启动高级 P2P 连接示例
                    Log.d(TAG, "启动高级 P2P 连接示例")
                    WebRTCAdvancedExample().start()
                    finish()
                }
            }
        }
        builder.setOnCancelListener {
            Log.d(TAG, "用户取消了 WebRTC 示例选择")
            finish()
        }
        builder.show()
        
        Log.d(TAG, "=== WebRTCMain.onCreate completed ===")
    }
}

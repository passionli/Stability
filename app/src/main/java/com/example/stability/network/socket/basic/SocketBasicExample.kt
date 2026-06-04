package com.example.stability.network.socket.basic

import android.util.Log
import java.io.IOException
import java.net.Socket

/**
 * Socket 通信初级示例
 * 展示 Socket 通信的基本功能，如创建 Socket、连接服务器等
 */
class SocketBasicExample {
    
    /**
     * 运行所有 Socket 初级示例
     */
    fun runAllExamples() {
        Log.d("Socket", "=== SocketBasicExample.runAllExamples called ===")
        Log.d("Socket", "Thread ID: ${Thread.currentThread().id}")
        
        // 创建 Socket 示例
        createSocket()
        
        Log.d("Socket", "=== SocketBasicExample.runAllExamples completed ===")
    }
    
    /**
     * 创建 Socket 示例（使用函数式风格）
     */
    private fun createSocket() {
        Log.d("Socket", "=== 运行创建 Socket 示例 ===")
        
        // 使用 runCatching 处理异常
        runCatching {
            // 使用 use 函数自动关闭资源
            Socket("8.8.8.8", 53).use { socket ->
                Log.d("Socket", "创建 Socket 成功")
                Log.d("Socket", "服务器地址: ${socket.inetAddress.hostAddress}")
                Log.d("Socket", "服务器端口: ${socket.port}")
                Log.d("Socket", "本地地址: ${socket.localAddress.hostAddress}")
                Log.d("Socket", "本地端口: ${socket.localPort}")
            }
        }.onFailure { e ->
            // 连接失败
            Log.d("Socket", "创建 Socket 失败: ${e.message}")
        }
        
        Log.d("Socket", "=== 创建 Socket 示例完成 ===")
    }
}
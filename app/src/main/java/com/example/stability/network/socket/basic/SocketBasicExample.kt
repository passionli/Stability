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
     * 创建 Socket 示例
     */
    private fun createSocket() {
        Log.d("Socket", "=== 运行创建 Socket 示例 ===")
        
        var socket: Socket? = null
        
        try {
            // 创建 Socket 并连接到服务器
            // 这里使用 Google 的公共 DNS 服务器作为示例
            socket = Socket("8.8.8.8", 53)
            Log.d("Socket", "创建 Socket 成功")
            Log.d("Socket", "服务器地址: ${socket.inetAddress.hostAddress}")
            Log.d("Socket", "服务器端口: ${socket.port}")
            Log.d("Socket", "本地地址: ${socket.localAddress.hostAddress}")
            Log.d("Socket", "本地端口: ${socket.localPort}")
        } catch (e: IOException) {
            // 连接失败
            Log.d("Socket", "创建 Socket 失败: ${e.message}")
        } finally {
            // 关闭 Socket
            if (socket != null && !socket.isClosed) {
                try {
                    socket.close()
                    Log.d("Socket", "关闭 Socket 成功")
                } catch (e: IOException) {
                    Log.d("Socket", "关闭 Socket 失败: ${e.message}")
                }
            }
        }
        
        Log.d("Socket", "=== 创建 Socket 示例完成 ===")
    }
}
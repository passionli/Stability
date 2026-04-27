package com.example.stability.network.tcp.basic

import android.util.Log
import java.io.IOException
import java.net.ServerSocket
import java.net.Socket

/**
 * TCP 通信初级示例
 * 展示 TCP 通信的基本功能，如创建 TCP 服务器和客户端等
 */
class TcpBasicExample {
    
    /**
     * 运行所有 TCP 初级示例
     */
    fun runAllExamples() {
        Log.d("TCP", "=== TcpBasicExample.runAllExamples called ===")
        Log.d("TCP", "Thread ID: ${Thread.currentThread().id}")
        
        // 创建 TCP 服务器示例
        createTcpServer()
        
        // 创建 TCP 客户端示例
        createTcpClient()
        
        Log.d("TCP", "=== TcpBasicExample.runAllExamples completed ===")
    }
    
    /**
     * 创建 TCP 服务器示例
     */
    private fun createTcpServer() {
        Log.d("TCP", "=== 运行创建 TCP 服务器示例 ===")
        
        var serverSocket: ServerSocket? = null
        
        try {
            // 创建 ServerSocket，监听端口 8080
            serverSocket = ServerSocket(8080)
            Log.d("TCP", "创建 TCP 服务器成功，监听端口: ${serverSocket.localPort}")
            Log.d("TCP", "服务器地址: ${serverSocket.inetAddress.hostAddress}")
            
            // 非阻塞模式，立即返回
            serverSocket.soTimeout = 1000 // 1秒超时
            
            // 尝试接受连接（非阻塞）
            val clientSocket = serverSocket.accept()
            if (clientSocket != null) {
                Log.d("TCP", "接受到客户端连接: ${clientSocket.inetAddress.hostAddress}:${clientSocket.port}")
                clientSocket.close()
            }
        } catch (e: IOException) {
            // 操作失败
            Log.d("TCP", "创建 TCP 服务器失败: ${e.message}")
        } finally {
            // 关闭 ServerSocket
            if (serverSocket != null && !serverSocket.isClosed) {
                try {
                    serverSocket.close()
                    Log.d("TCP", "关闭 TCP 服务器成功")
                } catch (e: IOException) {
                    Log.d("TCP", "关闭 TCP 服务器失败: ${e.message}")
                }
            }
        }
        
        Log.d("TCP", "=== 创建 TCP 服务器示例完成 ===")
    }
    
    /**
     * 创建 TCP 客户端示例
     */
    private fun createTcpClient() {
        Log.d("TCP", "=== 运行创建 TCP 客户端示例 ===")
        
        var socket: Socket? = null
        
        try {
            // 创建 Socket 并连接到服务器
            // 这里尝试连接到本地服务器
            socket = Socket("localhost", 8080)
            Log.d("TCP", "创建 TCP 客户端成功")
            Log.d("TCP", "服务器地址: ${socket.inetAddress.hostAddress}")
            Log.d("TCP", "服务器端口: ${socket.port}")
            Log.d("TCP", "本地地址: ${socket.localAddress.hostAddress}")
            Log.d("TCP", "本地端口: ${socket.localPort}")
        } catch (e: IOException) {
            // 连接失败
            Log.d("TCP", "创建 TCP 客户端失败: ${e.message}")
        } finally {
            // 关闭 Socket
            if (socket != null && !socket.isClosed) {
                try {
                    socket.close()
                    Log.d("TCP", "关闭 TCP 客户端成功")
                } catch (e: IOException) {
                    Log.d("TCP", "关闭 TCP 客户端失败: ${e.message}")
                }
            }
        }
        
        Log.d("TCP", "=== 创建 TCP 客户端示例完成 ===")
    }
}
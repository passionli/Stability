package com.example.stability.network.socket.advanced

import android.util.Log
import java.io.IOException
import java.net.Socket
import java.net.SocketOptions

/**
 * Socket 通信高级示例
 * 展示 Socket 通信的高级功能，如 Socket 选项设置、多线程处理等
 */
class SocketAdvancedExample {
    
    /**
     * 运行所有 Socket 高级示例
     */
    fun runAllExamples() {
        Log.d("Socket", "=== SocketAdvancedExample.runAllExamples called ===")
        Log.d("Socket", "Thread ID: ${Thread.currentThread().id}")
        
        // 设置 Socket 选项示例
        setSocketOptions()
        
        // 多线程 Socket 示例
        multiThreadedSocket()
        
        Log.d("Socket", "=== SocketAdvancedExample.runAllExamples completed ===")
    }
    
    /**
     * 设置 Socket 选项示例
     */
    private fun setSocketOptions() {
        Log.d("Socket", "=== 运行设置 Socket 选项示例 ===")
        
        var socket: Socket? = null
        
        try {
            // 创建 Socket
            socket = Socket("8.8.8.8", 53)
            Log.d("Socket", "连接成功")
            
            // 设置 TCP_NODELAY 选项，禁用 Nagle 算法
            socket.tcpNoDelay = true
            Log.d("Socket", "设置 TCP_NODELAY 成功: ${socket.tcpNoDelay}")
            
            // 设置 SO_KEEPALIVE 选项，启用 keep-alive
            socket.keepAlive = true
            Log.d("Socket", "设置 SO_KEEPALIVE 成功: ${socket.keepAlive}")
            
            // 设置 SO_REUSEADDR 选项，允许重用地址
            socket.reuseAddress = true
            Log.d("Socket", "设置 SO_REUSEADDR 成功: ${socket.reuseAddress}")
            
            // 设置接收缓冲区大小
            socket.receiveBufferSize = 8192
            Log.d("Socket", "设置接收缓冲区大小成功: ${socket.receiveBufferSize} bytes")
            
            // 设置发送缓冲区大小
            socket.sendBufferSize = 8192
            Log.d("Socket", "设置发送缓冲区大小成功: ${socket.sendBufferSize} bytes")
        } catch (e: IOException) {
            // 操作失败
            Log.d("Socket", "设置 Socket 选项失败: ${e.message}")
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
        
        Log.d("Socket", "=== 设置 Socket 选项示例完成 ===")
    }
    
    /**
     * 多线程 Socket 示例
     */
    private fun multiThreadedSocket() {
        Log.d("Socket", "=== 运行多线程 Socket 示例 ===")
        
        // 创建多个线程进行 Socket 连接
        for (i in 1..3) {
            val thread = Thread {
                Log.d("Socket", "线程 ${Thread.currentThread().id} 开始执行")
                
                var socket: Socket? = null
                
                try {
                    // 创建 Socket 并连接到服务器
                    socket = Socket("8.8.8.8", 53)
                    Log.d("Socket", "线程 ${Thread.currentThread().id} 连接成功")
                    
                    // 模拟一些操作
                    Thread.sleep(1000)
                    
                } catch (e: Exception) {
                    // 操作失败
                    Log.d("Socket", "线程 ${Thread.currentThread().id} 操作失败: ${e.message}")
                } finally {
                    // 关闭 Socket
                    if (socket != null && !socket.isClosed) {
                        try {
                            socket.close()
                            Log.d("Socket", "线程 ${Thread.currentThread().id} 关闭 Socket 成功")
                        } catch (e: IOException) {
                            Log.d("Socket", "线程 ${Thread.currentThread().id} 关闭 Socket 失败: ${e.message}")
                        }
                    }
                }
                
                Log.d("Socket", "线程 ${Thread.currentThread().id} 执行完成")
            }
            
            // 启动线程
            thread.start()
        }
        
        Log.d("Socket", "=== 多线程 Socket 示例完成 ===")
    }
}
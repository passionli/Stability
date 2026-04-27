package com.example.stability.network.socket.intermediate

import android.util.Log
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.Socket

/**
 * Socket 通信中级示例
 * 展示 Socket 通信的中级功能，如数据读写、超时设置等
 */
class SocketIntermediateExample {
    
    /**
     * 运行所有 Socket 中级示例
     */
    fun runAllExamples() {
        Log.d("Socket", "=== SocketIntermediateExample.runAllExamples called ===")
        Log.d("Socket", "Thread ID: ${Thread.currentThread().id}")
        
        // 设置 Socket 超时示例
        setSocketTimeout()
        
        // Socket 数据读写示例
        socketDataTransfer()
        
        Log.d("Socket", "=== SocketIntermediateExample.runAllExamples completed ===")
    }
    
    /**
     * 设置 Socket 超时示例
     */
    private fun setSocketTimeout() {
        Log.d("Socket", "=== 运行设置 Socket 超时示例 ===")
        
        var socket: Socket? = null
        
        try {
            // 创建 Socket
            socket = Socket()
            
            // 设置连接超时为 5 秒
            socket.connect(java.net.InetSocketAddress("8.8.8.8", 53), 5000)
            Log.d("Socket", "连接成功")
            
            // 设置读取超时为 3 秒
            socket.soTimeout = 3000
            Log.d("Socket", "设置读取超时成功: ${socket.soTimeout}ms")
        } catch (e: IOException) {
            // 连接失败
            Log.d("Socket", "设置 Socket 超时失败: ${e.message}")
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
        
        Log.d("Socket", "=== 设置 Socket 超时示例完成 ===")
    }
    
    /**
     * Socket 数据读写示例
     */
    private fun socketDataTransfer() {
        Log.d("Socket", "=== 运行 Socket 数据读写示例 ===")
        
        var socket: Socket? = null
        var outputStream: OutputStream? = null
        var inputStream: InputStream? = null
        
        try {
            // 创建 Socket 并连接到服务器
            // 这里使用 Google 的公共 DNS 服务器作为示例
            socket = Socket("8.8.8.8", 53)
            Log.d("Socket", "连接成功")
            
            // 获取输出流
            outputStream = socket.outputStream
            
            // 发送数据
            val data = "Hello, Socket!".toByteArray()
            outputStream.write(data)
            outputStream.flush()
            Log.d("Socket", "发送数据成功: ${data.size} bytes")
            
            // 获取输入流
            inputStream = socket.inputStream
            
            // 读取数据
            val buffer = ByteArray(1024)
            val bytesRead = inputStream.read(buffer)
            if (bytesRead > 0) {
                val response = String(buffer, 0, bytesRead)
                Log.d("Socket", "读取数据成功: ${bytesRead} bytes")
                Log.d("Socket", "响应数据: $response")
            }
        } catch (e: IOException) {
            // 操作失败
            Log.d("Socket", "Socket 数据读写失败: ${e.message}")
        } finally {
            // 关闭资源
            try {
                inputStream?.close()
                outputStream?.close()
                socket?.close()
                Log.d("Socket", "关闭资源成功")
            } catch (e: IOException) {
                Log.d("Socket", "关闭资源失败: ${e.message}")
            }
        }
        
        Log.d("Socket", "=== Socket 数据读写示例完成 ===")
    }
}
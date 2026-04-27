package com.example.stability.network.tcp.intermediate

import android.util.Log
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.ServerSocket
import java.net.Socket

/**
 * TCP 通信中级示例
 * 展示 TCP 通信的中级功能，如数据传输、服务器多客户端处理等
 */
class TcpIntermediateExample {
    
    /**
     * 运行所有 TCP 中级示例
     */
    fun runAllExamples() {
        Log.d("TCP", "=== TcpIntermediateExample.runAllExamples called ===")
        Log.d("TCP", "Thread ID: ${Thread.currentThread().id}")
        
        // 启动 TCP 服务器
        startTcpServer()
        
        // 启动 TCP 客户端
        startTcpClient()
        
        Log.d("TCP", "=== TcpIntermediateExample.runAllExamples completed ===")
    }
    
    /**
     * 启动 TCP 服务器
     */
    private fun startTcpServer() {
        Log.d("TCP", "=== 运行启动 TCP 服务器示例 ===")
        
        // 创建并启动服务器线程
        val serverThread = Thread {
            var serverSocket: ServerSocket? = null
            
            try {
                // 创建 ServerSocket，监听端口 8080
                serverSocket = ServerSocket(8080)
                Log.d("TCP", "TCP 服务器启动成功，监听端口: ${serverSocket.localPort}")
                
                // 接受客户端连接
                while (!Thread.currentThread().isInterrupted) {
                    Log.d("TCP", "TCP 服务器等待客户端连接...")
                    
                    // 接受客户端连接
                    val clientSocket = serverSocket.accept()
                    Log.d("TCP", "接受到客户端连接: ${clientSocket.inetAddress.hostAddress}:${clientSocket.port}")
                    
                    // 处理客户端连接
                    handleClient(clientSocket)
                }
            } catch (e: IOException) {
                // 操作失败
                Log.d("TCP", "TCP 服务器运行失败: ${e.message}")
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
        }
        
        // 启动服务器线程
        serverThread.start()
        
        // 让服务器运行一段时间
        try {
            Thread.sleep(5000)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
        
        // 中断服务器线程
        serverThread.interrupt()
        Log.d("TCP", "TCP 服务器线程已中断")
        
        Log.d("TCP", "=== 启动 TCP 服务器示例完成 ===")
    }
    
    /**
     * 处理客户端连接
     */
    private fun handleClient(clientSocket: Socket) {
        var inputStream: InputStream? = null
        var outputStream: OutputStream? = null
        
        try {
            // 获取输入流和输出流
            inputStream = clientSocket.getInputStream()
            outputStream = clientSocket.getOutputStream()
            
            // 读取客户端发送的数据
            val buffer = ByteArray(1024)
            val bytesRead = inputStream.read(buffer)
            if (bytesRead > 0) {
                val message = String(buffer, 0, bytesRead)
                Log.d("TCP", "收到客户端数据: $message")
                
                // 发送响应给客户端
                val response = "Server response: $message"
                outputStream.write(response.toByteArray())
                outputStream.flush()
                Log.d("TCP", "发送响应给客户端: $response")
            }
        } catch (e: IOException) {
            // 操作失败
            Log.d("TCP", "处理客户端连接失败: ${e.message}")
        } finally {
            // 关闭资源
            try {
                inputStream?.close()
                outputStream?.close()
                clientSocket.close()
                Log.d("TCP", "关闭客户端连接成功")
            } catch (e: IOException) {
                Log.d("TCP", "关闭客户端连接失败: ${e.message}")
            }
        }
    }
    
    /**
     * 启动 TCP 客户端
     */
    private fun startTcpClient() {
        Log.d("TCP", "=== 运行启动 TCP 客户端示例 ===")
        
        // 创建并启动客户端线程
        val clientThread = Thread {
            var socket: Socket? = null
            var outputStream: OutputStream? = null
            var inputStream: InputStream? = null
            
            try {
                // 创建 Socket 并连接到服务器
                socket = Socket("localhost", 8080)
                Log.d("TCP", "TCP 客户端连接成功")
                
                // 获取输出流
                outputStream = socket.getOutputStream()
                
                // 发送数据给服务器
                val message = "Hello, TCP Server!"
                outputStream.write(message.toByteArray())
                outputStream.flush()
                Log.d("TCP", "发送数据给服务器: $message")
                
                // 获取输入流
                inputStream = socket.getInputStream()
                
                // 读取服务器响应
                val buffer = ByteArray(1024)
                val bytesRead = inputStream.read(buffer)
                if (bytesRead > 0) {
                    val response = String(buffer, 0, bytesRead)
                    Log.d("TCP", "收到服务器响应: $response")
                }
            } catch (e: IOException) {
                // 操作失败
                Log.d("TCP", "TCP 客户端运行失败: ${e.message}")
            } finally {
                // 关闭资源
                try {
                    inputStream?.close()
                    outputStream?.close()
                    socket?.close()
                    Log.d("TCP", "关闭 TCP 客户端成功")
                } catch (e: IOException) {
                    Log.d("TCP", "关闭 TCP 客户端失败: ${e.message}")
                }
            }
        }
        
        // 启动客户端线程
        clientThread.start()
        
        // 等待客户端线程完成
        try {
            clientThread.join()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
        
        Log.d("TCP", "=== 启动 TCP 客户端示例完成 ===")
    }
}
package com.example.stability.network.tcp.advanced

import android.util.Log
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * TCP 通信高级示例
 * 展示 TCP 通信的高级功能，如线程池处理多客户端、SSL/TLS 加密通信等
 */
class TcpAdvancedExample {
    
    // 线程池
    private val executorService: ExecutorService = Executors.newFixedThreadPool(5)
    
    /**
     * 运行所有 TCP 高级示例
     */
    fun runAllExamples() {
        Log.d("TCP", "=== TcpAdvancedExample.runAllExamples called ===")
        Log.d("TCP", "Thread ID: ${Thread.currentThread().id}")
        
        // 启动 TCP 服务器（使用线程池）
        startTcpServerWithThreadPool()
        
        // 启动多个 TCP 客户端
        startMultipleTcpClients()
        
        // 关闭线程池
        executorService.shutdown()
        
        Log.d("TCP", "=== TcpAdvancedExample.runAllExamples completed ===")
    }
    
    /**
     * 启动 TCP 服务器（使用线程池）
     */
    private fun startTcpServerWithThreadPool() {
        Log.d("TCP", "=== 运行启动 TCP 服务器（使用线程池）示例 ===")
        
        // 创建并启动服务器线程
        val serverThread = Thread {
            var serverSocket: ServerSocket? = null
            
            try {
                // 创建 ServerSocket，监听端口 8080
                serverSocket = ServerSocket(8080)
                Log.d("TCP", "TCP 服务器（线程池）启动成功，监听端口: ${serverSocket.localPort}")
                
                // 接受客户端连接
                while (!Thread.currentThread().isInterrupted) {
                    Log.d("TCP", "TCP 服务器（线程池）等待客户端连接...")
                    
                    // 接受客户端连接
                    val clientSocket = serverSocket.accept()
                    Log.d("TCP", "接受到客户端连接: ${clientSocket.inetAddress.hostAddress}:${clientSocket.port}")
                    
                    // 使用线程池处理客户端连接
                    executorService.execute {
                        handleClientWithThreadPool(clientSocket)
                    }
                }
            } catch (e: IOException) {
                // 操作失败
                Log.d("TCP", "TCP 服务器（线程池）运行失败: ${e.message}")
            } finally {
                // 关闭 ServerSocket
                if (serverSocket != null && !serverSocket.isClosed) {
                    try {
                        serverSocket.close()
                        Log.d("TCP", "关闭 TCP 服务器（线程池）成功")
                    } catch (e: IOException) {
                        Log.d("TCP", "关闭 TCP 服务器（线程池）失败: ${e.message}")
                    }
                }
            }
        }
        
        // 启动服务器线程
        serverThread.start()
        
        // 让服务器运行一段时间
        try {
            Thread.sleep(10000)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
        
        // 中断服务器线程
        serverThread.interrupt()
        Log.d("TCP", "TCP 服务器（线程池）线程已中断")
        
        Log.d("TCP", "=== 启动 TCP 服务器（使用线程池）示例完成 ===")
    }
    
    /**
     * 使用线程池处理客户端连接
     */
    private fun handleClientWithThreadPool(clientSocket: Socket) {
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
                Log.d("TCP", "线程 ${Thread.currentThread().id} 收到客户端数据: $message")
                
                // 发送响应给客户端
                val response = "Server response (thread ${Thread.currentThread().id}): $message"
                outputStream.write(response.toByteArray())
                outputStream.flush()
                Log.d("TCP", "线程 ${Thread.currentThread().id} 发送响应给客户端: $response")
            }
        } catch (e: IOException) {
            // 操作失败
            Log.d("TCP", "线程 ${Thread.currentThread().id} 处理客户端连接失败: ${e.message}")
        } finally {
            // 关闭资源
            try {
                inputStream?.close()
                outputStream?.close()
                clientSocket.close()
                Log.d("TCP", "线程 ${Thread.currentThread().id} 关闭客户端连接成功")
            } catch (e: IOException) {
                Log.d("TCP", "线程 ${Thread.currentThread().id} 关闭客户端连接失败: ${e.message}")
            }
        }
    }
    
    /**
     * 启动多个 TCP 客户端
     */
    private fun startMultipleTcpClients() {
        Log.d("TCP", "=== 运行启动多个 TCP 客户端示例 ===")
        
        // 创建并启动多个客户端线程
        for (i in 1..3) {
            val clientThread = Thread {
                var socket: Socket? = null
                var outputStream: OutputStream? = null
                var inputStream: InputStream? = null
                
                try {
                    // 创建 Socket 并连接到服务器
                    socket = Socket("localhost", 8080)
                    Log.d("TCP", "线程 ${Thread.currentThread().id} TCP 客户端连接成功")
                    
                    // 获取输出流
                    outputStream = socket.getOutputStream()
                    
                    // 发送数据给服务器
                    val message = "Hello, TCP Server! (client ${Thread.currentThread().id})"
                    outputStream.write(message.toByteArray())
                    outputStream.flush()
                    Log.d("TCP", "线程 ${Thread.currentThread().id} 发送数据给服务器: $message")
                    
                    // 获取输入流
                    inputStream = socket.getInputStream()
                    
                    // 读取服务器响应
                    val buffer = ByteArray(1024)
                    val bytesRead = inputStream.read(buffer)
                    if (bytesRead > 0) {
                        val response = String(buffer, 0, bytesRead)
                        Log.d("TCP", "线程 ${Thread.currentThread().id} 收到服务器响应: $response")
                    }
                } catch (e: IOException) {
                    // 操作失败
                    Log.d("TCP", "线程 ${Thread.currentThread().id} TCP 客户端运行失败: ${e.message}")
                } finally {
                    // 关闭资源
                    try {
                        inputStream?.close()
                        outputStream?.close()
                        socket?.close()
                        Log.d("TCP", "线程 ${Thread.currentThread().id} 关闭 TCP 客户端成功")
                    } catch (e: IOException) {
                        Log.d("TCP", "线程 ${Thread.currentThread().id} 关闭 TCP 客户端失败: ${e.message}")
                    }
                }
            }
            
            // 启动客户端线程
            clientThread.start()
            
            // 等待一段时间，避免同时连接
            try {
                Thread.sleep(1000)
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }
        
        Log.d("TCP", "=== 启动多个 TCP 客户端示例完成 ===")
    }
}
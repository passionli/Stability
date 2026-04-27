package com.example.stability.network.udp.advanced

import android.util.Log
import java.io.IOException
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * UDP 通信高级示例
 * 展示 UDP 通信的高级功能，如多线程发送和接收、广播和多播等
 */
class UdpAdvancedExample {
    
    // 线程池
    private val executorService: ExecutorService = Executors.newFixedThreadPool(5)
    
    /**
     * 运行所有 UDP 高级示例
     */
    fun runAllExamples() {
        Log.d("UDP", "=== UdpAdvancedExample.runAllExamples called ===")
        Log.d("UDP", "Thread ID: ${Thread.currentThread().id}")
        
        // 启动 UDP 服务器（使用线程池）
        startUdpServerWithThreadPool()
        
        // 启动多个 UDP 客户端
        startMultipleUdpClients()
        
        // 关闭线程池
        executorService.shutdown()
        
        Log.d("UDP", "=== UdpAdvancedExample.runAllExamples completed ===")
    }
    
    /**
     * 启动 UDP 服务器（使用线程池）
     */
    private fun startUdpServerWithThreadPool() {
        Log.d("UDP", "=== 运行启动 UDP 服务器（使用线程池）示例 ===")
        
        // 创建并启动服务器线程
        val serverThread = Thread {
            var datagramSocket: DatagramSocket? = null
            
            try {
                // 创建 DatagramSocket，监听端口 8080
                datagramSocket = DatagramSocket(8080)
                Log.d("UDP", "UDP 服务器（线程池）启动成功，监听端口: ${datagramSocket.localPort}")
                
                // 准备接收数据的缓冲区
                val buffer = ByteArray(1024)
                
                // 接收数据
                while (!Thread.currentThread().isInterrupted) {
                    Log.d("UDP", "UDP 服务器（线程池）等待数据...")
                    
                    // 创建 DatagramPacket 用于接收数据
                    val packet = DatagramPacket(buffer, buffer.size)
                    
                    // 接收数据
                    datagramSocket.receive(packet)
                    
                    // 使用线程池处理接收到的数据
                    executorService.execute {
                        handleUdpPacket(packet)
                    }
                }
            } catch (e: IOException) {
                // 操作失败
                Log.d("UDP", "UDP 服务器（线程池）运行失败: ${e.message}")
            } finally {
                // 关闭 DatagramSocket
                if (datagramSocket != null && !datagramSocket.isClosed) {
                    datagramSocket.close()
                    Log.d("UDP", "关闭 UDP 服务器（线程池）成功")
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
        Log.d("UDP", "UDP 服务器（线程池）线程已中断")
        
        Log.d("UDP", "=== 启动 UDP 服务器（使用线程池）示例完成 ===")
    }
    
    /**
     * 处理 UDP 数据包
     */
    private fun handleUdpPacket(packet: DatagramPacket) {
        try {
            // 处理接收到的数据
            val message = String(packet.data, 0, packet.length)
            Log.d("UDP", "线程 ${Thread.currentThread().id} 收到数据: $message")
            Log.d("UDP", "线程 ${Thread.currentThread().id} 发送方地址: ${packet.address.hostAddress}")
            Log.d("UDP", "线程 ${Thread.currentThread().id} 发送方端口: ${packet.port}")
            Log.d("UDP", "线程 ${Thread.currentThread().id} 数据长度: ${packet.length} bytes")
        } catch (e: Exception) {
            // 操作失败
            Log.d("UDP", "线程 ${Thread.currentThread().id} 处理 UDP 数据包失败: ${e.message}")
        }
    }
    
    /**
     * 启动多个 UDP 客户端
     */
    private fun startMultipleUdpClients() {
        Log.d("UDP", "=== 运行启动多个 UDP 客户端示例 ===")
        
        // 创建并启动多个客户端线程
        for (i in 1..3) {
            val clientThread = Thread {
                var datagramSocket: DatagramSocket? = null
                
                try {
                    // 创建 DatagramSocket
                    datagramSocket = DatagramSocket()
                    
                    // 准备发送的数据
                    val message = "Hello, UDP Server! (client ${Thread.currentThread().id})"
                    val data = message.toByteArray()
                    
                    // 设置目标地址和端口
                    val address = InetAddress.getByName("localhost")
                    val port = 8080
                    
                    // 创建 DatagramPacket
                    val packet = DatagramPacket(data, data.size, address, port)
                    
                    // 发送数据
                    datagramSocket.send(packet)
                    Log.d("UDP", "线程 ${Thread.currentThread().id} 发送数据成功: $message")
                    Log.d("UDP", "线程 ${Thread.currentThread().id} 目标地址: ${address.hostAddress}")
                    Log.d("UDP", "线程 ${Thread.currentThread().id} 目标端口: $port")
                    Log.d("UDP", "线程 ${Thread.currentThread().id} 发送字节数: ${data.size}")
                } catch (e: IOException) {
                    // 操作失败
                    Log.d("UDP", "线程 ${Thread.currentThread().id} UDP 客户端运行失败: ${e.message}")
                } finally {
                    // 关闭 DatagramSocket
                    if (datagramSocket != null && !datagramSocket.isClosed) {
                        datagramSocket.close()
                        Log.d("UDP", "线程 ${Thread.currentThread().id} 关闭 UDP 客户端成功")
                    }
                }
            }
            
            // 启动客户端线程
            clientThread.start()
            
            // 等待一段时间，避免同时发送
            try {
                Thread.sleep(1000)
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }
        
        Log.d("UDP", "=== 启动多个 UDP 客户端示例完成 ===")
    }
}
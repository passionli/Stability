package com.example.stability.network.udp.intermediate

import android.util.Log
import java.io.IOException
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress

/**
 * UDP 通信中级示例
 * 展示 UDP 通信的中级功能，如接收数据、多线程处理等
 */
class UdpIntermediateExample {
    
    /**
     * 运行所有 UDP 中级示例
     */
    fun runAllExamples() {
        Log.d("UDP", "=== UdpIntermediateExample.runAllExamples called ===")
        Log.d("UDP", "Thread ID: ${Thread.currentThread().id}")
        
        // 启动 UDP 服务器（接收数据）
        startUdpServer()
        
        // 启动 UDP 客户端（发送数据）
        startUdpClient()
        
        Log.d("UDP", "=== UdpIntermediateExample.runAllExamples completed ===")
    }
    
    /**
     * 启动 UDP 服务器（接收数据）
     */
    private fun startUdpServer() {
        Log.d("UDP", "=== 运行启动 UDP 服务器示例 ===")
        
        // 创建并启动服务器线程
        val serverThread = Thread {
            var datagramSocket: DatagramSocket? = null
            
            try {
                // 创建 DatagramSocket，监听端口 8080
                datagramSocket = DatagramSocket(8080)
                Log.d("UDP", "UDP 服务器启动成功，监听端口: ${datagramSocket.localPort}")
                
                // 准备接收数据的缓冲区
                val buffer = ByteArray(1024)
                
                // 接收数据
                while (!Thread.currentThread().isInterrupted) {
                    Log.d("UDP", "UDP 服务器等待数据...")
                    
                    // 创建 DatagramPacket 用于接收数据
                    val packet = DatagramPacket(buffer, buffer.size)
                    
                    // 接收数据
                    datagramSocket.receive(packet)
                    
                    // 处理接收到的数据
                    val message = String(packet.data, 0, packet.length)
                    Log.d("UDP", "收到数据: $message")
                    Log.d("UDP", "发送方地址: ${packet.address.hostAddress}")
                    Log.d("UDP", "发送方端口: ${packet.port}")
                    Log.d("UDP", "数据长度: ${packet.length} bytes")
                }
            } catch (e: IOException) {
                // 操作失败
                Log.d("UDP", "UDP 服务器运行失败: ${e.message}")
            } finally {
                // 关闭 DatagramSocket
                if (datagramSocket != null && !datagramSocket.isClosed) {
                    datagramSocket.close()
                    Log.d("UDP", "关闭 UDP 服务器成功")
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
        Log.d("UDP", "UDP 服务器线程已中断")
        
        Log.d("UDP", "=== 启动 UDP 服务器示例完成 ===")
    }
    
    /**
     * 启动 UDP 客户端（发送数据）
     */
    private fun startUdpClient() {
        Log.d("UDP", "=== 运行启动 UDP 客户端示例 ===")
        
        // 创建并启动客户端线程
        val clientThread = Thread {
            var datagramSocket: DatagramSocket? = null
            
            try {
                // 创建 DatagramSocket
                datagramSocket = DatagramSocket()
                
                // 准备发送的数据
                val message = "Hello, UDP Server!"
                val data = message.toByteArray()
                
                // 设置目标地址和端口
                val address = InetAddress.getByName("localhost")
                val port = 8080
                
                // 创建 DatagramPacket
                val packet = DatagramPacket(data, data.size, address, port)
                
                // 发送数据
                datagramSocket.send(packet)
                Log.d("UDP", "发送数据成功: $message")
                Log.d("UDP", "目标地址: ${address.hostAddress}")
                Log.d("UDP", "目标端口: $port")
                Log.d("UDP", "发送字节数: ${data.size}")
            } catch (e: IOException) {
                // 操作失败
                Log.d("UDP", "UDP 客户端运行失败: ${e.message}")
            } finally {
                // 关闭 DatagramSocket
                if (datagramSocket != null && !datagramSocket.isClosed) {
                    datagramSocket.close()
                    Log.d("UDP", "关闭 UDP 客户端成功")
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
        
        Log.d("UDP", "=== 启动 UDP 客户端示例完成 ===")
    }
}
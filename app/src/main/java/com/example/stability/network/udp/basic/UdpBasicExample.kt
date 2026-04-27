package com.example.stability.network.udp.basic

import android.util.Log
import java.io.IOException
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress

/**
 * UDP 通信初级示例
 * 展示 UDP 通信的基本功能，如创建 UDP 套接字、发送和接收数据等
 */
class UdpBasicExample {
    
    /**
     * 运行所有 UDP 初级示例
     */
    fun runAllExamples() {
        Log.d("UDP", "=== UdpBasicExample.runAllExamples called ===")
        Log.d("UDP", "Thread ID: ${Thread.currentThread().id}")
        
        // 创建 UDP 套接字示例
        createUdpSocket()
        
        // UDP 发送数据示例
        sendUdpData()
        
        Log.d("UDP", "=== UdpBasicExample.runAllExamples completed ===")
    }
    
    /**
     * 创建 UDP 套接字示例
     */
    private fun createUdpSocket() {
        Log.d("UDP", "=== 运行创建 UDP 套接字示例 ===")
        
        var datagramSocket: DatagramSocket? = null
        
        try {
            // 创建 DatagramSocket
            datagramSocket = DatagramSocket()
            Log.d("UDP", "创建 UDP 套接字成功")
            Log.d("UDP", "本地端口: ${datagramSocket.localPort}")
            Log.d("UDP", "本地地址: ${datagramSocket.localAddress.hostAddress}")
        } catch (e: IOException) {
            // 操作失败
            Log.d("UDP", "创建 UDP 套接字失败: ${e.message}")
        } finally {
            // 关闭 DatagramSocket
            if (datagramSocket != null && !datagramSocket.isClosed) {
                datagramSocket.close()
                Log.d("UDP", "关闭 UDP 套接字成功")
            }
        }
        
        Log.d("UDP", "=== 创建 UDP 套接字示例完成 ===")
    }
    
    /**
     * UDP 发送数据示例
     */
    private fun sendUdpData() {
        Log.d("UDP", "=== 运行 UDP 发送数据示例 ===")
        
        var datagramSocket: DatagramSocket? = null
        
        try {
            // 创建 DatagramSocket
            datagramSocket = DatagramSocket()
            
            // 准备发送的数据
            val message = "Hello, UDP!"
            val data = message.toByteArray()
            
            // 设置目标地址和端口
            val address = InetAddress.getByName("8.8.8.8")
            val port = 53
            
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
            Log.d("UDP", "UDP 发送数据失败: ${e.message}")
        } finally {
            // 关闭 DatagramSocket
            if (datagramSocket != null && !datagramSocket.isClosed) {
                datagramSocket.close()
                Log.d("UDP", "关闭 UDP 套接字成功")
            }
        }
        
        Log.d("UDP", "=== UDP 发送数据示例完成 ===")
    }
}
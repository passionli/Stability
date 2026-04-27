package com.example.stability.network

import android.content.Context
import android.util.Log
import com.example.stability.network.socket.basic.SocketBasicExample
import com.example.stability.network.socket.intermediate.SocketIntermediateExample
import com.example.stability.network.socket.advanced.SocketAdvancedExample
import com.example.stability.network.tcp.basic.TcpBasicExample
import com.example.stability.network.tcp.intermediate.TcpIntermediateExample
import com.example.stability.network.tcp.advanced.TcpAdvancedExample
import com.example.stability.network.udp.basic.UdpBasicExample
import com.example.stability.network.udp.intermediate.UdpIntermediateExample
import com.example.stability.network.udp.advanced.UdpAdvancedExample

/**
 * 网络协议学习主类，用于管理和启动不同网络协议的示例
 */
class NetworkMain(private val context: Context) {
    
    /**
     * 运行所有网络协议示例
     */
    fun runAllExamples() {
        Log.d("Network", "=== NetworkMain.runAllExamples called ===")
        Log.d("Network", "Thread ID: ${Thread.currentThread().id}")
        
        // 运行 Socket 示例
        runSocketExamples()
        
        // 运行 TCP 示例
        runTcpExamples()
        
        // 运行 UDP 示例
        runUdpExamples()
        
        Log.d("Network", "=== NetworkMain.runAllExamples completed ===")
    }
    
    /**
     * 运行 Socket 示例
     */
    private fun runSocketExamples() {
        Log.d("Network", "=== 运行 Socket 示例 ===")
        
        // 运行 Socket 初级示例
        val socketBasicExample = SocketBasicExample()
        socketBasicExample.runAllExamples()
        
        // 运行 Socket 中级示例
        val socketIntermediateExample = SocketIntermediateExample()
        socketIntermediateExample.runAllExamples()
        
        // 运行 Socket 高级示例
        val socketAdvancedExample = SocketAdvancedExample()
        socketAdvancedExample.runAllExamples()
        
        Log.d("Network", "=== Socket 示例运行完成 ===")
    }
    
    /**
     * 运行 TCP 示例
     */
    private fun runTcpExamples() {
        Log.d("Network", "=== 运行 TCP 示例 ===")
        
        // 运行 TCP 初级示例
        val tcpBasicExample = TcpBasicExample()
        tcpBasicExample.runAllExamples()
        
        // 运行 TCP 中级示例
        val tcpIntermediateExample = TcpIntermediateExample()
        tcpIntermediateExample.runAllExamples()
        
        // 运行 TCP 高级示例
        val tcpAdvancedExample = TcpAdvancedExample()
        tcpAdvancedExample.runAllExamples()
        
        Log.d("Network", "=== TCP 示例运行完成 ===")
    }
    
    /**
     * 运行 UDP 示例
     */
    private fun runUdpExamples() {
        Log.d("Network", "=== 运行 UDP 示例 ===")
        
        // 运行 UDP 初级示例
        val udpBasicExample = UdpBasicExample()
        udpBasicExample.runAllExamples()
        
        // 运行 UDP 中级示例
        val udpIntermediateExample = UdpIntermediateExample()
        udpIntermediateExample.runAllExamples()
        
        // 运行 UDP 高级示例
        val udpAdvancedExample = UdpAdvancedExample()
        udpAdvancedExample.runAllExamples()
        
        Log.d("Network", "=== UDP 示例运行完成 ===")
    }
}
package com.example.stability.communication.wifi_direct.advanced

import android.content.Context
import android.net.wifi.p2p.WifiP2pConfig
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pManager
import android.util.Log

/**
 * Wi-Fi 直连高级示例
 * 展示 Wi-Fi 直连的高级功能，如设备连接、数据传输等
 */
class WifiDirectAdvancedExample(private val context: Context) {
    
    // Wi-Fi P2P 管理器
    private val wifiP2pManager: WifiP2pManager?
    // 通道
    private val channel: WifiP2pManager.Channel?
    
    init {
        // 获取 Wi-Fi P2P 管理器
        wifiP2pManager = context.getSystemService(Context.WIFI_P2P_SERVICE) as WifiP2pManager?
        // 初始化通道
        channel = wifiP2pManager?.initialize(context, context.mainLooper, null)
    }
    
    /**
     * 运行所有 Wi-Fi 直连高级示例
     */
    fun runAllExamples() {
        Log.d("WiFiDirect", "=== WifiDirectAdvancedExample.runAllExamples called ===")
        Log.d("WiFiDirect", "Thread ID: ${Thread.currentThread().id}")
        
        // 连接到对等设备
        connectToPeer()
        
        Log.d("WiFiDirect", "=== WifiDirectAdvancedExample.runAllExamples completed ===")
    }
    
    /**
     * 连接到对等设备
     */
    private fun connectToPeer() {
        Log.d("WiFiDirect", "=== 运行连接到对等设备示例 ===")
        
        if (wifiP2pManager == null || channel == null) {
            Log.d("WiFiDirect", "设备不支持 Wi-Fi 直连，无法连接设备")
            return
        }
        
        // 模拟一个对等设备
        val device = WifiP2pDevice()
        device.deviceAddress = "00:11:22:33:44:55"
        device.deviceName = "Test Device"
        
        // 创建连接配置
        val config = WifiP2pConfig()
        config.deviceAddress = device.deviceAddress
        
        // 连接到设备
        wifiP2pManager.connect(channel, config, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                // 连接成功
                Log.d("WiFiDirect", "连接到设备成功")
            }
            
            override fun onFailure(reasonCode: Int) {
                // 连接失败
                Log.d("WiFiDirect", "连接到设备失败，原因码: $reasonCode")
            }
        })
        
        Log.d("WiFiDirect", "=== 连接到对等设备示例完成 ===")
    }
}
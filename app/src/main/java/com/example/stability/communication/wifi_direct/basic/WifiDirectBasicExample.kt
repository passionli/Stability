package com.example.stability.communication.wifi_direct.basic

import android.content.Context
import android.net.wifi.p2p.WifiP2pManager
import android.util.Log

/**
 * Wi-Fi 直连初级示例
 * 展示 Wi-Fi 直连的基本功能，如 Wi-Fi 直连初始化、设备发现等
 */
class WifiDirectBasicExample(private val context: Context) {
    
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
     * 运行所有 Wi-Fi 直连初级示例
     */
    fun runAllExamples() {
        Log.d("WiFiDirect", "=== WifiDirectBasicExample.runAllExamples called ===")
        Log.d("WiFiDirect", "Thread ID: ${Thread.currentThread().id}")
        
        // 检查 Wi-Fi 直连是否可用
        checkWifiDirectAvailability()
        
        Log.d("WiFiDirect", "=== WifiDirectBasicExample.runAllExamples completed ===")
    }
    
    /**
     * 检查 Wi-Fi 直连是否可用
     */
    private fun checkWifiDirectAvailability() {
        Log.d("WiFiDirect", "=== 运行检查 Wi-Fi 直连是否可用示例 ===")
        
        if (wifiP2pManager == null || channel == null) {
            // 设备不支持 Wi-Fi 直连
            Log.d("WiFiDirect", "设备不支持 Wi-Fi 直连")
        } else {
            // 设备支持 Wi-Fi 直连
            Log.d("WiFiDirect", "设备支持 Wi-Fi 直连")
        }
        
        Log.d("WiFiDirect", "=== 检查 Wi-Fi 直连是否可用示例完成 ===")
    }
}
package com.example.stability.communication.wifi_direct.intermediate

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.p2p.WifiP2pManager
import android.net.wifi.p2p.WifiP2pManager.PeerListListener
import android.util.Log

/**
 * Wi-Fi 直连中级示例
 * 展示 Wi-Fi 直连的中级功能，如设备发现、连接等
 */
class WifiDirectIntermediateExample(private val context: Context) {
    
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
     * 运行所有 Wi-Fi 直连中级示例
     */
    fun runAllExamples() {
        Log.d("WiFiDirect", "=== WifiDirectIntermediateExample.runAllExamples called ===")
        Log.d("WiFiDirect", "Thread ID: ${Thread.currentThread().id}")
        
        // 注册广播接收器
        registerBroadcastReceiver()
        
        // 开始发现设备
        startDeviceDiscovery()
        
        Log.d("WiFiDirect", "=== WifiDirectIntermediateExample.runAllExamples completed ===")
    }
    
    /**
     * 注册广播接收器
     */
    private fun registerBroadcastReceiver() {
        Log.d("WiFiDirect", "=== 运行注册广播接收器示例 ===")
        
        // 创建广播接收器
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val action = intent?.action
                if (action == null) return
                
                when (action) {
                    WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION -> {
                        // Wi-Fi P2P 状态变化
                        val state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1)
                        Log.d("WiFiDirect", "Wi-Fi P2P 状态: $state")
                    }
                    WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION -> {
                        // 对等设备列表变化
                        Log.d("WiFiDirect", "对等设备列表变化")
                    }
                    WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION -> {
                        // 连接状态变化
                        Log.d("WiFiDirect", "连接状态变化")
                    }
                    WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION -> {
                        // 本设备信息变化
                        Log.d("WiFiDirect", "本设备信息变化")
                    }
                }
            }
        }
        
        // 注册广播接收器
        val intentFilter = IntentFilter()
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION)
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION)
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION)
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION)
        context.registerReceiver(receiver, intentFilter)
        
        Log.d("WiFiDirect", "=== 注册广播接收器示例完成 ===")
    }
    
    /**
     * 开始发现设备
     */
    private fun startDeviceDiscovery() {
        Log.d("WiFiDirect", "=== 运行开始发现设备示例 ===")
        
        if (wifiP2pManager == null || channel == null) {
            Log.d("WiFiDirect", "设备不支持 Wi-Fi 直连，无法发现设备")
            return
        }
        
        // 开始发现设备
        wifiP2pManager.discoverPeers(channel, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                // 发现设备成功
                Log.d("WiFiDirect", "开始发现设备成功")
            }
            
            override fun onFailure(reasonCode: Int) {
                // 发现设备失败
                Log.d("WiFiDirect", "开始发现设备失败，原因码: $reasonCode")
            }
        })
        
        Log.d("WiFiDirect", "=== 开始发现设备示例完成 ===")
    }
}
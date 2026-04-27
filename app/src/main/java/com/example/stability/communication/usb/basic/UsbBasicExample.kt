package com.example.stability.communication.usb.basic

import android.content.Context
import android.hardware.usb.UsbManager
import android.util.Log

/**
 * USB 通信初级示例
 * 展示 USB 通信的基本功能，如 USB 设备检测、权限请求等
 */
class UsbBasicExample(private val context: Context) {
    
    // USB 管理器
    private val usbManager: UsbManager
    
    init {
        // 获取 USB 管理器
        usbManager = context.getSystemService(Context.USB_SERVICE) as UsbManager
    }
    
    /**
     * 运行所有 USB 初级示例
     */
    fun runAllExamples() {
        Log.d("USB", "=== UsbBasicExample.runAllExamples called ===")
        Log.d("USB", "Thread ID: ${Thread.currentThread().id}")
        
        // 列出所有已连接的 USB 设备
        listConnectedUsbDevices()
        
        Log.d("USB", "=== UsbBasicExample.runAllExamples completed ===")
    }
    
    /**
     * 列出所有已连接的 USB 设备
     */
    private fun listConnectedUsbDevices() {
        Log.d("USB", "=== 运行列出所有已连接的 USB 设备示例 ===")
        
        // 获取所有已连接的 USB 设备
        val deviceList = usbManager.deviceList
        Log.d("USB", "已连接的 USB 设备数量: ${deviceList.size}")
        
        for ((deviceName, device) in deviceList) {
            Log.d("USB", "设备名称: $deviceName")
            Log.d("USB", "  设备厂商 ID: ${device.vendorId}")
            Log.d("USB", "  设备产品 ID: ${device.productId}")
            Log.d("USB", "  设备类: ${device.deviceClass}")
            Log.d("USB", "  设备子类: ${device.deviceSubclass}")
            Log.d("USB", "  设备协议: ${device.deviceProtocol}")
        }
        
        Log.d("USB", "=== 列出所有已连接的 USB 设备示例完成 ===")
    }
}
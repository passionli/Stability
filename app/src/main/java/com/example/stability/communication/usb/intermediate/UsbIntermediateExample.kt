package com.example.stability.communication.usb.intermediate

import android.content.Context
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.util.Log

/**
 * USB 通信中级示例
 * 展示 USB 通信的中级功能，如 USB 设备连接、数据传输等
 */
class UsbIntermediateExample(private val context: Context) {
    
    // USB 管理器
    private val usbManager: UsbManager
    
    init {
        // 获取 USB 管理器
        usbManager = context.getSystemService(Context.USB_SERVICE) as UsbManager
    }
    
    /**
     * 运行所有 USB 中级示例
     */
    fun runAllExamples() {
        Log.d("USB", "=== UsbIntermediateExample.runAllExamples called ===")
        Log.d("USB", "Thread ID: ${Thread.currentThread().id}")
        
        // 检查 USB 设备权限
        checkUsbDevicePermission()
        
        Log.d("USB", "=== UsbIntermediateExample.runAllExamples completed ===")
    }
    
    /**
     * 检查 USB 设备权限
     */
    private fun checkUsbDevicePermission() {
        Log.d("USB", "=== 运行检查 USB 设备权限示例 ===")
        
        // 获取所有已连接的 USB 设备
        val deviceList = usbManager.deviceList
        
        for ((deviceName, device) in deviceList) {
            Log.d("USB", "检查设备权限: $deviceName")
            
            // 检查是否有设备权限
            val hasPermission = usbManager.hasPermission(device)
            Log.d("USB", "  有设备权限: $hasPermission")
            
            if (hasPermission) {
                // 可以进行设备操作
                Log.d("USB", "  可以进行设备操作")
            } else {
                // 需要请求设备权限
                Log.d("USB", "  需要请求设备权限")
            }
        }
        
        Log.d("USB", "=== 检查 USB 设备权限示例完成 ===")
    }
}
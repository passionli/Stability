package com.example.stability.communication.usb.advanced

import android.content.Context
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbEndpoint
import android.hardware.usb.UsbInterface
import android.hardware.usb.UsbManager
import android.util.Log

/**
 * USB 通信高级示例
 * 展示 USB 通信的高级功能，如 USB 设备数据传输、批量传输等
 */
class UsbAdvancedExample(private val context: Context) {
    
    // USB 管理器
    private val usbManager: UsbManager
    
    init {
        // 获取 USB 管理器
        usbManager = context.getSystemService(Context.USB_SERVICE) as UsbManager
    }
    
    /**
     * 运行所有 USB 高级示例
     */
    fun runAllExamples() {
        Log.d("USB", "=== UsbAdvancedExample.runAllExamples called ===")
        Log.d("USB", "Thread ID: ${Thread.currentThread().id}")
        
        // 连接到 USB 设备并进行数据传输
        connectAndTransferData()
        
        Log.d("USB", "=== UsbAdvancedExample.runAllExamples completed ===")
    }
    
    /**
     * 连接到 USB 设备并进行数据传输
     */
    private fun connectAndTransferData() {
        Log.d("USB", "=== 运行连接到 USB 设备并进行数据传输示例 ===")
        
        // 获取所有已连接的 USB 设备
        val deviceList = usbManager.deviceList
        
        for ((deviceName, device) in deviceList) {
            Log.d("USB", "尝试连接设备: $deviceName")
            
            // 检查是否有设备权限
            val hasPermission = usbManager.hasPermission(device)
            if (!hasPermission) {
                Log.d("USB", "  没有设备权限，跳过")
                continue
            }
            
            // 打开设备连接
            val connection: UsbDeviceConnection? = usbManager.openDevice(device)
            if (connection == null) {
                Log.d("USB", "  无法打开设备连接")
                continue
            }
            
            // 遍历设备接口
            for (i in 0 until device.interfaceCount) {
                val usbInterface: UsbInterface = device.getInterface(i)
                Log.d("USB", "  接口 $i: ${usbInterface.id}, 类: ${usbInterface.interfaceClass}")
                
                // 遍历接口端点
                for (j in 0 until usbInterface.endpointCount) {
                    val endpoint: UsbEndpoint = usbInterface.getEndpoint(j)
                    Log.d("USB", "    端点 $j: 地址: ${endpoint.address}, 类型: ${endpoint.type}, 方向: ${endpoint.direction}")
                }
                
                // 尝试 claiming 接口
                val claimed = connection.claimInterface(usbInterface, true)
                Log.d("USB", "  Claim 接口: $claimed")
                
                if (claimed) {
                    // 可以进行数据传输
                    Log.d("USB", "  可以进行数据传输")
                    
                    // 释放接口
                    connection.releaseInterface(usbInterface)
                }
            }
            
            // 关闭连接
            connection.close()
        }
        
        Log.d("USB", "=== 连接到 USB 设备并进行数据传输示例完成 ===")
    }
}
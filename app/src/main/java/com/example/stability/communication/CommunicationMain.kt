package com.example.stability.communication

import android.content.Context
import android.util.Log
import com.example.stability.communication.ble.basic.BleBasicExample
import com.example.stability.communication.ble.intermediate.BleIntermediateExample
import com.example.stability.communication.ble.advanced.BleAdvancedExample
import com.example.stability.communication.usb.basic.UsbBasicExample
import com.example.stability.communication.usb.intermediate.UsbIntermediateExample
import com.example.stability.communication.usb.advanced.UsbAdvancedExample
import com.example.stability.communication.wifi_direct.basic.WifiDirectBasicExample
import com.example.stability.communication.wifi_direct.intermediate.WifiDirectIntermediateExample
import com.example.stability.communication.wifi_direct.advanced.WifiDirectAdvancedExample

/**
 * 通信协议学习主类，用于管理和启动不同通信协议的示例
 */
class CommunicationMain(private val context: Context) {
    
    /**
     * 运行所有通信协议示例
     */
    fun runAllExamples() {
        Log.d("Communication", "=== CommunicationMain.runAllExamples called ===")
        Log.d("Communication", "Thread ID: ${Thread.currentThread().id}")
        
        // 运行 BLE 示例
        runBleExamples()
        
        // 运行 USB 示例
        runUsbExamples()
        
        // 运行 Wi-Fi 直连示例
        runWifiDirectExamples()
        
        Log.d("Communication", "=== CommunicationMain.runAllExamples completed ===")
    }
    
    /**
     * 运行 BLE 示例
     */
    private fun runBleExamples() {
        Log.d("Communication", "=== 运行 BLE 示例 ===")
        
        // 运行 BLE 初级示例
        val bleBasicExample = BleBasicExample(context)
        bleBasicExample.runAllExamples()
        
        // 运行 BLE 中级示例
        val bleIntermediateExample = BleIntermediateExample(context)
        bleIntermediateExample.runAllExamples()
        
        // 运行 BLE 高级示例
        val bleAdvancedExample = BleAdvancedExample(context)
        bleAdvancedExample.runAllExamples()
        
        Log.d("Communication", "=== BLE 示例运行完成 ===")
    }
    
    /**
     * 运行 USB 示例
     */
    private fun runUsbExamples() {
        Log.d("Communication", "=== 运行 USB 示例 ===")
        
        // 运行 USB 初级示例
        val usbBasicExample = UsbBasicExample(context)
        usbBasicExample.runAllExamples()
        
        // 运行 USB 中级示例
        val usbIntermediateExample = UsbIntermediateExample(context)
        usbIntermediateExample.runAllExamples()
        
        // 运行 USB 高级示例
        val usbAdvancedExample = UsbAdvancedExample(context)
        usbAdvancedExample.runAllExamples()
        
        Log.d("Communication", "=== USB 示例运行完成 ===")
    }
    
    /**
     * 运行 Wi-Fi 直连示例
     */
    private fun runWifiDirectExamples() {
        Log.d("Communication", "=== 运行 Wi-Fi 直连示例 ===")
        
        // 运行 Wi-Fi 直连初级示例
        val wifiDirectBasicExample = WifiDirectBasicExample(context)
        wifiDirectBasicExample.runAllExamples()
        
        // 运行 Wi-Fi 直连中级示例
        val wifiDirectIntermediateExample = WifiDirectIntermediateExample(context)
        wifiDirectIntermediateExample.runAllExamples()
        
        // 运行 Wi-Fi 直连高级示例
        val wifiDirectAdvancedExample = WifiDirectAdvancedExample(context)
        wifiDirectAdvancedExample.runAllExamples()
        
        Log.d("Communication", "=== Wi-Fi 直连示例运行完成 ===")
    }
}
package com.example.stability.communication.ble.basic

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.util.Log

/**
 * BLE 通信初级示例
 * 展示 BLE 通信的基本功能，如蓝牙开启、设备扫描等
 */
class BleBasicExample(private val context: Context) {
    
    // 蓝牙适配器
    private val bluetoothAdapter: BluetoothAdapter?
    
    init {
        // 获取蓝牙管理器
        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        // 获取蓝牙适配器
        bluetoothAdapter = bluetoothManager.adapter
    }
    
    /**
     * 运行所有 BLE 初级示例
     */
    fun runAllExamples() {
        Log.d("BLE", "=== BleBasicExample.runAllExamples called ===")
        Log.d("BLE", "Thread ID: ${Thread.currentThread().id}")
        
        // 检查蓝牙是否可用
        checkBluetoothAvailability()
        
        // 检查蓝牙是否开启
        checkBluetoothEnabled()
        
        Log.d("BLE", "=== BleBasicExample.runAllExamples completed ===")
    }
    
    /**
     * 检查蓝牙是否可用
     */
    private fun checkBluetoothAvailability() {
        Log.d("BLE", "=== 运行检查蓝牙是否可用示例 ===")
        
        if (bluetoothAdapter == null) {
            // 设备不支持蓝牙
            Log.d("BLE", "设备不支持蓝牙")
        } else {
            // 设备支持蓝牙
            Log.d("BLE", "设备支持蓝牙")
        }
        
        Log.d("BLE", "=== 检查蓝牙是否可用示例完成 ===")
    }
    
    /**
     * 检查蓝牙是否开启
     */
    private fun checkBluetoothEnabled() {
        Log.d("BLE", "=== 运行检查蓝牙是否开启示例 ===")
        
        if (bluetoothAdapter == null) {
            Log.d("BLE", "设备不支持蓝牙，无法检查蓝牙状态")
            return
        }
        
        if (bluetoothAdapter.isEnabled) {
            // 蓝牙已开启
            Log.d("BLE", "蓝牙已开启")
        } else {
            // 蓝牙未开启
            Log.d("BLE", "蓝牙未开启")
        }
        
        Log.d("BLE", "=== 检查蓝牙是否开启示例完成 ===")
    }
}
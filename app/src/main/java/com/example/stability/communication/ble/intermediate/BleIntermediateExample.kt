package com.example.stability.communication.ble.intermediate

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log

/**
 * BLE 通信中级示例
 * 展示 BLE 通信的中级功能，如设备扫描、连接等
 */
class BleIntermediateExample(private val context: Context) {
    
    // 蓝牙适配器
    private val bluetoothAdapter: BluetoothAdapter?
    // 设备列表
    private val deviceList = mutableListOf<BluetoothDevice>()
    
    init {
        // 获取蓝牙管理器
        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        // 获取蓝牙适配器
        bluetoothAdapter = bluetoothManager.adapter
    }
    
    /**
     * 运行所有 BLE 中级示例
     */
    fun runAllExamples() {
        Log.d("BLE", "=== BleIntermediateExample.runAllExamples called ===")
        Log.d("BLE", "Thread ID: ${Thread.currentThread().id}")
        
        // 注册蓝牙设备发现广播接收器
        registerBluetoothReceiver()
        
        // 开始扫描蓝牙设备
        startBluetoothScan()
        
        Log.d("BLE", "=== BleIntermediateExample.runAllExamples completed ===")
    }
    
    /**
     * 注册蓝牙设备发现广播接收器
     */
    private fun registerBluetoothReceiver() {
        Log.d("BLE", "=== 运行注册蓝牙设备发现广播接收器示例 ===")
        
        // 创建蓝牙设备发现广播接收器
        val bluetoothReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val action = intent?.action
                if (BluetoothDevice.ACTION_FOUND == action) {
                    // 发现蓝牙设备
                    val device: BluetoothDevice? = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    if (device != null) {
                        // 添加设备到列表
                        if (!deviceList.contains(device)) {
                            deviceList.add(device)
                            Log.d("BLE", "发现设备: ${device.name}, ${device.address}")
                        }
                    }
                } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED == action) {
                    // 扫描完成
                    Log.d("BLE", "扫描完成，共发现 ${deviceList.size} 个设备")
                }
            }
        }
        
        // 注册广播接收器
        val filter = IntentFilter()
        filter.addAction(BluetoothDevice.ACTION_FOUND)
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        context.registerReceiver(bluetoothReceiver, filter)
        
        Log.d("BLE", "=== 注册蓝牙设备发现广播接收器示例完成 ===")
    }
    
    /**
     * 开始扫描蓝牙设备
     */
    private fun startBluetoothScan() {
        Log.d("BLE", "=== 运行开始扫描蓝牙设备示例 ===")
        
        if (bluetoothAdapter == null) {
            Log.d("BLE", "设备不支持蓝牙，无法扫描设备")
            return
        }
        
        if (!bluetoothAdapter.isEnabled) {
            Log.d("BLE", "蓝牙未开启，无法扫描设备")
            return
        }
        
        // 取消之前的扫描
        if (bluetoothAdapter.isDiscovering) {
            bluetoothAdapter.cancelDiscovery()
        }
        
        // 开始扫描
        val started = bluetoothAdapter.startDiscovery()
        Log.d("BLE", "开始扫描: $started")
        
        Log.d("BLE", "=== 开始扫描蓝牙设备示例完成 ===")
    }
}
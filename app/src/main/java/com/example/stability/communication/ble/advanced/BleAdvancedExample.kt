package com.example.stability.communication.ble.advanced

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothManager
import android.content.Context
import android.util.Log

/**
 * BLE 通信高级示例
 * 展示 BLE 通信的高级功能，如 GATT 服务、特征值读写等
 */
class BleAdvancedExample(private val context: Context) {
    
    // 蓝牙适配器
    private val bluetoothAdapter: BluetoothAdapter?
    // 蓝牙 GATT 客户端
    private var bluetoothGatt: BluetoothGatt? = null
    
    init {
        // 获取蓝牙管理器
        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        // 获取蓝牙适配器
        bluetoothAdapter = bluetoothManager.adapter
    }
    
    /**
     * 运行所有 BLE 高级示例
     */
    fun runAllExamples() {
        Log.d("BLE", "=== BleAdvancedExample.runAllExamples called ===")
        Log.d("BLE", "Thread ID: ${Thread.currentThread().id}")
        
        // 连接到 BLE 设备（这里需要指定设备地址）
        connectToBleDevice()
        
        Log.d("BLE", "=== BleAdvancedExample.runAllExamples completed ===")
    }
    
    /**
     * 连接到 BLE 设备
     */
    private fun connectToBleDevice() {
        Log.d("BLE", "=== 运行连接到 BLE 设备示例 ===")
        
        if (bluetoothAdapter == null) {
            Log.d("BLE", "设备不支持蓝牙，无法连接设备")
            return
        }
        
        if (!bluetoothAdapter.isEnabled) {
            Log.d("BLE", "蓝牙未开启，无法连接设备")
            return
        }
        
        // 这里需要替换为实际的 BLE 设备地址
        val deviceAddress = "00:11:22:33:44:55"
        val device = bluetoothAdapter.getRemoteDevice(deviceAddress)
        
        // 连接到设备
        Log.d("BLE", "正在连接到设备: $deviceAddress")
        bluetoothGatt = device.connectGatt(context, false, gattCallback)
        
        Log.d("BLE", "=== 连接到 BLE 设备示例完成 ===")
    }
    
    /**
     * 蓝牙 GATT 回调
     */
    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)
            
            if (newState == BluetoothGatt.STATE_CONNECTED) {
                // 连接成功，开始发现服务
                Log.d("BLE", "连接成功，开始发现服务")
                gatt?.discoverServices()
            } else if (newState == BluetoothGatt.STATE_DISCONNECTED) {
                // 连接断开
                Log.d("BLE", "连接断开")
            }
        }
        
        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            super.onServicesDiscovered(gatt, status)
            
            if (status == BluetoothGatt.GATT_SUCCESS) {
                // 发现服务成功
                Log.d("BLE", "发现服务成功")
                // 打印所有服务
                printServices(gatt)
            } else {
                // 发现服务失败
                Log.d("BLE", "发现服务失败，状态码: $status")
            }
        }
        
        override fun onCharacteristicRead(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?, status: Int) {
            super.onCharacteristicRead(gatt, characteristic, status)
            
            if (status == BluetoothGatt.GATT_SUCCESS && characteristic != null) {
                // 读取特征值成功
                val value = characteristic.value
                Log.d("BLE", "读取特征值成功: ${value.contentToString()}")
            } else {
                // 读取特征值失败
                Log.d("BLE", "读取特征值失败，状态码: $status")
            }
        }
        
        override fun onCharacteristicWrite(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?, status: Int) {
            super.onCharacteristicWrite(gatt, characteristic, status)
            
            if (status == BluetoothGatt.GATT_SUCCESS) {
                // 写入特征值成功
                Log.d("BLE", "写入特征值成功")
            } else {
                // 写入特征值失败
                Log.d("BLE", "写入特征值失败，状态码: $status")
            }
        }
    }
    
    /**
     * 打印所有服务
     */
    private fun printServices(gatt: BluetoothGatt?) {
        if (gatt == null) {
            return
        }
        
        val services: List<BluetoothGattService> = gatt.services
        for (service in services) {
            Log.d("BLE", "服务 UUID: ${service.uuid}")
            
            val characteristics: List<BluetoothGattCharacteristic> = service.characteristics
            for (characteristic in characteristics) {
                Log.d("BLE", "  特征值 UUID: ${characteristic.uuid}")
                Log.d("BLE", "  特征值属性: ${characteristic.properties}")
            }
        }
    }
}
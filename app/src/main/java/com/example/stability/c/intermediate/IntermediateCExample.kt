package com.example.stability.c.intermediate

import android.util.Log

/**
 * C 语言中级示例
 * 展示指针、数组和结构体等中级特性
 */
class IntermediateCExample {
    
    // 加载 C 库
    init {
        System.loadLibrary("native-c-lib")
    }
    
    /**
     * 运行所有中级 C 语言示例
     */
    fun runAllExamples() {
        Log.d("C", "=== IntermediateCExample.runAllExamples called ===")
        Log.d("C", "Thread ID: ${Thread.currentThread().id}")
        
        // 运行指针示例
        runPointers()
        
        // 运行数组示例
        runArrays()
        
        // 运行结构体示例
        runStructures()
        
        Log.d("C", "=== IntermediateCExample.runAllExamples completed ===")
    }
    
    /**
     * 运行指针示例
     */
    private fun runPointers() {
        Log.d("C", "=== 运行指针示例 ===")
        
        // 调用 C 函数，演示指针
        val result = pointers()
        Log.d("C", "Pointers result: $result")
        
        Log.d("C", "=== 指针示例完成 ===")
    }
    
    /**
     * 运行数组示例
     */
    private fun runArrays() {
        Log.d("C", "=== 运行数组示例 ===")
        
        // 调用 C 函数，演示数组
        val result = arrays()
        Log.d("C", "Arrays result: $result")
        
        Log.d("C", "=== 数组示例完成 ===")
    }
    
    /**
     * 运行结构体示例
     */
    private fun runStructures() {
        Log.d("C", "=== 运行结构体示例 ===")
        
        // 调用 C 函数，演示结构体
        val result = structures()
        Log.d("C", "Structures result: $result")
        
        Log.d("C", "=== 结构体示例完成 ===")
    }
    
    // 声明 native 方法
    private external fun pointers(): Int
    private external fun arrays(): Int
    private external fun structures(): Int
}
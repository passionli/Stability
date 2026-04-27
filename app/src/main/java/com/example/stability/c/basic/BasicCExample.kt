package com.example.stability.c.basic

import android.util.Log

/**
 * C 语言初级示例
 * 展示基本的 C 语言语法和功能
 */
class BasicCExample {
    
    // 加载 C 库
    init {
        System.loadLibrary("native-c-lib")
    }
    
    /**
     * 运行所有初级 C 语言示例
     */
    fun runAllExamples() {
        Log.d("C", "=== BasicCExample.runAllExamples called ===")
        Log.d("C", "Thread ID: ${Thread.currentThread().id}")
        
        // 运行基本数据类型示例
        runBasicDataTypes()
        
        // 运行控制流示例
        runControlFlow()
        
        // 运行函数示例
        runFunctions()
        
        Log.d("C", "=== BasicCExample.runAllExamples completed ===")
    }
    
    /**
     * 运行基本数据类型示例
     */
    private fun runBasicDataTypes() {
        Log.d("C", "=== 运行基本数据类型示例 ===")
        
        // 调用 C 函数，演示基本数据类型
        val result = basicDataTypes()
        Log.d("C", "Basic data types result: $result")
        
        Log.d("C", "=== 基本数据类型示例完成 ===")
    }
    
    /**
     * 运行控制流示例
     */
    private fun runControlFlow() {
        Log.d("C", "=== 运行控制流示例 ===")
        
        // 调用 C 函数，演示控制流
        val result = controlFlow()
        Log.d("C", "Control flow result: $result")
        
        Log.d("C", "=== 控制流示例完成 ===")
    }
    
    /**
     * 运行函数示例
     */
    private fun runFunctions() {
        Log.d("C", "=== 运行函数示例 ===")
        
        // 调用 C 函数，演示函数
        val result = functions()
        Log.d("C", "Functions result: $result")
        
        Log.d("C", "=== 函数示例完成 ===")
    }
    
    // 声明 native 方法
    private external fun basicDataTypes(): Int
    private external fun controlFlow(): Int
    private external fun functions(): Int
}
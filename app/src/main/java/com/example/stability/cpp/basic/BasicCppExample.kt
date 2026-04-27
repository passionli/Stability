package com.example.stability.cpp.basic

import android.util.Log

/**
 * C++ 初级示例
 * 展示基本的 C++ 语法和功能
 */
class BasicCppExample {
    
    // 加载 C++ 库
    init {
        System.loadLibrary("native-lib")
    }
    
    /**
     * 运行所有初级 C++ 示例
     */
    fun runAllExamples() {
        Log.d("Cpp", "=== BasicCppExample.runAllExamples called ===")
        Log.d("Cpp", "Thread ID: ${Thread.currentThread().id}")
        
        // 运行基本数据类型示例
        runBasicDataTypes()
        
        // 运行控制流示例
        runControlFlow()
        
        // 运行函数示例
        runFunctions()
        
        Log.d("Cpp", "=== BasicCppExample.runAllExamples completed ===")
    }
    
    /**
     * 运行基本数据类型示例
     */
    private fun runBasicDataTypes() {
        Log.d("Cpp", "=== 运行基本数据类型示例 ===")
        
        // 调用 C++ 函数，演示基本数据类型
        val result = basicDataTypes()
        Log.d("Cpp", "Basic data types result: $result")
        
        Log.d("Cpp", "=== 基本数据类型示例完成 ===")
    }
    
    /**
     * 运行控制流示例
     */
    private fun runControlFlow() {
        Log.d("Cpp", "=== 运行控制流示例 ===")
        
        // 调用 C++ 函数，演示控制流
        val result = controlFlow()
        Log.d("Cpp", "Control flow result: $result")
        
        Log.d("Cpp", "=== 控制流示例完成 ===")
    }
    
    /**
     * 运行函数示例
     */
    private fun runFunctions() {
        Log.d("Cpp", "=== 运行函数示例 ===")
        
        // 调用 C++ 函数，演示函数
        val result = functions()
        Log.d("Cpp", "Functions result: $result")
        
        Log.d("Cpp", "=== 函数示例完成 ===")
    }
    
    // 声明 native 方法
    private external fun basicDataTypes(): Int
    private external fun controlFlow(): Int
    private external fun functions(): Int
}
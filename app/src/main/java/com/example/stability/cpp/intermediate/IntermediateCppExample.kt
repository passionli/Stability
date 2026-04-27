package com.example.stability.cpp.intermediate

import android.util.Log

/**
 * C++ 中级示例
 * 展示面向对象编程和STL等中级特性
 */
class IntermediateCppExample {
    
    // 加载 C++ 库
    init {
        System.loadLibrary("native-lib")
    }
    
    /**
     * 运行所有中级 C++ 示例
     */
    fun runAllExamples() {
        Log.d("Cpp", "=== IntermediateCppExample.runAllExamples called ===")
        Log.d("Cpp", "Thread ID: ${Thread.currentThread().id}")
        
        // 运行面向对象编程示例
        runObjectOrientedProgramming()
        
        // 运行 STL 示例
        runSTL()
        
        // 运行异常处理示例
        runExceptionHandling()
        
        Log.d("Cpp", "=== IntermediateCppExample.runAllExamples completed ===")
    }
    
    /**
     * 运行面向对象编程示例
     */
    private fun runObjectOrientedProgramming() {
        Log.d("Cpp", "=== 运行面向对象编程示例 ===")
        
        // 调用 C++ 函数，演示面向对象编程
        val result = objectOrientedProgramming()
        Log.d("Cpp", "Object oriented programming result: $result")
        
        Log.d("Cpp", "=== 面向对象编程示例完成 ===")
    }
    
    /**
     * 运行 STL 示例
     */
    private fun runSTL() {
        Log.d("Cpp", "=== 运行 STL 示例 ===")
        
        // 调用 C++ 函数，演示 STL
        val result = stl()
        Log.d("Cpp", "STL result: $result")
        
        Log.d("Cpp", "=== STL 示例完成 ===")
    }
    
    /**
     * 运行异常处理示例
     */
    private fun runExceptionHandling() {
        Log.d("Cpp", "=== 运行异常处理示例 ===")
        
        // 调用 C++ 函数，演示异常处理
        val result = exceptionHandling()
        Log.d("Cpp", "Exception handling result: $result")
        
        Log.d("Cpp", "=== 异常处理示例完成 ===")
    }
    
    // 声明 native 方法
    private external fun objectOrientedProgramming(): Int
    private external fun stl(): Int
    private external fun exceptionHandling(): Int
}
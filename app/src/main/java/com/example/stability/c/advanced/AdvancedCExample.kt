package com.example.stability.c.advanced

import android.util.Log

/**
 * C 语言高级示例
 * 展示内存管理、文件操作和多线程等高级特性
 */
class AdvancedCExample {
    
    // 加载 C 库
    init {
        System.loadLibrary("native-c-lib")
    }
    
    /**
     * 运行所有高级 C 语言示例
     */
    fun runAllExamples() {
        Log.d("C", "=== AdvancedCExample.runAllExamples called ===")
        Log.d("C", "Thread ID: ${Thread.currentThread().id}")
        
        // 运行内存管理示例
        runMemoryManagement()
        
        // 运行文件操作示例
        runFileOperations()
        
        // 运行多线程示例
        runMultithreading()
        
        Log.d("C", "=== AdvancedCExample.runAllExamples completed ===")
    }
    
    /**
     * 运行内存管理示例
     */
    private fun runMemoryManagement() {
        Log.d("C", "=== 运行内存管理示例 ===")
        
        // 调用 C 函数，演示内存管理
        val result = memoryManagement()
        Log.d("C", "Memory management result: $result")
        
        Log.d("C", "=== 内存管理示例完成 ===")
    }
    
    /**
     * 运行文件操作示例
     */
    private fun runFileOperations() {
        Log.d("C", "=== 运行文件操作示例 ===")
        
        // 调用 C 函数，演示文件操作
        val result = fileOperations()
        Log.d("C", "File operations result: $result")
        
        Log.d("C", "=== 文件操作示例完成 ===")
    }
    
    /**
     * 运行多线程示例
     */
    private fun runMultithreading() {
        Log.d("C", "=== 运行多线程示例 ===")
        
        // 调用 C 函数，演示多线程
        val result = multithreading()
        Log.d("C", "Multithreading result: $result")
        
        Log.d("C", "=== 多线程示例完成 ===")
    }
    
    // 声明 native 方法
    private external fun memoryManagement(): Int
    private external fun fileOperations(): Int
    private external fun multithreading(): Int
}
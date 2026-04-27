package com.example.stability.cpp.advanced

import android.util.Log

/**
 * C++ 高级示例
 * 展示模板、智能指针和多线程等高级特性
 */
class AdvancedCppExample {
    
    // 加载 C++ 库
    init {
        System.loadLibrary("native-lib")
    }
    
    /**
     * 运行所有高级 C++ 示例
     */
    fun runAllExamples() {
        Log.d("Cpp", "=== AdvancedCppExample.runAllExamples called ===")
        Log.d("Cpp", "Thread ID: ${Thread.currentThread().id}")
        
        // 运行模板示例
        runTemplates()
        
        // 运行智能指针示例
        runSmartPointers()
        
        // 运行多线程示例
        runMultithreading()
        
        Log.d("Cpp", "=== AdvancedCppExample.runAllExamples completed ===")
    }
    
    /**
     * 运行模板示例
     */
    private fun runTemplates() {
        Log.d("Cpp", "=== 运行模板示例 ===")
        
        // 调用 C++ 函数，演示模板
        val result = templates()
        Log.d("Cpp", "Templates result: $result")
        
        Log.d("Cpp", "=== 模板示例完成 ===")
    }
    
    /**
     * 运行智能指针示例
     */
    private fun runSmartPointers() {
        Log.d("Cpp", "=== 运行智能指针示例 ===")
        
        // 调用 C++ 函数，演示智能指针
        val result = smartPointers()
        Log.d("Cpp", "Smart pointers result: $result")
        
        Log.d("Cpp", "=== 智能指针示例完成 ===")
    }
    
    /**
     * 运行多线程示例
     */
    private fun runMultithreading() {
        Log.d("Cpp", "=== 运行多线程示例 ===")
        
        // 调用 C++ 函数，演示多线程
        val result = multithreading()
        Log.d("Cpp", "Multithreading result: $result")
        
        Log.d("Cpp", "=== 多线程示例完成 ===")
    }
    
    // 声明 native 方法
    private external fun templates(): Int
    private external fun smartPointers(): Int
    private external fun multithreading(): Int
}
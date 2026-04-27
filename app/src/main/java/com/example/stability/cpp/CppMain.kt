package com.example.stability.cpp

import android.content.Context
import android.util.Log

/**
 * C++ 学习主类，用于管理和启动不同级别的 C++ 示例
 */
class CppMain(private val context: Context) {
    
    /**
     * 运行所有 C++ 示例
     */
    fun runAllExamples() {
        Log.d("Cpp", "=== CppMain.runAllExamples called ===")
        Log.d("Cpp", "Thread ID: ${Thread.currentThread().id}")
        
        // 运行初级 C++ 示例
        runBasicExamples()
        
        // 运行中级 C++ 示例
        runIntermediateExamples()
        
        // 运行高级 C++ 示例
        runAdvancedExamples()
        
        Log.d("Cpp", "=== CppMain.runAllExamples completed ===")
    }
    
    /**
     * 运行初级 C++ 示例
     */
    private fun runBasicExamples() {
        Log.d("Cpp", "=== 运行初级 C++ 示例 ===")
        val basicExample = com.example.stability.cpp.basic.BasicCppExample()
        basicExample.runAllExamples()
        Log.d("Cpp", "=== 初级 C++ 示例运行完成 ===")
    }
    
    /**
     * 运行中级 C++ 示例
     */
    private fun runIntermediateExamples() {
        Log.d("Cpp", "=== 运行中级 C++ 示例 ===")
        val intermediateExample = com.example.stability.cpp.intermediate.IntermediateCppExample()
        intermediateExample.runAllExamples()
        Log.d("Cpp", "=== 中级 C++ 示例运行完成 ===")
    }
    
    /**
     * 运行高级 C++ 示例
     */
    private fun runAdvancedExamples() {
        Log.d("Cpp", "=== 运行高级 C++ 示例 ===")
        val advancedExample = com.example.stability.cpp.advanced.AdvancedCppExample()
        advancedExample.runAllExamples()
        Log.d("Cpp", "=== 高级 C++ 示例运行完成 ===")
    }
}
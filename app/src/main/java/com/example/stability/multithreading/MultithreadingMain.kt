package com.example.stability.multithreading

import android.content.Context
import android.util.Log

/**
 * 多线程学习主类，用于管理和启动不同级别的多线程示例
 */
class MultithreadingMain(private val context: Context) {
    
    /**
     * 运行所有多线程示例
     */
    fun runAllExamples() {
        Log.d("Multithreading", "=== MultithreadingMain.runAllExamples called ===")
        Log.d("Multithreading", "Thread ID: ${Thread.currentThread().id}")
        
        // 运行初级多线程示例
        runBasicExamples()
        
        // 运行中级多线程示例
        runIntermediateExamples()
        
        // 运行高级多线程示例
        runAdvancedExamples()
        
        Log.d("Multithreading", "=== MultithreadingMain.runAllExamples completed ===")
    }
    
    /**
     * 运行初级多线程示例
     */
    private fun runBasicExamples() {
        Log.d("Multithreading", "=== 运行初级多线程示例 ===")
        val basicExample = com.example.stability.multithreading.basic.BasicThreadExample()
        basicExample.runAllExamples()
        Log.d("Multithreading", "=== 初级多线程示例运行完成 ===")
    }
    
    /**
     * 运行中级多线程示例
     */
    private fun runIntermediateExamples() {
        Log.d("Multithreading", "=== 运行中级多线程示例 ===")
        val intermediateExample = com.example.stability.multithreading.intermediate.IntermediateThreadExample()
        intermediateExample.runAllExamples()
        Log.d("Multithreading", "=== 中级多线程示例运行完成 ===")
    }
    
    /**
     * 运行高级多线程示例
     */
    private fun runAdvancedExamples() {
        Log.d("Multithreading", "=== 运行高级多线程示例 ===")
        val advancedExample = com.example.stability.multithreading.advanced.AdvancedThreadExample()
        advancedExample.runAllExamples()
        Log.d("Multithreading", "=== 高级多线程示例运行完成 ===")
    }
}
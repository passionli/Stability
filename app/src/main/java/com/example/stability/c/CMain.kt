package com.example.stability.c

import android.content.Context
import android.util.Log

/**
 * C 语言学习主类，用于管理和启动不同级别的 C 示例
 */
class CMain(private val context: Context) {
    
    /**
     * 运行所有 C 语言示例
     */
    fun runAllExamples() {
        Log.d("C", "=== CMain.runAllExamples called ===")
        Log.d("C", "Thread ID: ${Thread.currentThread().id}")
        
        // 运行初级 C 语言示例
        runBasicExamples()
        
        // 运行中级 C 语言示例
        runIntermediateExamples()
        
        // 运行高级 C 语言示例
        runAdvancedExamples()
        
        Log.d("C", "=== CMain.runAllExamples completed ===")
    }
    
    /**
     * 运行初级 C 语言示例
     */
    private fun runBasicExamples() {
        Log.d("C", "=== 运行初级 C 语言示例 ===")
        val basicExample = com.example.stability.c.basic.BasicCExample()
        basicExample.runAllExamples()
        Log.d("C", "=== 初级 C 语言示例运行完成 ===")
    }
    
    /**
     * 运行中级 C 语言示例
     */
    private fun runIntermediateExamples() {
        Log.d("C", "=== 运行中级 C 语言示例 ===")
        val intermediateExample = com.example.stability.c.intermediate.IntermediateCExample()
        intermediateExample.runAllExamples()
        Log.d("C", "=== 中级 C 语言示例运行完成 ===")
    }
    
    /**
     * 运行高级 C 语言示例
     */
    private fun runAdvancedExamples() {
        Log.d("C", "=== 运行高级 C 语言示例 ===")
        val advancedExample = com.example.stability.c.advanced.AdvancedCExample()
        advancedExample.runAllExamples()
        Log.d("C", "=== 高级 C 语言示例运行完成 ===")
    }
}
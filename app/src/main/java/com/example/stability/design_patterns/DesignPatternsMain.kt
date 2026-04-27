package com.example.stability.design_patterns

import android.content.Context
import android.util.Log
import com.example.stability.design_patterns.creational.basic.CreationalBasicExample
import com.example.stability.design_patterns.creational.intermediate.CreationalIntermediateExample
import com.example.stability.design_patterns.creational.advanced.CreationalAdvancedExample
import com.example.stability.design_patterns.structural.basic.StructuralBasicExample
import com.example.stability.design_patterns.structural.intermediate.StructuralIntermediateExample
import com.example.stability.design_patterns.structural.advanced.StructuralAdvancedExample
import com.example.stability.design_patterns.behavioral.basic.BehavioralBasicExample
import com.example.stability.design_patterns.behavioral.intermediate.BehavioralIntermediateExample
import com.example.stability.design_patterns.behavioral.advanced.BehavioralAdvancedExample

/**
 * 设计模式学习主类，用于管理和启动不同设计模式的示例
 */
class DesignPatternsMain(private val context: Context) {
    
    /**
     * 运行所有设计模式示例
     */
    fun runAllExamples() {
        Log.d("DesignPatterns", "=== DesignPatternsMain.runAllExamples called ===")
        Log.d("DesignPatterns", "Thread ID: ${Thread.currentThread().id}")
        
        // 运行创建型设计模式示例
        runCreationalPatterns()
        
        // 运行结构型设计模式示例
        runStructuralPatterns()
        
        // 运行行为型设计模式示例
        runBehavioralPatterns()
        
        Log.d("DesignPatterns", "=== DesignPatternsMain.runAllExamples completed ===")
    }
    
    /**
     * 运行创建型设计模式示例
     */
    private fun runCreationalPatterns() {
        Log.d("DesignPatterns", "=== 运行创建型设计模式示例 ===")
        
        // 运行创建型初级示例
        val creationalBasicExample = CreationalBasicExample()
        creationalBasicExample.runAllExamples()
        
        // 运行创建型中级示例
        val creationalIntermediateExample = CreationalIntermediateExample()
        creationalIntermediateExample.runAllExamples()
        
        // 运行创建型高级示例
        val creationalAdvancedExample = CreationalAdvancedExample()
        creationalAdvancedExample.runAllExamples()
        
        Log.d("DesignPatterns", "=== 创建型设计模式示例运行完成 ===")
    }
    
    /**
     * 运行结构型设计模式示例
     */
    private fun runStructuralPatterns() {
        Log.d("DesignPatterns", "=== 运行结构型设计模式示例 ===")
        
        // 运行结构型初级示例
        val structuralBasicExample = StructuralBasicExample()
        structuralBasicExample.runAllExamples()
        
        // 运行结构型中级示例
        val structuralIntermediateExample = StructuralIntermediateExample()
        structuralIntermediateExample.runAllExamples()
        
        // 运行结构型高级示例
        val structuralAdvancedExample = StructuralAdvancedExample()
        structuralAdvancedExample.runAllExamples()
        
        Log.d("DesignPatterns", "=== 结构型设计模式示例运行完成 ===")
    }
    
    /**
     * 运行行为型设计模式示例
     */
    private fun runBehavioralPatterns() {
        Log.d("DesignPatterns", "=== 运行行为型设计模式示例 ===")
        
        // 运行行为型初级示例
        val behavioralBasicExample = BehavioralBasicExample()
        behavioralBasicExample.runAllExamples()
        
        // 运行行为型中级示例
        val behavioralIntermediateExample = BehavioralIntermediateExample()
        behavioralIntermediateExample.runAllExamples()
        
        // 运行行为型高级示例
        val behavioralAdvancedExample = BehavioralAdvancedExample()
        behavioralAdvancedExample.runAllExamples()
        
        Log.d("DesignPatterns", "=== 行为型设计模式示例运行完成 ===")
    }
}
package com.example.stability.kotlin_learning

import android.util.Log
import com.example.stability.kotlin_learning.basic.BasicSyntax
import com.example.stability.kotlin_learning.basic.ClassesAndObjects
import com.example.stability.kotlin_learning.intermediate.AdvancedFeatures
import com.example.stability.kotlin_learning.intermediate.Coroutines
import com.example.stability.kotlin_learning.advanced.ExpertFeatures

/**
 * Kotlin 学习主入口
 */
class KotlinLearningMain {
    
    /**
     * 运行所有 Kotlin 学习示例
     */
    fun runAllExamples() {
        Log.d("KotlinLearning", "=== KotlinLearningMain.runAllExamples called ===")
        Log.d("KotlinLearning", "Thread ID: ${Thread.currentThread().id}")
        
        // 打印调用堆栈
        Log.d("KotlinLearning", "Call stack:")
        Thread.currentThread().stackTrace.forEachIndexed { index, element ->
            if (index > 2) {
                Log.d("KotlinLearning", "  $index: ${element.className}.${element.methodName}(${element.fileName}:${element.lineNumber})")
            }
        }
        
        // 运行基础示例
        Log.d("KotlinLearning", "\n=== Running Basic Examples ===")
        val basicSyntax = BasicSyntax()
        basicSyntax.runBasicSyntax()
        
        val classesAndObjects = ClassesAndObjects()
        classesAndObjects.runClassesAndObjects()
        
        // 运行进阶示例
        Log.d("KotlinLearning", "\n=== Running Intermediate Examples ===")
        val advancedFeatures = AdvancedFeatures()
        advancedFeatures.runAdvancedFeatures()
        
        val coroutines = Coroutines()
        coroutines.runCoroutines()
        
        // 运行精通示例
        Log.d("KotlinLearning", "\n=== Running Advanced Examples ===")
        val expertFeatures = ExpertFeatures()
        expertFeatures.runExpertFeatures()
        
        Log.d("KotlinLearning", "\n=== KotlinLearningMain.runAllExamples completed ===")
    }
}

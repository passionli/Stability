package com.example.stability

import org.junit.Test
import com.example.stability.kotlin_learning.KotlinLearningMain

/**
 * 测试 Kotlin 学习示例
 */
class KotlinLearningInstrumentedTest {
    
    @Test
    fun testKotlinLearning() {
        println("=== 开始测试 Kotlin 学习示例 ===")
        
        val kotlinLearning = KotlinLearningMain()
        kotlinLearning.runAllExamples()
        
        println("=== Kotlin 学习示例测试完成 ===")
    }
}

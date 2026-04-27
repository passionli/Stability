package com.example.stability

import org.junit.Test

/**
 * JetPack 示例测试
 */
class JetpackDemoTest {
    
    @Test
    fun testJetpackExamples() {
        println("=== 开始测试 JetPack 示例 ===")
        
        val jetpackDemo = JetpackDemoMain()
        jetpackDemo.runAllExamples()
        
        println("=== JetPack 示例测试完成 ===")
    }
}

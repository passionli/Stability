package com.example.stability

import android.util.Log
import com.example.stability.jetpack.JetpackMain

/**
 * JetPack 示例主入口
 */
class JetpackDemoMain {
    
    /**
     * 运行所有 JetPack 示例
     */
    fun runAllExamples() {
        Log.d("JetpackDemo", "=== JetpackDemoMain.runAllExamples called ===")
        Log.d("JetpackDemo", "Thread ID: ${Thread.currentThread().id}")
        
        // 运行 JetPack 示例
        val jetpackMain = JetpackMain()
        jetpackMain.runAllExamples()
        
        Log.d("JetpackDemo", "=== JetpackDemoMain.runAllExamples completed ===")
    }
}

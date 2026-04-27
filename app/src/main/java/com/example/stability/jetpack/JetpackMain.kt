package com.example.stability.jetpack

import android.util.Log

/**
 * JetPack 框架示例主入口
 */
class JetpackMain {
    
    /**
     * 运行所有 JetPack 示例
     */
    fun runAllExamples() {
        Log.d("JetpackExample", "=== JetpackMain.runAllExamples called ===")
        Log.d("JetpackExample", "Thread ID: ${Thread.currentThread().id}")
        
        // 运行 ViewModel 示例
        Log.d("JetpackExample", "\n=== Running ViewModel Example ===")
        val viewModelExample = ViewModelExample()
        viewModelExample.runViewModelExample()
        
        // 运行 LiveData 示例
        Log.d("JetpackExample", "\n=== Running LiveData Example ===")
        val liveDataExample = LiveDataExample()
        liveDataExample.runLiveDataExample()
        
        // 运行 DataBinding 示例
        Log.d("JetpackExample", "\n=== Running DataBinding Example ===")
        val dataBindingExample = DataBindingExample()
        dataBindingExample.runDataBindingExample()
        
        Log.d("JetpackExample", "\n=== JetpackMain.runAllExamples completed ===")
    }
}

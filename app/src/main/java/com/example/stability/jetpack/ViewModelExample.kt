package com.example.stability.jetpack

import android.util.Log

/**
 * ViewModel 示例
 * 作用：在配置变更（如屏幕旋转）时保存 UI 相关的数据
 */
class ViewModelExample {
    
    /**
     * 运行 ViewModel 示例
     */
    fun runViewModelExample() {
        Log.d("JetpackExample", "=== ViewModelExample.runViewModelExample called ===")
        
        // 创建 ViewModel 实例
        val viewModel = CounterViewModel()
        
        // 模拟用户交互
        Log.d("JetpackExample", "初始计数: ${viewModel.count}")
        
        viewModel.increment()
        Log.d("JetpackExample", "增加后计数: ${viewModel.count}")
        
        viewModel.decrement()
        Log.d("JetpackExample", "减少后计数: ${viewModel.count}")
        
        // 测试 ViewModelProvider.Factory
        val factory = CounterViewModelFactory(100)
        val customViewModel = factory.create(CounterViewModel::class.java)
        Log.d("JetpackExample", "自定义初始值: ${customViewModel.count}")
        
        Log.d("JetpackExample", "=== ViewModelExample.runViewModelExample completed ===")
    }
}

/**
 * 模拟 ViewModel 基类
 */
open class ViewModel {
    open fun onCleared() {
        // 默认实现
    }
}

/**
 * 模拟 ViewModelProvider.Factory
 */
interface ViewModelProviderFactory {
    fun <T : ViewModel> create(modelClass: Class<T>): T
}

/**
 * 计数器 ViewModel
 */
class CounterViewModel(private val initialCount: Int = 0) : ViewModel() {
    var count: Int = initialCount
        private set
    
    fun increment() {
        count++
        Log.d("JetpackExample", "CounterViewModel.increment() called, count: $count")
    }
    
    fun decrement() {
        count--
        Log.d("JetpackExample", "CounterViewModel.decrement() called, count: $count")
    }
    
    override fun onCleared() {
        super.onCleared()
        Log.d("JetpackExample", "CounterViewModel.onCleared() called")
    }
}

/**
 * ViewModel 工厂类
 */
class CounterViewModelFactory(private val initialCount: Int) : ViewModelProviderFactory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CounterViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CounterViewModel(initialCount) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

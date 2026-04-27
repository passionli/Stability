package com.example.stability.jetpack

import android.util.Log

/**
 * DataBinding 示例
 * 作用：将 UI 组件与数据模型绑定
 */
class DataBindingExample {
    
    /**
     * 运行 DataBinding 示例
     */
    fun runDataBindingExample() {
        Log.d("JetpackExample", "=== DataBindingExample.runDataBindingExample called ===")
        
        // 创建 User 实例
        val user = UserObservable("John Doe", 30, "john@example.com")
        
        // 模拟数据变化
        Log.d("JetpackExample", "初始用户: ${user.name}, ${user.age}, ${user.email}")
        
        user.name = "Jane Smith"
        Log.d("JetpackExample", "更新后用户: ${user.name}, ${user.age}, ${user.email}")
        
        user.age = 25
        Log.d("JetpackExample", "更新后用户: ${user.name}, ${user.age}, ${user.email}")
        
        Log.d("JetpackExample", "=== DataBindingExample.runDataBindingExample completed ===")
    }
}

/**
 * 模拟 BaseObservable 基类
 */
open class BaseObservable {
    private val propertyChangeListeners = mutableListOf<(Int) -> Unit>()
    
    fun addOnPropertyChangedCallback(callback: (Int) -> Unit) {
        propertyChangeListeners.add(callback)
    }
    
    fun removeOnPropertyChangedCallback(callback: (Int) -> Unit) {
        propertyChangeListeners.remove(callback)
    }
    
    protected fun notifyPropertyChanged(propertyId: Int) {
        propertyChangeListeners.forEach { it(propertyId) }
    }
}

/**
 * 模拟 BR 类
 */
object BR {
    const val name = 1
    const val age = 2
    const val email = 3
}

/**
 * 可观察的用户类
 */
class UserObservable(
    name: String,
    age: Int,
    email: String
) : BaseObservable() {
    
    var name: String = name
        set(value) {
            field = value
            notifyPropertyChanged(BR.name)
            Log.d("JetpackExample", "UserObservable.name set to: $value")
        }
    
    var age: Int = age
        set(value) {
            field = value
            notifyPropertyChanged(BR.age)
            Log.d("JetpackExample", "UserObservable.age set to: $value")
        }
    
    var email: String = email
        set(value) {
            field = value
            notifyPropertyChanged(BR.email)
            Log.d("JetpackExample", "UserObservable.email set to: $value")
        }
}

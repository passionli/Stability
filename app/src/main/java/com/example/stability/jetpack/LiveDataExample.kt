package com.example.stability.jetpack

import android.util.Log

/**
 * LiveData 示例
 * 作用：具有生命周期感知能力的可观察数据持有者
 */
class LiveDataExample {
    
    /**
     * 运行 LiveData 示例
     */
    fun runLiveDataExample() {
        Log.d("JetpackExample", "=== LiveDataExample.runLiveDataExample called ===")
        
        // 创建 UserViewModel 实例
        val userViewModel = UserViewModel()
        
        // 模拟观察数据变化
        Log.d("JetpackExample", "初始用户名: ${userViewModel.username.value}")
        
        // 模拟用户更新
        userViewModel.updateUsername("John Doe")
        Log.d("JetpackExample", "更新后用户名: ${userViewModel.username.value}")
        
        userViewModel.updateAge(30)
        Log.d("JetpackExample", "更新后年龄: ${userViewModel.age.value}")
        
        // 测试转换后的 LiveData
        Log.d("JetpackExample", "用户信息: ${userViewModel.userInfo.value}")
        
        Log.d("JetpackExample", "=== LiveDataExample.runLiveDataExample completed ===")
    }
}

/**
 * 模拟 LiveData 基类
 */
open class LiveData<T> {
    private val observers = mutableListOf<(T?) -> Unit>()
    protected var _value: T? = null
    
    open val value: T? get() = _value
    
    fun observeForever(observer: (T?) -> Unit) {
        observers.add(observer)
        observer(_value)
    }
    
    protected fun setValueInternal(newValue: T?) {
        _value = newValue
        observers.forEach { it(newValue) }
    }
}

/**
 * 模拟 MutableLiveData 类
 */
class MutableLiveData<T>(initialValue: T? = null) : LiveData<T>() {
    init {
        _value = initialValue
    }
    
    override var value: T?
        get() = super.value
        set(value) = setValueInternal(value)
}

/**
 * 模拟 Observer 接口
 */
typealias Observer<T> = (T?) -> Unit

/**
 * 用户 ViewModel
 */
class UserViewModel : ViewModel() {
    // MutableLiveData 是 LiveData 的可变子类
    private val _username = MutableLiveData<String>("Guest")
    private val _age = MutableLiveData<Int>(18)
    
    // 对外暴露不可变的 LiveData
    val username: LiveData<String> = _username
    val age: LiveData<Int> = _age
    
    // 转换后的 LiveData
    val userInfo: LiveData<String> = username.combine(
        age
    ) { name, age -> "$name, $age years old" }
    
    fun updateUsername(newName: String) {
        _username.value = newName
        Log.d("JetpackExample", "UserViewModel.updateUsername() called, newName: $newName")
    }
    
    fun updateAge(newAge: Int) {
        _age.value = newAge
        Log.d("JetpackExample", "UserViewModel.updateAge() called, newAge: $newAge")
    }
}

/**
 * LiveData 扩展函数，用于组合多个 LiveData
 */
fun <T1, T2, R> LiveData<T1>.combine(
    other: LiveData<T2>,
    transform: (T1?, T2?) -> R
): LiveData<R> {
    return MutableLiveData<R>().apply {
        val observer1: (T1?) -> Unit = { value = transform(it, other.value) }
        val observer2: (T2?) -> Unit = { value = transform(this@combine.value, it) }
        
        this@combine.observeForever(observer1)
        other.observeForever(observer2)
        
        // 初始值
        value = transform(this@combine.value, other.value)
    }
}

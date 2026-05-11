# Jetpack Activity 完整示例教程

## 目录

1. [项目概述](#项目概述)
2. [Activity 基础架构](#activity-基础架构)
3. [ViewModel 集成](#viewmodel-集成)
4. [LiveData 观察模式](#livedata-观察模式)
5. [协程与线程切换](#协程与线程切换)
6. [加载状态管理](#加载状态管理)
7. [完整代码解析](#完整代码解析)
8. [最佳实践](#最佳实践)

---

## 项目概述

本教程基于 `JetpackActivity.kt` 完整示例，展示如何在 Android 应用中正确使用 Jetpack 组件，包括：

- **AppCompatActivity**：Jetpack 提供的基础 Activity 类
- **ViewModel**：管理 UI 相关数据
- **LiveData**：生命周期感知的数据观察
- **LifecycleScope**：与生命周期绑定的协程作用域
- **Coroutines**：异步任务处理

---

## Activity 基础架构

### 1. Activity 类结构

```kotlin
class JetpackActivity : AppCompatActivity() {
    
    // 视图成员变量
    private lateinit var tvCount: TextView
    private lateinit var btnIncrement: Button
    private lateinit var btnReset: Button
    private lateinit var btnTestCoroutine: Button
    private lateinit var progressBar: ProgressBar
    
    // ViewModel
    private lateinit var viewModel: JetpackViewModel
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_jetpack)
        
        // 初始化视图
        initViews()
        
        // 初始化 ViewModel
        initViewModel()
        
        // 设置事件监听
        setupListeners()
    }
}
```

### 2. 视图初始化

```kotlin
private fun initViews() {
    tvCount = findViewById(R.id.tvCount)
    btnIncrement = findViewById(R.id.btnIncrement)
    btnReset = findViewById(R.id.btnReset)
    btnTestCoroutine = findViewById(R.id.btnTestCoroutine)
    progressBar = findViewById(R.id.progressBar)
}
```

### 3. onCreate 生命周期

`onCreate` 是 Activity 创建时的回调方法，主要完成以下工作：
- 设置布局文件
- 初始化视图组件
- 创建 ViewModel 实例
- 观察 LiveData 数据
- 设置事件监听器

---

## ViewModel 集成

### 1. ViewModel 定义

```kotlin
class JetpackViewModel : androidx.lifecycle.ViewModel() {
    
    // 内部可变的 LiveData
    private val _count = androidx.lifecycle.MutableLiveData(0)
    private val _isLoading = androidx.lifecycle.MutableLiveData(false)
    private val _message = androidx.lifecycle.MutableLiveData<String>()
    
    // 对外暴露不可变的 LiveData
    val count = _count
    val isLoading = _isLoading
    val message = _message
    
    // 业务方法
    fun incrementCount() { /* ... */ }
    fun resetCount() { /* ... */ }
    fun testCoroutine(activity: JetpackActivity) { /* ... */ }
}
```

### 2. ViewModel 获取方式

```kotlin
// 在 Activity 中获取 ViewModel
viewModel = ViewModelProvider(this).get(JetpackViewModel::class.java)
```

**关键特点**：
- ViewModel 的生命周期与 Activity 一致
- 屏幕旋转时 ViewModel 不会被销毁
- 避免内存泄漏，不持有 Context 引用

---

## LiveData 观察模式

### 1. 观察 count 数据

```kotlin
viewModel.count.observe(this) {
    Log.d("JetpackActivity", "count LiveData changed: $it")
    tvCount.text = "Count: $it"
}
```

### 2. 观察加载状态

```kotlin
viewModel.isLoading.observe(this) {
    Log.d("JetpackActivity", "isLoading LiveData changed: $it")
    btnIncrement.isEnabled = !it
    btnReset.isEnabled = !it
    progressBar.visibility = if (it) View.VISIBLE else View.GONE
}
```

### 3. 观察消息提示

```kotlin
viewModel.message.observe(this) {
    Log.d("JetpackActivity", "message LiveData changed: $it")
    if (!it.isNullOrEmpty()) {
        Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
    }
}
```

### LiveData 数据流图

```
ViewModel                  Activity
    |                          |
    |-- _count ---observe--->  |---> 更新 UI (TextView)
    |                          |
    |-- _isLoading -observe->  |---> 控制按钮状态和进度条
    |                          |
    |-- _message ---observe--> |---> 显示 Toast 消息
```

---

## 协程与线程切换

### 1. 使用 lifecycleScope 启动协程

```kotlin
fun testCoroutine(activity: JetpackActivity) {
    _isLoading.value = true
    _message.value = "Testing coroutine..."
    
    // 使用 activity 的 lifecycleScope 启动协程
    activity.lifecycleScope.launch {
        try {
            // 主线程执行
            Log.d("JetpackViewModel", "Coroutine: Executing in main thread")
            
            // 切换到 IO 线程
            withContext(Dispatchers.IO) {
                Log.d("JetpackViewModel", "Coroutine: Switched to IO thread")
                delay(2000) // 模拟耗时操作
                Log.d("JetpackViewModel", "Coroutine: Delay completed")
            }
            
            // 自动切回主线程
            _message.value = "Coroutine test completed!"
        } catch (e: Exception) {
            Log.e("JetpackViewModel", "Coroutine error", e)
            _message.value = "Error: ${e.message}"
        } finally {
            _isLoading.value = false
        }
    }
}
```

### 2. 线程切换流程

```
主线程                      IO线程                    主线程
    |                          |                          |
    |-- launch 启动协程 --->    |                          |
    |                          |                          |
    |                          |-- withContext(IO) --->    |
    |                          |    delay(2000)            |
    |                          |                          |
    |<-- 自动切换回主线程 ------|                          |
    |                          |                          |
    |-- 更新 UI ----------------|--------------------------|
```

### 3. Dispatchers 说明

| Dispatcher | 用途 |
|------------|------|
| `Dispatchers.Main` | 主线程，用于更新 UI |
| `Dispatchers.IO` | IO 操作（网络请求、数据库操作、文件读写） |
| `Dispatchers.Default` | CPU 密集型操作（计算、排序等） |

---

## 加载状态管理

### 状态管理流程

```kotlin
// 开始加载
_isLoading.value = true
message.value = "Testing coroutine..."

try {
    // 执行耗时操作
    withContext(Dispatchers.IO) {
        delay(2000)
    }
    message.value = "Coroutine test completed!"
} catch (e: Exception) {
    message.value = "Error: ${e.message}"
} finally {
    // 结束加载
    _isLoading.value = false
}
```

### UI 响应逻辑

```kotlin
viewModel.isLoading.observe(this) { isLoading ->
    btnIncrement.isEnabled = !isLoading  // 禁用按钮
    btnReset.isEnabled = !isLoading      // 禁用按钮
    progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
}
```

---

## 完整代码解析

### 1. Activity 核心代码

```kotlin
class JetpackActivity : AppCompatActivity() {
    
    private lateinit var viewModel: JetpackViewModel
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_jetpack)
        
        // 初始化视图
        tvCount = findViewById(R.id.tvCount)
        btnIncrement = findViewById(R.id.btnIncrement)
        btnReset = findViewById(R.id.btnReset)
        btnTestCoroutine = findViewById(R.id.btnTestCoroutine)
        progressBar = findViewById(R.id.progressBar)
        
        // 获取 ViewModel
        viewModel = ViewModelProvider(this).get(JetpackViewModel::class.java)
        
        // 观察 LiveData
        viewModel.count.observe(this) { tvCount.text = "Count: $it" }
        viewModel.isLoading.observe(this) { updateLoadingState(it) }
        viewModel.message.observe(this) { showMessage(it) }
        
        // 设置点击事件
        btnIncrement.setOnClickListener { viewModel.incrementCount() }
        btnReset.setOnClickListener { viewModel.resetCount() }
        btnTestCoroutine.setOnClickListener { viewModel.testCoroutine(this) }
    }
}
```

### 2. ViewModel 核心代码

```kotlin
class JetpackViewModel : androidx.lifecycle.ViewModel() {
    
    private val _count = androidx.lifecycle.MutableLiveData(0)
    val count = _count
    
    fun incrementCount() {
        _count.value = _count.value?.plus(1)
        _message.value = "Count incremented!"
    }
    
    fun resetCount() {
        _count.value = 0
        _message.value = "Count reset!"
    }
    
    fun testCoroutine(activity: JetpackActivity) {
        _isLoading.value = true
        
        activity.lifecycleScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    delay(2000)
                }
                _message.value = "Coroutine test completed!"
            } catch (e: Exception) {
                _message.value = "Error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
```

---

## 最佳实践

### 1. ViewModel 最佳实践

```kotlin
// 正确：使用私有可变 LiveData，公开不可变 LiveData
class GoodViewModel : ViewModel() {
    private val _data = MutableLiveData<String>()
    val data: LiveData<String> = _data  // 对外只读
    
    fun updateData(newData: String) {
        _data.value = newData
    }
}
```

### 2. 协程最佳实践

```kotlin
// 使用 lifecycleScope 确保协程与生命周期同步
class MyActivity : AppCompatActivity() {
    fun fetchData() {
        lifecycleScope.launch {
            // 协程会在 Activity 销毁时自动取消
            val result = withContext(Dispatchers.IO) {
                // 网络请求
            }
            // 更新 UI
        }
    }
}
```

### 3. 状态管理最佳实践

```kotlin
// 使用密封类管理状态
sealed class DataState<out T> {
    object Loading : DataState<Nothing>()
    data class Success<out T>(val data: T) : DataState<T>()
    data class Error(val message: String) : DataState<Nothing>()
}

class MyViewModel : ViewModel() {
    private val _state = MutableLiveData<DataState<String>>()
    val state = _state
    
    fun loadData() {
        _state.value = DataState.Loading
        viewModelScope.launch {
            try {
                val data = fetchFromNetwork()
                _state.value = DataState.Success(data)
            } catch (e: Exception) {
                _state.value = DataState.Error(e.message ?: "Unknown error")
            }
        }
    }
}
```

### 4. 避免内存泄漏

```kotlin
// 错误示例：ViewModel 持有 Context
class BadViewModel(context: Context) : ViewModel() {
    // 这会导致内存泄漏！
}

// 正确示例：不持有 Context
class GoodViewModel : ViewModel() {
    // 使用 Application Context（如果需要）
    // 或通过其他方式获取所需资源
}
```

---

## 总结

通过 `JetpackActivity.kt` 示例，我们学习了：

1. **Activity 结构**：如何组织 Activity 代码
2. **ViewModel 使用**：数据持久化和配置变更处理
3. **LiveData 观察**：生命周期感知的数据更新
4. **协程实践**：线程切换和异步任务处理
5. **状态管理**：加载状态和错误处理

这些模式是现代 Android 开发的核心，掌握它们可以帮助你构建更健壮、更可维护的应用程序。

---

*本文档基于 Stability 项目中的 JetpackActivity.kt 整理，可作为实际开发参考。*

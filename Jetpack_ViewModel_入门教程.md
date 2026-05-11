# Jetpack ViewModel 入门与实践

## 目录

1. [Jetpack 简介](#jetpack-简介)
2. [ViewModel 详解](#viewmodel-详解)
3. [LiveData 详解](#livedata-详解)
4. [DataBinding 详解](#databinding-详解)
5. [ViewModel 与 LiveData 配合使用](#viewmodel-与-livedata-配合使用)
6. [ViewModelProvider.Factory 用法](#viewmodelproviderfactory-用法)
7. [最佳实践](#最佳实践)

---

## Jetpack 简介

Jetpack 是 Google 提供的一套 Android 组件库，旨在帮助开发者更高效地构建优质应用。Jetpack 包含多个组件，其中 ViewModel、LiveData 和 DataBinding 是实现 MVVM 架构的核心组件。

### 主要组件

- **ViewModel**：负责管理 UI 相关的数据，在配置变更时保留数据
- **LiveData**：具有生命周期感知能力的可观察数据持有者
- **DataBinding**：将 UI 组件与数据模型绑定，实现声明式编程

---

## ViewModel 详解

### 1. ViewModel 的作用

ViewModel 是一种用于存储和管理 UI 相关数据的类。它能够在配置变更（如屏幕旋转）时保留数据，与传统的 onSaveInstanceState 相比更加简单和高效。

### 2. 基础 ViewModel 实现

```kotlin
/**
 * 模拟 ViewModel 基类
 */
open class ViewModel {
    open fun onCleared() {
        // 默认实现
    }
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
```

### 3. 使用 ViewModel

```kotlin
class ViewModelExample {
    
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
        
        Log.d("JetpackExample", "=== ViewModelExample.runViewModelExample completed ===")
    }
}
```

### 4. ViewModel 的生命周期

ViewModel 的生命周期与 Activity 或 Fragment 的生命周期相关联。当 Activity 因配置变更而被销毁时，ViewModel 不会被销毁，只有当 Activity 完全结束（调用 onDestroy 且不是因为配置变更）时，ViewModel 才会被调用 onCleared 方法。

---

## LiveData 详解

### 1. LiveData 的作用

LiveData 是一种具有生命周期感知能力的可观察数据持有者。它遵循应用组件的生命周期（如 Activity、Fragment、Service），只有在组件处于活跃状态时才会更新数据。

### 2. 基础 LiveData 实现

```kotlin
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
```

### 3. LiveData 的两种类型

- **MutableLiveData**：可变的 LiveData，用于内部数据存储
- **LiveData**：不可变的 LiveData，用于对外暴露数据

```kotlin
class UserViewModel : ViewModel() {
    // MutableLiveData 是 LiveData 的可变子类
    private val _username = MutableLiveData<String>("Guest")
    private val _age = MutableLiveData<Int>(18)
    
    // 对外暴露不可变的 LiveData
    val username: LiveData<String> = _username
    val age: LiveData<Int> = _age
    
    fun updateUsername(newName: String) {
        _username.value = newName
        Log.d("JetpackExample", "UserViewModel.updateUsername() called, newName: $newName")
    }
    
    fun updateAge(newAge: Int) {
        _age.value = newAge
        Log.d("JetpackExample", "UserViewModel.updateAge() called, newAge: $newAge")
    }
}
```

### 4. LiveData 转换

可以使用 map 和 switchMap 等转换函数对 LiveData 进行转换。

```kotlin
// 转换后的 LiveData
val userInfo: LiveData<String> = username.combine(
    age
) { name, age -> "$name, $age years old" }
```

### 5. LiveData 组合

```kotlin
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
```

---

## DataBinding 详解

### 1. DataBinding 的作用

DataBinding 是 Google 推出的实现声明式绑定的库。它能够将 UI 组件与数据模型绑定，减少样板代码，提高开发效率。

### 2. BaseObservable 实现

```kotlin
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
```

### 3. 可观察属性

```kotlin
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
```

### 4. 使用 DataBinding

```kotlin
class DataBindingExample {
    
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
```

---

## ViewModel 与 LiveData 配合使用

### 1. 经典 MVVM 架构

ViewModel 负责管理数据，LiveData 用于观察数据变化，View（Activity/Fragment）负责展示数据。

```kotlin
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
```

### 2. 在 Activity 中使用

```kotlin
class MainActivity : AppCompatActivity() {
    
    private lateinit var viewModel: UserViewModel
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 获取 ViewModel 实例
        viewModel = ViewModelProvider(this)[UserViewModel::class.java]
        
        // 观察数据变化
        viewModel.username.observe(this) { name ->
            textView.text = name
        }
        
        viewModel.userInfo.observe(this) { info ->
            detailTextView.text = info
        }
    }
    
    fun updateUser() {
        viewModel.updateUsername("John Doe")
        viewModel.updateAge(30)
    }
}
```

---

## ViewModelProvider.Factory 用法

### 1. 为什么需要 Factory

当 ViewModel 需要初始化参数时，需要使用 Factory 来创建 ViewModel 实例。

### 2. Factory 实现

```kotlin
/**
 * 模拟 ViewModelProvider.Factory
 */
interface ViewModelProviderFactory {
    fun <T : ViewModel> create(modelClass: Class<T>): T
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
```

### 3. 使用 Factory 创建 ViewModel

```kotlin
class ViewModelExample {
    
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
```

---

## 最佳实践

### 1. 不要直接持有 View 或 Context

ViewModel 不应该持有任何 View 或 Context 的引用，这可能导致内存泄漏。

```kotlin
// 错误示例
class BadViewModel : ViewModel() {
    var activity: Activity? = null  // 不推荐
    var context: Context? = null    // 不推荐
}

// 正确示例
class GoodViewModel : ViewModel() {
    // 使用 LiveData 或其他生命周期安全的方式
    private val _data = MutableLiveData<String>()
    val data: LiveData<String> = _data
}
```

### 2. 使用 LiveData 暴露数据

永远不要直接暴露 MutableLiveData，使用不可变的 LiveData 对外暴露。

```kotlin
class GoodViewModel : ViewModel() {
    private val _username = MutableLiveData<String>("Guest")
    
    // 对外暴露不可变的 LiveData
    val username: LiveData<String> = _username
    
    fun updateUsername(name: String) {
        _username.value = name
    }
}
```

### 3. 合理划分 ViewModel 的职责

每个 ViewModel 应该只负责管理一组相关的数据。

```kotlin
// 分页管理的 ViewModel
class UserListViewModel : ViewModel() {
    private val _users = MutableLiveData<List<User>>()
    val users: LiveData<List<User>> = _users
    
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    fun loadUsers() {
        _isLoading.value = true
        // 加载数据逻辑
    }
}
```

### 4. 使用 Factory 传递初始化参数

```kotlin
class DetailViewModel(
    private val userId: String,
    private val repository: UserRepository
) : ViewModel() {
    
    private val _userDetail = MutableLiveData<UserDetail>()
    val userDetail: LiveData<UserDetail> = _userDetail
    
    fun loadDetail() {
        repository.getUserDetail(userId) { detail ->
            _userDetail.value = detail
        }
    }
}

class DetailViewModelFactory(
    private val userId: String,
    private val repository: UserRepository
) : ViewModelProviderFactory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DetailViewModel(userId, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
```

### 5. 在 onCleared 中清理资源

如果 ViewModel 中有协程或异步操作，在 onCleared 中取消它们。

```kotlin
class AsyncViewModel : ViewModel() {
    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.Main + job)
    
    fun loadData() {
        scope.launch {
            // 加载数据
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        job.cancel()  // 取消所有协程
    }
}
```

---

## 总结

通过本教程，你学习了 Jetpack ViewModel 生态系统的核心组件：

1. **ViewModel**：负责管理 UI 相关的数据，在配置变更时保留数据
2. **LiveData**：具有生命周期感知能力的可观察数据持有者
3. **DataBinding**：将 UI 组件与数据模型绑定
4. **ViewModelProvider.Factory**：用于创建带参数的 ViewModel 实例

合理使用这些组件可以帮助你构建更加清晰、可维护的 Android 应用。

---

*本文档基于 Stability 项目中的 ViewModelExample.kt、LiveDataExample.kt 和 DataBindingExample.kt 整理，可作为实际开发参考。*

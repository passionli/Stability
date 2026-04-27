package com.example.stability
// 包声明，指定当前文件所在的包名

import android.os.Bundle
// 导入 Bundle 类，用于在 Activity 之间传递数据
import android.util.Log
// 导入 Log 类，用于打印日志
import android.view.View
// 导入 View 类，用于设置视图的可见性
import android.widget.Button
// 导入 Button 类，用于处理按钮点击事件
import android.widget.ProgressBar
// 导入 ProgressBar 类，用于显示加载状态
import android.widget.TextView
// 导入 TextView 类，用于显示文本
import android.widget.Toast
// 导入 Toast 类，用于显示短消息提示
import androidx.appcompat.app.AppCompatActivity
// 导入 AppCompatActivity 类，这是 Jetpack 提供的基础 Activity 类
import androidx.lifecycle.ViewModelProvider
// 导入 ViewModelProvider 类，用于创建和获取 ViewModel 实例
import androidx.lifecycle.lifecycleScope
// 导入 lifecycleScope，这是与 Activity 生命周期绑定的协程作用域
import kotlinx.coroutines.Dispatchers
// 导入 Dispatchers 类，用于指定协程运行的线程
import kotlinx.coroutines.delay
// 导入 delay 函数，用于在协程中添加延迟
import kotlinx.coroutines.launch
// 导入 launch 函数，用于启动协程
import kotlinx.coroutines.withContext
// 导入 withContext 函数，用于在协程中切换线程

class JetpackActivity : AppCompatActivity() {
    // 创建一个继承自 AppCompatActivity 的类
    
    private lateinit var tvCount: TextView
    // 声明一个 TextView 类型的变量，用于显示计数
    // lateinit 表示延迟初始化，在 onCreate 方法中才会赋值
    
    private lateinit var btnIncrement: Button
    // 声明一个 Button 类型的变量，用于增加计数
    
    private lateinit var btnReset: Button
    // 声明一个 Button 类型的变量，用于重置计数
    
    private lateinit var btnTestCoroutine: Button
    // 声明一个 Button 类型的变量，用于测试协程
    
    private lateinit var progressBar: ProgressBar
    // 声明一个 ProgressBar 类型的变量，用于显示加载状态
    
    private lateinit var viewModel: JetpackViewModel
    // 声明一个 JetpackViewModel 类型的变量，用于管理 UI 相关的数据
    
    override fun onCreate(savedInstanceState: Bundle?) {
        // 重写 onCreate 方法，这是 Activity 创建时的回调方法
        // savedInstanceState 参数用于保存和恢复 Activity 的状态
        
        Log.d("JetpackActivity", "onCreate called: this=$this, savedInstanceState=$savedInstanceState")
        // 记录 onCreate 方法的调用，包括 this 对象和 savedInstanceState 参数
        Log.d("JetpackActivity", "Call stack:")
        // 打印调用堆栈
        Thread.currentThread().stackTrace.forEachIndexed { index, element ->
            if (index > 2) { // 跳过前 3 行（Thread.getStackTrace 本身的调用）
                Log.d("JetpackActivity", "  $index: ${element.className}.${element.methodName}(${element.fileName}:${element.lineNumber})")
            }
        }
        
        super.onCreate(savedInstanceState)
        // 调用父类的 onCreate 方法，必须调用
        
        setContentView(R.layout.activity_jetpack)
        // 设置 Activity 的布局文件
        Log.d("JetpackActivity", "setContentView completed")
        // 记录布局设置完成
        
        // 初始化视图
        tvCount = findViewById(R.id.tvCount)
        // 通过 findViewById 方法获取布局文件中的 TextView 实例
        Log.d("JetpackActivity", "tvCount initialized: $tvCount")
        // 记录 tvCount 初始化完成
        
        btnIncrement = findViewById(R.id.btnIncrement)
        // 通过 findViewById 方法获取布局文件中的增加计数按钮实例
        Log.d("JetpackActivity", "btnIncrement initialized: $btnIncrement")
        // 记录 btnIncrement 初始化完成
        
        btnReset = findViewById(R.id.btnReset)
        // 通过 findViewById 方法获取布局文件中的重置计数按钮实例
        Log.d("JetpackActivity", "btnReset initialized: $btnReset")
        // 记录 btnReset 初始化完成
        
        btnTestCoroutine = findViewById(R.id.btnTestCoroutine)
        // 通过 findViewById 方法获取布局文件中的测试协程按钮实例
        Log.d("JetpackActivity", "btnTestCoroutine initialized: $btnTestCoroutine")
        // 记录 btnTestCoroutine 初始化完成
        
        progressBar = findViewById(R.id.progressBar)
        // 通过 findViewById 方法获取布局文件中的 ProgressBar 实例
        Log.d("JetpackActivity", "progressBar initialized: $progressBar")
        // 记录 progressBar 初始化完成
        
        // 初始化 ViewModel
        viewModel = ViewModelProvider(this).get(JetpackViewModel::class.java)
        // 使用 ViewModelProvider 创建或获取 JetpackViewModel 实例
        // 这样做的好处是 ViewModel 的生命周期与 Activity 一致，不会因为屏幕旋转等操作而销毁
        Log.d("JetpackActivity", "viewModel initialized: $viewModel")
        // 记录 viewModel 初始化完成
        
        // 观察 LiveData
        viewModel.count.observe(this) {
            // 观察 ViewModel 中的 count LiveData
            // 当 count 的值发生变化时，会执行这里的代码
            Log.d("JetpackActivity", "count LiveData changed: $it")
            // 记录 count 值的变化
            tvCount.text = "Count: $it"
            // 更新 TextView 的文本，显示当前计数
        }
        
        viewModel.isLoading.observe(this) {
            // 观察 ViewModel 中的 isLoading LiveData
            // 当 isLoading 的值发生变化时，会执行这里的代码
            Log.d("JetpackActivity", "isLoading LiveData changed: $it")
            // 记录 isLoading 值的变化
            btnIncrement.isEnabled = !it
            // 根据 isLoading 的值设置增加计数按钮是否可用
            btnReset.isEnabled = !it
            // 根据 isLoading 的值设置重置计数按钮是否可用
            progressBar.visibility = if (it) View.VISIBLE else View.GONE
            // 根据 isLoading 的值设置 ProgressBar 是否可见
        }
        
        viewModel.message.observe(this) {
            // 观察 ViewModel 中的 message LiveData
            // 当 message 的值发生变化时，会执行这里的代码
            Log.d("JetpackActivity", "message LiveData changed: $it")
            // 记录 message 值的变化
            if (!it.isNullOrEmpty()) {
                // 检查 message 是否不为空
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
                // 显示 Toast 消息，提示用户操作结果
            }
        }
        
        // 设置按钮点击事件
        btnIncrement.setOnClickListener {
            // 为增加计数按钮设置点击事件监听器
            Log.d("JetpackActivity", "btnIncrement clicked")
            // 记录增加计数按钮被点击
            Log.d("JetpackActivity", "Call stack:")
            // 打印调用堆栈
            Thread.currentThread().stackTrace.forEachIndexed { index, element ->
                if (index > 2) { // 跳过前 3 行（Thread.getStackTrace 本身的调用）
                    Log.d("JetpackActivity", "  $index: ${element.className}.${element.methodName}(${element.fileName}:${element.lineNumber})")
                }
            }
            viewModel.incrementCount()
            // 调用 ViewModel 的 incrementCount 方法，增加计数
        }
        
        btnReset.setOnClickListener {
            // 为重置计数按钮设置点击事件监听器
            Log.d("JetpackActivity", "btnReset clicked")
            // 记录重置计数按钮被点击
            Log.d("JetpackActivity", "Call stack:")
            // 打印调用堆栈
            Thread.currentThread().stackTrace.forEachIndexed { index, element ->
                if (index > 2) { // 跳过前 3 行（Thread.getStackTrace 本身的调用）
                    Log.d("JetpackActivity", "  $index: ${element.className}.${element.methodName}(${element.fileName}:${element.lineNumber})")
                }
            }
            viewModel.resetCount()
            // 调用 ViewModel 的 resetCount 方法，重置计数
        }
        
        // 测试协程
        btnTestCoroutine.setOnClickListener {
            // 为测试协程按钮设置点击事件监听器
            Log.d("JetpackActivity", "btnTestCoroutine clicked")
            // 记录测试协程按钮被点击
            Log.d("JetpackActivity", "Call stack:")
            // 打印调用堆栈
            Thread.currentThread().stackTrace.forEachIndexed { index, element ->
                if (index > 2) { // 跳过前 3 行（Thread.getStackTrace 本身的调用）
                    Log.d("JetpackActivity", "  $index: ${element.className}.${element.methodName}(${element.fileName}:${element.lineNumber})")
                }
            }
            viewModel.testCoroutine(this)
            // 调用 ViewModel 的 testCoroutine 方法，测试协程功能
            // 传入 this 是为了获取 Activity 的 lifecycleScope
        }
        
        Log.d("JetpackActivity", "onCreate completed")
        // 记录 onCreate 方法执行完成
    }
}

class JetpackViewModel : androidx.lifecycle.ViewModel() {
    // 创建一个继承自 ViewModel 的类
    // ViewModel 用于存储和管理 UI 相关的数据，生命周期与 Activity 一致
    
    private val _count = androidx.lifecycle.MutableLiveData(0)
    // 创建一个 MutableLiveData 实例，用于存储计数
    // MutableLiveData 是一种可变的 LiveData，可以通过 value 属性修改其值
    
    val count = _count
    // 提供一个公开的 LiveData 引用，用于外部观察
    // 这样做的好处是外部只能观察，不能直接修改值
    
    private val _isLoading = androidx.lifecycle.MutableLiveData(false)
    // 创建一个 MutableLiveData 实例，用于存储加载状态
    
    val isLoading = _isLoading
    // 提供一个公开的 LiveData 引用，用于外部观察
    
    private val _message = androidx.lifecycle.MutableLiveData<String>()
    // 创建一个 MutableLiveData 实例，用于存储消息
    
    val message = _message
    // 提供一个公开的 LiveData 引用，用于外部观察
    
    fun incrementCount() {
        // 定义一个方法，用于增加计数
        Log.d("JetpackViewModel", "incrementCount called")
        // 记录 incrementCount 方法被调用
        Log.d("JetpackViewModel", "Call stack:")
        // 打印调用堆栈
        Thread.currentThread().stackTrace.forEachIndexed { index, element ->
            if (index > 2) { // 跳过前 3 行（Thread.getStackTrace 本身的调用）
                Log.d("JetpackViewModel", "  $index: ${element.className}.${element.methodName}(${element.fileName}:${element.lineNumber})")
            }
        }
        
        val oldValue = _count.value
        // 获取当前计数的旧值
        Log.d("JetpackViewModel", "Current count value: $oldValue")
        // 记录当前计数的值
        
        _count.value = _count.value?.plus(1)
        // 获取当前计数的值，加 1 后再设置回去
        // ?. 是安全调用操作符，如果 _count.value 为 null，则不会执行 plus(1) 操作
        
        val newValue = _count.value
        // 获取更新后的计数新值
        Log.d("JetpackViewModel", "New count value: $newValue")
        // 记录更新后的计数的值
        
        _message.value = "Count incremented!"
        // 设置消息为 "Count incremented!"
        Log.d("JetpackViewModel", "Message set to: Count incremented!")
        // 记录消息设置完成
    }
    
    fun resetCount() {
        // 定义一个方法，用于重置计数
        Log.d("JetpackViewModel", "resetCount called")
        // 记录 resetCount 方法被调用
        Log.d("JetpackViewModel", "Call stack:")
        // 打印调用堆栈
        Thread.currentThread().stackTrace.forEachIndexed { index, element ->
            if (index > 2) { // 跳过前 3 行（Thread.getStackTrace 本身的调用）
                Log.d("JetpackViewModel", "  $index: ${element.className}.${element.methodName}(${element.fileName}:${element.lineNumber})")
            }
        }
        
        val oldValue = _count.value
        // 获取当前计数的旧值
        Log.d("JetpackViewModel", "Current count value: $oldValue")
        // 记录当前计数的值
        
        _count.value = 0
        // 将计数设置为 0
        Log.d("JetpackViewModel", "Count reset to: 0")
        // 记录计数重置完成
        
        _message.value = "Count reset!"
        // 设置消息为 "Count reset!"
        Log.d("JetpackViewModel", "Message set to: Count reset!")
        // 记录消息设置完成
    }
    
    fun testCoroutine(activity: JetpackActivity) {
        // 定义一个方法，用于测试协程
        // 传入 activity 是为了获取其 lifecycleScope
        Log.d("JetpackViewModel", "testCoroutine called: activity=$activity")
        // 记录 testCoroutine 方法被调用，包括 activity 参数
        Log.d("JetpackViewModel", "Call stack:")
        // 打印调用堆栈
        Thread.currentThread().stackTrace.forEachIndexed { index, element ->
            if (index > 2) { // 跳过前 3 行（Thread.getStackTrace 本身的调用）
                Log.d("JetpackViewModel", "  $index: ${element.className}.${element.methodName}(${element.fileName}:${element.lineNumber})")
            }
        }
        
        _isLoading.value = true
        // 设置加载状态为 true
        Log.d("JetpackViewModel", "isLoading set to: true")
        // 记录加载状态设置完成
        
        _message.value = "Testing coroutine..."
        // 设置消息为 "Testing coroutine..."
        Log.d("JetpackViewModel", "Message set to: Testing coroutine...")
        // 记录消息设置完成
        
        // 使用 activity 的 lifecycleScope 启动协程
        activity.lifecycleScope.launch {
            // 启动一个协程，该协程的生命周期与 activity 一致
            // 当 activity 销毁时，协程也会自动取消
            Log.d("JetpackViewModel", "Coroutine started")
            // 记录协程启动
            
            try {
                // 尝试执行以下代码
                // 这里的代码在主线程执行
                Log.d("JetpackViewModel", "Coroutine: Executing in main thread")
                // 记录协程在主线程执行
                
                // 模拟网络请求
                Log.d("JetpackViewModel", "Coroutine: Before switching to IO thread, current thread: ${Thread.currentThread().name}")
                // 记录切换前的线程信息
                Log.d("JetpackViewModel", "Call stack before switching:")
                // 打印切换前的调用堆栈
                Thread.currentThread().stackTrace.forEachIndexed { index, element ->
                    if (index > 2) { // 跳过前 3 行（Thread.getStackTrace 本身的调用）
                        Log.d("JetpackViewModel", "  $index: ${element.className}.${element.methodName}(${element.fileName}:${element.lineNumber})")
                    }
                }
                
                withContext(Dispatchers.IO) {
                    // 切换到 IO 线程执行延迟操作
                    // 这部分代码不在主线程执行，而是在 IO 线程执行
                    // 这样不会阻塞主线程，保证 UI 的流畅性
                    Log.d("JetpackViewModel", "Coroutine: Switched to IO thread, current thread: ${Thread.currentThread().name}")
                    // 记录协程切换到 IO 线程
                    Log.d("JetpackViewModel", "Call stack in IO thread:")
                    // 打印 IO 线程中的调用堆栈
                    Thread.currentThread().stackTrace.forEachIndexed { index, element ->
                        if (index > 2) { // 跳过前 3 行（Thread.getStackTrace 本身的调用）
                            Log.d("JetpackViewModel", "  $index: ${element.className}.${element.methodName}(${element.fileName}:${element.lineNumber})")
                        }
                    }
                    
                    delay(2000)
                    // 延迟 2 秒，模拟网络请求的耗时
                    // 这行代码也不在主线程执行
                    Log.d("JetpackViewModel", "Coroutine: Delay completed in IO thread")
                    // 记录延迟完成
                }
                
                // withContext 执行完毕后，会自动切回主线程
                // 所以这里的代码又回到了主线程执行
                Log.d("JetpackViewModel", "Coroutine: Switched back to main thread, current thread: ${Thread.currentThread().name}")
                // 记录协程切回主线程
                Log.d("JetpackViewModel", "Call stack after switching back:")
                // 打印切换回主线程后的调用堆栈
                Thread.currentThread().stackTrace.forEachIndexed { index, element ->
                    if (index > 2) { // 跳过前 3 行（Thread.getStackTrace 本身的调用）
                        Log.d("JetpackViewModel", "  $index: ${element.className}.${element.methodName}(${element.fileName}:${element.lineNumber})")
                    }
                }
                
                _message.value = "Coroutine test completed!"
                // 延迟结束后，设置消息为 "Coroutine test completed!"
                Log.d("JetpackViewModel", "Message set to: Coroutine test completed!")
                // 记录消息设置完成
            } catch (e: Exception) {
                // 捕获异常
                // 这里的代码在主线程执行
                Log.e("JetpackViewModel", "Coroutine error", e)
                // 打印错误日志
                _message.value = "Error: ${e.message}"
                // 设置消息为错误信息
                Log.d("JetpackViewModel", "Message set to: Error: ${e.message}")
                // 记录错误消息设置完成
            } finally {
                // 无论是否发生异常，都会执行这里的代码
                // 这里的代码在主线程执行
                _isLoading.value = false
                // 设置加载状态为 false
                Log.d("JetpackViewModel", "isLoading set to: false")
                // 记录加载状态设置完成
                Log.d("JetpackViewModel", "Coroutine completed")
                // 记录协程执行完成
            }
        }
    }
}
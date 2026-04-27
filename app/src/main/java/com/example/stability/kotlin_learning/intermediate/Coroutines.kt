// package - 包声明关键字
// 作用：指定当前文件所在的包，用于组织代码结构
// 使用方式：package 包名
package com.example.stability.kotlin_learning.intermediate

// import - 导入语句关键字
// 作用：导入其他包中的类或函数，以便在当前文件中使用
// 使用方式：import 包名.类名
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withTimeout

/**
 * Kotlin 协程示例
 */
// class - 类声明关键字
// 作用：定义一个类，用于封装数据和行为
// 使用方式：class 类名 {
//     // 类成员
// }
class Coroutines {
    
    /**
     * 运行协程示例
     */
    // fun - 函数声明关键字
    // 作用：定义一个函数，用于执行特定的任务
    // 使用方式：fun 函数名(参数列表): 返回类型 {
    //     // 函数体
    // }
    fun runCoroutines() {
        Log.d("KotlinLearning", "=== Coroutines.runCoroutines called ===")
        Log.d("KotlinLearning", "Thread ID: ${Thread.currentThread().id}")
        
        // 打印调用堆栈
        Log.d("KotlinLearning", "Call stack:")
        Thread.currentThread().stackTrace.forEachIndexed { index, element ->
            if (index > 2) {
                Log.d("KotlinLearning", "  $index: ${element.className}.${element.methodName}(${element.fileName}:${element.lineNumber})")
            }
        }
        
        // 运行协程示例
        // runBlocking - 协程构建器
        // 作用：创建一个阻塞当前线程的协程作用域
        // 使用方式：runBlocking { 协程代码 }
        runBlocking {
            Log.d("KotlinLearning", "runBlocking started, thread: ${Thread.currentThread().id}")
            
            // 启动一个协程
            // val - 不可变变量声明关键字
            // 作用：声明一个不可变（只读）的变量，值一旦赋值就不能更改
            // 使用方式：val 变量名: 类型 = 值
            // launch - 协程构建器
            // 作用：启动一个新的协程
            // 使用方式：val job = launch { 协程代码 }
            val job1 = launch {
                Log.d("KotlinLearning", "Job 1 started, thread: ${Thread.currentThread().id}")
                doSomething("Job 1")
                Log.d("KotlinLearning", "Job 1 completed, thread: ${Thread.currentThread().id}")
            }
            
            // 启动另一个协程
            val job2 = launch {
                Log.d("KotlinLearning", "Job 2 started, thread: ${Thread.currentThread().id}")
                doSomething("Job 2")
                Log.d("KotlinLearning", "Job 2 completed, thread: ${Thread.currentThread().id}")
            }
            
            // 等待所有协程完成
            // join - 协程等待函数
            // 作用：等待协程执行完成
            // 使用方式：job.join()
            job1.join()
            job2.join()
            
            // 测试协程高级特性
            testAdvancedCoroutines()
            
            Log.d("KotlinLearning", "runBlocking completed, thread: ${Thread.currentThread().id}")
        }
        
        Log.d("KotlinLearning", "=== Coroutines.runCoroutines completed ===")
    }
    
    /**
     * 模拟耗时操作
     */
    // private - 访问修饰符关键字
    // 作用：限制函数或属性只能在当前类中访问
    // 使用方式：private fun 函数名() { ... }
    // suspend - 挂起函数关键字
    // 作用：标记一个函数为挂起函数，可以在协程中使用
    // 使用方式：suspend fun 函数名() { ... }
    private suspend fun doSomething(name: String) {
        Log.d("KotlinLearning", "doSomething called with name: $name, thread: ${Thread.currentThread().id}")
        
        // 模拟网络请求
        // delay - 挂起函数
        // 作用：暂停协程执行一段时间
        // 使用方式：delay(毫秒)
        delay(1000)
        Log.d("KotlinLearning", "$name: First delay completed")
        
        // 切换到 IO 线程
        // withContext - 挂起函数
        // 作用：在指定的调度器上执行代码
        // 使用方式：withContext(调度器) { 代码 }
        // Dispatchers.IO - IO 调度器
        // 作用：用于执行 IO 操作的调度器
        // 使用方式：Dispatchers.IO
        withContext(Dispatchers.IO) {
            Log.d("KotlinLearning", "$name: Switched to IO thread: ${Thread.currentThread().id}")
            delay(1000)
            Log.d("KotlinLearning", "$name: Second delay completed in IO thread")
        }
        
        Log.d("KotlinLearning", "$name: Back to original thread: ${Thread.currentThread().id}")
        Log.d("KotlinLearning", "doSomething completed for $name")
    }
    
    /**
     * 切换到指定调度器
     */
    // private - 访问修饰符关键字
    // 作用：限制函数或属性只能在当前类中访问
    // 使用方式：private fun 函数名() { ... }
    // suspend - 挂起函数关键字
    // 作用：标记一个函数为挂起函数，可以在协程中使用
    // 使用方式：suspend fun 函数名() { ... }
    // <T> - 泛型参数
    // 作用：使函数可以处理不同类型的数据
    // 使用方式：fun <T> 函数名(): T { ... }
    private suspend fun <T> withContext(dispatcher: kotlinx.coroutines.CoroutineDispatcher, block: suspend () -> T): T {
        return kotlinx.coroutines.withContext(dispatcher) {
            block()
        }
    }
    
    /**
     * 测试协程高级特性
     */
    private suspend fun testAdvancedCoroutines() {
        Log.d("KotlinLearning", "=== testAdvancedCoroutines called ===")
        
        // 测试 async/await
        testAsyncAwait()
        
        // 测试 coroutineScope
        testCoroutineScope()
        
        // 测试 supervisorScope
        testSupervisorScope()
        
        // 测试 withTimeout
        testWithTimeout()
        
        Log.d("KotlinLearning", "=== testAdvancedCoroutines completed ===")
    }
    
    /**
     * 测试 async/await
     */
    private suspend fun testAsyncAwait() {
        Log.d("KotlinLearning", "=== testAsyncAwait called ===")
        
        // async - 协程构建器
        // 作用：启动一个异步协程，返回 Deferred 对象
        // 使用方式：val deferred = async { 协程代码 }
        // await - 等待函数
        // 作用：等待 Deferred 对象完成并返回结果
        // 使用方式：val result = deferred.await()
        coroutineScope {
            val deferred1 = async {
                Log.d("KotlinLearning", "Async 1 started")
                delay(1000)
                Log.d("KotlinLearning", "Async 1 completed")
                42
            }
            
            val deferred2 = async {
                Log.d("KotlinLearning", "Async 2 started")
                delay(1500)
                Log.d("KotlinLearning", "Async 2 completed")
                "Hello"
            }
            
            val result1 = deferred1.await()
            val result2 = deferred2.await()
            Log.d("KotlinLearning", "Async results: $result1, $result2")
        }
        
        Log.d("KotlinLearning", "=== testAsyncAwait completed ===")
    }
    
    /**
     * 测试 coroutineScope
     */
    private suspend fun testCoroutineScope() {
        Log.d("KotlinLearning", "=== testCoroutineScope called ===")
        
        // coroutineScope - 协程作用域构建器
        // 作用：创建一个协程作用域，等待所有子协程完成
        // 使用方式：coroutineScope { 协程代码 }
        coroutineScope {
            launch {
                Log.d("KotlinLearning", "CoroutineScope launch 1 started")
                delay(1000)
                Log.d("KotlinLearning", "CoroutineScope launch 1 completed")
            }
            
            launch {
                Log.d("KotlinLearning", "CoroutineScope launch 2 started")
                delay(1500)
                Log.d("KotlinLearning", "CoroutineScope launch 2 completed")
            }
            
            Log.d("KotlinLearning", "CoroutineScope body")
        }
        
        Log.d("KotlinLearning", "=== testCoroutineScope completed ===")
    }
    
    /**
     * 测试 supervisorScope
     */
    private suspend fun testSupervisorScope() {
        Log.d("KotlinLearning", "=== testSupervisorScope called ===")
        
        // supervisorScope - 监督协程作用域构建器
        // 作用：创建一个监督协程作用域，子协程失败不会影响其他子协程
        // 使用方式：supervisorScope { 协程代码 }
        supervisorScope {
            launch {
                Log.d("KotlinLearning", "SupervisorScope launch 1 started")
                delay(1000)
                throw Exception("Test exception")
            }
            
            launch {
                Log.d("KotlinLearning", "SupervisorScope launch 2 started")
                delay(1500)
                Log.d("KotlinLearning", "SupervisorScope launch 2 completed")
            }
        }
        
        Log.d("KotlinLearning", "=== testSupervisorScope completed ===")
    }
    
    /**
     * 测试 withTimeout
     */
    private suspend fun testWithTimeout() {
        Log.d("KotlinLearning", "=== testWithTimeout called ===")
        
        // withTimeout - 超时函数
        // 作用：设置协程执行的超时时间
        // 使用方式：withTimeout(毫秒) { 协程代码 }
        try {
            withTimeout(1000) {
                Log.d("KotlinLearning", "withTimeout started")
                delay(1500)
                Log.d("KotlinLearning", "withTimeout completed")
            }
        } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
            Log.d("KotlinLearning", "Timeout exception caught: ${e.message}")
        }
        
        Log.d("KotlinLearning", "=== testWithTimeout completed ===")
    }
}

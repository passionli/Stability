package com.example.stability.kotlin_learning.intermediate

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/**
 * Flow 示例类，展示 kotlinx.coroutines.flow 的经典用法
 * 
 * Flow 是 Kotlin 协程中用于处理异步数据流的工具，类似于 RxJava 的 Observable
 * 但具有更简洁的 API 和更好的协程集成
 */
class FlowExample {
    
    /**
     * 基本的 Flow 创建和收集
     * 
     * 这是 Flow 最基础的用法，展示如何创建一个 Flow 并收集其发射的值
     */
    fun basicFlow() {
        println("=== 基本 Flow 创建和收集 ===")
        // runBlocking 用于在主线程中启动协程，方便我们直接运行示例
        runBlocking {
            // 使用 flow 构建器创建一个 Flow
            // flow 构建器接收一个 suspend 函数，在其中可以使用 emit() 发射值
            val flow = flow {
                // 模拟异步操作，每隔 100ms 发射一个值
                for (i in 1..5) {
                    delay(100) // 挂起函数，不会阻塞线程
                    emit(i) // 发射当前值
                }
            }
            
            // 收集 Flow 的值
            // collect 是一个挂起函数，会一直等待 Flow 完成
            flow.collect {
                println("收集到: $it")
            }
        }
    }
    
    /**
     * 使用 Flow 操作符
     * 
     * Flow 提供了丰富的操作符，用于转换、过滤和处理数据流
     */
    fun flowOperators() {
        println("\n=== Flow 操作符使用 ===")
        runBlocking {
            flow {
                // 发射 1-10 的数字
                for (i in 1..10) {
                    delay(50)
                    emit(i)
                }
            }
            // filter 操作符：过滤出满足条件的值（这里是偶数）
            .filter { it % 2 == 0 } 
            // map 操作符：将每个值转换为新的值（这里是原值的两倍）
            .map { it * 2 } 
            // take 操作符：只取前 N 个值
            .take(3) 
            // 收集处理后的值
            .collect {
                println("处理后: $it")
            }
        }
    }
    
    /**
     * 冷流特性演示
     * 
     * Flow 是冷流，这意味着只有当有人收集时，Flow 才会开始执行
     * 每次收集都会重新执行 Flow 的代码
     */
    fun coldFlowDemo() {
        println("\n=== 冷流特性演示 ===")
        // 创建一个 Flow，但此时不会执行任何代码
        val coldFlow = flow {
            println("Flow 开始执行")
            for (i in 1..3) {
                delay(100)
                emit(i)
            }
            println("Flow 执行结束")
        }
        
        runBlocking {
            println("第一次收集:")
            // 第一次收集，Flow 开始执行
            coldFlow.collect { println("收集到: $it") }
            
            println("\n第二次收集:")
            // 第二次收集，Flow 会重新执行
            coldFlow.collect { println("收集到: $it") }
        }
    }
    
    /**
     * 异常处理
     * 
     * Flow 提供了 catch 操作符来处理流中的异常
     */
    fun flowExceptionHandling() {
        println("\n=== 异常处理 ===")
        runBlocking {
            flow {
                for (i in 1..5) {
                    if (i == 3) {
                        // 模拟异常
                        throw Exception("出现错误")
                    }
                    emit(i)
                }
            }
            // catch 操作符：捕获流中的异常
            .catch { e ->
                println("捕获到异常: ${e.message}")
                // 可以在 catch 块中发射值，这样流就不会中断
                emit(-1) 
            }
            .collect {
                println("收集到: $it")
            }
        }
    }
    
    /**
     * 背压处理
     * 
     * 当 Flow 发射值的速度快于收集处理的速度时，会产生背压
     * buffer 操作符可以创建一个缓冲区来缓解背压
     */
    fun backpressureHandling() {
        println("\n=== 背压处理 ===")
        runBlocking {
            flow {
                // 快速发射值（每 50ms 一个）
                for (i in 1..10) {
                    delay(50) // 快速发射
                    emit(i)
                    println("发射: $i")
                }
            }
            // buffer 操作符：创建一个大小为 2 的缓冲区
            // 这样发射方可以继续发射，不需要等待收集方处理
            .buffer(2) 
            // 缓慢处理值（每 200ms 处理一个）
            .collect {
                delay(200) // 缓慢处理
                println("处理: $it")
            }
        }
    }
    
    /**
     * 组合多个 Flow
     * 
     * 使用 combine 操作符可以将多个 Flow 的值组合在一起
     */
    fun combineFlows() {
        println("\n=== 组合多个 Flow ===")
        runBlocking {
            // 第一个 Flow：先发射 1，100ms 后发射 2
            val flow1 = flow {
                emit(1)
                delay(100)
                emit(2)
            }
            
            // 第二个 Flow：50ms 后发射 "A"，再过 100ms 发射 "B"
            val flow2 = flow {
                delay(50)
                emit("A")
                delay(100)
                emit("B")
            }
            
            // combine 操作符：当任一 Flow 发射新值时，将两个 Flow 的最新值组合
            flow1.combine(flow2) { a, b ->
                "$a$b" // 将两个值组合成一个字符串
            }
            .collect {
                println("组合结果: $it")
            }
        }
    }
    
    /**
     * 状态流 (StateFlow)
     * 
     * StateFlow 是一种特殊的 Flow，用于表示应用状态
     * 它始终有一个初始值，并且只在值发生变化时才会通知收集器
     */
    fun stateFlowExample() {
        println("\n=== 状态流 (StateFlow) ===")
        runBlocking {
            // 创建一个初始值为 0 的 MutableStateFlow
            val stateFlow = MutableStateFlow(0)
            
            // 收集状态流
            // 注意：StateFlow 会立即发射当前值（0）
            stateFlow.collect {
                println("状态更新: $it")
            }
            
            // 更新状态
            stateFlow.value = 1 // 状态变化，会通知收集器
            stateFlow.value = 2 // 状态变化，会通知收集器
            stateFlow.value = 2 // 状态未变化，不会通知收集器
            stateFlow.value = 3 // 状态变化，会通知收集器
        }
    }
    
    /**
     * 共享流 (SharedFlow)
     * 
     * SharedFlow 是一种热流，可以被多个收集器共享
     * 它不会立即发射值，只有当调用 emit() 时才会发射
     */
    fun sharedFlowExample() {
        println("\n=== 共享流 (SharedFlow) ===")
        runBlocking {
            // 创建一个 MutableSharedFlow
            val sharedFlow = MutableSharedFlow<Int>()
            
            // 第一个收集器
            this.launch {
                sharedFlow.collect {
                    println("收集器1: $it")
                }
            }
            
            // 等待收集器准备就绪
            delay(100)
            
            // 发送一些值
            sharedFlow.emit(1) // 收集器1 会收到
            sharedFlow.emit(2) // 收集器1 会收到
            
            // 第二个收集器（迟到的收集器）
            this.launch {
                sharedFlow.collect {
                    println("收集器2: $it")
                }
            }
            
            // 等待收集器准备就绪
            delay(100)
            
            // 再发送一些值
            sharedFlow.emit(3) // 收集器1 和 2 都会收到
            sharedFlow.emit(4) // 收集器1 和 2 都会收到
        }
    }
    
    /**
     * 转换 Flow 为其他类型
     * 
     * Flow 提供了多种方法将流转换为其他类型，如列表、集合等
     */
    fun flowTransformations() {
        println("\n=== Flow 转换 ===")
        runBlocking {
            // 将 Flow 转换为列表
            val result = flow {
                for (i in 1..5) {
                    emit(i)
                }
            }
            .toList() // toList() 是一个挂起函数，会收集所有值并返回一个列表
            
            println("转换为列表: $result")
            
            // 使用 fold 操作符计算总和
            val sum = flow {
                for (i in 1..5) {
                    emit(i)
                }
            }
            // fold 操作符：从初始值开始，对每个值应用累加器函数
            .fold(0) { acc, value -> acc + value }
            
            println("求和结果: $sum")
        }
    }
}

/**
 * 运行所有 Flow 示例
 * 
 * 当你运行这个文件时，会依次执行所有的 Flow 示例
 * 每个示例都会打印详细的执行过程，帮助你理解 Flow 的工作原理
 */
fun main() {
    val example = FlowExample()
    // 依次运行所有示例
    example.basicFlow()
    example.flowOperators()
    example.coldFlowDemo()
    example.flowExceptionHandling()
    example.backpressureHandling()
    example.combineFlows()
    example.stateFlowExample()
    example.sharedFlowExample()
    example.flowTransformations()
}

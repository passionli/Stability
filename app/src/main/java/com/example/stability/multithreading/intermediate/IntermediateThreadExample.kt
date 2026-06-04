package com.example.stability.multithreading.intermediate

import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * 多线程中级示例
 * 展示线程同步、协程和并发集合等内容
 */
class IntermediateThreadExample {
    
    /**
     * 运行所有中级多线程示例
     */
    fun runAllExamples() {
        Log.d("Multithreading", "=== IntermediateThreadExample.runAllExamples called ===")
        Log.d("Multithreading", "Thread ID: ${Thread.currentThread().id}")
        
        // 运行线程同步示例
        runThreadSynchronizationExample()
        
        // 运行协程示例
        runCoroutineExample()
        
        // 运行线程通信示例
        runThreadCommunicationExample()
        
        Log.d("Multithreading", "=== IntermediateThreadExample.runAllExamples completed ===")
    }
    
    /**
     * 线程同步示例（使用协程和互斥锁）
     */
    private fun runThreadSynchronizationExample() {
        Log.d("Multithreading", "=== 运行线程同步示例 ===")
        
        // 创建共享资源
        val sharedResource = SharedResource()
        
        // 使用协程替代手动线程
        runBlocking {
            // 创建两个协程同时访问共享资源
            val jobs = listOf(
                launch {
                    repeat(5) {
                        sharedResource.increment()
                        delay(100)
                    }
                },
                launch {
                    repeat(5) {
                        sharedResource.increment()
                        delay(100)
                    }
                }
            )
            
            // 等待所有协程完成
            jobs.forEach { it.join() }
        }
        
        // 打印最终结果
        Log.d("Multithreading", "Final count: ${sharedResource.count}")
        
        Log.d("Multithreading", "=== 线程同步示例完成 ===")
    }
    
    /**
     * 协程示例
     */
    private fun runCoroutineExample() {
        Log.d("Multithreading", "=== 运行协程示例 ===")
        
        runBlocking {
            // 使用 repeat 函数创建多个协程
            val jobs = (1..5).map { taskId ->
                launch(Dispatchers.Default) {
                    Log.d("Multithreading", "Task $taskId started, Thread ID: ${Thread.currentThread().id}")
                    // 模拟耗时操作
                    delay(1000)
                    Log.d("Multithreading", "Task $taskId completed, Thread ID: ${Thread.currentThread().id}")
                }
            }
            
            // 等待所有任务完成
            jobs.forEach { it.join() }
        }
        
        Log.d("Multithreading", "=== 协程示例完成 ===")
    }
    
    /**
     * 线程通信示例（使用 Channel）
     */
    private fun runThreadCommunicationExample() {
        Log.d("Multithreading", "=== 运行线程通信示例 ===")
        
        runBlocking {
            // 创建 Channel 用于协程通信
            val channel = Channel<Int>(capacity = 1)
            
            // 生产者协程
            val producer = launch {
                repeat(5) { i ->
                    channel.send(i + 1)
                    Log.d("Multithreading", "Produced data: ${i + 1}, Thread ID: ${Thread.currentThread().id}")
                    delay(500)
                }
                channel.close()
            }
            
            // 消费者协程
            val consumer = launch {
                for (value in channel) {
                    Log.d("Multithreading", "Consumed data: $value, Thread ID: ${Thread.currentThread().id}")
                    delay(1000)
                }
            }
            
            // 等待协程完成
            producer.join()
            consumer.join()
        }
        
        Log.d("Multithreading", "=== 线程通信示例完成 ===")
    }
    
    /**
     * 共享资源类，演示线程同步（使用协程互斥锁）
     */
    private class SharedResource {
        // 共享变量
        private var _count = 0
        val count: Int get() = _count
        
        // 互斥锁
        private val mutex = Mutex()
        
        /**
         * 增加计数（协程安全）
         */
        suspend fun increment() {
            mutex.withLock {
                _count++
                Log.d("Multithreading", "Incremented count to $_count, Thread ID: ${Thread.currentThread().id}")
            }
        }
    }
}
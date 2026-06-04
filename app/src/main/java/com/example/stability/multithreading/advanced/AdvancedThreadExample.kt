package com.example.stability.multithreading.advanced

import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.atomic.AtomicInteger

/**
 * 多线程高级示例
 * 展示协程的高级使用、并发工具类、原子操作等内容
 */
class AdvancedThreadExample {
    
    /**
     * 运行所有高级多线程示例
     */
    fun runAllExamples() {
        Log.d("Multithreading", "=== AdvancedThreadExample.runAllExamples called ===")
        Log.d("Multithreading", "Thread ID: ${Thread.currentThread().id}")
        
        // 运行原子操作示例
        runAtomicOperationExample()
        
        // 运行高级协程示例
        runAdvancedCoroutineExample()
        
        // 运行并发工具类示例
        runConcurrentUtilitiesExample()
        
        Log.d("Multithreading", "=== AdvancedThreadExample.runAllExamples completed ===")
    }
    
    /**
     * 原子操作示例（使用协程）
     */
    private fun runAtomicOperationExample() {
        Log.d("Multithreading", "=== 运行原子操作示例 ===")
        
        // 创建原子整数
        val atomicCount = AtomicInteger(0)
        
        runBlocking {
            // 使用函数式方式创建多个协程
            val jobs = (1..10).map { _ ->
                launch(Dispatchers.Default) {
                    repeat(1000) { j ->
                        // 原子递增操作
                        val value = atomicCount.incrementAndGet()
                        if (j % 100 == 0) {
                            Log.d("Multithreading", "Thread ${Thread.currentThread().id} incremented to $value")
                        }
                    }
                }
            }
            
            // 等待所有协程完成
            jobs.forEach { it.join() }
        }
        
        // 打印最终结果
        Log.d("Multithreading", "Final atomic count: ${atomicCount.get()}")
        
        Log.d("Multithreading", "=== 原子操作示例完成 ===")
    }
    
    /**
     * 高级协程示例（带返回值）
     */
    private fun runAdvancedCoroutineExample() {
        Log.d("Multithreading", "=== 运行高级协程示例 ===")
        
        runBlocking {
            // 使用 async 创建带返回值的协程
            val deferreds = (1..5).map { taskId ->
                async(Dispatchers.Default) {
                    Log.d("Multithreading", "Task $taskId started, Thread ID: ${Thread.currentThread().id}")
                    // 模拟耗时操作
                    delay(1000)
                    val result = taskId * 10
                    Log.d("Multithreading", "Task $taskId completed, result: $result, Thread ID: ${Thread.currentThread().id}")
                    result
                }
            }
            
            // 使用 awaitAll 获取所有结果
            val results = deferreds.awaitAll()
            
            // 打印结果
            results.forEachIndexed { index, result ->
                Log.d("Multithreading", "Task ${index + 1} result: $result")
            }
        }
        
        Log.d("Multithreading", "=== 高级协程示例完成 ===")
    }
    
    /**
     * 并发工具类示例
     */
    private fun runConcurrentUtilitiesExample() {
        Log.d("Multithreading", "=== 运行并发工具类示例 ===")
        
        // 运行 CountDownLatch 类似功能（使用协程）
        runCountDownLatchExample()
        
        // 运行 CyclicBarrier 类似功能（使用协程）
        runCyclicBarrierExample()
        
        // 运行读写锁示例（使用协程）
        runReadWriteLockExample()
        
        Log.d("Multithreading", "=== 并发工具类示例完成 ===")
    }
    
    /**
     * CountDownLatch 类似功能（使用协程）
     */
    private fun runCountDownLatchExample() {
        Log.d("Multithreading", "=== 运行 CountDownLatch 示例 ===")
        
        runBlocking {
            // 创建一个计数器
            var countDown = 3
            
            // 创建三个协程
            val jobs = (1..3).map { threadId ->
                launch(Dispatchers.Default) {
                    Log.d("Multithreading", "Thread $threadId started, Thread ID: ${Thread.currentThread().id}")
                    // 模拟耗时操作
                    delay(threadId * 500L)
                    Log.d("Multithreading", "Thread $threadId completed, Thread ID: ${Thread.currentThread().id}")
                    // 减少计数
                    countDown--
                }
            }
            
            Log.d("Multithreading", "Main thread waiting for all threads to complete")
            
            // 等待所有协程完成
            jobs.forEach { it.join() }
            
            Log.d("Multithreading", "All threads completed, Main thread continues")
        }
        
        Log.d("Multithreading", "=== CountDownLatch 示例完成 ===")
    }
    
    /**
     * CyclicBarrier 类似功能（使用协程）
     */
    private fun runCyclicBarrierExample() {
        Log.d("Multithreading", "=== 运行 CyclicBarrier 示例 ===")
        
        runBlocking {
            // 创建一个 barrier，计数为 3
            val barrier = CountDownLatch(3)
            
            // 创建三个协程
            repeat(3) { threadId ->
                launch(Dispatchers.Default) {
                    Log.d("Multithreading", "Thread ${threadId + 1} started, Thread ID: ${Thread.currentThread().id}")
                    // 模拟耗时操作
                    delay((threadId + 1) * 500L)
                    Log.d("Multithreading", "Thread ${threadId + 1} waiting at barrier, Thread ID: ${Thread.currentThread().id}")
                    // 等待所有协程到达屏障
                    barrier.await()
                    Log.d("Multithreading", "Thread ${threadId + 1} passed barrier, Thread ID: ${Thread.currentThread().id}")
                }
            }
            
            // 等待屏障操作完成
            barrier.await()
            Log.d("Multithreading", "Barrier action executed")
        }
        
        Log.d("Multithreading", "=== CyclicBarrier 示例完成 ===")
    }
    
    /**
     * 读写锁示例（使用协程）
     */
    private fun runReadWriteLockExample() {
        Log.d("Multithreading", "=== 运行读写锁示例 ===")
        
        runBlocking {
            // 使用互斥锁模拟读写锁
            val readLock = Mutex()
            val writeLock = Mutex()
            var sharedValue = 0
            
            // 创建读取协程
            val readerJobs = (1..3).map { _ ->
                launch(Dispatchers.Default) {
                    repeat(5) {
                        readLock.withLock {
                            Log.d("Multithreading", "Reader thread ${Thread.currentThread().id} read value: $sharedValue")
                            delay(100)
                        }
                        delay(50)
                    }
                }
            }
            
            // 创建写入协程
            val writerJob = launch(Dispatchers.Default) {
                repeat(5) {
                    writeLock.withLock {
                        sharedValue++
                        Log.d("Multithreading", "Writer thread ${Thread.currentThread().id} wrote value: $sharedValue")
                        delay(200)
                    }
                    delay(100)
                }
            }
            
            // 等待所有协程完成
            readerJobs.forEach { it.join() }
            writerJob.join()
        }
        
        Log.d("Multithreading", "=== 读写锁示例完成 ===")
    }
    
    /**
     * 简单的 CountDownLatch 实现（用于演示）
     */
    private class CountDownLatch(private var count: Int) {
        private val mutex = Mutex()
        
        suspend fun await() {
            mutex.withLock {
                while (count > 0) {
                    // 简化实现，实际生产中应使用条件变量
                    delay(10)
                }
            }
        }
        
        fun countDown() {
            count--
        }
    }
}
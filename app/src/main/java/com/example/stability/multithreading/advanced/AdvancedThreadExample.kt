package com.example.stability.multithreading.advanced

import android.util.Log
import java.util.concurrent.*
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.locks.StampedLock

/**
 * 多线程高级示例
 * 展示线程池的高级使用、并发工具类、原子操作等内容
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
        
        // 运行高级线程池示例
        runAdvancedThreadPoolExample()
        
        // 运行并发工具类示例
        runConcurrentUtilitiesExample()
        
        Log.d("Multithreading", "=== AdvancedThreadExample.runAllExamples completed ===")
    }
    
    /**
     * 原子操作示例
     */
    private fun runAtomicOperationExample() {
        Log.d("Multithreading", "=== 运行原子操作示例 ===")
        
        // 创建原子整数
        val atomicCount = AtomicInteger(0)
        
        // 创建多个线程同时修改原子整数
        val threads = mutableListOf<Thread>()
        for (i in 1..10) {
            val thread = Thread {
                for (j in 1..1000) {
                    // 原子递增操作
                    val value = atomicCount.incrementAndGet()
                    if (j % 100 == 0) {
                        Log.d("Multithreading", "Thread ${Thread.currentThread().id} incremented to $value")
                    }
                }
            }
            threads.add(thread)
            thread.start()
        }
        
        // 等待所有线程执行完成
        for (thread in threads) {
            try {
                thread.join()
            } catch (e: InterruptedException) {
                Log.e("Multithreading", "Thread interrupted", e)
            }
        }
        
        // 打印最终结果
        Log.d("Multithreading", "Final atomic count: ${atomicCount.get()}")
        
        Log.d("Multithreading", "=== 原子操作示例完成 ===")
    }
    
    /**
     * 高级线程池示例
     */
    private fun runAdvancedThreadPoolExample() {
        Log.d("Multithreading", "=== 运行高级线程池示例 ===")
        
        // 创建一个自定义的线程池
        val executor: ExecutorService = ThreadPoolExecutor(
            2, // 核心线程数
            4, // 最大线程数
            30, // 线程存活时间
            TimeUnit.SECONDS, // 时间单位
            LinkedBlockingQueue(10), // 工作队列
            ThreadPoolExecutor.CallerRunsPolicy() // 拒绝策略
        )
        
        // 提交多个任务到线程池
        val futures = mutableListOf<Future<Int>>()
        for (i in 1..5) {
            val taskId = i
            val future = executor.submit(Callable<Int> {
                Log.d("Multithreading", "Task $taskId started, Thread ID: ${Thread.currentThread().id}")
                // 模拟耗时操作
                Thread.sleep(1000)
                val result = taskId * 10
                Log.d("Multithreading", "Task $taskId completed, result: $result, Thread ID: ${Thread.currentThread().id}")
                return@Callable result
            })
            futures.add(future)
        }
        
        // 获取任务结果
        for ((index, future) in futures.withIndex()) {
            try {
                val result = future.get(2, TimeUnit.SECONDS)
                Log.d("Multithreading", "Task ${index + 1} result: $result")
            } catch (e: Exception) {
                Log.e("Multithreading", "Error getting task result", e)
            }
        }
        
        // 关闭线程池
        executor.shutdown()
        
        Log.d("Multithreading", "=== 高级线程池示例完成 ===")
    }
    
    /**
     * 并发工具类示例
     */
    private fun runConcurrentUtilitiesExample() {
        Log.d("Multithreading", "=== 运行并发工具类示例 ===")
        
        // 运行 CountDownLatch 示例
        runCountDownLatchExample()
        
        // 运行 CyclicBarrier 示例
        runCyclicBarrierExample()
        
        // 运行 StampedLock 示例
        runStampedLockExample()
        
        Log.d("Multithreading", "=== 并发工具类示例完成 ===")
    }
    
    /**
     * CountDownLatch 示例
     */
    private fun runCountDownLatchExample() {
        Log.d("Multithreading", "=== 运行 CountDownLatch 示例 ===")
        
        // 创建 CountDownLatch，计数为 3
        val latch = CountDownLatch(3)
        
        // 创建三个线程
        for (i in 1..3) {
            val threadId = i
            Thread {
                Log.d("Multithreading", "Thread $threadId started, Thread ID: ${Thread.currentThread().id}")
                // 模拟耗时操作
                try {
                    Thread.sleep(threadId * 500L)
                } catch (e: InterruptedException) {
                    Log.e("Multithreading", "Thread interrupted", e)
                }
                Log.d("Multithreading", "Thread $threadId completed, Thread ID: ${Thread.currentThread().id}")
                // 减少计数
                latch.countDown()
            }.start()
        }
        
        Log.d("Multithreading", "Main thread waiting for all threads to complete")
        
        // 等待所有线程完成
        try {
            latch.await(5, TimeUnit.SECONDS)
        } catch (e: InterruptedException) {
            Log.e("Multithreading", "Thread interrupted", e)
        }
        
        Log.d("Multithreading", "All threads completed, Main thread continues")
        
        Log.d("Multithreading", "=== CountDownLatch 示例完成 ===")
    }
    
    /**
     * CyclicBarrier 示例
     */
    private fun runCyclicBarrierExample() {
        Log.d("Multithreading", "=== 运行 CyclicBarrier 示例 ===")
        
        // 创建 CyclicBarrier，计数为 3，设置屏障操作
        val barrier = CyclicBarrier(3) { 
            Log.d("Multithreading", "Barrier action executed, Thread ID: ${Thread.currentThread().id}")
        }
        
        // 创建三个线程
        for (i in 1..3) {
            val threadId = i
            Thread {
                Log.d("Multithreading", "Thread $threadId started, Thread ID: ${Thread.currentThread().id}")
                // 模拟耗时操作
                try {
                    Thread.sleep(threadId * 500L)
                } catch (e: InterruptedException) {
                    Log.e("Multithreading", "Thread interrupted", e)
                }
                Log.d("Multithreading", "Thread $threadId waiting at barrier, Thread ID: ${Thread.currentThread().id}")
                // 等待所有线程到达屏障
                try {
                    barrier.await()
                } catch (e: Exception) {
                    Log.e("Multithreading", "Error at barrier", e)
                }
                Log.d("Multithreading", "Thread $threadId passed barrier, Thread ID: ${Thread.currentThread().id}")
            }.start()
        }
        
        Log.d("Multithreading", "=== CyclicBarrier 示例完成 ===")
    }
    
    /**
     * StampedLock 示例
     */
    private fun runStampedLockExample() {
        Log.d("Multithreading", "=== 运行 StampedLock 示例 ===")
        
        // 创建 StampedLock
        val lock = StampedLock()
        var sharedValue = 0
        
        // 创建读取线程
        for (i in 1..3) {
            Thread {
                for (j in 1..5) {
                    // 获取读锁
                    val stamp = lock.readLock()
                    try {
                        Log.d("Multithreading", "Reader thread ${Thread.currentThread().id} read value: $sharedValue")
                        Thread.sleep(100)
                    } catch (e: InterruptedException) {
                        Log.e("Multithreading", "Thread interrupted", e)
                    } finally {
                        // 释放读锁
                        lock.unlockRead(stamp)
                    }
                    try {
                        Thread.sleep(50)
                    } catch (e: InterruptedException) {
                        Log.e("Multithreading", "Thread interrupted", e)
                    }
                }
            }.start()
        }
        
        // 创建写入线程
        Thread {
            for (i in 1..5) {
                // 获取写锁
                val stamp = lock.writeLock()
                try {
                    sharedValue++
                    Log.d("Multithreading", "Writer thread ${Thread.currentThread().id} wrote value: $sharedValue")
                    Thread.sleep(200)
                } catch (e: InterruptedException) {
                    Log.e("Multithreading", "Thread interrupted", e)
                } finally {
                    // 释放写锁
                    lock.unlockWrite(stamp)
                }
                try {
                    Thread.sleep(100)
                } catch (e: InterruptedException) {
                    Log.e("Multithreading", "Thread interrupted", e)
                }
            }
        }.start()
        
        // 等待所有线程执行完成
        try {
            Thread.sleep(3000)
        } catch (e: InterruptedException) {
            Log.e("Multithreading", "Thread interrupted", e)
        }
        
        Log.d("Multithreading", "=== StampedLock 示例完成 ===")
    }
}
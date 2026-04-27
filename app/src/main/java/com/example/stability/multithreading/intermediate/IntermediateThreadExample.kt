package com.example.stability.multithreading.intermediate

import android.util.Log
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.locks.ReentrantLock

/**
 * 多线程中级示例
 * 展示线程同步、线程池和并发集合等内容
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
        
        // 运行线程池示例
        runThreadPoolExample()
        
        // 运行线程通信示例
        runThreadCommunicationExample()
        
        Log.d("Multithreading", "=== IntermediateThreadExample.runAllExamples completed ===")
    }
    
    /**
     * 线程同步示例
     */
    private fun runThreadSynchronizationExample() {
        Log.d("Multithreading", "=== 运行线程同步示例 ===")
        
        // 创建共享资源
        val sharedResource = SharedResource()
        
        // 创建两个线程同时访问共享资源
        val thread1 = Thread {
            for (i in 1..5) {
                sharedResource.increment()
                try {
                    Thread.sleep(100)
                } catch (e: InterruptedException) {
                    Log.e("Multithreading", "Thread interrupted", e)
                }
            }
        }
        
        val thread2 = Thread {
            for (i in 1..5) {
                sharedResource.increment()
                try {
                    Thread.sleep(100)
                } catch (e: InterruptedException) {
                    Log.e("Multithreading", "Thread interrupted", e)
                }
            }
        }
        
        // 启动线程
        thread1.start()
        thread2.start()
        
        // 等待线程执行完成
        try {
            thread1.join()
            thread2.join()
        } catch (e: InterruptedException) {
            Log.e("Multithreading", "Thread interrupted", e)
        }
        
        // 打印最终结果
        Log.d("Multithreading", "Final count: ${sharedResource.count}")
        
        Log.d("Multithreading", "=== 线程同步示例完成 ===")
    }
    
    /**
     * 线程池示例
     */
    private fun runThreadPoolExample() {
        Log.d("Multithreading", "=== 运行线程池示例 ===")
        
        // 创建一个固定大小的线程池
        val executor: ExecutorService = Executors.newFixedThreadPool(3)
        
        // 提交多个任务到线程池
        for (i in 1..5) {
            val taskId = i
            executor.submit {
                Log.d("Multithreading", "Task $taskId started, Thread ID: ${Thread.currentThread().id}")
                // 模拟耗时操作
                try {
                    Thread.sleep(1000)
                } catch (e: InterruptedException) {
                    Log.e("Multithreading", "Thread interrupted", e)
                }
                Log.d("Multithreading", "Task $taskId completed, Thread ID: ${Thread.currentThread().id}")
            }
        }
        
        // 关闭线程池
        executor.shutdown()
        
        // 等待所有任务执行完成
        try {
            // 等待最多 10 秒
            if (!executor.awaitTermination(10, java.util.concurrent.TimeUnit.SECONDS)) {
                Log.d("Multithreading", "Some tasks may not have completed")
            }
        } catch (e: InterruptedException) {
            Log.e("Multithreading", "Thread interrupted", e)
        }
        
        Log.d("Multithreading", "=== 线程池示例完成 ===")
    }
    
    /**
     * 线程通信示例
     */
    private fun runThreadCommunicationExample() {
        Log.d("Multithreading", "=== 运行线程通信示例 ===")
        
        // 创建共享数据
        val sharedData = SharedData()
        
        // 创建生产者线程
        val producer = Thread {
            for (i in 1..5) {
                sharedData.produce(i)
                try {
                    Thread.sleep(500)
                } catch (e: InterruptedException) {
                    Log.e("Multithreading", "Thread interrupted", e)
                }
            }
        }
        
        // 创建消费者线程
        val consumer = Thread {
            for (i in 1..5) {
                sharedData.consume()
                try {
                    Thread.sleep(1000)
                } catch (e: InterruptedException) {
                    Log.e("Multithreading", "Thread interrupted", e)
                }
            }
        }
        
        // 启动线程
        producer.start()
        consumer.start()
        
        // 等待线程执行完成
        try {
            producer.join()
            consumer.join()
        } catch (e: InterruptedException) {
            Log.e("Multithreading", "Thread interrupted", e)
        }
        
        Log.d("Multithreading", "=== 线程通信示例完成 ===")
    }
    
    /**
     * 共享资源类，演示线程同步
     */
    private class SharedResource {
        // 共享变量
        var count = 0
        
        // 可重入锁
        private val lock = ReentrantLock()
        
        /**
         * 增加计数
         */
        fun increment() {
            // 获取锁
            lock.lock()
            try {
                // 临界区代码
                count++
                Log.d("Multithreading", "Incremented count to $count, Thread ID: ${Thread.currentThread().id}")
            } finally {
                // 释放锁
                lock.unlock()
            }
        }
    }
    
    /**
     * 共享数据类，演示线程通信
     */
    private class SharedData {
        // 数据
        private var data: Int? = null
        // 数据是否可用
        private var isDataAvailable = false
        
        /**
         * 生产数据
         */
        @Synchronized
        fun produce(value: Int) {
            // 等待数据被消费
            while (isDataAvailable) {
                try {
                    Log.d("Multithreading", "Producer waiting for data to be consumed, Thread ID: ${Thread.currentThread().id}")
                    (this as Object).wait()
                } catch (e: InterruptedException) {
                    Log.e("Multithreading", "Thread interrupted", e)
                }
            }
            
            // 生产数据
            data = value
            isDataAvailable = true
            Log.d("Multithreading", "Produced data: $value, Thread ID: ${Thread.currentThread().id}")
            
            // 通知消费者
            (this as Object).notify()
        }
        
        /**
         * 消费数据
         */
        @Synchronized
        fun consume() {
            // 等待数据可用
            while (!isDataAvailable) {
                try {
                    Log.d("Multithreading", "Consumer waiting for data to be produced, Thread ID: ${Thread.currentThread().id}")
                    (this as Object).wait()
                } catch (e: InterruptedException) {
                    Log.e("Multithreading", "Thread interrupted", e)
                }
            }
            
            // 消费数据
            val value = data
            isDataAvailable = false
            Log.d("Multithreading", "Consumed data: $value, Thread ID: ${Thread.currentThread().id}")
            
            // 通知生产者
            (this as Object).notify()
        }
    }
}
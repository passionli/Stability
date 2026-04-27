package com.example.stability.multithreading.basic

import android.util.Log

/**
 * 多线程初级示例
 * 展示基本的线程创建和使用方法
 */
class BasicThreadExample {
    
    /**
     * 运行所有初级多线程示例
     */
    fun runAllExamples() {
        Log.d("Multithreading", "=== BasicThreadExample.runAllExamples called ===")
        Log.d("Multithreading", "Thread ID: ${Thread.currentThread().id}")
        
        // 运行通过 Thread 类创建线程的示例
        runThreadClassExample()
        
        // 运行通过 Runnable 接口创建线程的示例
        runRunnableExample()
        
        // 运行线程状态示例
        runThreadStateExample()
        
        Log.d("Multithreading", "=== BasicThreadExample.runAllExamples completed ===")
    }
    
    /**
     * 通过 Thread 类创建线程的示例
     */
    private fun runThreadClassExample() {
        Log.d("Multithreading", "=== 运行 Thread 类创建线程示例 ===")
        
        // 创建自定义线程类的实例
        val myThread = MyThread()
        // 启动线程
        myThread.start()
        
        // 主线程继续执行
        Log.d("Multithreading", "Main thread continues, Thread ID: ${Thread.currentThread().id}")
        
        // 等待子线程执行完成
        try {
            myThread.join()
        } catch (e: InterruptedException) {
            Log.e("Multithreading", "Thread interrupted", e)
        }
        
        Log.d("Multithreading", "=== Thread 类创建线程示例完成 ===")
    }
    
    /**
     * 通过 Runnable 接口创建线程的示例
     */
    private fun runRunnableExample() {
        Log.d("Multithreading", "=== 运行 Runnable 接口创建线程示例 ===")
        
        // 创建 Runnable 实现类的实例
        val myRunnable = MyRunnable()
        // 创建 Thread 对象并传入 Runnable
        val thread = Thread(myRunnable)
        // 启动线程
        thread.start()
        
        // 主线程继续执行
        Log.d("Multithreading", "Main thread continues, Thread ID: ${Thread.currentThread().id}")
        
        // 等待子线程执行完成
        try {
            thread.join()
        } catch (e: InterruptedException) {
            Log.e("Multithreading", "Thread interrupted", e)
        }
        
        Log.d("Multithreading", "=== Runnable 接口创建线程示例完成 ===")
    }
    
    /**
     * 线程状态示例
     */
    private fun runThreadStateExample() {
        Log.d("Multithreading", "=== 运行线程状态示例 ===")
        
        // 创建线程
        val thread = Thread {
            Log.d("Multithreading", "Thread running, Thread ID: ${Thread.currentThread().id}")
            try {
                // 线程睡眠 1 秒
                Thread.sleep(1000)
            } catch (e: InterruptedException) {
                Log.e("Multithreading", "Thread interrupted", e)
            }
            Log.d("Multithreading", "Thread completed, Thread ID: ${Thread.currentThread().id}")
        }
        
        // 打印线程初始状态
        Log.d("Multithreading", "Thread state after creation: ${thread.state}")
        
        // 启动线程
        thread.start()
        
        // 打印线程运行状态
        Log.d("Multithreading", "Thread state after start: ${thread.state}")
        
        // 等待线程执行完成
        try {
            thread.join()
        } catch (e: InterruptedException) {
            Log.e("Multithreading", "Thread interrupted", e)
        }
        
        // 打印线程终止状态
        Log.d("Multithreading", "Thread state after completion: ${thread.state}")
        
        Log.d("Multithreading", "=== 线程状态示例完成 ===")
    }
    
    /**
     * 自定义线程类
     */
    private class MyThread : Thread() {
        override fun run() {
            Log.d("Multithreading", "MyThread.run() called, Thread ID: ${Thread.currentThread().id}")
            // 模拟耗时操作
            for (i in 1..5) {
                Log.d("Multithreading", "MyThread: $i")
                try {
                    // 线程睡眠 500 毫秒
                    Thread.sleep(500)
                } catch (e: InterruptedException) {
                    Log.e("Multithreading", "Thread interrupted", e)
                }
            }
            Log.d("Multithreading", "MyThread.run() completed, Thread ID: ${Thread.currentThread().id}")
        }
    }
    
    /**
     * 自定义 Runnable 实现类
     */
    private class MyRunnable : Runnable {
        override fun run() {
            Log.d("Multithreading", "MyRunnable.run() called, Thread ID: ${Thread.currentThread().id}")
            // 模拟耗时操作
            for (i in 1..5) {
                Log.d("Multithreading", "MyRunnable: $i")
                try {
                    // 线程睡眠 500 毫秒
                    Thread.sleep(500)
                } catch (e: InterruptedException) {
                    Log.e("Multithreading", "Thread interrupted", e)
                }
            }
            Log.d("Multithreading", "MyRunnable.run() completed, Thread ID: ${Thread.currentThread().id}")
        }
    }
}
package com.example.stability.anr.examples

import com.example.stability.anr.prevention.ThreadChecker
import com.example.stability.anr.utils.AnrLog
import java.net.URL

/**
 * 阻塞操作示例
 * 展示各种可能导致 ANR 的阻塞操作及其正确实现方式
 */
object BlockingOperationExample {
    
    /**
     * 错误示例：在主线程执行网络请求
     */
    fun badNetworkRequest() {
        // ❌ 错误：在主线程执行网络请求
        ThreadChecker.warnIfMainThread("badNetworkRequest")
        
        try {
            val url = URL("https://api.example.com/data")
            val connection = url.openConnection()
            connection.connectTimeout = 5000
            // 这会阻塞主线程！
            val inputStream = connection.getInputStream()
            // ... 处理响应
        } catch (e: Exception) {
            AnrLog.e("Network request failed", e)
        }
    }
    
    /**
     * 正确示例：在后台线程执行网络请求
     */
    fun goodNetworkRequest(callback: (String) -> Unit) {
        // ✅ 正确：在后台线程执行
        Thread {
            ThreadChecker.validateNetworkOperation("goodNetworkRequest")
            
            try {
                val url = URL("https://api.example.com/data")
                val connection = url.openConnection()
                connection.connectTimeout = 5000
                val inputStream = connection.getInputStream()
                val result = inputStream.bufferedReader().readText()
                
                // 在主线程回调
                android.os.Handler(android.os.Looper.getMainLooper()).post {
                    callback(result)
                }
            } catch (e: Exception) {
                AnrLog.e("Network request failed", e)
            }
        }.start()
    }
    
    /**
     * 错误示例：在主线程执行数据库查询
     */
    fun badDatabaseQuery() {
        ThreadChecker.warnIfMainThread("badDatabaseQuery")
        
        // ❌ 错误：在主线程执行数据库操作
        // db.query(...) // 这会阻塞主线程！
        AnrLog.w("Simulated bad database query on main thread")
    }
    
    /**
     * 正确示例：使用协程执行数据库操作
     */
    suspend fun goodDatabaseQuery(): String {
        // ✅ 正确：在 IO 线程执行
        // withContext(Dispatchers.IO) {
        //     return db.query(...)
        // }
        AnrLog.d("Simulated good database query")
        return "query result"
    }
    
    /**
     * 错误示例：在主线程执行文件操作
     */
    fun badFileOperation() {
        ThreadChecker.warnIfMainThread("badFileOperation")
        
        // ❌ 错误：在主线程执行文件操作
        // val file = File("/data/data/com.example.app/data.txt")
        // file.readText() // 这会阻塞主线程！
        AnrLog.w("Simulated bad file operation on main thread")
    }
    
    /**
     * 正确示例：在后台线程执行文件操作
     */
    fun goodFileOperation() {
        Thread {
            ThreadChecker.validateFileOperation("goodFileOperation")
            
            // ✅ 正确：在后台线程执行
            // val file = File("/data/data/com.example.app/data.txt")
            // val content = file.readText()
            
            AnrLog.d("Simulated good file operation")
        }.start()
    }
    
    /**
     * 错误示例：死锁
     */
    fun deadlockExample() {
        val lockA = Object()
        val lockB = Object()
        
        // 线程1：先获取 lockA，再获取 lockB
        Thread {
            synchronized(lockA) {
                AnrLog.d("Thread 1 acquired lockA")
                Thread.sleep(100)
                synchronized(lockB) {  // 等待 lockB
                    AnrLog.d("Thread 1 acquired lockB")
                }
            }
        }.start()
        
        // 线程2：先获取 lockB，再获取 lockA
        Thread {
            synchronized(lockB) {
                AnrLog.d("Thread 2 acquired lockB")
                Thread.sleep(100)
                synchronized(lockA) {  // 等待 lockA，死锁！
                    AnrLog.d("Thread 2 acquired lockA")
                }
            }
        }.start()
    }
    
    /**
     * 正确示例：避免死锁（统一锁的获取顺序）
     */
    fun noDeadlockExample() {
        val lockA = Object()
        val lockB = Object()
        
        // 两个线程都先获取 lockA，再获取 lockB
        Thread {
            synchronized(lockA) {
                synchronized(lockB) {
                    AnrLog.d("Thread 1 acquired both locks")
                }
            }
        }.start()
        
        Thread {
            synchronized(lockA) {
                synchronized(lockB) {
                    AnrLog.d("Thread 2 acquired both locks")
                }
            }
        }.start()
    }
    
    /**
     * 错误示例：长时间的计算操作
     */
    fun badLongComputation() {
        ThreadChecker.warnIfMainThread("badLongComputation")
        
        // ❌ 错误：在主线程执行长时间计算
        var result = 0L
        for (i in 0..1_000_000_000) {
            result += i
        }
        AnrLog.w("Long computation result: $result")
    }
    
    /**
     * 正确示例：在后台线程执行计算操作
     */
    fun goodLongComputation(callback: (Long) -> Unit) {
        Thread {
            var result = 0L
            for (i in 0..1_000_000_000) {
                result += i
            }
            
            // 在主线程回调
            android.os.Handler(android.os.Looper.getMainLooper()).post {
                callback(result)
            }
        }.start()
    }
}

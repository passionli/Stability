package com.example.stability.anr.prevention

import android.os.Looper
import com.example.stability.anr.utils.AnrLog

/**
 * 线程检查工具类
 * 提供线程相关的检查和验证功能
 */
object ThreadChecker {
    
    /**
     * 检查当前线程是否是主线程
     * @return true 如果是主线程
     */
    fun isMainThread(): Boolean {
        return Looper.getMainLooper().thread === Thread.currentThread()
    }
    
    /**
     * 检查当前线程是否是主线程，如果不是则抛出异常
     * @param message 错误消息
     * @throws IllegalStateException 如果不在主线程
     */
    fun checkMainThread(message: String = "This operation must be called on the main thread") {
        if (!isMainThread()) {
            val threadName = Thread.currentThread().name
            AnrLog.e("Thread check failed: Expected main thread but was $threadName")
            throw IllegalStateException("$message. Current thread: $threadName")
        }
    }
    
    /**
     * 检查当前线程是否不是主线程，如果是则抛出异常
     * @param message 错误消息
     * @throws IllegalStateException 如果在主线程
     */
    fun checkNotMainThread(message: String = "This operation must not be called on the main thread") {
        if (isMainThread()) {
            AnrLog.e("Thread check failed: Should not be on main thread")
            throw IllegalStateException(message)
        }
    }
    
    /**
     * 检查当前线程是否是主线程，如果不是则记录警告日志
     * @param operationName 操作名称，用于日志记录
     */
    fun warnIfNotMainThread(operationName: String) {
        if (!isMainThread()) {
            val threadName = Thread.currentThread().name
            AnrLog.w("Operation '$operationName' should be called on main thread but is running on $threadName")
        }
    }
    
    /**
     * 检查当前线程是否是主线程，如果是则记录警告日志（用于耗时操作）
     * @param operationName 操作名称，用于日志记录
     */
    fun warnIfMainThread(operationName: String) {
        if (isMainThread()) {
            AnrLog.w("Potential ANR risk: '$operationName' is running on main thread")
        }
    }
    
    /**
     * 检查线程是否在指定的线程池中执行
     * @param threadPoolName 线程池名称
     * @return true 如果当前线程属于指定线程池
     */
    fun isInThreadPool(threadPoolName: String): Boolean {
        val currentThread = Thread.currentThread()
        return currentThread.name.contains(threadPoolName)
    }
    
    /**
     * 获取当前线程信息
     * @return 线程信息字符串
     */
    fun getCurrentThreadInfo(): String {
        val thread = Thread.currentThread()
        return buildString {
            append("Thread: ${thread.name}\n")
            append("ID: ${thread.id}\n")
            append("State: ${thread.state}\n")
            append("Priority: ${thread.priority}\n")
            append("Is Main: ${isMainThread()}\n")
        }
    }
    
    /**
     * 验证网络操作是否在后台线程执行
     * @param operationName 操作名称
     */
    fun validateNetworkOperation(operationName: String) {
        if (isMainThread()) {
            AnrLog.e("Network operation '$operationName' is running on main thread! This can cause ANR.")
            // 在开发阶段可以抛出异常
            if (isDebugMode()) {
                throw IllegalStateException("Network operation '$operationName' must not run on main thread")
            }
        } else {
            AnrLog.d("Network operation '$operationName' is running on background thread: ${Thread.currentThread().name}")
        }
    }
    
    /**
     * 验证数据库操作是否在后台线程执行
     * @param operationName 操作名称
     */
    fun validateDatabaseOperation(operationName: String) {
        if (isMainThread()) {
            AnrLog.e("Database operation '$operationName' is running on main thread! This can cause ANR.")
            if (isDebugMode()) {
                throw IllegalStateException("Database operation '$operationName' must not run on main thread")
            }
        } else {
            AnrLog.d("Database operation '$operationName' is running on background thread: ${Thread.currentThread().name}")
        }
    }
    
    /**
     * 验证文件操作是否在后台线程执行
     * @param operationName 操作名称
     */
    fun validateFileOperation(operationName: String) {
        if (isMainThread()) {
            AnrLog.e("File operation '$operationName' is running on main thread! This can cause ANR.")
            if (isDebugMode()) {
                throw IllegalStateException("File operation '$operationName' must not run on main thread")
            }
        } else {
            AnrLog.d("File operation '$operationName' is running on background thread: ${Thread.currentThread().name}")
        }
    }
    
    /**
     * 检查是否是调试模式
     */
    private fun isDebugMode(): Boolean {
        return try {
            val buildConfigClass = Class.forName("com.example.stability.BuildConfig")
            val debugField = buildConfigClass.getField("DEBUG")
            debugField.getBoolean(null)
        } catch (e: Exception) {
            false
        }
    }
}

package com.example.stability.anr.utils

import android.util.Log

/**
 * ANR 日志工具类
 * 提供统一的日志记录接口，支持不同级别和标签的日志输出
 */
object AnrLog {
    
    private const val TAG = "ANR_MONITOR"
    
    const val DEBUG = Log.DEBUG
    const val INFO = Log.INFO
    const val WARN = Log.WARN
    const val ERROR = Log.ERROR
    
    var currentLevel = DEBUG
    var enabled = true
    
    /**
     * 日志级别检查函数（高阶函数）
     */
    private inline fun logIf(level: Int, tag: String, message: String, block: () -> Unit) {
        if (enabled && currentLevel <= level) {
            Log.v(tag, "[$level] $message")
            block()
        }
    }
    
    /**
     * 简化的日志执行函数
     */
    private inline fun log(level: Int, tag: String, message: String) {
        if (enabled && currentLevel <= level) {
            when (level) {
                DEBUG -> Log.d(tag, message)
                INFO -> Log.i(tag, message)
                WARN -> Log.w(tag, message)
                ERROR -> Log.e(tag, message)
                else -> Log.v(tag, message)
            }
        }
    }
    
    fun d(message: String) = log(DEBUG, TAG, message)
    fun d(tag: String, message: String) = log(DEBUG, tag, message)
    fun i(message: String) = log(INFO, TAG, message)
    fun i(tag: String, message: String) = log(INFO, tag, message)
    fun w(message: String) = log(WARN, TAG, message)
    fun w(tag: String, message: String) = log(WARN, tag, message)
    fun e(message: String) = log(ERROR, TAG, message)
    fun e(tag: String, message: String) = log(ERROR, tag, message)
    
    fun e(message: String, throwable: Throwable) {
        if (enabled && currentLevel <= ERROR) {
            Log.e(TAG, message, throwable)
        }
    }
    
    /**
     * 记录详细的 ANR 信息（使用函数式方式处理长字符串）
     */
    fun anr(stackTrace: String) {
        if (!enabled) return
        
        val maxLength = 4000
        
        Log.e(TAG, "================== ANR DETECTED ==================")
        
        // 使用函数式方式分段输出
        generateSequence(0) { start ->
            if (start < stackTrace.length) start + maxLength else null
        }
        .forEach { start ->
            val end = minOf(start + maxLength, stackTrace.length)
            Log.e(TAG, stackTrace.substring(start, end))
        }
        
        Log.e(TAG, "================== ANR END ==================")
    }
    
    fun performanceWarning(message: String, durationMs: Long) {
        w("PERFORMANCE_WARNING: $message - ${durationMs}ms")
    }
    
    fun performanceError(message: String, durationMs: Long) {
        e("PERFORMANCE_ERROR: $message - ${durationMs}ms")
    }
}
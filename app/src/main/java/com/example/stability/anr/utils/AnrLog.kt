package com.example.stability.anr.utils

import android.util.Log

/**
 * ANR 日志工具类
 * 提供统一的日志记录接口，支持不同级别和标签的日志输出
 */
object AnrLog {
    
    private const val TAG = "ANR_MONITOR"
    
    /**
     * 调试日志级别
     */
    const val DEBUG = Log.DEBUG
    
    /**
     * 信息日志级别
     */
    const val INFO = Log.INFO
    
    /**
     * 警告日志级别
     */
    const val WARN = Log.WARN
    
    /**
     * 错误日志级别
     */
    const val ERROR = Log.ERROR
    
    /**
     * 当前日志级别，低于此级别的日志将被过滤
     */
    var currentLevel = DEBUG
    
    /**
     * 是否启用日志输出
     */
    var enabled = true
    
    /**
     * 记录调试日志
     */
    fun d(message: String) {
        if (enabled && currentLevel <= DEBUG) {
            Log.d(TAG, message)
        }
    }
    
    /**
     * 记录调试日志（带自定义标签）
     */
    fun d(tag: String, message: String) {
        if (enabled && currentLevel <= DEBUG) {
            Log.d(tag, message)
        }
    }
    
    /**
     * 记录信息日志
     */
    fun i(message: String) {
        if (enabled && currentLevel <= INFO) {
            Log.i(TAG, message)
        }
    }
    
    /**
     * 记录信息日志（带自定义标签）
     */
    fun i(tag: String, message: String) {
        if (enabled && currentLevel <= INFO) {
            Log.i(tag, message)
        }
    }
    
    /**
     * 记录警告日志
     */
    fun w(message: String) {
        if (enabled && currentLevel <= WARN) {
            Log.w(TAG, message)
        }
    }
    
    /**
     * 记录警告日志（带自定义标签）
     */
    fun w(tag: String, message: String) {
        if (enabled && currentLevel <= WARN) {
            Log.w(tag, message)
        }
    }
    
    /**
     * 记录错误日志
     */
    fun e(message: String) {
        if (enabled && currentLevel <= ERROR) {
            Log.e(TAG, message)
        }
    }
    
    /**
     * 记录错误日志（带异常）
     */
    fun e(message: String, throwable: Throwable) {
        if (enabled && currentLevel <= ERROR) {
            Log.e(TAG, message, throwable)
        }
    }
    
    /**
     * 记录错误日志（带自定义标签）
     */
    fun e(tag: String, message: String) {
        if (enabled && currentLevel <= ERROR) {
            Log.e(tag, message)
        }
    }
    
    /**
     * 记录详细的 ANR 信息
     */
    fun anr(stackTrace: String) {
        if (!enabled) return
        
        // 将长堆栈信息分段输出
        val maxLength = 4000
        var start = 0
        
        Log.e(TAG, "================== ANR DETECTED ==================")
        
        while (start < stackTrace.length) {
            val end = minOf(start + maxLength, stackTrace.length)
            Log.e(TAG, stackTrace.substring(start, end))
            start = end
        }
        
        Log.e(TAG, "================== ANR END ==================")
    }
    
    /**
     * 记录性能警告
     */
    fun performanceWarning(message: String, durationMs: Long) {
        w("PERFORMANCE_WARNING: $message - ${durationMs}ms")
    }
    
    /**
     * 记录性能错误
     */
    fun performanceError(message: String, durationMs: Long) {
        e("PERFORMANCE_ERROR: $message - ${durationMs}ms")
    }
}

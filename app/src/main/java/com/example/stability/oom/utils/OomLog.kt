package com.example.stability.oom.utils

import android.util.Log

/**
 * OOM 模块日志工具类
 * 提供统一的日志输出接口
 */
object OomLog {
    
    private const val TAG = "OOM"
    
    /**
     * 输出详细日志
     */
    fun d(message: String) {
        Log.d(TAG, message)
    }
    
    /**
     * 输出信息日志
     */
    fun i(message: String) {
        Log.i(TAG, message)
    }
    
    /**
     * 输出警告日志
     */
    fun w(message: String) {
        Log.w(TAG, message)
    }
    
    /**
     * 输出错误日志
     */
    fun e(message: String) {
        Log.e(TAG, message)
    }
    
    /**
     * 输出错误日志（带异常）
     */
    fun e(message: String, throwable: Throwable) {
        Log.e(TAG, message, throwable)
    }
    
    /**
     * 输出详细日志（带标签）
     */
    fun d(tag: String, message: String) {
        Log.d(TAG + "-" + tag, message)
    }
    
    /**
     * 输出信息日志（带标签）
     */
    fun i(tag: String, message: String) {
        Log.i(TAG + "-" + tag, message)
    }
    
    /**
     * 输出警告日志（带标签）
     */
    fun w(tag: String, message: String) {
        Log.w(TAG + "-" + tag, message)
    }
    
    /**
     * 输出错误日志（带标签）
     */
    fun e(tag: String, message: String) {
        Log.e(TAG + "-" + tag, message)
    }
    
    /**
     * 输出错误日志（带标签和异常）
     */
    fun e(tag: String, message: String, throwable: Throwable) {
        Log.e(TAG + "-" + tag, message, throwable)
    }
}

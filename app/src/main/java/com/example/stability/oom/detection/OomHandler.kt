package com.example.stability.oom.detection

import android.util.Log
import com.example.stability.oom.utils.OomLog
import java.io.PrintWriter
import java.io.StringWriter

/**
 * OOM 异常处理器
 * 捕获并处理 OutOfMemoryError，收集相关信息用于分析
 */
class OomHandler private constructor() {
    
    companion object {
        /**
         * 单例实例
         */
        @Volatile
        private var instance: OomHandler? = null
        
        /**
         * 获取单例实例
         */
        fun getInstance(): OomHandler {
            if (instance == null) {
                synchronized(OomHandler::class.java) {
                    if (instance == null) {
                        instance = OomHandler()
                    }
                }
            }
            return instance!!
        }
    }
    
    /**
     * 默认未捕获异常处理器
     */
    private var defaultHandler: Thread.UncaughtExceptionHandler? = null
    
    /**
     * OOM 监听回调
     */
    private var listener: OomListener? = null
    
    /**
     * OOM 监听接口
     */
    interface OomListener {
        /**
         * OOM 发生时回调
         * @param exception OutOfMemoryError 异常
         * @param info 内存信息
         * @param stackTrace 完整堆栈信息
         */
        fun onOom(exception: OutOfMemoryError, info: MemoryInfo, stackTrace: String)
    }
    
    /**
     * 设置 OOM 监听回调
     * @param listener 监听回调
     */
    fun setListener(listener: OomListener) {
        this.listener = listener
        OomLog.i("OomHandler", "OOM listener set")
    }
    
    /**
     * 注册 OOM 处理器
     * 替换默认的未捕获异常处理器，捕获 OutOfMemoryError
     */
    fun register() {
        if (defaultHandler == null) {
            defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
            
            Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
                handleUncaughtException(thread, throwable)
            }
            
            OomLog.i("OomHandler", "OOM handler registered")
        } else {
            OomLog.w("OomHandler", "OOM handler is already registered")
        }
    }
    
    /**
     * 注销 OOM 处理器
     * 恢复默认的未捕获异常处理器
     */
    fun unregister() {
        if (defaultHandler != null) {
            Thread.setDefaultUncaughtExceptionHandler(defaultHandler)
            defaultHandler = null
            OomLog.i("OomHandler", "OOM handler unregistered")
        } else {
            OomLog.w("OomHandler", "OOM handler is not registered")
        }
    }
    
    /**
     * 处理未捕获异常
     */
    private fun handleUncaughtException(thread: Thread, throwable: Throwable) {
        // 检查是否是 OOM 或 OOM 引起的异常
        if (throwable is OutOfMemoryError || isOomRelated(throwable)) {
            handleOom(throwable as OutOfMemoryError)
        }
        
        // 交给默认处理器处理
        defaultHandler?.uncaughtException(thread, throwable)
    }
    
    /**
     * 判断异常是否与 OOM 相关
     */
    private fun isOomRelated(throwable: Throwable): Boolean {
        var cause: Throwable? = throwable.cause
        while (cause != null) {
            if (cause is OutOfMemoryError) {
                return true
            }
            cause = cause.cause
        }
        return false
    }
    
    /**
     * 处理 OOM 异常
     */
    private fun handleOom(exception: OutOfMemoryError) {
        OomLog.e("OomHandler", "OutOfMemoryError detected!", exception)
        
        // 收集内存信息
        val memoryInfo = collectMemoryInfo()
        
        // 获取完整堆栈信息
        val stackTrace = getFullStackTrace(exception)
        
        // 通知监听器
        listener?.onOom(exception, memoryInfo, stackTrace)
        
        // 尝试释放一些内存
        tryReleaseMemory()
    }
    
    /**
     * 收集内存信息
     */
    private fun collectMemoryInfo(): MemoryInfo {
        val runtime = Runtime.getRuntime()
        return MemoryInfo(
            maxMemory = runtime.maxMemory(),
            totalMemory = runtime.totalMemory(),
            usedMemory = runtime.totalMemory() - runtime.freeMemory(),
            timestamp = System.currentTimeMillis(),
            threadName = Thread.currentThread().name,
            availableProcessors = Runtime.getRuntime().availableProcessors()
        )
    }
    
    /**
     * 获取完整堆栈信息
     */
    private fun getFullStackTrace(throwable: Throwable): String {
        val sw = StringWriter()
        val pw = PrintWriter(sw)
        throwable.printStackTrace(pw)
        
        // 添加所有线程的堆栈信息
        pw.println("\n=== All Threads Stack Trace ===")
        val threads = Thread.getAllStackTraces()
        for ((thread, stack) in threads) {
            pw.println("\n\"${thread.name}\" prio=${thread.priority} tid=${thread.id} ${thread.state}")
            for (element in stack) {
                pw.println("\tat $element")
            }
        }
        
        return sw.toString()
    }
    
    /**
     * 尝试释放内存
     * 触发 GC 并清除一些缓存
     */
    private fun tryReleaseMemory() {
        OomLog.i("OomHandler", "Attempting to release memory")
        
        // 触发 GC
        System.gc()
        
        // 尝试清除软引用缓存
        // 这里可以添加具体的缓存清理逻辑
        
        OomLog.i("OomHandler", "Memory release attempt completed")
    }
    
    /**
     * 内存信息数据类
     */
    data class MemoryInfo(
        val maxMemory: Long,
        val totalMemory: Long,
        val usedMemory: Long,
        val timestamp: Long,
        val threadName: String,
        val availableProcessors: Int
    ) {
        /**
         * 获取内存使用率百分比
         */
        fun getUsedPercent(): Float {
            return if (maxMemory > 0) {
                (usedMemory.toFloat() / maxMemory.toFloat()) * 100
            } else {
                0f
            }
        }
        
        /**
         * 获取格式化的字符串表示
         */
        override fun toString(): String {
            return buildString {
                append("MemoryInfo {\n")
                append("  maxMemory: ${formatSize(maxMemory)}\n")
                append("  totalMemory: ${formatSize(totalMemory)}\n")
                append("  usedMemory: ${formatSize(usedMemory)} (${getUsedPercent().toInt()}%)\n")
                append("  timestamp: $timestamp\n")
                append("  threadName: $threadName\n")
                append("  availableProcessors: $availableProcessors\n")
                append("}")
            }
        }
        
        private fun formatSize(bytes: Long): String {
            return when {
                bytes < 1024 -> "$bytes B"
                bytes < 1024 * 1024 -> String.format("%.2f KB", bytes / 1024.0)
                bytes < 1024 * 1024 * 1024 -> String.format("%.2f MB", bytes / (1024.0 * 1024))
                else -> String.format("%.2f GB", bytes / (1024.0 * 1024 * 1024))
            }
        }
    }
}

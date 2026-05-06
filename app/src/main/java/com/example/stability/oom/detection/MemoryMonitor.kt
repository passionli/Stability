package com.example.stability.oom.detection

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.app.ActivityManager
import com.example.stability.oom.utils.OomLog
import java.util.concurrent.atomic.AtomicBoolean

/**
 * 内存监控器
 * 实时监控应用内存使用情况，当内存不足时发出警告
 */
class MemoryMonitor private constructor(private val context: Context) {
    
    companion object {
        /**
         * 单例实例
         */
        @Volatile
        private var instance: MemoryMonitor? = null
        
        /**
         * 获取单例实例
         */
        fun getInstance(context: Context): MemoryMonitor {
            if (instance == null) {
                synchronized(MemoryMonitor::class.java) {
                    if (instance == null) {
                        instance = MemoryMonitor(context.applicationContext)
                    }
                }
            }
            return instance!!
        }
    }
    
    /**
     * 内存警告阈值（可用内存低于此值触发警告）
     * 默认 50MB
     */
    private var warningThreshold = 50 * 1024 * 1024L
    
    /**
     * 严重警告阈值（可用内存低于此值触发严重警告）
     * 默认 20MB
     */
    private var criticalThreshold = 20 * 1024 * 1024L
    
    /**
     * 监控间隔（毫秒）
     * 默认 2 秒检查一次
     */
    private var checkInterval = 2000L
    
    /**
     * 主线程 Handler
     */
    private val handler = Handler(Looper.getMainLooper())
    
    /**
     * 监控是否运行中
     */
    private val isRunning = AtomicBoolean(false)
    
    /**
     * 内存监听回调
     */
    private var listener: MemoryListener? = null
    
    /**
     * 内存监听接口
     */
    interface MemoryListener {
        /**
         * 内存警告回调
         * @param availableMemory 当前可用内存（字节）
         */
        fun onMemoryWarning(availableMemory: Long)
        
        /**
         * 严重内存警告回调
         * @param availableMemory 当前可用内存（字节）
         */
        fun onMemoryCritical(availableMemory: Long)
    }
    
    /**
     * 设置警告阈值
     * @param warningThreshold 警告阈值（字节）
     * @param criticalThreshold 严重警告阈值（字节）
     */
    fun setThresholds(warningThreshold: Long, criticalThreshold: Long) {
        this.warningThreshold = warningThreshold
        this.criticalThreshold = criticalThreshold
        OomLog.i("MemoryMonitor", "Thresholds updated: warning=$warningThreshold, critical=$criticalThreshold")
    }
    
    /**
     * 设置检查间隔
     * @param interval 检查间隔（毫秒）
     */
    fun setCheckInterval(interval: Long) {
        this.checkInterval = interval
        OomLog.i("MemoryMonitor", "Check interval updated: $interval ms")
    }
    
    /**
     * 开始监控
     * @param listener 内存监听回调
     */
    fun start(listener: MemoryListener) {
        if (isRunning.compareAndSet(false, true)) {
            this.listener = listener
            OomLog.i("MemoryMonitor", "Starting memory monitoring")
            scheduleCheck()
        } else {
            OomLog.w("MemoryMonitor", "Memory monitor is already running")
        }
    }
    
    /**
     * 停止监控
     */
    fun stop() {
        if (isRunning.compareAndSet(true, false)) {
            handler.removeCallbacksAndMessages(null)
            OomLog.i("MemoryMonitor", "Stopping memory monitoring")
        } else {
            OomLog.w("MemoryMonitor", "Memory monitor is not running")
        }
    }
    
    /**
     * 检查是否运行中
     */
    fun isRunning(): Boolean {
        return isRunning.get()
    }
    
    /**
     * 定时检查内存状态
     */
    private fun scheduleCheck() {
        if (!isRunning.get()) {
            return
        }
        
        try {
            val availableMemory = getAvailableMemory()
            OomLog.d("MemoryMonitor", "Available memory: ${formatSize(availableMemory)}")
            
            when {
                availableMemory < criticalThreshold -> {
                    listener?.onMemoryCritical(availableMemory)
                    OomLog.e("MemoryMonitor", "Critical memory level: ${formatSize(availableMemory)}")
                }
                availableMemory < warningThreshold -> {
                    listener?.onMemoryWarning(availableMemory)
                    OomLog.w("MemoryMonitor", "Warning memory level: ${formatSize(availableMemory)}")
                }
            }
        } catch (e: Exception) {
            OomLog.e("MemoryMonitor", "Error checking memory", e)
        }
        
        // 调度下次检查
        handler.postDelayed({ scheduleCheck() }, checkInterval)
    }
    
    /**
     * 获取当前可用内存
     * @return 可用内存（字节）
     */
    fun getAvailableMemory(): Long {
        val memoryInfo = ActivityManager.MemoryInfo()
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        activityManager.getMemoryInfo(memoryInfo)
        return memoryInfo.availMem
    }
    
    /**
     * 获取内存使用详情
     * @return 内存信息字符串
     */
    fun getMemoryInfo(): String {
        val memoryInfo = ActivityManager.MemoryInfo()
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        activityManager.getMemoryInfo(memoryInfo)
        
        val runtime = Runtime.getRuntime()
        val usedHeap = runtime.totalMemory() - runtime.freeMemory()
        val maxHeap = runtime.maxMemory()
        
        return buildString {
            append("=== Memory Info ===\n")
            append("System Total: ${formatSize(memoryInfo.totalMem)}\n")
            append("System Available: ${formatSize(memoryInfo.availMem)}\n")
            append("System Threshold: ${formatSize(memoryInfo.threshold)}\n")
            append("System Low Memory: ${memoryInfo.lowMemory}\n")
            append("Heap Max: ${formatSize(maxHeap)}\n")
            append("Heap Used: ${formatSize(usedHeap)} (${(usedHeap.toFloat() / maxHeap.toFloat() * 100).toInt()}%)\n")
        }
    }
    
    /**
     * 获取堆内存快照
     * @return 堆内存快照对象
     */
    fun getHeapSnapshot(): HeapSnapshot {
        val runtime = Runtime.getRuntime()
        return HeapSnapshot(
            freeMemory = runtime.freeMemory(),
            totalMemory = runtime.totalMemory(),
            maxMemory = runtime.maxMemory(),
            usedMemory = runtime.totalMemory() - runtime.freeMemory()
        )
    }
    
    /**
     * 格式化内存大小
     * @param bytes 字节数
     * @return 格式化后的字符串
     */
    private fun formatSize(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> String.format("%.2f KB", bytes / 1024.0)
            bytes < 1024 * 1024 * 1024 -> String.format("%.2f MB", bytes / (1024.0 * 1024))
            else -> String.format("%.2f GB", bytes / (1024.0 * 1024 * 1024))
        }
    }
    
    /**
     * 堆内存快照数据类
     */
    data class HeapSnapshot(
        val freeMemory: Long,
        val totalMemory: Long,
        val maxMemory: Long,
        val usedMemory: Long
    )
}

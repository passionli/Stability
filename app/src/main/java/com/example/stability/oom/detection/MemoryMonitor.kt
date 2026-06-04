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
class MemoryMonitor private constructor(
    private val context: Context,
    private var config: Config = Config()
) {
    
    companion object {
        @Volatile
        private var instance: MemoryMonitor? = null
        
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
     * 内存监控配置（不可变数据类）
     */
    data class Config(
        val warningThreshold: Long = 50 * 1024 * 1024L,
        val criticalThreshold: Long = 20 * 1024 * 1024L,
        val checkInterval: Long = 2000L
    )
    
    private val handler = Handler(Looper.getMainLooper())
    private val isRunning = AtomicBoolean(false)
    private var listener: MemoryListener? = null
    
    interface MemoryListener {
        fun onMemoryWarning(availableMemory: Long)
        fun onMemoryCritical(availableMemory: Long)
    }
    
    /**
     * 更新配置
     */
    fun updateConfig(newConfig: Config) {
        this.config = newConfig
        OomLog.i("MemoryMonitor", "Config updated: $newConfig")
    }
    
    /**
     * 设置阈值（兼容旧 API）
     */
    fun setThresholds(warningThreshold: Long, criticalThreshold: Long) {
        updateConfig(config.copy(warningThreshold = warningThreshold, criticalThreshold = criticalThreshold))
    }
    
    /**
     * 设置检查间隔（兼容旧 API）
     */
    fun setCheckInterval(interval: Long) {
        updateConfig(config.copy(checkInterval = interval))
    }
    
    fun start(listener: MemoryListener) {
        if (isRunning.compareAndSet(false, true)) {
            this.listener = listener
            OomLog.i("MemoryMonitor", "Starting memory monitoring")
            scheduleCheck()
        } else {
            OomLog.w("MemoryMonitor", "Memory monitor is already running")
        }
    }
    
    fun stop() {
        if (isRunning.compareAndSet(true, false)) {
            handler.removeCallbacksAndMessages(null)
            OomLog.i("MemoryMonitor", "Stopping memory monitoring")
        } else {
            OomLog.w("MemoryMonitor", "Memory monitor is not running")
        }
    }
    
    fun isRunning(): Boolean = isRunning.get()
    
    /**
     * 内存级别枚举
     */
    private enum class MemoryLevel {
        NORMAL, WARNING, CRITICAL
    }
    
    /**
     * 检查内存级别（纯函数）
     */
    private fun checkMemoryLevel(availableMemory: Long): MemoryLevel = when {
        availableMemory < config.criticalThreshold -> MemoryLevel.CRITICAL
        availableMemory < config.warningThreshold -> MemoryLevel.WARNING
        else -> MemoryLevel.NORMAL
    }
    
    /**
     * 定时检查内存状态
     */
    private fun scheduleCheck() {
        if (!isRunning.get()) {
            return
        }
        
        runCatching {
            val availableMemory = getAvailableMemory()
            OomLog.d("MemoryMonitor", "Available memory: ${formatSize(availableMemory)}")
            
            // 使用函数式方式处理内存级别
            handleMemoryLevel(checkMemoryLevel(availableMemory), availableMemory)
        }.onFailure { e ->
            OomLog.e("MemoryMonitor", "Error checking memory", e)
        }
        
        handler.postDelayed({ scheduleCheck() }, config.checkInterval)
    }
    
    /**
     * 处理内存级别（副作用处理）
     */
    private fun handleMemoryLevel(level: MemoryLevel, availableMemory: Long) {
        when (level) {
            MemoryLevel.CRITICAL -> {
                listener?.onMemoryCritical(availableMemory)
                OomLog.e("MemoryMonitor", "Critical memory level: ${formatSize(availableMemory)}")
            }
            MemoryLevel.WARNING -> {
                listener?.onMemoryWarning(availableMemory)
                OomLog.w("MemoryMonitor", "Warning memory level: ${formatSize(availableMemory)}")
            }
            MemoryLevel.NORMAL -> {
                // 正常状态，无需处理
            }
        }
    }
    
    fun getAvailableMemory(): Long {
        val memoryInfo = ActivityManager.MemoryInfo()
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        activityManager.getMemoryInfo(memoryInfo)
        return memoryInfo.availMem
    }
    
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
    
    fun getHeapSnapshot(): HeapSnapshot {
        val runtime = Runtime.getRuntime()
        return HeapSnapshot(
            freeMemory = runtime.freeMemory(),
            totalMemory = runtime.totalMemory(),
            maxMemory = runtime.maxMemory(),
            usedMemory = runtime.totalMemory() - runtime.freeMemory()
        )
    }
    
    private fun formatSize(bytes: Long): String = when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> String.format("%.2f KB", bytes / 1024.0)
        bytes < 1024 * 1024 * 1024 -> String.format("%.2f MB", bytes / (1024.0 * 1024))
        else -> String.format("%.2f GB", bytes / (1024.0 * 1024 * 1024))
    }
    
    data class HeapSnapshot(
        val freeMemory: Long,
        val totalMemory: Long,
        val maxMemory: Long,
        val usedMemory: Long
    )
}
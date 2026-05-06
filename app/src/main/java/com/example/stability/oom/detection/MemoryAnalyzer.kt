package com.example.stability.oom.detection

import android.content.Context
import android.os.Debug
import android.app.ActivityManager
import com.example.stability.oom.utils.OomLog
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

/**
 * 内存分析器
 * 提供内存转储、分析等功能
 */
class MemoryAnalyzer(private val context: Context) {
    
    /**
     * 堆转储文件保存目录
     */
    private val dumpDir: File by lazy {
        val dir = File(context.cacheDir, "heap_dumps")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        dir
    }
    
    /**
     * 触发堆转储
     * @param fileName 转储文件名（不含扩展名）
     * @return 转储文件路径，如果失败返回 null
     */
    fun dumpHeap(fileName: String): String? {
        return try {
            val timestamp = System.currentTimeMillis()
            val dumpFile = File(dumpDir, "${fileName}_$timestamp.hprof")
            
            // 触发堆转储
            Debug.dumpHprofData(dumpFile.absolutePath)
            
            OomLog.i("MemoryAnalyzer", "Heap dump saved to: ${dumpFile.absolutePath}")
            dumpFile.absolutePath
        } catch (e: IOException) {
            OomLog.e("MemoryAnalyzer", "Failed to dump heap", e)
            null
        }
    }
    
    /**
     * 获取当前内存使用状态
     * @return 内存状态对象
     */
    fun getMemoryStatus(): MemoryStatus {
        val runtime = Runtime.getRuntime()
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)
        
        return MemoryStatus(
            // Java 堆内存信息
            heapMax = runtime.maxMemory(),
            heapTotal = runtime.totalMemory(),
            heapUsed = runtime.totalMemory() - runtime.freeMemory(),
            heapFree = runtime.freeMemory(),
            
            // 系统内存信息
            systemTotal = memoryInfo.totalMem,
            systemAvailable = memoryInfo.availMem,
            systemThreshold = memoryInfo.threshold,
            isLowMemory = memoryInfo.lowMemory,
            
            // 应用内存信息
            appPss = getAppPss(),
            appPrivateDirty = getAppPrivateDirty()
        )
    }
    
    /**
     * 获取应用 PSS（Proportional Set Size）内存
     * PSS 是应用实际使用的物理内存大小
     * @return PSS 值（字节），如果获取失败返回 0
     */
    fun getAppPss(): Long {
        return try {
            val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val myPid = android.os.Process.myPid()
            
            // 获取所有进程的内存信息
            val processInfos = activityManager.getProcessMemoryInfo(intArrayOf(myPid))
            
            if (processInfos.isNotEmpty()) {
                // 返回 PSS（单位：KB）转换为字节
                processInfos[0].getTotalPss() * 1024L
            } else {
                0L
            }
        } catch (e: Exception) {
            OomLog.e("MemoryAnalyzer", "Failed to get PSS", e)
            0L
        }
    }
    
    /**
     * 获取应用私有脏内存
     * Private Dirty 是应用独占的、已修改的内存页大小
     * @return Private Dirty 值（字节），如果获取失败返回 0
     */
    fun getAppPrivateDirty(): Long {
        return try {
            val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val myPid = android.os.Process.myPid()
            
            val processInfos = activityManager.getProcessMemoryInfo(intArrayOf(myPid))
            
            if (processInfos.isNotEmpty()) {
                // 返回 Private Dirty（单位：KB）转换为字节
                processInfos[0].getTotalPrivateDirty() * 1024L
            } else {
                0L
            }
        } catch (e: Exception) {
            OomLog.e("MemoryAnalyzer", "Failed to get Private Dirty", e)
            0L
        }
    }
    
    /**
     * 获取内存状态描述
     * @return 格式化的内存状态字符串
     */
    fun getMemoryStatusDescription(): String {
        val status = getMemoryStatus()
        return buildString {
            append("=== Memory Status ===\n")
            append("\n[Java Heap]\n")
            append("  Max: ${formatSize(status.heapMax)}\n")
            append("  Total: ${formatSize(status.heapTotal)}\n")
            append("  Used: ${formatSize(status.heapUsed)} (${status.heapUsedPercent}%)\n")
            append("  Free: ${formatSize(status.heapFree)}\n")
            append("\n[System Memory]\n")
            append("  Total: ${formatSize(status.systemTotal)}\n")
            append("  Available: ${formatSize(status.systemAvailable)}\n")
            append("  Threshold: ${formatSize(status.systemThreshold)}\n")
            append("  Is Low Memory: ${status.isLowMemory}\n")
            append("\n[App Memory]\n")
            append("  PSS: ${formatSize(status.appPss)}\n")
            append("  Private Dirty: ${formatSize(status.appPrivateDirty)}\n")
        }
    }
    
    /**
     * 检查是否存在内存问题
     * @return 内存问题描述列表
     */
    fun checkMemoryIssues(): List<String> {
        val issues = mutableListOf<String>()
        val status = getMemoryStatus()
        
        // 检查堆内存使用率
        if (status.heapUsedPercent > 90) {
            issues.add("High heap usage: ${status.heapUsedPercent}% (consider releasing memory)")
        }
        
        // 检查系统低内存状态
        if (status.isLowMemory) {
            issues.add("System is in low memory state")
        }
        
        // 检查可用系统内存
        if (status.systemAvailable < 50 * 1024 * 1024) {
            issues.add("System available memory is low: ${formatSize(status.systemAvailable)}")
        }
        
        return issues
    }
    
    /**
     * 格式化内存大小
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
     * 内存状态数据类
     */
    data class MemoryStatus(
        // Java 堆内存
        val heapMax: Long,
        val heapTotal: Long,
        val heapUsed: Long,
        val heapFree: Long,
        
        // 系统内存
        val systemTotal: Long,
        val systemAvailable: Long,
        val systemThreshold: Long,
        val isLowMemory: Boolean,
        
        // 应用内存
        val appPss: Long,
        val appPrivateDirty: Long
    ) {
        /**
         * 堆内存使用率百分比
         */
        val heapUsedPercent: Int
            get() = if (heapMax > 0) {
                ((heapUsed.toFloat() / heapMax.toFloat()) * 100).toInt()
            } else {
                0
            }
    }
}

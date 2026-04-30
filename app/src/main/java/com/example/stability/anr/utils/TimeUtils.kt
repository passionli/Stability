package com.example.stability.anr.utils

/**
 * 时间工具类
 * 提供时间相关的辅助方法
 */
object TimeUtils {
    
    /**
     * 毫秒转秒
     */
    fun msToSeconds(ms: Long): Double {
        return ms / 1000.0
    }
    
    /**
     * 秒转毫秒
     */
    fun secondsToMs(seconds: Double): Long {
        return (seconds * 1000).toLong()
    }
    
    /**
     * 格式化时间（毫秒）为可读字符串
     */
    fun formatTimeMs(ms: Long): String {
        return when {
            ms < 1000 -> "${ms}ms"
            ms < 60000 -> String.format("%.2fs", msToSeconds(ms))
            ms < 3600000 -> {
                val minutes = ms / 60000
                val seconds = (ms % 60000) / 1000
                String.format("%dm %ds", minutes, seconds)
            }
            else -> {
                val hours = ms / 3600000
                val minutes = (ms % 3600000) / 60000
                val seconds = (ms % 60000) / 1000
                String.format("%dh %dm %ds", hours, minutes, seconds)
            }
        }
    }
    
    /**
     * 获取当前时间戳（毫秒）
     */
    fun currentTimeMillis(): Long {
        return System.currentTimeMillis()
    }
    
    /**
     * 获取当前时间戳（纳秒）
     */
    fun currentTimeNanos(): Long {
        return System.nanoTime()
    }
    
    /**
     * 计算时间差（毫秒）
     */
    fun elapsedMs(startTime: Long): Long {
        return currentTimeMillis() - startTime
    }
    
    /**
     * 计算时间差（纳秒）
     */
    fun elapsedNanos(startTime: Long): Long {
        return currentTimeNanos() - startTime
    }
    
    /**
     * 纳秒转毫秒
     */
    fun nanosToMs(nanos: Long): Double {
        return nanos / 1_000_000.0
    }
    
    /**
     * 格式化纳秒为可读字符串
     */
    fun formatNanos(nanos: Long): String {
        return when {
            nanos < 1000 -> "${nanos}ns"
            nanos < 1_000_000 -> String.format("%.2fμs", nanos / 1000.0)
            nanos < 1_000_000_000 -> String.format("%.2fms", nanos / 1_000_000.0)
            else -> String.format("%.2fs", nanos / 1_000_000_000.0)
        }
    }
    
    /**
     * 判断是否超过指定时间阈值
     */
    fun isOverThreshold(startTime: Long, thresholdMs: Long): Boolean {
        return elapsedMs(startTime) > thresholdMs
    }
    
    /**
     * 获取时间戳的可读格式
     */
    fun getReadableTimestamp(): String {
        val now = java.util.Date()
        val formatter = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", java.util.Locale.getDefault())
        return formatter.format(now)
    }
}

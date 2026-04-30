package com.example.stability.anr.prevention

import android.os.Handler
import android.os.Looper
import com.example.stability.anr.utils.AnrLog
import com.example.stability.anr.utils.TimeUtils

/**
 * 主线程阻塞检测器
 * 通过定期向主线程消息队列发送检测消息来监控主线程响应性
 */
class MainThreadBlockDetector {
    
    /**
     * 警告阈值（100ms）- 超过此时间认为是慢操作
     */
    private val WARNING_THRESHOLD = 100L
    
    /**
     * 严重阈值（500ms）- 超过此时间认为是严重阻塞
     */
    private val CRITICAL_THRESHOLD = 500L
    
    /**
     * 检测间隔（50ms）
     */
    private val CHECK_INTERVAL = 50L
    
    /**
     * 主线程 Handler
     */
    private val mainHandler = Handler(Looper.getMainLooper())
    
    /**
     * 检测回调
     */
    private var listener: BlockListener? = null
    
    /**
     * 是否正在运行
     */
    @Volatile
    private var isRunning = false
    
    /**
     * 最后一次检测时间
     */
    @Volatile
    private var lastCheckTime = 0L
    
    /**
     * 慢操作计数
     */
    @Volatile
    private var slowOperationCount = 0
    
    /**
     * 阻塞回调接口
     */
    interface BlockListener {
        /**
         * 检测到慢操作
         * @param durationMs 操作耗时
         */
        fun onSlowOperation(durationMs: Long)
        
        /**
         * 检测到严重阻塞
         * @param durationMs 阻塞时长
         */
        fun onCriticalBlock(durationMs: Long)
        
        /**
         * 检测到持续慢操作
         * @param count 连续慢操作次数
         */
        fun onContinuousSlowOperations(count: Int)
    }
    
    /**
     * 设置监听器
     */
    fun setListener(listener: BlockListener): MainThreadBlockDetector {
        this.listener = listener
        return this
    }
    
    /**
     * 开始检测
     */
    fun start() {
        if (isRunning) {
            AnrLog.w("MainThreadBlockDetector is already running")
            return
        }
        
        isRunning = true
        lastCheckTime = TimeUtils.currentTimeMillis()
        slowOperationCount = 0
        
        AnrLog.i("Starting MainThreadBlockDetector")
        
        scheduleNextCheck()
    }
    
    /**
     * 停止检测
     */
    fun stop() {
        isRunning = false
        listener = null
        AnrLog.i("MainThreadBlockDetector stopped")
    }
    
    /**
     * 调度下一次检测
     */
    private fun scheduleNextCheck() {
        if (!isRunning) return
        
        mainHandler.postDelayed({
            if (!isRunning) return@postDelayed
            
            val currentTime = TimeUtils.currentTimeMillis()
            val elapsed = currentTime - lastCheckTime
            
            // 检查是否超过阈值
            when {
                elapsed >= CRITICAL_THRESHOLD -> {
                    AnrLog.performanceError("Main thread critical block detected", elapsed)
                    listener?.onCriticalBlock(elapsed)
                    slowOperationCount = 0
                }
                elapsed >= WARNING_THRESHOLD -> {
                    AnrLog.performanceWarning("Main thread slow operation detected", elapsed)
                    listener?.onSlowOperation(elapsed)
                    slowOperationCount++
                    
                    // 连续慢操作检测
                    if (slowOperationCount >= 5) {
                        listener?.onContinuousSlowOperations(slowOperationCount)
                        slowOperationCount = 0
                    }
                }
                else -> {
                    // 正常响应，重置计数器
                    slowOperationCount = 0
                }
            }
            
            lastCheckTime = currentTime
            scheduleNextCheck()
        }, CHECK_INTERVAL)
    }
    
    /**
     * 获取当前检测状态
     */
    fun getStatus(): String {
        return buildString {
            append("MainThreadBlockDetector Status:\n")
            append("  Running: $isRunning\n")
            append("  Last Check: ${TimeUtils.formatTimeMs(lastCheckTime)}\n")
            append("  Slow Operation Count: $slowOperationCount\n")
        }
    }
    
    /**
     * 重置计数器
     */
    fun resetCounters() {
        slowOperationCount = 0
        AnrLog.d("MainThreadBlockDetector counters reset")
    }
}

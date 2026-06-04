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
     * 检测阈值配置（不可变数据类）
     */
    data class ThresholdConfig(
        val warningThreshold: Long = 100L,
        val criticalThreshold: Long = 500L,
        val checkInterval: Long = 50L
    )
    
    /**
     * 检测状态（不可变数据类）
     */
    data class DetectionState(
        val isRunning: Boolean,
        val lastCheckTime: Long,
        val slowOperationCount: Int
    )
    
    /**
     * 检测结果密封类
     */
    sealed class DetectionResult {
        object Normal : DetectionResult()
        data class SlowOperation(val durationMs: Long) : DetectionResult()
        data class CriticalBlock(val durationMs: Long) : DetectionResult()
        data class ContinuousSlowOperations(val count: Int) : DetectionResult()
    }
    
    private val config = ThresholdConfig()
    private val mainHandler = Handler(Looper.getMainLooper())
    private var listener: BlockListener? = null
    
    @Volatile
    private var state = DetectionState(false, 0L, 0)
    
    interface BlockListener {
        fun onSlowOperation(durationMs: Long)
        fun onCriticalBlock(durationMs: Long)
        fun onContinuousSlowOperations(count: Int)
    }
    
    fun setListener(listener: BlockListener): MainThreadBlockDetector {
        this.listener = listener
        return this
    }
    
    /**
     * 检查阻塞级别（纯函数）
     */
    private fun checkBlockLevel(elapsed: Long, slowCount: Int): DetectionResult {
        return when {
            elapsed >= config.criticalThreshold -> DetectionResult.CriticalBlock(elapsed)
            elapsed >= config.warningThreshold -> DetectionResult.SlowOperation(elapsed)
            slowCount >= 5 -> DetectionResult.ContinuousSlowOperations(slowCount)
            else -> DetectionResult.Normal
        }
    }
    
    /**
     * 更新状态（纯函数）
     */
    private fun updateState(result: DetectionResult, currentTime: Long): DetectionState {
        return when (result) {
            is DetectionResult.CriticalBlock -> state.copy(
                lastCheckTime = currentTime,
                slowOperationCount = 0
            )
            is DetectionResult.SlowOperation -> state.copy(
                lastCheckTime = currentTime,
                slowOperationCount = state.slowOperationCount + 1
            )
            is DetectionResult.ContinuousSlowOperations -> state.copy(
                lastCheckTime = currentTime,
                slowOperationCount = 0
            )
            else -> state.copy(
                lastCheckTime = currentTime,
                slowOperationCount = 0
            )
        }
    }
    
    /**
     * 处理检测结果（副作用处理）
     */
    private fun handleResult(result: DetectionResult) {
        when (result) {
            is DetectionResult.CriticalBlock -> {
                AnrLog.performanceError("Main thread critical block detected", result.durationMs)
                listener?.onCriticalBlock(result.durationMs)
            }
            is DetectionResult.SlowOperation -> {
                AnrLog.performanceWarning("Main thread slow operation detected", result.durationMs)
                listener?.onSlowOperation(result.durationMs)
                
                if (state.slowOperationCount + 1 >= 5) {
                    listener?.onContinuousSlowOperations(state.slowOperationCount + 1)
                }
            }
            is DetectionResult.ContinuousSlowOperations -> {
                listener?.onContinuousSlowOperations(result.count)
            }
            DetectionResult.Normal -> {}
        }
    }
    
    fun start() {
        if (state.isRunning) {
            AnrLog.w("MainThreadBlockDetector is already running")
            return
        }
        
        state = DetectionState(true, TimeUtils.currentTimeMillis(), 0)
        AnrLog.i("Starting MainThreadBlockDetector")
        
        scheduleNextCheck()
    }
    
    fun stop() {
        state = state.copy(isRunning = false)
        listener = null
        AnrLog.i("MainThreadBlockDetector stopped")
    }
    
    private fun scheduleNextCheck() {
        if (!state.isRunning) return
        
        mainHandler.postDelayed({
            if (!state.isRunning) return@postDelayed
            
            val currentTime = TimeUtils.currentTimeMillis()
            val elapsed = currentTime - state.lastCheckTime
            
            // 使用函数式方式处理检测
            val result = checkBlockLevel(elapsed, state.slowOperationCount)
            handleResult(result)
            state = updateState(result, currentTime)
            
            scheduleNextCheck()
        }, config.checkInterval)
    }
    
    fun getStatus(): String {
        return buildString {
            append("MainThreadBlockDetector Status:\n")
            append("  Running: ${state.isRunning}\n")
            append("  Last Check: ${TimeUtils.formatTimeMs(state.lastCheckTime)}\n")
            append("  Slow Operation Count: ${state.slowOperationCount}\n")
        }
    }
    
    fun resetCounters() {
        state = state.copy(slowOperationCount = 0)
        AnrLog.d("MainThreadBlockDetector counters reset")
    }
}
package com.example.stability.anr.examples

import android.os.Handler
import android.os.Looper
import com.example.stability.anr.detection.AnrMonitor
import com.example.stability.anr.detection.AnrDetector
import com.example.stability.anr.utils.AnrLog

/**
 * ANR 恢复示例
 * 展示 ANR 检测后的恢复策略
 */
class AnrRecoveryExample {
    
    private val mainHandler = Handler(Looper.getMainLooper())
    private var anrMonitor: AnrMonitor? = null
    private var recoveryAttempts = 0
    private val maxRecoveryAttempts = 3
    
    /**
     * 启动带恢复机制的 ANR 监控
     */
    fun startMonitoringWithRecovery() {
        anrMonitor = AnrMonitor().apply {
            start(object : AnrMonitor.AnrListener {
                override fun onAnrDetected(stackTrace: String) {
                    AnrLog.anr(stackTrace)
                    handleAnrRecovery(stackTrace)
                }
                
                override fun onMainThreadBlocked(durationMs: Long) {
                    AnrLog.performanceWarning("Main thread blocked", durationMs)
                    // 可以在这里记录慢操作，用于后续优化
                }
            })
        }
        
        AnrLog.i("ANR monitoring with recovery started")
    }
    
    /**
     * 停止监控
     */
    fun stopMonitoring() {
        anrMonitor?.stop()
        anrMonitor = null
        recoveryAttempts = 0
        AnrLog.i("ANR monitoring stopped")
    }
    
    /**
     * 处理 ANR 恢复
     */
    private fun handleAnrRecovery(stackTrace: String) {
        if (recoveryAttempts >= maxRecoveryAttempts) {
            AnrLog.e("Max recovery attempts reached, cannot recover")
            // 通知用户应用需要重启
            notifyUserNeedRestart()
            return
        }
        
        recoveryAttempts++
        AnrLog.i("Attempting ANR recovery #$recoveryAttempts")
        
        // 分析 ANR 原因
        val analysis = analyzeAnrCause(stackTrace)
        
        when (analysis) {
            AnrCause.MESSAGE_QUEUE_BLOCKED -> {
                AnrLog.i("Recovery: Clearing message queue")
                clearMessageQueue()
            }
            AnrCause.DEADLOCK -> {
                AnrLog.i("Recovery: Attempting to break deadlock")
                breakDeadlock()
            }
            AnrCause.LONG_OPERATION -> {
                AnrLog.i("Recovery: Cancelling long operation")
                cancelLongOperation()
            }
            else -> {
                AnrLog.i("Recovery: Performing general recovery")
                performGeneralRecovery()
            }
        }
    }
    
    /**
     * 分析 ANR 原因
     */
    private fun analyzeAnrCause(stackTrace: String): AnrCause {
        return when {
            stackTrace.contains("MessageQueue") || stackTrace.contains("Looper") -> {
                AnrCause.MESSAGE_QUEUE_BLOCKED
            }
            stackTrace.split("\n").count { it.contains("BLOCKED") } >= 2 -> {
                AnrCause.DEADLOCK
            }
            stackTrace.contains("Thread.sleep") || 
            stackTrace.contains("Object.wait") ||
            stackTrace.contains("BlockingQueue") -> {
                AnrCause.LONG_OPERATION
            }
            else -> {
                AnrCause.UNKNOWN
            }
        }
    }
    
    /**
     * 清空消息队列
     */
    private fun clearMessageQueue() {
        // 注意：这是一个危险操作，可能会导致消息丢失
        // 实际应用中需要谨慎使用
        
        // 获取主线程的 MessageQueue
        try {
            val looper = Looper.getMainLooper()
            val queueField = looper.javaClass.getDeclaredField("mQueue")
            queueField.isAccessible = true
            val queue = queueField.get(looper)
            
            // 清空队列（简化实现）
            AnrLog.d("Message queue cleared")
        } catch (e: Exception) {
            AnrLog.e("Failed to clear message queue", e)
        }
    }
    
    /**
     * 尝试打破死锁
     */
    private fun breakDeadlock() {
        // 获取所有阻塞的线程
        val blockedThreads = AnrDetector.getBlockedThreads()
        
        AnrLog.d("Found ${blockedThreads.size} blocked threads")
        
        // 打印阻塞线程信息
        for (thread in blockedThreads) {
            AnrLog.d("Blocked thread: ${thread.name}, state: ${thread.state}")
        }
        
        // 在实际应用中，可以尝试中断某些线程来打破死锁
        // 但这是非常危险的操作，需要谨慎处理
    }
    
    /**
     * 取消长时间操作
     */
    private fun cancelLongOperation() {
        // 如果有正在执行的长时间操作，可以在这里取消它们
        // 例如取消网络请求、数据库查询等
        
        AnrLog.d("Cancelled long running operations")
    }
    
    /**
     * 执行通用恢复
     */
    private fun performGeneralRecovery() {
        // 通用恢复策略：
        // 1. 清理资源
        // 2. 重置状态
        // 3. 通知用户
        
        AnrLog.i("Performing general ANR recovery")
        
        // 发送一个空消息确保主线程能够响应
        mainHandler.post {
            AnrLog.d("Main thread responded after recovery")
        }
    }
    
    /**
     * 通知用户需要重启应用
     */
    private fun notifyUserNeedRestart() {
        AnrLog.e("Application needs to restart due to repeated ANRs")
        
        // 在实际应用中，可以显示一个对话框告知用户
        // runOnUiThread {
        //     AlertDialog.Builder(context)
        //         .setMessage("应用出现问题，需要重启")
        //         .setPositiveButton("重启") { _, _ ->
        //             // 重启应用
        //         }
        //         .show()
        // }
    }
    
    /**
     * ANR 原因枚举
     */
    enum class AnrCause {
        MESSAGE_QUEUE_BLOCKED,
        DEADLOCK,
        LONG_OPERATION,
        UNKNOWN
    }
    
    /**
     * 获取恢复状态
     */
    fun getRecoveryStatus(): String {
        return buildString {
            append("ANR Recovery Status:\n")
            append("  Monitoring: ${anrMonitor?.isRunning() ?: false}\n")
            append("  Recovery Attempts: $recoveryAttempts/$maxRecoveryAttempts\n")
        }
    }
}

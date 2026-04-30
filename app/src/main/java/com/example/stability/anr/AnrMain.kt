package com.example.stability.anr

import android.content.Context
import com.example.stability.anr.detection.AnrDetector
import com.example.stability.anr.detection.AnrMonitor
import com.example.stability.anr.detection.Watchdog
import com.example.stability.anr.prevention.MainThreadBlockDetector
import com.example.stability.anr.prevention.StrictModeExample
import com.example.stability.anr.utils.AnrLog

/**
 * ANR 模块入口
 * 提供统一的 ANR 监控和预防功能入口
 */
object AnrMain {
    
    /**
     * ANR 监控器实例
     */
    private var anrMonitor: AnrMonitor? = null
    
    /**
     * 看门狗实例
     */
    private var watchdog: Watchdog? = null
    
    /**
     * 主线程阻塞检测器
     */
    private var blockDetector: MainThreadBlockDetector? = null
    
    /**
     * 初始化 ANR 模块
     * @param context 上下文
     * @param enableStrictMode 是否启用 StrictMode（建议仅在开发阶段启用）
     */
    fun initialize(context: Context, enableStrictMode: Boolean = false) {
        AnrLog.i("Initializing ANR module")
        
        // 启用 StrictMode（仅开发阶段）
        if (enableStrictMode) {
            StrictModeExample.enableStrictMode()
        }
        
        // 初始化组件
        anrMonitor = AnrMonitor()
        watchdog = Watchdog()
        blockDetector = MainThreadBlockDetector()
        
        AnrLog.i("ANR module initialized successfully")
    }
    
    /**
     * 启动 ANR 监控
     * @param listener ANR 监听器
     */
    fun startMonitoring(listener: AnrMonitor.AnrListener) {
        AnrLog.i("Starting ANR monitoring")
        
        // 启动 ANR 监控器
        anrMonitor?.start(listener)
        
        // 启动看门狗
        watchdog?.let { wd ->
            wd.setListener(object : Watchdog.WatchdogListener {
                override fun onAnr(timeoutMs: Long) {
                    AnrLog.e("Watchdog detected ANR after ${timeoutMs}ms")
                }
                
                override fun onSlowResponse(responseTimeMs: Long) {
                    AnrLog.w("Watchdog detected slow response: ${responseTimeMs}ms")
                }
            })
            wd.start()
        }
        
        // 启动主线程阻塞检测器
        blockDetector?.let { detector ->
            detector.setListener(object : MainThreadBlockDetector.BlockListener {
                override fun onSlowOperation(durationMs: Long) {
                    AnrLog.w("Main thread slow operation: ${durationMs}ms")
                }
                
                override fun onCriticalBlock(durationMs: Long) {
                    AnrLog.e("Main thread critical block: ${durationMs}ms")
                }
                
                override fun onContinuousSlowOperations(count: Int) {
                    AnrLog.w("Continuous slow operations detected: $count")
                }
            })
            detector.start()
        }
    }
    
    /**
     * 停止 ANR 监控
     */
    fun stopMonitoring() {
        AnrLog.i("Stopping ANR monitoring")
        
        anrMonitor?.stop()
        watchdog?.stop()
        blockDetector?.stop()
    }
    
    /**
     * 检查当前主线程状态
     */
    fun checkMainThreadStatus(): String {
        return buildString {
            append("=== Main Thread Status ===\n")
            append("Is Main Thread: ${AnrDetector.isMainThread()}\n")
            append("Thread: ${AnrDetector.getMainThread().name}\n")
            append("State: ${AnrDetector.getMainThread().state}\n")
        }
    }
    
    /**
     * 输出线程转储信息
     */
    fun dumpThreads() {
        AnrDetector.dumpThreadsToLog()
    }
    
    /**
     * 检查死锁风险
     */
    fun checkDeadlockRisk(): Boolean {
        return AnrDetector.checkDeadlockRisk()
    }
    
    /**
     * 获取模块状态
     */
    fun getStatus(): String {
        return buildString {
            append("=== ANR Module Status ===\n")
            append("ANR Monitor: ${anrMonitor?.isRunning() ?: false}\n")
            append("Watchdog: ${watchdog?.isRunning() ?: false}\n")
            append("Block Detector: ${blockDetector?.getStatus() ?: "Not initialized"}\n")
            append("StrictMode Enabled: ${isStrictModeEnabled()}\n")
        }
    }
    
    /**
     * 检查 StrictMode 是否启用
     */
    private fun isStrictModeEnabled(): Boolean {
        try {
            val threadPolicy = android.os.StrictMode.getThreadPolicy()
            // 使用反射检查 StrictMode 策略
            val threadPolicyClass = threadPolicy.javaClass
            return threadPolicyClass.simpleName != "Lax"
        } catch (e: Exception) {
            return false
        }
    }
    
    /**
     * 释放资源
     */
    fun release() {
        stopMonitoring()
        anrMonitor = null
        watchdog = null
        blockDetector = null
        AnrLog.i("ANR module released")
    }
}

package com.example.stability.anr.detection

import android.os.Handler
import android.os.Looper
import com.example.stability.anr.utils.AnrLog
import java.util.concurrent.atomic.AtomicBoolean

/**
 * 看门狗实现
 * 简化版的 Android Watchdog 机制，用于监控主线程响应
 * 
 * 原理：通过在主线程消息队列中定期设置一个标记，后台线程检查该标记是否被更新
 * 如果标记在超时时间内未被更新，说明主线程被阻塞
 */
class Watchdog {
    
    /**
     * 默认超时时间（5秒，与系统ANR阈值一致）
     */
    private val DEFAULT_TIMEOUT = 5000L
    
    /**
     * 默认检查间隔（1秒）
     */
    private val DEFAULT_INTERVAL = 1000L
    
    /**
     * 主线程 Handler
     */
    private val mainHandler = Handler(Looper.getMainLooper())
    
    /**
     * 响应标记，由主线程更新
     */
    @Volatile
    private var responded = false
    
    /**
     * 看门狗线程
     */
    private var watchdogThread: Thread? = null
    
    /**
     * 是否正在运行
     */
    @Volatile
    private var isRunning = false
    
    /**
     * 超时时间（毫秒）
     */
    private var timeoutMs = DEFAULT_TIMEOUT
    
    /**
     * 检查间隔（毫秒）
     */
    private var intervalMs = DEFAULT_INTERVAL
    
    /**
     * 监听器
     */
    private var listener: WatchdogListener? = null
    
    /**
     * 看门狗监听器接口
     */
    interface WatchdogListener {
        /**
         * 检测到 ANR
         * @param timeoutMs 超时时间
         */
        fun onAnr(timeoutMs: Long)
        
        /**
         * 检测到慢响应
         * @param responseTimeMs 响应时间
         */
        fun onSlowResponse(responseTimeMs: Long)
    }
    
    /**
     * 配置超时时间
     */
    fun setTimeout(timeoutMs: Long): Watchdog {
        this.timeoutMs = timeoutMs
        return this
    }
    
    /**
     * 配置检查间隔
     */
    fun setInterval(intervalMs: Long): Watchdog {
        this.intervalMs = intervalMs
        return this
    }
    
    /**
     * 设置监听器
     */
    fun setListener(listener: WatchdogListener): Watchdog {
        this.listener = listener
        return this
    }
    
    /**
     * 开始监控
     */
    fun start() {
        if (isRunning) {
            AnrLog.w("Watchdog is already running")
            return
        }
        
        isRunning = true
        AnrLog.i("Starting watchdog with timeout: ${timeoutMs}ms, interval: ${intervalMs}ms")
        
        watchdogThread = Thread(
            {
                while (isRunning) {
                    try {
                        check()
                    } catch (e: InterruptedException) {
                        AnrLog.d("Watchdog thread interrupted")
                        Thread.currentThread().interrupt()
                        break
                    } catch (e: Exception) {
                        AnrLog.e("Watchdog error", e)
                    }
                }
            },
            "Watchdog_Thread"
        ).apply {
            isDaemon = true
            start()
        }
    }
    
    /**
     * 停止监控
     */
    fun stop() {
        isRunning = false
        watchdogThread?.interrupt()
        watchdogThread = null
        listener = null
        AnrLog.i("Watchdog stopped")
    }
    
    /**
     * 执行一次检查
     */
    private fun check() {
        responded = false
        
        // 发送检测消息到主线程
        mainHandler.post {
            responded = true
        }
        
        // 等待超时时间
        Thread.sleep(timeoutMs)
        
        // 检查是否响应
        if (!responded) {
            // ANR 检测到！
            AnrLog.e("Watchdog detected ANR! Timeout: ${timeoutMs}ms")
            listener?.onAnr(timeoutMs)
        }
        
        // 等待检查间隔
        Thread.sleep(intervalMs)
    }
    
    /**
     * 检查当前是否正在运行
     */
    fun isRunning(): Boolean {
        return isRunning
    }
    
    /**
     * 获取当前配置的超时时间
     */
    fun getTimeout(): Long {
        return timeoutMs
    }
    
    /**
     * 获取当前配置的检查间隔
     */
    fun getInterval(): Long {
        return intervalMs
    }
}

package com.example.stability.anr.detection

import android.os.Handler
import android.os.Looper
import com.example.stability.anr.utils.AnrLog
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * ANR 监控器
 * 通过监控主线程消息队列的响应时间来检测 ANR
 * 
 * 工作原理：
 * 1. 在后台线程定期向主线程发送一个检测消息
 * 2. 如果主线程在规定时间内没有处理完这个消息，说明主线程可能被阻塞
 * 3. 当检测到阻塞时，收集所有线程的堆栈信息并回调给监听器
 */
class AnrMonitor(
    /**
     * ANR 检测超时时间（毫秒），默认5秒（系统标准）
     */
    private val timeoutMs: Long = 5000L,
    
    /**
     * 检测间隔时间（毫秒），默认1秒
     */
    private val checkIntervalMs: Long = 1000L
) {
    
    /**
     * 主线程 Handler
     */
    private val mainHandler = Handler(Looper.getMainLooper())
    
    /**
     * 监控线程
     */
    private var monitorThread: Thread? = null
    
    /**
     * ANR 监听器
     */
    private var listener: AnrListener? = null
    
    /**
     * 监控是否正在运行
     */
    @Volatile
    private var isRunning = false
    
    /**
     * ANR 监听器接口
     */
    interface AnrListener {
        /**
         * ANR 检测回调
         * @param stackTrace 所有线程的堆栈信息
         */
        fun onAnrDetected(stackTrace: String)
        
        /**
         * 主线程阻塞警告回调（未达到 ANR 级别）
         * @param durationMs 阻塞时长（毫秒）
         */
        fun onMainThreadBlocked(durationMs: Long)
    }
    
    /**
     * 开始监控
     * @param listener ANR 监听器
     */
    fun start(listener: AnrListener) {
        if (isRunning) {
            AnrLog.w("ANR monitor is already running")
            return
        }
        
        this.listener = listener
        isRunning = true
        
        AnrLog.i("Starting ANR monitor with timeout: ${timeoutMs}ms")
        
        // 创建并启动监控线程
        monitorThread = Thread(
            {
                while (isRunning) {
                    try {
                        checkMainThreadResponsiveness()
                    } catch (e: InterruptedException) {
                        AnrLog.d("ANR monitor thread interrupted")
                        Thread.currentThread().interrupt()
                        break
                    } catch (e: Exception) {
                        AnrLog.e("ANR monitor error", e)
                    }
                    
                    // 等待检测间隔
                    Thread.sleep(checkIntervalMs)
                }
            },
            "ANR_Monitor_Thread"
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
        monitorThread?.interrupt()
        monitorThread = null
        listener = null
        AnrLog.i("ANR monitor stopped")
    }
    
    /**
     * 检查主线程响应性
     */
    private fun checkMainThreadResponsiveness() {
        val startTime = System.currentTimeMillis()
        val latch = CountDownLatch(1)
        
        // 发送一个空消息到主线程
        mainHandler.post {
            latch.countDown()
        }
        
        // 等待主线程响应
        val completed = latch.await(timeoutMs, TimeUnit.MILLISECONDS)
        val elapsed = System.currentTimeMillis() - startTime
        
        if (!completed) {
            // ANR 发生！
            AnrLog.e("ANR detected! Main thread blocked for ${elapsed}ms")
            val stackTrace = captureStackTrace()
            listener?.onAnrDetected(stackTrace)
        } else if (elapsed > timeoutMs / 2) {
            // 接近超时，发出警告
            AnrLog.performanceWarning("Main thread slow", elapsed)
            listener?.onMainThreadBlocked(elapsed)
        }
    }
    
    /**
     * 捕获所有线程的堆栈信息
     * @return 格式化的堆栈信息字符串
     */
    fun captureStackTrace(): String {
        val sb = StringBuilder()
        sb.append("=== ANR Stack Trace ===\n")
        sb.append("Timestamp: ${System.currentTimeMillis()}\n")
        sb.append("Process ID: ${android.os.Process.myPid()}\n")
        sb.append("Thread Count: ${Thread.activeCount()}\n\n")
        
        val threadMap = Thread.getAllStackTraces()
        
        // 先获取主线程
        val mainThread = threadMap.keys.find { it.name == "main" }
        
        // 优先显示主线程
        mainThread?.let {
            sb.append("=== MAIN THREAD (ID: ${it.id}, State: ${it.state}) ===\n")
            appendThreadStackTrace(sb, threadMap[it] ?: emptyArray())
            sb.append("\n")
        }
        
        // 显示其他线程
        for (thread in threadMap.keys.sortedBy { it.id }) {
            if (thread != mainThread) {
                sb.append("=== ${thread.name} (ID: ${thread.id}, State: ${thread.state}) ===\n")
                appendThreadStackTrace(sb, threadMap[thread] ?: emptyArray())
                sb.append("\n")
            }
        }
        
        return sb.toString()
    }
    
    /**
     * 将单个线程的堆栈信息追加到 StringBuilder
     */
    private fun appendThreadStackTrace(sb: StringBuilder, stackTrace: Array<StackTraceElement>) {
        // 限制堆栈深度，最多显示30行
        val maxDepth = minOf(stackTrace.size, 30)
        
        for (i in 0 until maxDepth) {
            val element = stackTrace[i]
            sb.append("\tat ${element.className}.${element.methodName}(")
            sb.append("${element.fileName}:${element.lineNumber})\n")
        }
        
        if (stackTrace.size > maxDepth) {
            sb.append("\t... (${stackTrace.size - maxDepth} more lines)\n")
        }
    }
    
    /**
     * 获取当前监控状态
     */
    fun isRunning(): Boolean {
        return isRunning
    }
}

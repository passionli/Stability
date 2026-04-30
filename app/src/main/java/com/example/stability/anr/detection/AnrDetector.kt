package com.example.stability.anr.detection

import android.os.Looper
import com.example.stability.anr.utils.AnrLog

/**
 * ANR 检测器
 * 提供检测主线程状态和获取堆栈信息的工具方法
 */
object AnrDetector {
    
    /**
     * 检查当前线程是否是主线程
     */
    fun isMainThread(): Boolean {
        return Looper.getMainLooper().thread === Thread.currentThread()
    }
    
    /**
     * 检查主线程是否正在处理消息
     */
    fun isMainThreadBusy(): Boolean {
        return !isMainThread() && Looper.getMainLooper().queue.isIdle
    }
    
    /**
     * 获取主线程对象
     */
    fun getMainThread(): Thread {
        return Looper.getMainLooper().thread
    }
    
    /**
     * 获取主线程的堆栈信息
     */
    fun getMainThreadStackTrace(): String {
        val mainThread = getMainThread()
        return getThreadStackTrace(mainThread)
    }
    
    /**
     * 获取指定线程的堆栈信息
     * @param thread 目标线程
     */
    fun getThreadStackTrace(thread: Thread): String {
        val sb = StringBuilder()
        sb.append("Thread: ${thread.name} (id=${thread.id}, state=${thread.state})\n")
        
        val stackTrace = thread.stackTrace
        for (element in stackTrace) {
            sb.append("\tat ${element.className}.${element.methodName}(")
            sb.append("${element.fileName}:${element.lineNumber})\n")
        }
        
        return sb.toString()
    }
    
    /**
     * 获取所有线程的信息（简化版）
     */
    fun getAllThreadsInfo(): String {
        val sb = StringBuilder()
        val threads = Thread.getAllStackTraces()
        
        sb.append("=== All Threads (${threads.size}) ===\n")
        
        for ((thread, stackTrace) in threads) {
            sb.append("\n--- ${thread.name} ---\n")
            sb.append("  ID: ${thread.id}\n")
            sb.append("  State: ${thread.state}\n")
            sb.append("  Priority: ${thread.priority}\n")
            
            // 只显示前10行堆栈
            val maxLines = minOf(stackTrace.size, 10)
            for (i in 0 until maxLines) {
                sb.append("    at ${stackTrace[i].className}.${stackTrace[i].methodName}\n")
            }
            
            if (stackTrace.size > maxLines) {
                sb.append("    ... (${stackTrace.size - maxLines} more)\n")
            }
        }
        
        return sb.toString()
    }
    
    /**
     * 获取阻塞的线程列表
     * @return 状态为 BLOCKED 或 WAITING 的线程列表
     */
    fun getBlockedThreads(): List<Thread> {
        val threads = Thread.getAllStackTraces().keys
        return threads.filter {
            it.state == Thread.State.BLOCKED || 
            it.state == Thread.State.WAITING ||
            it.state == Thread.State.TIMED_WAITING
        }.toList()
    }
    
    /**
     * 检查是否存在死锁风险
     * 通过检查是否有多个线程互相等待对方持有的锁
     */
    fun checkDeadlockRisk(): Boolean {
        val blockedThreads = getBlockedThreads()
        
        if (blockedThreads.size < 2) {
            return false
        }
        
        // 检查是否存在循环等待的情况
        val lockMap = mutableMapOf<Thread, Any?>()
        
        for (thread in blockedThreads) {
            // 尝试获取线程正在等待的锁
            val waitingLock = getWaitingLock(thread)
            if (waitingLock != null) {
                lockMap[thread] = waitingLock
            }
        }
        
        // 检查是否存在循环等待
        for ((thread, lock) in lockMap) {
            // 查找是否有其他线程持有这个锁并等待当前线程持有的锁
            for ((otherThread, otherLock) in lockMap) {
                if (thread != otherThread && 
                    lock != null && 
                    otherLock != null &&
                    isThreadHoldingLock(otherThread, lock) &&
                    isThreadHoldingLock(thread, otherLock)) {
                    AnrLog.w("Potential deadlock detected between ${thread.name} and ${otherThread.name}")
                    return true
                }
            }
        }
        
        return false
    }
    
    /**
     * 获取线程正在等待的锁对象（简化实现）
     */
    private fun getWaitingLock(thread: Thread): Any? {
        // 实际实现需要使用反射访问线程内部状态
        // 这里返回 null 作为占位
        return null
    }
    
    /**
     * 检查线程是否持有指定锁（简化实现）
     */
    private fun isThreadHoldingLock(thread: Thread, lock: Any): Boolean {
        // 实际实现需要使用反射访问线程内部状态
        return false
    }
    
    /**
     * 输出线程转储信息到日志
     */
    fun dumpThreadsToLog() {
        AnrLog.d(getAllThreadsInfo())
    }
}

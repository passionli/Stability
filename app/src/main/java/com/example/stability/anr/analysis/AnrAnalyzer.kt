package com.example.stability.anr.analysis

import com.example.stability.anr.utils.AnrLog

/**
 * ANR 分析器
 * 用于分析 ANR 发生时的堆栈信息，定位问题原因
 */
class AnrAnalyzer {
    
    /**
     * ANR 分析结果
     */
    data class AnalysisResult(
        /** 是否检测到问题 */
        val hasIssue: Boolean,
        
        /** 问题类型 */
        val issueType: IssueType,
        
        /** 问题描述 */
        val description: String,
        
        /** 建议的解决方案 */
        val suggestion: String,
        
        /** 相关的堆栈信息 */
        val relevantStack: String
    )
    
    /**
     * 问题类型枚举
     */
    enum class IssueType {
        /** 主线程执行耗时操作 */
        MAIN_THREAD_BLOCKING,
        
        /** 死锁 */
        DEADLOCK,
        
        /** 锁竞争 */
        LOCK_CONTENTION,
        
        /** 消息队列阻塞 */
        MESSAGE_QUEUE_BLOCKED,
        
        /** GC 频繁 */
        GC_THROTTLING,
        
        /** 网络请求在主线程 */
        NETWORK_ON_MAIN_THREAD,
        
        /** 数据库操作在主线程 */
        DATABASE_ON_MAIN_THREAD,
        
        /** 文件操作在主线程 */
        FILE_IO_ON_MAIN_THREAD,
        
        /** 未知问题 */
        UNKNOWN
    }
    
    /**
     * 分析堆栈信息
     * @param stackTrace 完整的堆栈信息
     * @return 分析结果
     */
    fun analyze(stackTrace: String): List<AnalysisResult> {
        val results = mutableListOf<AnalysisResult>()
        
        // 检查主线程状态
        if (isMainThreadBlocked(stackTrace)) {
            results.add(analyzeMainThreadBlocking(stackTrace))
        }
        
        // 检查死锁
        if (hasDeadlock(stackTrace)) {
            results.add(AnalysisResult(
                hasIssue = true,
                issueType = IssueType.DEADLOCK,
                description = "检测到死锁，多个线程互相等待对方持有的锁",
                suggestion = "检查锁的获取顺序，确保所有线程以相同的顺序获取锁",
                relevantStack = extractRelevantStack(stackTrace, "BLOCKED")
            ))
        }
        
        // 检查锁竞争
        if (hasLockContention(stackTrace)) {
            results.add(AnalysisResult(
                hasIssue = true,
                issueType = IssueType.LOCK_CONTENTION,
                description = "检测到严重的锁竞争，多个线程等待同一个锁",
                suggestion = "减少锁的粒度，使用更细粒度的同步策略，或使用无锁数据结构",
                relevantStack = extractRelevantStack(stackTrace, "WAITING")
            ))
        }
        
        // 检查网络请求
        if (hasNetworkOnMainThread(stackTrace)) {
            results.add(AnalysisResult(
                hasIssue = true,
                issueType = IssueType.NETWORK_ON_MAIN_THREAD,
                description = "在主线程执行网络请求",
                suggestion = "将网络请求移到后台线程，使用 Coroutine 或 AsyncTask",
                relevantStack = extractRelevantStack(stackTrace, "HttpURLConnection|OkHttp|Retrofit")
            ))
        }
        
        // 检查数据库操作
        if (hasDatabaseOnMainThread(stackTrace)) {
            results.add(AnalysisResult(
                hasIssue = true,
                issueType = IssueType.DATABASE_ON_MAIN_THREAD,
                description = "在主线程执行数据库操作",
                suggestion = "将数据库操作移到后台线程，使用 Room 的异步查询",
                relevantStack = extractRelevantStack(stackTrace, "SQLite|Database|Cursor")
            ))
        }
        
        // 检查文件操作
        if (hasFileIoOnMainThread(stackTrace)) {
            results.add(AnalysisResult(
                hasIssue = true,
                issueType = IssueType.FILE_IO_ON_MAIN_THREAD,
                description = "在主线程执行文件 I/O 操作",
                suggestion = "将文件操作移到后台线程",
                relevantStack = extractRelevantStack(stackTrace, "FileInputStream|FileOutputStream|BufferedReader")
            ))
        }
        
        // 如果没有找到特定问题，检查是否有一般的主线程阻塞
        if (results.isEmpty() && stackTrace.contains("main") && 
            stackTrace.contains("RUNNABLE")) {
            results.add(AnalysisResult(
                hasIssue = true,
                issueType = IssueType.MAIN_THREAD_BLOCKING,
                description = "主线程长时间处于运行状态，可能在执行耗时操作",
                suggestion = "检查主线程中的耗时操作，将其移到后台线程",
                relevantStack = extractMainThreadStack(stackTrace)
            ))
        }
        
        return results
    }
    
    /**
     * 判断主线程是否被阻塞
     */
    private fun isMainThreadBlocked(stackTrace: String): Boolean {
        val mainThreadSection = extractMainThreadSection(stackTrace)
        return mainThreadSection.contains("BLOCKED") || 
               mainThreadSection.contains("WAITING") ||
               mainThreadSection.contains("TIMED_WAITING")
    }
    
    /**
     * 判断是否存在死锁
     */
    private fun hasDeadlock(stackTrace: String): Boolean {
        // 检查是否有多个线程处于 BLOCKED 状态并且互相等待
        val blockedCount = stackTrace.split("\n").count { 
            it.contains("BLOCKED") 
        }
        return blockedCount >= 2
    }
    
    /**
     * 判断是否存在严重的锁竞争
     */
    private fun hasLockContention(stackTrace: String): Boolean {
        val waitingCount = stackTrace.split("\n").count { 
            it.contains("WAITING") && it.contains("lock") 
        }
        return waitingCount >= 3
    }
    
    /**
     * 检查是否在主线程执行网络请求
     */
    private fun hasNetworkOnMainThread(stackTrace: String): Boolean {
        val mainThreadSection = extractMainThreadSection(stackTrace)
        val networkPatterns = listOf(
            "HttpURLConnection",
            "OkHttp",
            "Retrofit",
            "Volley",
            "AsyncHttpClient",
            "URLConnection"
        )
        return networkPatterns.any { mainThreadSection.contains(it) }
    }
    
    /**
     * 检查是否在主线程执行数据库操作
     */
    private fun hasDatabaseOnMainThread(stackTrace: String): Boolean {
        val mainThreadSection = extractMainThreadSection(stackTrace)
        val dbPatterns = listOf(
            "SQLiteDatabase",
            "SQLiteStatement",
            "Cursor",
            "RoomDatabase",
            "ContentResolver"
        )
        return dbPatterns.any { mainThreadSection.contains(it) }
    }
    
    /**
     * 检查是否在主线程执行文件操作
     */
    private fun hasFileIoOnMainThread(stackTrace: String): Boolean {
        val mainThreadSection = extractMainThreadSection(stackTrace)
        val filePatterns = listOf(
            "FileInputStream",
            "FileOutputStream",
            "FileReader",
            "FileWriter",
            "BufferedReader",
            "BufferedWriter",
            "RandomAccessFile"
        )
        return filePatterns.any { mainThreadSection.contains(it) }
    }
    
    /**
     * 分析主线程阻塞情况
     */
    private fun analyzeMainThreadBlocking(stackTrace: String): AnalysisResult {
        val mainThreadSection = extractMainThreadSection(stackTrace)
        
        // 判断具体的阻塞原因
        val description = when {
            mainThreadSection.contains("Object.wait") -> "主线程在等待对象锁"
            mainThreadSection.contains("Thread.sleep") -> "主线程主动休眠"
            mainThreadSection.contains("LockSupport.park") -> "主线程被挂起"
            else -> "主线程被阻塞"
        }
        
        return AnalysisResult(
            hasIssue = true,
            issueType = IssueType.MAIN_THREAD_BLOCKING,
            description = description,
            suggestion = "检查主线程中的同步代码块，确保不会长时间阻塞",
            relevantStack = mainThreadSection
        )
    }
    
    /**
     * 提取主线程的堆栈部分
     */
    private fun extractMainThreadSection(stackTrace: String): String {
        val lines = stackTrace.split("\n")
        val startIndex = lines.indexOfFirst { it.contains("main") }
        if (startIndex == -1) return ""
        
        val endIndex = lines.subList(startIndex, lines.size)
            .indexOfFirst { it.startsWith("===") && !it.contains("main") }
        
        if (endIndex == -1) {
            return lines.subList(startIndex, lines.size).joinToString("\n")
        }
        
        return lines.subList(startIndex, startIndex + endIndex).joinToString("\n")
    }
    
    /**
     * 提取主线程的堆栈信息
     */
    private fun extractMainThreadStack(stackTrace: String): String {
        return extractMainThreadSection(stackTrace)
    }
    
    /**
     * 提取与特定模式相关的堆栈信息
     */
    private fun extractRelevantStack(stackTrace: String, pattern: String): String {
        return stackTrace.split("\n")
            .filter { it.contains(pattern) }
            .joinToString("\n")
    }
    
    /**
     * 打印分析结果到日志
     */
    fun logAnalysisResults(results: List<AnalysisResult>) {
        if (results.isEmpty()) {
            AnrLog.i("No issues detected in ANR stack trace")
            return
        }
        
        AnrLog.e("=== ANR ANALYSIS RESULTS ===")
        for ((index, result) in results.withIndex()) {
            AnrLog.e("Issue ${index + 1}: ${result.issueType.name}")
            AnrLog.e("Description: ${result.description}")
            AnrLog.e("Suggestion: ${result.suggestion}")
            AnrLog.e("Relevant Stack:\n${result.relevantStack}")
            AnrLog.e("---")
        }
        AnrLog.e("=== END ANALYSIS ===")
    }
}

package com.example.stability.anr.analysis

import com.example.stability.anr.utils.AnrLog

/**
 * 堆栈跟踪分析器
 * 用于分析线程堆栈，定位性能问题和阻塞原因
 */
object StackTraceAnalyzer {
    
    /**
     * 堆栈分析结果
     */
    data class AnalysisResult(
        /** 是否发现问题 */
        val hasProblem: Boolean,
        
        /** 问题严重程度 */
        val severity: Severity,
        
        /** 问题描述 */
        val description: String,
        
        /** 问题位置 */
        val location: String,
        
        /** 建议 */
        val suggestion: String
    )
    
    /**
     * 严重程度枚举
     */
    enum class Severity {
        /** 严重问题 */
        CRITICAL,
        
        /** 中等问题 */
        MODERATE,
        
        /** 轻微问题 */
        MINOR,
        
        /** 无问题 */
        NONE
    }
    
    /**
     * 分析单个线程的堆栈
     * @param threadName 线程名称
     * @param state 线程状态
     * @param stackTrace 堆栈跟踪
     * @return 分析结果列表
     */
    fun analyzeThread(
        threadName: String,
        state: Thread.State,
        stackTrace: Array<StackTraceElement>
    ): List<AnalysisResult> {
        val results = mutableListOf<AnalysisResult>()
        
        // 检查线程状态
        when (state) {
            Thread.State.BLOCKED -> {
                results.add(AnalysisResult(
                    hasProblem = true,
                    severity = Severity.CRITICAL,
                    description = "线程被阻塞，正在等待锁",
                    location = threadName,
                    suggestion = "检查锁的获取顺序，避免死锁"
                ))
            }
            Thread.State.WAITING -> {
                results.add(AnalysisResult(
                    hasProblem = true,
                    severity = Severity.MODERATE,
                    description = "线程正在等待",
                    location = threadName,
                    suggestion = "检查是否存在不必要的等待"
                ))
            }
            Thread.State.TIMED_WAITING -> {
                // 定时等待可能是正常的（如 sleep），暂时不标记为问题
            }
            else -> {}
        }
        
        // 分析堆栈中的潜在问题
        for (element in stackTrace) {
            val analysis = analyzeStackTraceElement(element)
            if (analysis != null) {
                results.add(analysis.copy(location = "${threadName}:${element.className}.${element.methodName}"))
            }
        }
        
        return results
    }
    
    /**
     * 分析单个堆栈元素
     */
    private fun analyzeStackTraceElement(element: StackTraceElement): AnalysisResult? {
        val className = element.className
        val methodName = element.methodName
        
        // 检查网络操作
        if (className.contains("HttpURLConnection") || 
            className.contains("OkHttp") ||
            className.contains("Retrofit") ||
            className.contains("Volley")) {
            return AnalysisResult(
                hasProblem = true,
                severity = Severity.CRITICAL,
                description = "在主线程执行网络请求",
                location = "${className}.${methodName}",
                suggestion = "将网络请求移到后台线程"
            )
        }
        
        // 检查数据库操作
        if (className.contains("SQLite") || 
            className.contains("Database") ||
            className.contains("Cursor") ||
            className.contains("Room")) {
            return AnalysisResult(
                hasProblem = true,
                severity = Severity.CRITICAL,
                description = "在主线程执行数据库操作",
                location = "${className}.${methodName}",
                suggestion = "使用异步数据库查询"
            )
        }
        
        // 检查文件操作
        if (className.contains("FileInputStream") ||
            className.contains("FileOutputStream") ||
            className.contains("BufferedReader") ||
            className.contains("BufferedWriter")) {
            return AnalysisResult(
                hasProblem = true,
                severity = Severity.CRITICAL,
                description = "在主线程执行文件 I/O",
                location = "${className}.${methodName}",
                suggestion = "将文件操作移到后台线程"
            )
        }
        
        // 检查同步操作
        if (methodName == "wait" || 
            methodName == "sleep" ||
            methodName == "join") {
            return AnalysisResult(
                hasProblem = true,
                severity = Severity.MODERATE,
                description = "线程执行阻塞操作",
                location = "${className}.${methodName}",
                suggestion = "检查是否可以使用非阻塞方式"
            )
        }
        
        // 检查锁操作
        if (className.contains("Lock") ||
            methodName.contains("lock") ||
            methodName.contains("synchronized")) {
            return AnalysisResult(
                hasProblem = true,
                severity = Severity.MODERATE,
                description = "线程持有锁",
                location = "${className}.${methodName}",
                suggestion = "检查锁的粒度是否合适"
            )
        }
        
        return null
    }
    
    /**
     * 分析所有线程的堆栈
     * @return 分析结果列表
     */
    fun analyzeAllThreads(): List<AnalysisResult> {
        val results = mutableListOf<AnalysisResult>()
        val threads = Thread.getAllStackTraces()
        
        for ((thread, stackTrace) in threads) {
            val threadResults = analyzeThread(thread.name, thread.state, stackTrace)
            results.addAll(threadResults)
        }
        
        return results
    }
    
    /**
     * 检查是否存在性能问题
     */
    fun hasPerformanceIssues(): Boolean {
        val results = analyzeAllThreads()
        return results.any { it.hasProblem && it.severity != Severity.NONE }
    }
    
    /**
     * 获取主线程的分析结果
     */
    fun analyzeMainThread(): List<AnalysisResult> {
        val threads = Thread.getAllStackTraces()
        val mainThread = threads.keys.find { it.name == "main" }
        
        return if (mainThread != null) {
            analyzeThread(mainThread.name, mainThread.state, threads[mainThread] ?: emptyArray())
        } else {
            emptyList()
        }
    }
    
    /**
     * 打印分析结果到日志
     */
    fun logResults(results: List<AnalysisResult>) {
        if (results.isEmpty()) {
            AnrLog.i("No performance issues detected")
            return
        }
        
        AnrLog.e("=== STACK TRACE ANALYSIS RESULTS ===")
        
        // 按严重程度排序
        val sortedResults = results.sortedBy { 
            when (it.severity) {
                Severity.CRITICAL -> 0
                Severity.MODERATE -> 1
                Severity.MINOR -> 2
                Severity.NONE -> 3
            }
        }
        
        for (result in sortedResults) {
            AnrLog.e("[${result.severity.name}] ${result.description}")
            AnrLog.e("  Location: ${result.location}")
            AnrLog.e("  Suggestion: ${result.suggestion}")
            AnrLog.e("---")
        }
        
        AnrLog.e("=== END ANALYSIS ===")
    }
    
    /**
     * 获取性能问题摘要
     */
    fun getSummary(results: List<AnalysisResult>): String {
        if (results.isEmpty()) {
            return "No issues detected"
        }
        
        val criticalCount = results.count { it.severity == Severity.CRITICAL }
        val moderateCount = results.count { it.severity == Severity.MODERATE }
        val minorCount = results.count { it.severity == Severity.MINOR }
        
        return "Found ${results.size} issues: " +
               "${criticalCount} critical, " +
               "${moderateCount} moderate, " +
               "${minorCount} minor"
    }
}

package com.example.stability.anr.analysis

import com.example.stability.anr.utils.AnrLog
import java.io.File
import java.io.FileReader
import java.io.IOException

/**
 * Traces 文件解析器
 * 用于解析 Android 系统生成的 traces.txt 文件
 */
class TraceParser {
    
    /**
     * 解析 traces.txt 文件
     * @param filePath 文件路径
     * @return 解析结果
     */
    fun parseFile(filePath: String): TraceResult? {
        return try {
            val file = File(filePath)
            if (!file.exists()) {
                AnrLog.e("Traces file not found: $filePath")
                return null
            }
            
            val content = FileReader(file).readText()
            parseContent(content)
        } catch (e: IOException) {
            AnrLog.e("Failed to read traces file", e)
            null
        }
    }
    
    /**
     * 解析 traces 内容
     * @param content traces 内容字符串
     * @return 解析结果
     */
    fun parseContent(content: String): TraceResult {
        val lines = content.lines()
        var currentIndex = 0
        
        // 解析头部信息
        val header = parseHeader(lines, currentIndex)
        currentIndex += header.linesCount
        
        // 解析线程信息
        val threads = mutableListOf<ThreadInfo>()
        while (currentIndex < lines.size) {
            val threadInfo = parseThread(lines, currentIndex)
            if (threadInfo != null) {
                threads.add(threadInfo)
                currentIndex += threadInfo.linesCount
            } else {
                currentIndex++
            }
        }
        
        return TraceResult(header, threads)
    }
    
    /**
     * 解析头部信息
     */
    private fun parseHeader(lines: List<String>, startIndex: Int): HeaderInfo {
        var index = startIndex
        var pid: Int = -1
        var time: String = ""
        var cmdLine: String = ""
        
        while (index < lines.size) {
            val line = lines[index].trim()
            
            if (line.startsWith("----- pid")) {
                // 解析 pid 和时间
                val pidMatch = Regex("pid\\s+(\\d+)").find(line)
                pid = pidMatch?.groupValues?.get(1)?.toIntOrNull() ?: -1
                
                val timeMatch = Regex("at\\s+(\\d{4}-\\d{2}-\\d{2}\\s+\\d{2}:\\d{2}:\\d{2})").find(line)
                time = timeMatch?.groupValues?.get(1) ?: ""
            } else if (line.startsWith("Cmd line:")) {
                cmdLine = line.substring("Cmd line:".length).trim()
            } else if (line.startsWith("DALVIK THREADS:")) {
                break
            }
            
            index++
        }
        
        return HeaderInfo(
            pid = pid,
            time = time,
            cmdLine = cmdLine,
            linesCount = index - startIndex
        )
    }
    
    /**
     * 解析单个线程信息
     */
    private fun parseThread(lines: List<String>, startIndex: Int): ThreadInfo? {
        var index = startIndex
        
        if (index >= lines.size) {
            return null
        }
        
        val firstLine = lines[index].trim()
        
        // 检查是否是线程开始行
        if (!firstLine.startsWith("\"")) {
            return null
        }
        
        // 解析线程名称、优先级、tid、状态
        val threadMatch = Regex("\"([^\"]+)\"\\s+prio=(\\d+)\\s+tid=(\\d+)\\s+(\\w+)").find(firstLine)
        if (threadMatch == null) {
            return null
        }
        
        val name = threadMatch.groupValues[1]
        val priority = threadMatch.groupValues[2].toIntOrNull() ?: 0
        val tid = threadMatch.groupValues[3].toIntOrNull() ?: 0
        val state = threadMatch.groupValues[4]
        
        index++
        
        // 解析线程详细信息（以 | 开头的行）
        var sysTid: Int = -1
        var nice: Int = 0
        var stackSize: String = ""
        
        while (index < lines.size && lines[index].startsWith("|")) {
            val line = lines[index].trim()
            
            if (line.contains("sysTid=")) {
                val sysTidMatch = Regex("sysTid=(\\d+)").find(line)
                sysTid = sysTidMatch?.groupValues?.get(1)?.toIntOrNull() ?: -1
            }
            
            if (line.contains("nice=")) {
                val niceMatch = Regex("nice=(\\d+)").find(line)
                nice = niceMatch?.groupValues?.get(1)?.toIntOrNull() ?: 0
            }
            
            if (line.contains("stackSize=")) {
                val stackSizeMatch = Regex("stackSize=(\\S+)").find(line)
                stackSize = stackSizeMatch?.groupValues?.get(1) ?: ""
            }
            
            index++
        }
        
        // 解析堆栈跟踪
        val stackTrace = mutableListOf<StackTraceElement>()
        
        while (index < lines.size) {
            val line = lines[index].trim()
            
            // 检查是否是堆栈行（以 "at " 开头）
            if (!line.startsWith("at ")) {
                break
            }
            
            // 解析堆栈元素
            val stackMatch = Regex("at\\s+([^\\(]+)\\(([^:]+):(\\d+)\\)").find(line)
            if (stackMatch != null) {
                val className = stackMatch.groupValues[1]
                val fileName = stackMatch.groupValues[2]
                val lineNumber = stackMatch.groupValues[3].toIntOrNull() ?: 0
                
                // 分离类名和方法名
                val lastDotIndex = className.lastIndexOf('.')
                val simpleClassName = if (lastDotIndex > 0) {
                    className.substring(lastDotIndex + 1)
                } else {
                    className
                }
                val methodName = className.substring(lastDotIndex + 1)
                
                stackTrace.add(StackTraceElement(className, methodName, fileName, lineNumber))
            }
            
            index++
        }
        
        // 检查是否有 "waiting on" 信息
        var waitingOn: String? = null
        while (index < lines.size && lines[index].trim().startsWith("-")) {
            val line = lines[index].trim()
            if (line.contains("waiting on")) {
                waitingOn = line.substringAfter("waiting on").trim()
            }
            index++
        }
        
        return ThreadInfo(
            name = name,
            priority = priority,
            tid = tid,
            state = state,
            sysTid = sysTid,
            nice = nice,
            stackSize = stackSize,
            stackTrace = stackTrace,
            waitingOn = waitingOn,
            linesCount = index - startIndex
        )
    }
    
    /**
     * 头部信息数据类
     */
    data class HeaderInfo(
        val pid: Int,
        val time: String,
        val cmdLine: String,
        val linesCount: Int
    )
    
    /**
     * 线程信息数据类
     */
    data class ThreadInfo(
        val name: String,
        val priority: Int,
        val tid: Int,
        val state: String,
        val sysTid: Int,
        val nice: Int,
        val stackSize: String,
        val stackTrace: List<StackTraceElement>,
        val waitingOn: String?,
        val linesCount: Int
    )
    
    /**
     * 解析结果数据类
     */
    data class TraceResult(
        val header: HeaderInfo,
        val threads: List<ThreadInfo>
    ) {
        /**
         * 获取主线程信息
         */
        fun getMainThread(): ThreadInfo? {
            return threads.find { it.name == "main" }
        }
        
        /**
         * 获取阻塞的线程列表
         */
        fun getBlockedThreads(): List<ThreadInfo> {
            return threads.filter { 
                it.state == "BLOCKED" || 
                it.state == "WAITING" ||
                it.state == "TIMED_WAITING"
            }
        }
        
        /**
         * 转换为可读字符串
         */
        fun toReadableString(): String {
            val sb = StringBuilder()
            sb.append("=== Traces Info ===\n")
            sb.append("PID: ${header.pid}\n")
            sb.append("Time: ${header.time}\n")
            sb.append("Process: ${header.cmdLine}\n")
            sb.append("Thread Count: ${threads.size}\n\n")
            
            for (thread in threads) {
                sb.append("--- ${thread.name} ---\n")
                sb.append("  State: ${thread.state}\n")
                sb.append("  TID: ${thread.tid}\n")
                
                if (thread.stackTrace.isNotEmpty()) {
                    sb.append("  Stack Trace:\n")
                    for (element in thread.stackTrace.take(10)) {
                        sb.append("    at ${element.className}.${element.methodName}")
                        sb.append("(${element.fileName}:${element.lineNumber})\n")
                    }
                    if (thread.stackTrace.size > 10) {
                        sb.append("    ... (${thread.stackTrace.size - 10} more)\n")
                    }
                }
                
                sb.append("\n")
            }
            
            return sb.toString()
        }
    }
}

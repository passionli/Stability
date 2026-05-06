package com.example.stability.oom

import android.content.Context
import com.example.stability.oom.detection.MemoryAnalyzer
import com.example.stability.oom.detection.MemoryMonitor
import com.example.stability.oom.detection.OomHandler
import com.example.stability.oom.utils.OomLog

/**
 * OOM 模块入口
 * 提供统一的 OOM 监控和预防功能入口
 */
object OomMain {
    
    /**
     * 内存监控器实例
     */
    private var memoryMonitor: MemoryMonitor? = null
    
    /**
     * OOM 处理器实例
     */
    private var oomHandler: OomHandler? = null
    
    /**
     * 内存分析器实例
     */
    private var memoryAnalyzer: MemoryAnalyzer? = null
    
    /**
     * 是否已初始化
     */
    private var isInitialized = false
    
    /**
     * 初始化 OOM 模块
     * @param context 上下文
     */
    fun initialize(context: Context) {
        if (isInitialized) {
            OomLog.w("OomMain", "OOM module is already initialized")
            return
        }
        
        OomLog.i("OomMain", "Initializing OOM module")
        
        // 创建组件实例
        memoryMonitor = MemoryMonitor.getInstance(context)
        oomHandler = OomHandler.getInstance()
        memoryAnalyzer = MemoryAnalyzer(context)
        
        isInitialized = true
        OomLog.i("OomMain", "OOM module initialized successfully")
    }
    
    /**
     * 启动 OOM 监控
     * @param memoryListener 内存监听回调
     * @param oomListener OOM 监听回调（可选）
     */
    fun startMonitoring(
        memoryListener: MemoryMonitor.MemoryListener,
        oomListener: OomHandler.OomListener? = null
    ) {
        if (!isInitialized) {
            OomLog.e("OomMain", "OOM module not initialized, call initialize() first")
            return
        }
        
        OomLog.i("OomMain", "Starting OOM monitoring")
        
        // 启动内存监控
        memoryMonitor?.start(memoryListener)
        
        // 设置 OOM 监听并注册处理器
        oomListener?.let { listener ->
            oomHandler?.setListener(listener)
            oomHandler?.register()
        }
        
        OomLog.i("OomMain", "OOM monitoring started")
    }
    
    /**
     * 停止 OOM 监控
     */
    fun stopMonitoring() {
        if (!isInitialized) {
            OomLog.w("OomMain", "OOM module not initialized")
            return
        }
        
        OomLog.i("OomMain", "Stopping OOM monitoring")
        
        // 停止内存监控
        memoryMonitor?.stop()
        
        // 注销 OOM 处理器
        oomHandler?.unregister()
        
        OomLog.i("OomMain", "OOM monitoring stopped")
    }
    
    /**
     * 获取内存信息
     * @return 格式化的内存信息字符串
     */
    fun getMemoryInfo(): String {
        return memoryMonitor?.getMemoryInfo() ?: "Memory monitor not initialized"
    }
    
    /**
     * 获取内存状态
     * @return 内存状态对象，如果未初始化返回 null
     */
    fun getMemoryStatus(): MemoryAnalyzer.MemoryStatus? {
        return memoryAnalyzer?.getMemoryStatus()
    }
    
    /**
     * 获取内存状态描述
     * @return 格式化的内存状态字符串
     */
    fun getMemoryStatusDescription(): String {
        return memoryAnalyzer?.getMemoryStatusDescription() ?: "Memory analyzer not initialized"
    }
    
    /**
     * 检查内存问题
     * @return 内存问题描述列表
     */
    fun checkMemoryIssues(): List<String> {
        return memoryAnalyzer?.checkMemoryIssues() ?: emptyList()
    }
    
    /**
     * 触发堆转储
     * @param fileName 转储文件名（不含扩展名）
     * @return 转储文件路径，如果失败返回 null
     */
    fun dumpHeap(fileName: String): String? {
        return memoryAnalyzer?.dumpHeap(fileName)
    }
    
    /**
     * 获取模块状态
     * @return 模块状态字符串
     */
    fun getStatus(): String {
        return buildString {
            append("=== OOM Module Status ===\n")
            append("Initialized: $isInitialized\n")
            append("Memory Monitor: ${memoryMonitor?.isRunning() ?: false}\n")
            append("OOM Handler Registered: ${isOomHandlerRegistered()}\n")
            append("Memory Analyzer: ${memoryAnalyzer != null}")
        }
    }
    
    /**
     * 检查 OOM 处理器是否已注册
     */
    private fun isOomHandlerRegistered(): Boolean {
        // 这里简化处理，实际应该检查 defaultHandler 是否被设置
        return isInitialized && oomHandler != null
    }
    
    /**
     * 释放资源
     */
    fun release() {
        if (!isInitialized) {
            return
        }
        
        OomLog.i("OomMain", "Releasing OOM module")
        
        stopMonitoring()
        
        memoryMonitor = null
        oomHandler = null
        memoryAnalyzer = null
        
        isInitialized = false
        
        OomLog.i("OomMain", "OOM module released")
    }
}

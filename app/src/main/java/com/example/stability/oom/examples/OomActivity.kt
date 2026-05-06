package com.example.stability.oom.examples

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.stability.R
import com.example.stability.oom.OomMain
import com.example.stability.oom.detection.MemoryMonitor
import com.example.stability.oom.detection.OomHandler
import com.example.stability.oom.utils.OomLog

/**
 * OOM 示例活动
 * 演示 OOM 监控和内存管理功能
 */
class OomActivity : AppCompatActivity() {
    
    private lateinit var tvMemoryInfo: TextView
    private lateinit var tvStatus: TextView
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_oom)
        
        // 初始化视图
        tvMemoryInfo = findViewById(R.id.tv_memory_info)
        tvStatus = findViewById(R.id.tv_status)
        
        // 初始化 OOM 模块
        OomMain.initialize(this)
        
        // 启动内存监控
        startMemoryMonitoring()
    }
    
    /**
     * 启动内存监控
     */
    private fun startMemoryMonitoring() {
        val memoryListener = object : MemoryMonitor.MemoryListener {
            override fun onMemoryWarning(availableMemory: Long) {
                runOnUiThread {
                    tvStatus.text = "⚠️ 内存警告：可用内存 ${formatSize(availableMemory)}"
                    tvStatus.setBackgroundColor(0xFFFFE082.toInt()) // 黄色背景
                }
                OomLog.w("OomActivity", "Memory warning: ${formatSize(availableMemory)}")
            }
            
            override fun onMemoryCritical(availableMemory: Long) {
                runOnUiThread {
                    tvStatus.text = "🚨 严重警告：可用内存 ${formatSize(availableMemory)}"
                    tvStatus.setBackgroundColor(0xFFFFCDD2.toInt()) // 红色背景
                }
                OomLog.e("OomActivity", "Memory critical: ${formatSize(availableMemory)}")
            }
        }
        
        val oomListener = object : OomHandler.OomListener {
            override fun onOom(exception: OutOfMemoryError, info: OomHandler.MemoryInfo, stackTrace: String) {
                runOnUiThread {
                    tvStatus.text = "💥 OOM 发生！\n${info.toString()}"
                    tvStatus.setBackgroundColor(0xFFEF5350.toInt()) // 深红色背景
                }
                OomLog.e("OomActivity", "OutOfMemoryError detected!", exception)
            }
        }
        
        OomMain.startMonitoring(memoryListener, oomListener)
    }
    
    /**
     * 更新内存信息
     */
    fun updateMemoryInfo(view: View) {
        val memoryInfo = OomMain.getMemoryInfo()
        tvMemoryInfo.text = memoryInfo
    }
    
    /**
     * 检查内存问题
     */
    fun checkMemoryIssues(view: View) {
        val issues = OomMain.checkMemoryIssues()
        if (issues.isEmpty()) {
            tvStatus.text = "✅ 当前没有检测到内存问题"
            tvStatus.setBackgroundColor(0xFFC8E6C9.toInt()) // 绿色背景
        } else {
            val issuesStr = issues.joinToString("\n") { "- $it" }
            tvStatus.text = "⚠️ 检测到内存问题：\n$issuesStr"
            tvStatus.setBackgroundColor(0xFFFFE082.toInt()) // 黄色背景
        }
    }
    
    /**
     * 触发堆转储
     */
    fun dumpHeap(view: View) {
        val path = OomMain.dumpHeap("oom_example")
        if (path != null) {
            tvStatus.text = "📄 堆转储已保存：\n$path"
            tvStatus.setBackgroundColor(0xFFBBDEFB.toInt()) // 蓝色背景
        } else {
            tvStatus.text = "❌ 堆转储失败"
            tvStatus.setBackgroundColor(0xFFFFCDD2.toInt()) // 红色背景
        }
    }
    
    /**
     * 获取模块状态
     */
    fun getModuleStatus(view: View) {
        val status = OomMain.getStatus()
        tvMemoryInfo.text = status
    }
    
    /**
     * 格式化内存大小
     */
    private fun formatSize(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> String.format("%.2f KB", bytes / 1024.0)
            bytes < 1024 * 1024 * 1024 -> String.format("%.2f MB", bytes / (1024.0 * 1024))
            else -> String.format("%.2f GB", bytes / (1024.0 * 1024 * 1024))
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        // 停止监控并释放资源
        OomMain.release()
    }
}

package com.example.stability.anr.examples

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.stability.R
import com.example.stability.anr.detection.AnrMonitor
import com.example.stability.anr.utils.AnrLog

/**
 * ANR 演示 Activity
 * 用于演示 ANR 的触发和检测机制
 */
class AnrActivity : AppCompatActivity() {
    
    private lateinit var statusTextView: TextView
    private lateinit var anrMonitor: AnrMonitor
    private val mainHandler = Handler(Looper.getMainLooper())
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_anr)
        
        statusTextView = findViewById(R.id.status_text)
        val triggerAnrBtn = findViewById<Button>(R.id.trigger_anr_btn)
        val startMonitorBtn = findViewById<Button>(R.id.start_monitor_btn)
        val stopMonitorBtn = findViewById<Button>(R.id.stop_monitor_btn)
        val triggerSlowBtn = findViewById<Button>(R.id.trigger_slow_btn)
        
        // 初始化 ANR 监控器
        anrMonitor = AnrMonitor(timeoutMs = 3000L)
        
        // 设置按钮点击事件
        triggerAnrBtn.setOnClickListener {
            triggerAnr()
        }
        
        startMonitorBtn.setOnClickListener {
            startAnrMonitor()
        }
        
        stopMonitorBtn.setOnClickListener {
            stopAnrMonitor()
        }
        
        triggerSlowBtn.setOnClickListener {
            triggerSlowOperation()
        }
        
        updateStatus("Ready")
    }
    
    /**
     * 触发 ANR（主线程阻塞超过5秒）
     */
    private fun triggerAnr() {
        updateStatus("Triggering ANR...")
        
        // 在主线程执行耗时操作（6秒）
        mainHandler.post {
            try {
                // 模拟耗时操作
                Thread.sleep(6000)
                updateStatus("ANR trigger completed (should have shown ANR dialog)")
            } catch (e: InterruptedException) {
                Thread.currentThread().interrupt()
                updateStatus("ANR trigger interrupted")
            }
        }
    }
    
    /**
     * 触发慢操作（不超过ANR阈值）
     */
    private fun triggerSlowOperation() {
        updateStatus("Triggering slow operation...")
        
        mainHandler.post {
            try {
                // 模拟慢操作（2秒）
                Thread.sleep(2000)
                updateStatus("Slow operation completed")
            } catch (e: InterruptedException) {
                Thread.currentThread().interrupt()
                updateStatus("Slow operation interrupted")
            }
        }
    }
    
    /**
     * 启动 ANR 监控
     */
    private fun startAnrMonitor() {
        anrMonitor.start(object : AnrMonitor.AnrListener {
            override fun onAnrDetected(stackTrace: String) {
                AnrLog.anr(stackTrace)
                runOnUiThread {
                    updateStatus("ANR DETECTED! Check logcat for details")
                }
            }
            
            override fun onMainThreadBlocked(durationMs: Long) {
                runOnUiThread {
                    updateStatus("Main thread blocked for ${durationMs}ms")
                }
            }
        })
        
        updateStatus("ANR Monitor started (timeout: 3s)")
    }
    
    /**
     * 停止 ANR 监控
     */
    private fun stopAnrMonitor() {
        anrMonitor.stop()
        updateStatus("ANR Monitor stopped")
    }
    
    /**
     * 更新状态显示
     */
    private fun updateStatus(status: String) {
        statusTextView.text = status
        AnrLog.d("ANR Activity Status: $status")
    }
    
    override fun onDestroy() {
        super.onDestroy()
        if (anrMonitor.isRunning()) {
            anrMonitor.stop()
        }
    }
}

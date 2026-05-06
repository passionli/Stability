package com.example.stability.anr.examples

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.stability.R
import com.example.stability.anr.detection.AnrMonitor
import com.example.stability.anr.utils.AnrLog
import java.net.HttpURLConnection
import java.net.URL

/**
 * ANR 演示 Activity
 * 用于演示 ANR 的触发和检测机制及各种经典 ANR 场景
 */
class AnrActivity : AppCompatActivity() {
    
    private lateinit var statusTextView: TextView
    private lateinit var anrMonitor: AnrMonitor
    private val mainHandler = Handler(Looper.getMainLooper())
    
    // 死锁演示用的锁对象
    private val lock1 = Object()
    private val lock2 = Object()
    
    // SQLite 数据库示例
    private var db: SQLiteDatabase? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_anr)
        
        statusTextView = findViewById(R.id.status_text)
        val triggerAnrBtn = findViewById<Button>(R.id.trigger_anr_btn)
        val startMonitorBtn = findViewById<Button>(R.id.start_monitor_btn)
        val stopMonitorBtn = findViewById<Button>(R.id.stop_monitor_btn)
        val triggerSlowBtn = findViewById<Button>(R.id.trigger_slow_btn)
        
        // 经典 ANR 场景按钮
        val networkMainBtn = findViewById<Button>(R.id.network_main_btn)
        val dbMainBtn = findViewById<Button>(R.id.db_main_btn)
        val deadlockBtn = findViewById<Button>(R.id.deadlock_btn)
        val recursiveBtn = findViewById<Button>(R.id.recursive_btn)
        val busyLoopBtn = findViewById<Button>(R.id.busy_loop_btn)
        val messageQueueBtn = findViewById<Button>(R.id.message_queue_btn)
        val largeListBtn = findViewById<Button>(R.id.large_list_btn)
        
        // 初始化 ANR 监控器
        anrMonitor = AnrMonitor(timeoutMs = 3000L)
        
        // 初始化测试数据库
        initTestDatabase()
        
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
        
        // 经典 ANR 场景
        networkMainBtn.setOnClickListener {
            triggerNetworkOnMainThread()
        }
        
        dbMainBtn.setOnClickListener {
            triggerDatabaseOnMainThread()
        }
        
        deadlockBtn.setOnClickListener {
            triggerDeadlock()
        }
        
        recursiveBtn.setOnClickListener {
            triggerInfiniteRecursion()
        }
        
        busyLoopBtn.setOnClickListener {
            triggerBusyLoop()
        }
        
        messageQueueBtn.setOnClickListener {
            triggerMessageQueueBlock()
        }
        
        largeListBtn.setOnClickListener {
            triggerLargeListProcessing()
        }
        
        updateStatus("Ready")
    }
    
    /**
     * 初始化测试数据库
     */
    private fun initTestDatabase() {
        db = openOrCreateDatabase("test_anr.db", MODE_PRIVATE, null)
        db?.execSQL("CREATE TABLE IF NOT EXISTS test_table (id INTEGER PRIMARY KEY, data TEXT)")
        // 插入测试数据
        for (i in 0..10000) {
            db?.execSQL("INSERT OR IGNORE INTO test_table (id, data) VALUES ($i, 'test_data_$i')")
        }
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
     * 在主线程发起网络请求（经典 ANR 场景）
     */
    private fun triggerNetworkOnMainThread() {
        updateStatus("Network on Main Thread...")
        
        mainHandler.post {
            try {
                // 在主线程执行网络请求
                val url = URL("https://www.example.com")
                val connection = url.openConnection() as HttpURLConnection
                connection.connectTimeout = 10000
                connection.readTimeout = 10000
                connection.connect()
                
                val responseCode = connection.responseCode
                updateStatus("Network request completed: $responseCode")
                connection.disconnect()
            } catch (e: Exception) {
                updateStatus("Network error: ${e.message}")
            }
        }
    }
    
    /**
     * 在主线程执行数据库操作（经典 ANR 场景）
     */
    private fun triggerDatabaseOnMainThread() {
        updateStatus("Database on Main Thread...")
        
        mainHandler.post {
            try {
                // 在主线程执行复杂数据库查询
                val cursor: Cursor? = db?.rawQuery("SELECT * FROM test_table WHERE id > 5000", null)
                cursor?.use { c ->
                    val count = c.count
                    updateStatus("DB query completed: $count rows")
                }
            } catch (e: Exception) {
                updateStatus("DB error: ${e.message}")
            }
        }
    }
    
    /**
     * 死锁演示（经典 ANR 场景）
     */
    private fun triggerDeadlock() {
        updateStatus("Triggering Deadlock...")
        
        // 创建两个线程，互相等待对方的锁
        Thread {
            synchronized(lock1) {
                AnrLog.d("Thread 1 acquired lock1")
                Thread.sleep(500)
                synchronized(lock2) {
                    AnrLog.d("Thread 1 acquired lock2 - deadlock avoided!")
                }
            }
        }.start()
        
        Thread {
            synchronized(lock2) {
                AnrLog.d("Thread 2 acquired lock2")
                Thread.sleep(500)
                synchronized(lock1) {
                    AnrLog.d("Thread 2 acquired lock1 - deadlock avoided!")
                }
            }
        }.start()
        
        // 主线程尝试获取锁，可能导致阻塞
        mainHandler.post {
            try {
                synchronized(lock1) {
                    AnrLog.d("Main thread trying to acquire lock1")
                    // 如果其他线程持有锁，主线程会被阻塞
                    Thread.sleep(6000)
                    updateStatus("Deadlock scenario completed")
                }
            } catch (e: InterruptedException) {
                Thread.currentThread().interrupt()
                updateStatus("Deadlock interrupted")
            }
        }
    }
    
    /**
     * 无限递归（经典 ANR 场景）
     */
    private fun triggerInfiniteRecursion() {
        updateStatus("Triggering Infinite Recursion...")
        
        mainHandler.post {
            try {
                recursiveFunction(0)
            } catch (e: StackOverflowError) {
                updateStatus("Stack overflow occurred")
            } catch (e: Exception) {
                updateStatus("Recursion error: ${e.message}")
            }
        }
    }
    
    /**
     * 递归函数
     */
    private fun recursiveFunction(count: Int) {
        AnrLog.d("Recursion count: $count")
        // 无限递归，会导致栈溢出或长时间阻塞
        if (count < 10000) {
            recursiveFunction(count + 1)
        }
    }
    
    /**
     * 繁忙循环（经典 ANR 场景）
     */
    private fun triggerBusyLoop() {
        updateStatus("Triggering Busy Loop...")
        
        mainHandler.post {
            try {
                // 计算密集型操作
                var result = 0L
                for (i in 0..100000000) {
                    result += i * i + i / 2
                }
                updateStatus("Busy loop completed: $result")
            } catch (e: Exception) {
                updateStatus("Busy loop error: ${e.message}")
            }
        }
    }
    
    /**
     * 消息队列阻塞（经典 ANR 场景）
     */
    private fun triggerMessageQueueBlock() {
        updateStatus("Triggering Message Queue Block...")
        
        // 发送大量消息到主线程消息队列
        for (i in 0..10000) {
            mainHandler.post {
                // 每个消息都执行一些操作
                Thread.sleep(1)
            }
        }
        
        updateStatus("10000 messages posted to queue")
    }
    
    /**
     * 大数据量列表处理（经典 ANR 场景）
     */
    private fun triggerLargeListProcessing() {
        updateStatus("Processing Large List...")
        
        mainHandler.post {
            try {
                // 创建并处理大量数据
                val largeList = mutableListOf<String>()
                for (i in 0..100000) {
                    largeList.add("Item_$i")
                }
                
                // 排序和处理
                val sortedList = largeList.sorted()
                val filteredList = sortedList.filter { it.contains("5") }
                
                updateStatus("List processing completed: ${filteredList.size} items")
            } catch (e: Exception) {
                updateStatus("List processing error: ${e.message}")
            }
        }
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
        db?.close()
    }
}

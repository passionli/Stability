# ⚠️ ANR 完全解析教程

> 深入理解 Android ANR（Application Not Responding）机制，从原理到实践！

---

## 📚 目录

1. [ANR 概述](#anr-概述)
2. [ANR 触发机制](#anr-触发机制)
3. [ANR 检测机制](#anr-检测机制)
4. [ANR 日志分析](#anr-日志分析)
5. [ANR 预防策略](#anr-预防策略)
6. [ANR 监控方案](#anr-监控方案)
7. [代码示例](#代码示例)
8. [常见问题解答](#常见问题解答)

---

## 🎯 1. ANR 概述

### 什么是 ANR？

**ANR（Application Not Responding）** 是 Android 系统检测到应用程序在一定时间内无法响应用户输入或系统事件时触发的一种保护机制。

```
用户体验角度：
┌─────────────────────────────────────────────────┐
│  用户操作（点击按钮、滑动屏幕等）                 │
│         ↓                                       │
│  应用程序处理用户请求                            │
│         ↓                                       │
│  如果处理时间过长（>5秒）                        │
│         ↓                                       │
│  系统弹出 "应用无响应" 对话框                    │
│         ↓                                       │
│  用户选择：等待 / 强制关闭                       │
└─────────────────────────────────────────────────┘
```

### ANR 的影响

| 影响维度 | 具体影响 |
|---------|---------|
| **用户体验** | 应用卡顿、无响应，用户可能强制退出 |
| **应用评分** | 用户抱怨、差评，影响应用商店排名 |
| **系统稳定性** | 严重时可能导致系统看门狗重启应用 |
| **业务指标** | 转化率下降、用户流失、收入损失 |

### ANR 的常见场景

```
┌─────────────────────────────────────────────────┐
│              ANR 常见触发场景                    │
├─────────────────────────────────────────────────┤
│  🔴 主线程执行耗时操作                           │
│     ├── 数据库查询（无索引的复杂查询）            │
│     ├── 网络请求（同步调用）                     │
│     ├── 复杂计算（大数据量处理）                 │
│     └── 文件 I/O（大文件读写）                   │
├─────────────────────────────────────────────────┤
│  🔴 主线程死锁                                  │
│     ├── 锁等待循环                              │
│     └── 交叉锁死                               │
├─────────────────────────────────────────────────┤
│  🔴 消息队列阻塞                                │
│     ├── Handler.post() 消息堆积                 │
│     └── Looper 被阻塞                          │
├─────────────────────────────────────────────────┤
│  🔴 系统资源耗尽                                │
│     ├── CPU 占用过高                           │
│     ├── 内存不足导致 GC 频繁                    │
│     └── I/O 阻塞                               │
└─────────────────────────────────────────────────┘
```

---

## 🎯 2. ANR 触发机制

### ANR 的四种类型

Android 系统定义了四种主要的 ANR 类型：

| 类型 | 触发条件 | 超时时间 |
|------|---------|---------|
| **KeyDispatchTimeout** | 键盘/触摸事件在规定时间内未处理 | 5秒 |
| **BroadcastTimeout** | BroadcastReceiver 的 onReceive() 执行超时 | 10秒 |
| **ServiceTimeout** | Service 的 onCreate/onStartCommand/onBind 执行超时 | 20秒 |
| **ContentProviderTimeout** | ContentProvider 的 onCreate 执行超时 | 10秒 |

### ANR 触发流程

```
┌─────────────────────────────────────────────────────────┐
│                    ANR 触发流程                         │
├─────────────────────────────────────────────────────────┤
│                                                         │
│  用户输入 / 系统事件                                     │
│         ↓                                               │
│  事件加入主线程消息队列                                   │
│         ↓                                               │
│  主线程 Looper 处理消息                                  │
│         ↓                                               │
│  ┌─────────────────────────────────────┐                │
│  │   Watchdog 监控（每隔5秒检查）      │                │
│  └─────────────────────────────────────┘                │
│         ↓                                               │
│  检测到消息处理超时                                       │
│         ↓                                               │
│  收集当前进程的所有线程堆栈                               │
│         ↓                                               │
│  写入 traces.txt 文件                                   │
│         ↓                                               │
│  弹出 ANR 对话框                                         │
│                                                         │
└─────────────────────────────────────────────────────────┘
```

### 触发条件详解

#### 1. KeyDispatchTimeout（5秒）

```
触发场景：
1. 用户触摸屏幕或按键
2. 事件发送到应用的主线程消息队列
3. 主线程在5秒内没有处理这个事件

原因分析：
├── 主线程正在执行耗时操作
├── 主线程被阻塞（死锁、等待锁）
└── 消息队列中有大量消息堆积
```

#### 2. BroadcastTimeout（10秒）

```
触发场景：
1. 应用注册了广播接收器
2. onReceive() 方法执行时间超过10秒

原因分析：
├── 在广播接收器中执行耗时操作
└── 同步网络请求、数据库操作
```

#### 3. ServiceTimeout（20秒）

```
触发场景：
1. Service 的 onCreate() / onStartCommand() / onBind()
2. 执行时间超过20秒

原因分析：
├── 在 Service 启动时执行耗时初始化
└── 同步操作阻塞了主线程
```

#### 4. ContentProviderTimeout（10秒）

```
触发场景：
1. ContentProvider 的 onCreate()
2. 执行时间超过10秒

原因分析：
├── 在 onCreate() 中执行耗时数据库操作
└── 初始化复杂的数据结构
```

---

## 🎯 3. ANR 检测机制

### Watchdog 机制

Android 系统通过 **Watchdog** 服务监控应用的响应状态：

```kotlin
// 简化的 Watchdog 检测逻辑
class Watchdog {
    // 监控主线程
    private val mainHandler = Handler(Looper.getMainLooper())
    
    // ANR 检测标志
    @Volatile private var tick = false
    
    // 检测间隔（5秒）
    private val ANR_TIMEOUT = 5000L
    
    fun start() {
        // 后台线程定期检测
        thread {
            while (true) {
                tick = false
                
                // 发送一个空消息到主线程
                mainHandler.post {
                    tick = true  // 如果主线程正常，会执行这里
                }
                
                // 等待5秒
                Thread.sleep(ANR_TIMEOUT)
                
                // 如果 tick 仍为 false，说明主线程阻塞了
                if (!tick) {
                    triggerAnr()  // 触发 ANR
                }
            }
        }
    }
    
    private fun triggerAnr() {
        // 1. 收集所有线程的堆栈信息
        // 2. 写入 /data/anr/traces.txt
        // 3. 弹出 ANR 对话框
    }
}
```

### 消息队列监控

Android 提供了 `Looper.getMainLooper().setMessageLogging()` 来监控消息处理：

```kotlin
// 监控主线程消息处理时间
Looper.getMainLooper().setMessageLogging { msg ->
    if (msg.startsWith(">>>>> Dispatching")) {
        // 记录消息开始时间
        messageStartTime = System.currentTimeMillis()
    } else if (msg.startsWith("<<<<< Finished")) {
        // 计算消息处理耗时
        val duration = System.currentTimeMillis() - messageStartTime
        
        if (duration > SLOW_THRESHOLD) {
            // 记录慢消息
            Log.w("ANR", "Slow message detected: $duration ms")
        }
    }
}
```

### Native 层检测

Android 9.0+ 引入了更精细的 ANR 检测机制：

| 检测层面 | 说明 |
|---------|------|
| **Java 层** | 通过 Looper 监控消息处理 |
| **Native 层** | 通过 epoll 监控事件循环 |
| **Kernel 层** | 通过进程状态检测 |

---

## 🎯 4. ANR 日志分析

### 日志文件位置

ANR 发生时，系统会将堆栈信息写入：

```
/data/anr/traces.txt
```

需要 root 权限才能直接访问，也可以通过 adb 获取：

```bash
# 方法1：直接拉取文件
adb pull /data/anr/traces.txt

# 方法2：使用 logcat
adb logcat -b events -s am_anr
```

### traces.txt 格式解析

```
----- pid 1234 at 2024-01-15 10:30:00 -----
Cmd line: com.example.app

DALVIK THREADS:
"main" prio=5 tid=1 WAIT
  | group="main" sCount=1 dsCount=0 obj=0x7f000000 self=0xabc00000
  | sysTid=1234 nice=0 cgrp=default sched=0/0 handle=0xdef00000
  | state=S schedstat=( 1000000 2000000 3000 ) utm=100 stm=50 core=0 HZ=100
  | stack=0x12300000-0x12310000 stackSize=8MB
  | held mutexes=
  at java.lang.Object.wait(Native method)
  - waiting on <0x12345678> (a java.lang.Object)
  at java.lang.Object.wait(Object.java:442)
  at com.example.app.MainActivity.onCreate(MainActivity.java:42)
  at android.app.Activity.performCreate(Activity.java:7000)
  ...

"AsyncTask #1" prio=5 tid=10 BLOCKED
  ...
```

### 关键信息提取

| 字段 | 说明 |
|------|------|
| **pid** | 进程 ID |
| **Cmd line** | 应用包名 |
| **tid** | 线程 ID |
| **state** | 线程状态（WAIT/BLOCKED/RUNNABLE） |
| **stack** | 堆栈跟踪信息 |

### 常见线程状态

| 状态 | 含义 | 排查方向 |
|------|------|---------|
| **WAIT** | 等待状态 | 检查对象锁等待 |
| **BLOCKED** | 阻塞状态 | 检查锁竞争 |
| **RUNNABLE** | 运行状态 | 检查 CPU 密集操作 |
| **TIMED_WAIT** | 定时等待 | 检查 sleep/wait(timeout) |

---

## 🎯 5. ANR 预防策略

### 策略一：避免主线程耗时操作

```kotlin
// ❌ 错误做法：在主线程执行耗时操作
fun badExample() {
    // 主线程中执行网络请求
    val result = URL("https://api.example.com/data").readText()
    updateUI(result)  // 可能导致 ANR
}

// ✅ 正确做法：使用异步线程
fun goodExample() {
    // 使用 Coroutine 在后台执行
    lifecycleScope.launch(Dispatchers.IO) {
        val result = URL("https://api.example.com/data").readText()
        
        // 切换回主线程更新 UI
        withContext(Dispatchers.Main) {
            updateUI(result)
        }
    }
}
```

### 策略二：使用 StrictMode

```kotlin
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // 开发阶段启用 StrictMode
        if (BuildConfig.DEBUG) {
            StrictMode.setThreadPolicy(
                StrictMode.ThreadPolicy.Builder()
                    .detectDiskReads()      // 检测磁盘读取
                    .detectDiskWrites()     // 检测磁盘写入
                    .detectNetwork()        // 检测网络请求
                    .penaltyLog()           // 违规时输出日志
                    .penaltyDeath()         // 违规时崩溃（可选）
                    .build()
            )
            
            StrictMode.setVmPolicy(
                StrictMode.VmPolicy.Builder()
                    .detectLeakedSqlLiteObjects()    // 检测数据库泄漏
                    .detectLeakedClosableObjects()   // 检测资源泄漏
                    .penaltyLog()
                    .build()
            )
        }
    }
}
```

### 策略三：线程管理最佳实践

```kotlin
// 使用线程池管理后台任务
class ThreadPoolManager {
    // 创建固定大小的线程池
    private val executor = Executors.newFixedThreadPool(
        Runtime.getRuntime().availableProcessors() * 2
    )
    
    // 提交任务
    fun submitTask(task: Runnable) {
        executor.submit {
            try {
                task.run()
            } catch (e: Exception) {
                Log.e("ThreadPool", "Task failed", e)
            }
        }
    }
    
    // 关闭线程池
    fun shutdown() {
        executor.shutdown()
    }
}
```

### 策略四：避免死锁

```kotlin
// ❌ 错误：交叉锁导致死锁
fun deadlockExample() {
    val lockA = Object()
    val lockB = Object()
    
    // 线程1：先获取 lockA，再获取 lockB
    thread {
        synchronized(lockA) {
            Thread.sleep(100)
            synchronized(lockB) {  // 等待 lockB
                // ...
            }
        }
    }
    
    // 线程2：先获取 lockB，再获取 lockA
    thread {
        synchronized(lockB) {
            Thread.sleep(100)
            synchronized(lockA) {  // 等待 lockA，死锁！
                // ...
            }
        }
    }
}

// ✅ 正确：统一锁的获取顺序
fun noDeadlockExample() {
    val lockA = Object()
    val lockB = Object()
    
    // 两个线程都先获取 lockA，再获取 lockB
    thread {
        synchronized(lockA) {
            synchronized(lockB) {
                // ...
            }
        }
    }
    
    thread {
        synchronized(lockA) {
            synchronized(lockB) {
                // ...
            }
        }
    }
}
```

---

## 🎯 6. ANR 监控方案

### 方案一：自研监控

```kotlin
class AnrMonitor private constructor() {
    companion object {
        val instance = AnrMonitor()
    }
    
    // ANR 回调
    private var callback: ((String) -> Unit)? = null
    
    // 检测间隔（默认5秒）
    private val ANR_TIMEOUT = 5000L
    
    // 主线程 Handler
    private val mainHandler = Handler(Looper.getMainLooper())
    
    // ANR 检测标志
    @Volatile private var responded = false
    
    // 监控线程
    private var monitorThread: Thread? = null
    
    // 开始监控
    fun start(callback: (stackTrace: String) -> Unit) {
        this.callback = callback
        
        monitorThread = thread(start = true, isDaemon = true) {
            while (!Thread.currentThread().isInterrupted) {
                responded = false
                
                // 发送空消息到主线程
                mainHandler.post {
                    responded = true
                }
                
                // 等待检测超时
                Thread.sleep(ANR_TIMEOUT)
                
                // 检查是否响应
                if (!responded) {
                    // 获取堆栈信息
                    val stackTrace = getStackTrace()
                    callback(stackTrace)
                }
            }
        }
    }
    
    // 获取所有线程堆栈
    private fun getStackTrace(): String {
        val sb = StringBuilder()
        val threads = Thread.getAllStackTraces()
        
        for ((thread, stack) in threads) {
            sb.append("\"${thread.name}\" prio=${thread.priority} ")
            sb.append("tid=${thread.id} ${thread.state}\n")
            
            for (element in stack) {
                sb.append("\tat $element\n")
            }
            sb.append("\n")
        }
        
        return sb.toString()
    }
    
    // 停止监控
    fun stop() {
        monitorThread?.interrupt()
        monitorThread = null
    }
}
```

### 方案二：使用第三方 SDK

| SDK | 特点 | 适用场景 |
|-----|------|---------|
| **Firebase Crashlytics** | 自动捕获 ANR，集成简单 | 中小项目 |
| **Bugsnag** | 实时监控，详细分析 | 中大型项目 |
| **Sentry** | 全链路追踪，性能监控 | 大型项目 |
| **腾讯 Bugly** | 国内访问快，功能全面 | 国内项目 |

### 监控数据上报

```kotlin
class AnrReporter {
    // 上报 ANR 信息
    fun reportAnr(stackTrace: String) {
        val anrData = mapOf(
            "timestamp" to System.currentTimeMillis(),
            "stackTrace" to stackTrace,
            "processId" to android.os.Process.myPid(),
            "threadId" to Thread.currentThread().id,
            "appVersion" to BuildConfig.VERSION_NAME,
            "androidVersion" to Build.VERSION.SDK_INT,
            "deviceModel" to Build.MODEL
        )
        
        // 异步上报到服务器
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // 发送到后端监控服务
                val response = apiService.reportAnr(anrData)
                
                if (response.isSuccessful) {
                    Log.d("ANR", "ANR report sent successfully")
                }
            } catch (e: Exception) {
                Log.e("ANR", "Failed to report ANR", e)
            }
        }
    }
}
```

---

## 🎯 7. 代码示例

### 示例1：ANR 监控器

```kotlin
class SimpleAnrMonitor(private val timeout: Long = 5000L) {
    
    private val mainHandler = Handler(Looper.getMainLooper())
    private var monitorThread: Thread? = null
    private var listener: AnrListener? = null
    
    interface AnrListener {
        fun onAnrDetected(stackTrace: String)
    }
    
    fun start(listener: AnrListener) {
        this.listener = listener
        
        monitorThread = thread(start = true, isDaemon = true) {
            while (!Thread.currentThread().isInterrupted) {
                val startTime = System.currentTimeMillis()
                val latch = CountDownLatch(1)
                
                // 发送检测消息
                mainHandler.post {
                    latch.countDown()
                }
                
                // 等待主线程响应
                val completed = latch.await(timeout, TimeUnit.MILLISECONDS)
                
                if (!completed) {
                    // ANR 发生！
                    val stackTrace = captureStackTrace()
                    listener.onAnrDetected(stackTrace)
                }
                
                // 避免过于频繁检测
                Thread.sleep(1000)
            }
        }
    }
    
    fun stop() {
        monitorThread?.interrupt()
        monitorThread = null
    }
    
    private fun captureStackTrace(): String {
        val sb = StringBuilder()
        val threadMap = Thread.getAllStackTraces()
        
        // 优先显示主线程
        for (thread in threadMap.keys.sortedBy { it.id }) {
            if (thread.name == "main") {
                sb.append("=== MAIN THREAD ===\n")
                appendThreadInfo(sb, thread, threadMap[thread] ?: emptyArray())
            }
        }
        
        // 显示其他线程
        for (thread in threadMap.keys.sortedBy { it.id }) {
            if (thread.name != "main") {
                sb.append("\n=== ${thread.name} ===\n")
                appendThreadInfo(sb, thread, threadMap[thread] ?: emptyArray())
            }
        }
        
        return sb.toString()
    }
    
    private fun appendThreadInfo(
        sb: StringBuilder,
        thread: Thread,
        stack: Array<StackTraceElement>
    ) {
        sb.append("Thread: ${thread.name} (id=${thread.id}, state=${thread.state})\n")
        for (element in stack.take(30)) {
            sb.append("\tat $element\n")
        }
    }
}
```

### 示例2：主线程阻塞检测

```kotlin
class MainThreadBlockDetector {
    
    companion object {
        private const val WARNING_THRESHOLD = 100L  // 100ms 警告
        private const val CRITICAL_THRESHOLD = 500L // 500ms 严重
        
        private val handler = Handler(Looper.getMainLooper())
        private var lastCheckTime = System.currentTimeMillis()
        
        fun startMonitoring() {
            scheduleNextCheck()
        }
        
        private fun scheduleNextCheck() {
            handler.post {
                val currentTime = System.currentTimeMillis()
                val elapsed = currentTime - lastCheckTime
                
                if (elapsed > CRITICAL_THRESHOLD) {
                    Log.e("ANR", "CRITICAL: Main thread blocked for $elapsed ms")
                } else if (elapsed > WARNING_THRESHOLD) {
                    Log.w("ANR", "WARNING: Main thread slow for $elapsed ms")
                }
                
                lastCheckTime = currentTime
                scheduleNextCheck()
            }
        }
    }
}
```

### 示例3：严格模式配置

```kotlin
object StrictModeConfig {
    
    fun enableStrictMode() {
        if (BuildConfig.DEBUG) {
            // 线程策略
            val threadPolicy = StrictMode.ThreadPolicy.Builder()
                .detectAll()
                .penaltyLog()
                .build()
            StrictMode.setThreadPolicy(threadPolicy)
            
            // VM 策略
            val vmPolicy = StrictMode.VmPolicy.Builder()
                .detectAll()
                .penaltyLog()
                .build()
            StrictMode.setVmPolicy(vmPolicy)
        }
    }
}
```

---

## 🎯 8. 常见问题解答

### Q1: ANR 一定会弹出对话框吗？

**A**: 不一定。如果应用在系统检测到超时后很快恢复响应，系统可能不会弹出对话框，但仍会记录 traces.txt。

### Q2: 如何在开发阶段快速复现 ANR？

**A**: 

```kotlin
// 方式1：主线程 sleep
button.setOnClickListener {
    Thread.sleep(6000)  // 超过5秒，触发 ANR
}

// 方式2：死循环
button.setOnClickListener {
    while (true) {
        // 无限循环，触发 ANR
    }
}

// 方式3：同步网络请求
button.setOnClickListener {
    val url = URL("https://slow-api.example.com")
    val connection = url.openConnection()
    connection.readTimeout = 10000
    connection.getInputStream()  // 同步请求，触发 ANR
}
```

### Q3: ANR 发生后应用会怎样？

**A**: 

```
ANR 发生后的流程：
1. 系统收集线程堆栈 → 写入 traces.txt
2. 弹出 ANR 对话框
3. 用户选择：
   ├── 等待 → 继续等待应用响应
   └── 强制关闭 → 杀死应用进程
4. 如果用户长时间不操作，系统可能自动关闭应用
```

### Q4: 如何分析 ANR 日志？

**A**: 

```
分析步骤：
1. 找到 traces.txt 文件
2. 定位主线程（"main" 线程）
3. 查看线程状态：
   ├── BLOCKED → 查找锁持有者
   ├── WAIT → 查找等待的对象
   └── RUNNABLE → 分析堆栈中的耗时方法
4. 定位耗时操作所在的代码行
5. 优化代码，避免主线程阻塞
```

### Q5: 如何在生产环境监控 ANR？

**A**: 

```
监控方案：
1. 集成第三方监控 SDK（如 Crashlytics、Bugsnag）
2. 自研 ANR 监控（参考本章代码示例）
3. 定期分析 traces.txt
4. 设置告警阈值，及时发现问题

监控指标：
├── ANR 发生率（每日/每周）
├── 平均 ANR 恢复时间
├── 触发 ANR 的 Top 页面
└── 触发 ANR 的 Top 操作
```

---

## 📝 总结

### ANR 核心要点

```
1️⃣ ANR 是系统保护机制
   ├── 检测主线程阻塞
   ├── 超时时间：5/10/20秒
   └── 记录到 traces.txt

2️⃣ ANR 四大类型
   ├── KeyDispatchTimeout（输入事件）
   ├── BroadcastTimeout（广播）
   ├── ServiceTimeout（服务）
   └── ContentProviderTimeout（内容提供者）

3️⃣ 预防策略
   ├── 主线程不做耗时操作
   ├── 使用异步线程/协程
   ├── 启用 StrictMode
   └── 避免死锁

4️⃣ 监控方案
   ├── 自研监控（Looper 监控）
   └── 第三方 SDK

5️⃣ 分析方法
   ├── 查看 traces.txt
   ├── 定位主线程状态
   └── 找到阻塞点并优化
```

### 学习建议

```
第 1 步：理解 ANR 触发机制
第 2 步：学习如何分析 traces.txt
第 3 步：实践异步编程
第 4 步：配置 StrictMode
第 5 步：实现 ANR 监控
第 6 步：建立 ANR 告警体系
```

---

> 💡 **提示**：ANR 是 Android 开发中常见的稳定性问题，预防比修复更重要。养成良好的编程习惯，避免在主线程执行耗时操作！

# 💥 OOM 完全解析教程

> 深入理解 Android OOM（OutOfMemoryError）机制，从原理到实践！

---

## 📚 目录

1. [OOM 概述](#oom-概述)
2. [OOM 类型分类](#oom-类型分类)
3. [OOM 触发机制](#oom-触发机制)
4. [OOM 内存分析](#oom-内存分析)
5. [OOM 预防策略](#oom-预防策略)
6. [OOM 监控方案](#oom-监控方案)
7. [经典 OOM 例子](#经典-oom-例子)
8. [代码示例](#代码示例)
9. [常见问题解答](#常见问题解答)

---

## 🎯 1. OOM 概述

### 什么是 OOM？

**OOM（OutOfMemoryError）** 是 Java 虚拟机（JVM）在无法为新对象分配内存时抛出的错误，表示应用程序已耗尽可用的内存资源。

```
内存分配流程：
┌─────────────────────────────────────────────────┐
│  应用程序创建新对象                               │
│         ↓                                       │
│  JVM 尝试在堆内存中分配空间                        │
│         ↓                                       │
│  检查可用内存是否足够                             │
│         ↓                                       │
│  ┌─────────────┬───────────────────────────┐    │
│  │ 内存足够    │ 内存不足                   │    │
│  │    ↓       │       ↓                   │    │
│  │ 分配成功    │ 触发 GC 回收               │    │
│  │            │       ↓                   │    │
│  │            │ GC 后仍不足够               │    │
│  │            │       ↓                   │    │
│  │            │ 抛出 OutOfMemoryError      │    │
│  └─────────────┴───────────────────────────┘    │
└─────────────────────────────────────────────────┘
```

### OOM 的影响

| 影响维度 | 具体影响 |
|---------|---------|
| **应用稳定性** | 应用崩溃，用户体验极差 |
| **数据安全** | 未保存的数据丢失 |
| **系统资源** | 内存碎片化，影响其他应用 |
| **业务连续性** | 服务中断，影响用户信任 |

### OOM 的常见场景

```
┌─────────────────────────────────────────────────┐
│              OOM 常见触发场景                    │
├─────────────────────────────────────────────────┤
│  🚀 内存泄漏                                    │
│     ├── 静态引用持有 Activity                  │
│     ├── 未关闭的资源（Cursor、Stream）          │
│     ├── Handler 匿名内部类泄漏                  │
│     └── 监听器未注销                           │
├─────────────────────────────────────────────────┤
│  🚀 大对象分配                                  │
│     ├── 加载超大图片                           │
│     ├── 创建超大数组                           │
│     └── 大量小对象频繁创建                     │
├─────────────────────────────────────────────────┤
│  🚀 内存抖动                                    │
│     ├── 频繁创建临时对象                       │
│     ├── 字符串拼接产生大量中间对象              │
│     └── 循环中重复创建对象                     │
├─────────────────────────────────────────────────┤
│  🚀 内存溢出                                  │
│     ├── 无限递归调用                           │
│     ├── 集合无限制添加元素                     │
│     └── 内存泄漏累积                           │
└─────────────────────────────────────────────────┘
```

---

## 🎯 2. OOM 类型分类

### 按内存区域划分

| 类型 | 触发位置 | 原因 |
|------|---------|------|
| **Java Heap Space** | Java 堆内存 | 堆内存不足，无法分配对象 |
| **OutOfMemoryError: GC overhead limit exceeded** | GC 开销过大 | GC 回收效率过低 |
| **OutOfMemoryError: unable to create new native thread** | 线程栈 | 创建线程过多 |
| **OutOfMemoryError: Metaspace** | 元数据区 | 类定义、方法元数据过多 |
| **OutOfMemoryError: Requested array size exceeds VM limit** | 数组分配 | 请求创建超大数组 |
| **OutOfMemoryError: native allocation failed** | Native 层 | Native 代码分配内存失败 |

### Android 特有的内存问题

```
Android 内存区域：
┌─────────────────────────────────────────────────┐
│               进程地址空间                       │
├─────────────────────────────────────────────────┤
│  Native Heap       ← Native 代码使用            │
│  Dalvik/ART Heap   ← Java 对象                  │
│  Stack             ← 线程栈                     │
│  Zygote            ← 共享内存                   │
│  Code              ← 代码段                     │
│  Graphics          ← 图形缓冲区                 │
│  Other             ← 其他                      │
└─────────────────────────────────────────────────┘
```

---

## 🎯 3. OOM 触发机制

### 内存分配机制

```kotlin
// Java 对象分配流程（简化）
class ObjectAllocator {
    // 1. 尝试在 TLAB（Thread Local Allocation Buffer）分配
    // 2. TLAB 不足时，尝试在 Eden 区分配
    // 3. Eden 区不足时，触发 Minor GC
    // 4. Minor GC 后仍不足，尝试晋升到老年代
    // 5. 老年代不足时，触发 Full GC
    // 6. Full GC 后仍不足，抛出 OOM
    
    fun allocate(size: Int): Any? {
        // 简化的分配逻辑
        if (heap.hasEnoughMemory(size)) {
            return heap.allocate(size)
        } else {
            gc.collect()
            if (heap.hasEnoughMemory(size)) {
                return heap.allocate(size)
            } else {
                throw OutOfMemoryError("Java heap space")
            }
        }
    }
}
```

### GC 回收机制

| GC 类型 | 作用区域 | 特点 |
|--------|---------|------|
| **Minor GC** | 新生代（Eden + Survivor） | 频率高、速度快 |
| **Major GC** | 老年代 | 频率低、速度慢 |
| **Full GC** | 整个堆 | 暂停时间长、影响大 |

### OOM 触发条件

```
OOM 触发条件：
1. 堆内存已耗尽
2. GC 无法释放足够内存
3. 无法扩展堆大小（受限于系统限制）

触发流程：
┌─────────────────────────────────────────────────┐
│  对象分配请求                                   │
│         ↓                                       │
│  检查可用内存                                   │
│         ↓                                       │
│  内存不足 → 触发 GC                             │
│         ↓                                       │
│  GC 完成后检查                                  │
│         ↓                                       │
│  ┌─────────────┬───────────────────────────┐    │
│  │ 内存足够    │ 内存仍不足                 │    │
│  │    ↓       │       ↓                   │    │
│  │ 分配成功    │ 尝试扩展堆                 │    │
│  │            │       ↓                   │    │
│  │            │ 扩展失败                   │    │
│  │            │       ↓                   │    │
│  │            │ 抛出 OutOfMemoryError      │    │
│  └─────────────┴───────────────────────────┘    │
└─────────────────────────────────────────────────┘
```

---

## 🎯 4. OOM 内存分析

### 内存分析工具

| 工具 | 用途 | 获取方式 |
|------|------|---------|
| **Android Studio Profiler** | 实时内存监控 | 内置工具 |
| **Memory Analyzer (MAT)** | 离线堆转储分析 | Eclipse 插件 |
| **LeakCanary** | 自动检测内存泄漏 | 第三方库 |
| **Allocation Tracker** | 追踪内存分配 | Android Studio 内置 |
| **hprof-conv** | 转换 hprof 文件 | Android SDK 工具 |

### 堆转储分析流程

```
堆转储分析步骤：
1. 触发 OOM 或手动获取堆转储
2. 使用 hprof-conv 转换格式（如果需要）
3. 导入 MAT 或 Android Studio
4. 分析内存占用 Top 对象
5. 查找内存泄漏路径
6. 定位问题代码

命令行获取堆转储：
adb shell am dumpheap <pid> /sdcard/heap.hprof
adb pull /sdcard/heap.hprof .
hprof-conv heap.hprof converted.hprof
```

### 常见内存泄漏模式

```kotlin
// ❌ 常见内存泄漏模式

// 模式1：静态引用持有 Activity
object Singleton {
    private var context: Context? = null  // 泄漏！
    
    fun init(context: Context) {
        this.context = context  // 持有 Activity 引用
    }
}

// 模式2：Handler 匿名内部类
class LeakyActivity : AppCompatActivity() {
    private val handler = Handler()  // 隐式持有 Activity 引用
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handler.postDelayed({
            // Activity 可能已销毁，但 handler 仍持有引用
        }, 60000)
    }
}

// 模式3：未注销的监听器
class LeakyFragment : Fragment() {
    override fun onStart() {
        super.onStart()
        SomeBus.register(this)  // 注册但未注销
    }
}
```

---

## 🎯 5. OOM 预防策略

### 策略一：优化图片加载

```kotlin
// ✅ 正确：使用合适的图片尺寸
fun loadOptimizedImage(context: Context, resId: Int, targetWidth: Int, targetHeight: Int): Bitmap {
    // 先获取图片尺寸，不加载到内存
    val options = BitmapFactory.Options().apply {
        inJustDecodeBounds = true  // 只获取尺寸信息
    }
    BitmapFactory.decodeResource(context.resources, resId, options)
    
    // 计算采样率
    val sampleSize = calculateInSampleSize(options, targetWidth, targetHeight)
    
    // 加载缩小后的图片
    return BitmapFactory.Options().apply {
        inSampleSize = sampleSize  // 采样率
        inPreferredConfig = Bitmap.Config.RGB_565  // 减少内存占用
    }.let {
        BitmapFactory.decodeResource(context.resources, resId, it)
    }
}

// 计算采样率
private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
    val height = options.outHeight
    val width = options.outWidth
    var inSampleSize = 1
    
    if (height > reqHeight || width > reqWidth) {
        val halfHeight = height / 2
        val halfWidth = width / 2
        
        while ((halfHeight / inSampleSize) >= reqHeight && 
               (halfWidth / inSampleSize) >= reqWidth) {
            inSampleSize *= 2
        }
    }
    return inSampleSize
}
```

### 策略二：使用弱引用/软引用

```kotlin
// ✅ 使用 WeakReference 避免内存泄漏
class SafeHandler(activity: AppCompatActivity) {
    // 使用 WeakReference 持有 Activity
    private val activityRef = WeakReference(activity)
    
    private val handler = Handler(Looper.getMainLooper())
    
    fun postDelayed(delayMs: Long, action: () -> Unit) {
        handler.postDelayed({
            // 检查 Activity 是否还存在
            activityRef.get()?.let {
                action()
            }
        }, delayMs)
    }
}

// ✅ 使用 SoftReference 实现内存敏感缓存
class MemoryCache<T> {
    private val cache = mutableMapOf<String, SoftReference<T>>()
    
    fun put(key: String, value: T) {
        cache[key] = SoftReference(value)
    }
    
    fun get(key: String): T? {
        return cache[key]?.get()
    }
}
```

### 策略三：及时释放资源

```kotlin
// ✅ 正确释放资源
class ResourceManager {
    private var bitmap: Bitmap? = null
    private var inputStream: InputStream? = null
    
    fun loadResource() {
        bitmap = BitmapFactory.decodeResource(...)
        inputStream = FileInputStream(...)
    }
    
    fun release() {
        // 释放 Bitmap
        bitmap?.let {
            if (!it.isRecycled) {
                it.recycle()
            }
            bitmap = null
        }
        
        // 关闭流
        inputStream?.let {
            try {
                it.close()
            } catch (e: IOException) {
                // ignore
            }
            inputStream = null
        }
    }
}

// ✅ 在 Fragment 中正确处理生命周期
class SafeFragment : Fragment() {
    private var listener: SomeListener? = null
    
    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = context as? SomeListener
    }
    
    override fun onDetach() {
        super.onDetach()
        listener = null  // 防止泄漏
    }
}
```

### 策略四：使用对象池

```kotlin
// ✅ 使用对象池减少内存分配
class ObjectPool<T>(private val creator: () -> T) {
    private val pool = ArrayDeque<T>()
    
    fun acquire(): T {
        return pool.removeFirstOrNull() ?: creator()
    }
    
    fun release(obj: T) {
        // 重置对象状态
        // ...
        pool.add(obj)
    }
    
    fun clear() {
        pool.clear()
    }
}

// 使用示例
class NetworkRequestPool {
    private val requestPool = ObjectPool { NetworkRequest() }
    
    fun createRequest(): NetworkRequest {
        return requestPool.acquire()
    }
    
    fun recycleRequest(request: NetworkRequest) {
        request.reset()
        requestPool.release(request)
    }
}
```

---

## 🎯 6. OOM 监控方案

### 方案一：内存监控

```kotlin
class MemoryMonitor private constructor() {
    companion object {
        val instance = MemoryMonitor()
    }
    
    // 内存阈值（可用内存低于此值告警）
    private val WARNING_THRESHOLD = 50 * 1024 * 1024L  // 50MB
    private val CRITICAL_THRESHOLD = 20 * 1024 * 1024L  // 20MB
    
    // 监控回调
    private var listener: MemoryListener? = null
    
    interface MemoryListener {
        fun onMemoryWarning(availableMemory: Long)
        fun onMemoryCritical(availableMemory: Long)
    }
    
    // 开始监控
    fun start(listener: MemoryListener) {
        this.listener = listener
        
        // 定时检查内存状态
        thread(start = true, isDaemon = true) {
            while (!Thread.currentThread().isInterrupted) {
                val availableMemory = getAvailableMemory()
                
                when {
                    availableMemory < CRITICAL_THRESHOLD -> {
                        listener.onMemoryCritical(availableMemory)
                    }
                    availableMemory < WARNING_THRESHOLD -> {
                        listener.onMemoryWarning(availableMemory)
                    }
                }
                
                Thread.sleep(2000)  // 每2秒检查一次
            }
        }
    }
    
    // 获取可用内存
    private fun getAvailableMemory(): Long {
        val memoryInfo = ActivityManager.MemoryInfo()
        val activityManager = 
            App.instance.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        activityManager.getMemoryInfo(memoryInfo)
        return memoryInfo.availMem
    }
    
    // 获取当前内存使用详情
    fun getMemoryInfo(): String {
        val memoryInfo = ActivityManager.MemoryInfo()
        val activityManager = 
            App.instance.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        activityManager.getMemoryInfo(memoryInfo)
        
        return buildString {
            append("Total Memory: ${formatSize(memoryInfo.totalMem)}\n")
            append("Available Memory: ${formatSize(memoryInfo.availMem)}\n")
            append("Threshold: ${formatSize(memoryInfo.threshold)}\n")
            append("Low Memory: ${memoryInfo.lowMemory}\n")
        }
    }
    
    private fun formatSize(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> "${bytes / 1024} KB"
            bytes < 1024 * 1024 * 1024 -> "${bytes / (1024 * 1024)} MB"
            else -> "${bytes / (1024 * 1024 * 1024)} GB"
        }
    }
}
```

### 方案二：OOM 捕获与上报

```kotlin
class OomHandler private constructor() {
    companion object {
        val instance = OomHandler()
    }
    
    // OOM 回调
    private var listener: OomListener? = null
    
    interface OomListener {
        fun onOom(exception: OutOfMemoryError, info: MemoryInfo)
    }
    
    // 设置未捕获异常处理器
    fun setupOomHandler(listener: OomListener) {
        this.listener = listener
        
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            if (throwable is OutOfMemoryError) {
                // 捕获 OOM
                val memoryInfo = collectMemoryInfo()
                listener.onOom(throwable, memoryInfo)
            }
            
            // 交给默认处理器
            defaultHandler?.uncaughtException(thread, throwable)
        }
    }
    
    // 收集内存信息
    private fun collectMemoryInfo(): MemoryInfo {
        val runtime = Runtime.getRuntime()
        val maxMemory = runtime.maxMemory()
        val totalMemory = runtime.totalMemory()
        val freeMemory = runtime.freeMemory()
        
        return MemoryInfo(
            maxMemory = maxMemory,
            totalMemory = totalMemory,
            usedMemory = totalMemory - freeMemory,
            timestamp = System.currentTimeMillis(),
            threadName = Thread.currentThread().name
        )
    }
    
    data class MemoryInfo(
        val maxMemory: Long,
        val totalMemory: Long,
        val usedMemory: Long,
        val timestamp: Long,
        val threadName: String
    )
}
```

### 方案三：LeakCanary 集成

```kotlin
// 在 Application 中初始化 LeakCanary
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // 初始化 LeakCanary（仅在 debug 版本）
        if (BuildConfig.DEBUG) {
            LeakCanary.config = LeakCanary.config.copy(
                dumpHeap = true,
                watchDurationMillis = 5000
            )
            
            // 监听内存泄漏
            LeakCanary.showLeakDisplayActivityLauncherIcon(true)
        }
    }
}
```

---

## 🎯 7. 经典 OOM 例子

### 7.1 大数组分配 OOM

**场景描述**：尝试分配一个超出堆内存限制的超大数组。

```kotlin
// ❌ 危险代码：创建超大数组导致 OOM
fun triggerArrayOom() {
    try {
        // 请求分配 Int.MAX_VALUE 大小的数组，肯定会触发 OOM
        val hugeArray = ByteArray(Int.MAX_VALUE)
    } catch (e: OutOfMemoryError) {
        // 捕获 OOM，记录日志
        Log.e("OOM", "Array allocation OOM", e)
    }
}

// ❌ 危险代码：循环分配大量数组
fun triggerMultipleArrayOom() {
    val list = mutableListOf<ByteArray>()
    try {
        while (true) {
            // 不断分配 10MB 的数组
            list.add(ByteArray(10 * 1024 * 1024))
        }
    } catch (e: OutOfMemoryError) {
        Log.e("OOM", "Multiple array allocation OOM", e)
    }
}
```

**触发原因**：
- 数组大小超过 JVM 堆内存限制
- 循环分配数组导致内存不断累积

**解决方案**：
- 避免创建超出合理范围的数组
- 使用分批处理代替一次性加载
- 及时释放不再使用的数组引用

### 7.2 大图片加载 OOM

**场景描述**：直接加载超大图片而不进行采样压缩。

```kotlin
// ❌ 危险代码：直接加载大图导致 OOM
fun triggerBitmapOom(context: Context) {
    try {
        // 直接加载资源中的大图，不进行采样
        val bitmap = BitmapFactory.decodeResource(
            context.resources, 
            R.drawable.huge_image  // 假设有一张很大的图片
        )
        imageView.setImageBitmap(bitmap)
    } catch (e: OutOfMemoryError) {
        Log.e("OOM", "Bitmap OOM", e)
    }
}

// ❌ 危险代码：循环加载大量图片
fun triggerMultipleBitmapOom(context: Context) {
    val bitmapList = mutableListOf<Bitmap>()
    try {
        for (i in 0..100) {
            // 不断加载图片到内存
            val bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.image)
            bitmapList.add(bitmap)
        }
    } catch (e: OutOfMemoryError) {
        Log.e("OOM", "Multiple bitmap OOM", e)
    }
}
```

**触发原因**：
- 图片像素尺寸过大，解码后占用内存超出限制
- 大量图片未及时回收导致内存累积

**解决方案**：
- 使用 `BitmapFactory.Options` 进行采样压缩
- 使用 `inPreferredConfig = RGB_565` 减少内存占用
- 图片使用完毕后及时 `recycle()` 并置为 null

### 7.3 内存泄漏导致 OOM

**场景描述**：静态引用持有 Activity 导致内存泄漏，最终引发 OOM。

```kotlin
// ❌ 危险代码：静态引用持有 Activity
object LeakySingleton {
    // 静态变量持有 Context 引用
    private var context: Context? = null
    
    fun init(context: Context) {
        this.context = context  // 如果传入 Activity，会导致泄漏
    }
}

// ❌ 危险代码：Handler 匿名内部类泄漏
class LeakyActivity : AppCompatActivity() {
    // Handler 隐式持有 Activity 引用
    private val handler = Handler()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 延迟消息持有 Activity 引用
        handler.postDelayed({
            updateUI()
        }, 60000)  // 60秒后执行
    }
    
    private fun updateUI() {
        // 更新 UI
    }
    
    // 如果 Activity 在此期间销毁，Handler 仍持有引用
}

// ❌ 危险代码：监听器未注销
class LeakyFragment : Fragment() {
    override fun onStart() {
        super.onStart()
        EventBus.register(this)  // 注册但未注销
    }
}
```

**触发原因**：
- 静态引用生命周期长于 Activity
- Handler 消息队列持有 Activity 引用
- 监听器未在合适时机注销

**解决方案**：
- 使用 `WeakReference` 持有 Activity
- 在 Activity 销毁时移除 Handler 消息
- 在合适的生命周期方法中注销监听器

### 7.4 无限递归导致 OOM

**场景描述**：无限递归调用导致栈溢出或堆内存耗尽。

```kotlin
// ❌ 危险代码：无限递归
fun infiniteRecursion() {
    infiniteRecursion()  // 无限递归调用
}

// ❌ 危险代码：递归深度过大
fun deepRecursion(depth: Int) {
    if (depth > 0) {
        // 创建对象并递归
        val data = DataObject()
        deepRecursion(depth - 1)
    }
}

class DataObject {
    // 包含大量数据
    val array = ByteArray(1024)
}
```

**触发原因**：
- 递归没有终止条件或终止条件永远无法满足
- 递归深度过大导致栈溢出或堆内存耗尽

**解决方案**：
- 检查递归终止条件
- 使用迭代代替递归
- 增加递归深度限制

### 7.5 字符串拼接导致 OOM

**场景描述**：大量字符串拼接产生大量中间对象。

```kotlin
// ❌ 危险代码：循环字符串拼接
fun stringConcatOom() {
    var result = ""
    for (i in 0..1000000) {
        // 每次拼接都会创建新的 String 对象
        result += "Item $i\n"
    }
}

// ❌ 危险代码：使用 + 操作符拼接大量字符串
fun stringPlusOom() {
    val parts = mutableListOf<String>()
    for (i in 0..100000) {
        parts.add("Part $i")
    }
    
    var result = ""
    for (part in parts) {
        result += part  // 低效的字符串拼接
    }
}
```

**触发原因**：
- 每次字符串拼接都会创建新的 String 对象
- 大量中间对象占用堆内存

**解决方案**：
- 使用 `StringBuilder` 代替直接拼接
- 使用 `String.join()` 方法
- 分批处理大量字符串

### 7.6 集合无限制增长

**场景描述**：集合不断添加元素而不清理，导致内存耗尽。

```kotlin
// ❌ 危险代码：集合无限增长
class DataCache {
    // 静态集合，永不清理
    private val cache = mutableListOf<Data>()
    
    fun addData(data: Data) {
        cache.add(data)  // 只添加不删除
    }
}

// ❌ 危险代码：全局缓存无限制
object GlobalCache {
    val items = mutableMapOf<String, Any>()
    
    fun put(key: String, value: Any) {
        items[key] = value  // 永远不清理
    }
}
```

**触发原因**：
- 集合只添加元素不删除
- 缓存没有过期策略或大小限制

**解决方案**：
- 设置缓存大小上限
- 实现 LRU（最近最少使用）缓存策略
- 定期清理过期数据

### 7.7 线程创建过多

**场景描述**：创建大量线程导致内存不足。

```kotlin
// ❌ 危险代码：创建大量线程
fun createManyThreads() {
    for (i in 0..1000) {
        Thread {
            // 线程任务
            Thread.sleep(10000)
        }.start()
    }
}

// ❌ 危险代码：线程池配置不合理
fun badThreadPoolConfig() {
    val executor = ThreadPoolExecutor(
        100,      // corePoolSize
        1000,     // maximumPoolSize
        60L,
        TimeUnit.SECONDS,
        LinkedBlockingQueue()
    )
    
    for (i in 0..10000) {
        executor.execute {
            // 大量任务
        }
    }
}
```

**触发原因**：
- 每个线程都需要分配栈内存
- 线程数量超出系统限制

**解决方案**：
- 使用线程池复用线程
- 合理配置线程池参数
- 限制并发线程数量

---

## 🎯 8. 代码示例

### 示例1：内存监控器

```kotlin
class SimpleMemoryMonitor {
    
    private val handler = Handler(Looper.getMainLooper())
    private var listener: MemoryMonitorListener? = null
    private var isRunning = false
    
    interface MemoryMonitorListener {
        fun onMemoryStatus(usedPercent: Float, availableBytes: Long)
    }
    
    fun start(listener: MemoryMonitorListener) {
        this.listener = listener
        isRunning = true
        scheduleCheck()
    }
    
    fun stop() {
        isRunning = false
        handler.removeCallbacksAndMessages(null)
    }
    
    private fun scheduleCheck() {
        if (!isRunning) return
        
        // 获取内存状态
        val runtime = Runtime.getRuntime()
        val usedMemory = runtime.totalMemory() - runtime.freeMemory()
        val maxMemory = runtime.maxMemory()
        val usedPercent = (usedMemory.toFloat() / maxMemory.toFloat()) * 100
        val availableBytes = maxMemory - usedMemory
        
        listener?.onMemoryStatus(usedPercent, availableBytes)
        
        // 1秒后再次检查
        handler.postDelayed({ scheduleCheck() }, 1000)
    }
    
    fun getMemorySnapshot(): MemorySnapshot {
        val runtime = Runtime.getRuntime()
        return MemorySnapshot(
            freeMemory = runtime.freeMemory(),
            totalMemory = runtime.totalMemory(),
            maxMemory = runtime.maxMemory(),
            usedMemory = runtime.totalMemory() - runtime.freeMemory()
        )
    }
    
    data class MemorySnapshot(
        val freeMemory: Long,
        val totalMemory: Long,
        val maxMemory: Long,
        val usedMemory: Long
    )
}
```

### 示例2：Bitmap 内存管理

```kotlin
object BitmapManager {
    
    /**
     * 计算 Bitmap 占用内存大小
     */
    fun calculateBitmapMemory(bitmap: Bitmap): Long {
        // API 12+ 使用 getAllocationByteCount()
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
            bitmap.allocationByteCount.toLong()
        } else {
            bitmap.byteCount.toLong()
        }
    }
    
    /**
     * 安全回收 Bitmap
     */
    fun recycleBitmap(bitmap: Bitmap?) {
        bitmap?.let {
            if (!it.isRecycled) {
                it.recycle()
            }
        }
    }
    
    /**
     * 批量回收 Bitmap
     */
    fun recycleBitmaps(vararg bitmaps: Bitmap?) {
        bitmaps.forEach { recycleBitmap(it) }
    }
    
    /**
     * 创建采样后的 Bitmap
     */
    fun decodeSampledBitmapFromResource(
        resources: Resources,
        resId: Int,
        reqWidth: Int,
        reqHeight: Int
    ): Bitmap {
        // 第一步：获取图片尺寸
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        BitmapFactory.decodeResource(resources, resId, options)
        
        // 第二步：计算采样率
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)
        
        // 第三步：加载图片
        options.inJustDecodeBounds = false
        options.inPreferredConfig = Bitmap.Config.RGB_565
        options.inDither = true
        
        return BitmapFactory.decodeResource(resources, resId, options)
    }
    
    private fun calculateInSampleSize(
        options: BitmapFactory.Options,
        reqWidth: Int,
        reqHeight: Int
    ): Int {
        val (height: Int, width: Int) = options.run { outHeight to outWidth }
        var inSampleSize = 1
        
        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2
            
            while (halfHeight / inSampleSize >= reqHeight && 
                   halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        
        return inSampleSize
    }
}
```

### 示例3：内存泄漏检测工具

```kotlin
class LeakDetector {
    
    companion object {
        private const val TAG = "LeakDetector"
        
        /**
         * 检查 Context 是否可能泄漏
         */
        fun checkContextLeak(context: Context, tag: String) {
            if (context is Activity && context.isFinishing) {
                Log.w(TAG, "Potential context leak: $tag - Activity is finishing")
            }
        }
        
        /**
         * 检查 Handler 是否可能导致泄漏
         */
        fun checkHandlerLeak(handler: Handler, context: Context) {
            val callbackField = Handler::class.java.getDeclaredField("mCallback")
            callbackField.isAccessible = true
            val callback = callbackField.get(handler)
            
            if (callback != null) {
                val callbackClass = callback.javaClass
                if (callbackClass.enclosingClass != null) {
                    Log.w(TAG, "Handler has inner class callback, potential leak")
                }
            }
        }
        
        /**
         * 检查静态字段是否持有 Activity
         */
        fun checkStaticFieldLeak(clazz: Class<*>) {
            val fields = clazz.declaredFields
            
            for (field in fields) {
                if (java.lang.reflect.Modifier.isStatic(field.modifiers)) {
                    field.isAccessible = true
                    val value = field.get(null)
                    
                    if (value is Context && value is Activity) {
                        Log.e(TAG, "Static field '${field.name}' holds Activity reference!")
                    }
                }
            }
        }
    }
}
```

---

## 🎯 8. 常见问题解答

### Q1: 如何快速复现 OOM？

**A**: 

```kotlin
// 方式1：创建超大数组
fun triggerArrayOom() {
    // 请求分配超大数组，超过堆内存限制
    val array = ByteArray(Int.MAX_VALUE)  // 肯定会触发 OOM
}

// 方式2：无限创建对象
fun triggerObjectOom() {
    val list = mutableListOf<Any>()
    while (true) {
        list.add(Object())  // 不断添加对象直到内存耗尽
    }
}

// 方式3：加载超大图片
fun triggerBitmapOom(context: Context) {
    // 直接加载大图不进行采样
    val bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.huge_image)
}
```

### Q2: OOM 发生后应用会怎样？

**A**: 

```
OOM 发生后的流程：
1. JVM 抛出 OutOfMemoryError
2. 异常传播到线程的未捕获异常处理器
3. 如果没有处理，应用崩溃
4. 系统记录 crash 日志

OOM 的影响：
├── 应用崩溃退出
├── 用户数据可能丢失
├── 进程被系统杀死
└── 需要重新启动应用
```

### Q3: 如何分析 OOM 日志？

**A**: 

```
分析步骤：
1. 查找 crash 日志中的 OutOfMemoryError
2. 查看堆栈跟踪，定位分配内存的代码位置
3. 分析内存使用情况：
   ├── Java 堆内存使用
   ├── Native 堆内存使用
   └── 内存泄漏线索
4. 使用 MAT 分析堆转储文件
5. 找到大对象或泄漏源

日志关键字：
├── "OutOfMemoryError: Java heap space"
├── "OutOfMemoryError: GC overhead limit exceeded"
└── "OutOfMemoryError: unable to create new native thread"
```

### Q4: 如何在生产环境监控 OOM？

**A**: 

```
监控方案：
1. 集成第三方监控 SDK（如 Firebase Crashlytics、Bugsnag）
2. 设置 OOM 异常捕获，收集内存信息
3. 定期上报内存使用趋势
4. 设置内存阈值告警

监控指标：
├── 内存使用率（实时）
├── GC 频率和耗时
├── OOM 发生率
├── 大对象分配次数
└── 内存泄漏检测
```

### Q5: 如何优化应用内存使用？

**A**: 

```
优化策略：
1. 图片优化：使用合适尺寸、压缩格式
2. 对象复用：使用对象池、避免频繁创建
3. 及时释放：资源使用完毕后及时关闭
4. 弱引用：使用 WeakReference 避免强引用泄漏
5. 懒加载：延迟初始化，按需加载
6. 内存缓存：合理设置缓存大小和过期策略
7. 避免内存抖动：减少临时对象创建

检查清单：
├── Activity/Fragment 销毁时清理引用
├── Handler 使用 WeakReference
├── 监听器在合适时机注销
├── Cursor/Stream 等资源及时关闭
├── Bitmap 使用后及时 recycle
```

---

## 📝 总结

### OOM 核心要点

```
1️⃣ OOM 类型
   ├── Java Heap Space（最常见）
   ├── GC overhead limit exceeded
   ├── unable to create new native thread
   └── Metaspace

2️⃣ 常见原因
   ├── 内存泄漏（最大元凶）
   ├── 大对象分配
   ├── 内存抖动
   └── 无限递归/循环

3️⃣ 预防策略
   ├── 图片优化（采样、压缩）
   ├── 使用弱引用/软引用
   ├── 及时释放资源
   ├── 对象池复用
   └── 内存监控告警

4️⃣ 分析工具
   ├── Android Studio Profiler
   ├── Memory Analyzer Tool (MAT)
   ├── LeakCanary
   └── Allocation Tracker

5️⃣ 监控方案
   ├── 内存阈值监控
   ├── OOM 异常捕获
   └── 第三方监控 SDK
```

### 学习建议

```
第 1 步：理解 JVM 内存模型
第 2 步：学习 GC 机制
第 3 步：实践内存分析工具
第 4 步：掌握常见泄漏模式
第 5 步：实现内存监控
第 6 步：建立 OOM 告警体系
```

---

> 💡 **提示**：OOM 是 Android 开发中最严重的稳定性问题之一，预防的关键在于良好的编程习惯和及时的内存监控！

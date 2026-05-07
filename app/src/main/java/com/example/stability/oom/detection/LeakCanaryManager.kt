package com.example.stability.oom.detection

import android.content.Context
import com.example.stability.oom.utils.OomLog

/**
 * LeakCanary 管理类
 * 封装 LeakCanary 的配置和操作，提供统一的内存泄漏检测入口
 * 
 * LeakCanary 2.x 版本简化了 API，移除了 RefWatcher
 * 所有功能通过 LeakCanary 单例直接访问
 */
object LeakCanaryManager {

    /**
     * 日志标签
     */
    private const val TAG = "LeakCanaryManager"

    /**
     * 是否已初始化
     */
    private var isInitialized = false

    /**
     * 初始化 LeakCanary 管理器
     * @param context 上下文
     */
    fun initialize(context: Context) {
        if (isInitialized) {
            OomLog.w(TAG, "LeakCanaryManager is already initialized")
            return
        }

        OomLog.i(TAG, "Initializing LeakCanaryManager")

        // 检查是否是分析进程（使用反射避免编译错误）
        if (isInAnalyzerProcess(context)) {
            OomLog.i(TAG, "Running in analyzer process, skipping initialization")
            isInitialized = true
            return
        }

        try {
            // 通过反射调用 LeakCanary.install()，避免 release 版本编译错误
            val leakCanaryClass = Class.forName("com.squareup.leakcanary.LeakCanary")
            val installMethod = leakCanaryClass.getMethod("install", android.app.Application::class.java)
            installMethod.invoke(null, context.applicationContext)

            // 配置 LeakCanary（通过反射）
            configureLeakCanary()

            isInitialized = true
            OomLog.i(TAG, "LeakCanaryManager initialized successfully")
        } catch (e: ClassNotFoundException) {
            OomLog.w(TAG, "LeakCanary not available (probably release build)")
            isInitialized = true // 标记为已初始化，避免重复尝试
        } catch (e: Exception) {
            OomLog.e(TAG, "Failed to initialize LeakCanaryManager", e)
        }
    }

    /**
     * 检查是否是分析进程
     */
    private fun isInAnalyzerProcess(context: Context): Boolean {
        return try {
            val leakCanaryClass = Class.forName("com.squareup.leakcanary.LeakCanary")
            val method = leakCanaryClass.getMethod("isInAnalyzerProcess", Context::class.java)
            method.invoke(null, context) as Boolean
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 配置 LeakCanary 选项
     */
    private fun configureLeakCanary() {
        try {
            // 获取当前配置
            val leakCanaryClass = Class.forName("com.squareup.leakcanary.LeakCanary")
            val configField = leakCanaryClass.getDeclaredField("config")
            configField.isAccessible = true
            val currentConfig = configField.get(null)

            // 获取 Config 类
            val configClass = Class.forName("com.squareup.leakcanary.Config")
            
            // 创建新配置（使用 copy 方法）
            val copyMethod = configClass.getMethod("copy", 
                Boolean::class.java, 
                Long::class.java, 
                Int::class.java,
                Boolean::class.java
            )
            
            // 参数：dumpHeap, watchDurationMillis, maxStoredHeapDumps, showLeakDisplayActivityLauncherIcon
            val customConfig = copyMethod.invoke(currentConfig, 
                true,      // dumpHeap
                5000L,     // watchDurationMillis
                10,        // maxStoredHeapDumps
                true       // showLeakDisplayActivityLauncherIcon
            )

            // 设置新配置
            configField.set(null, customConfig)

            OomLog.d(TAG, "LeakCanary configured successfully")
        } catch (e: Exception) {
            OomLog.w(TAG, "Failed to configure LeakCanary: ${e.message}")
        }
    }

    /**
     * 手动观察指定对象
     * 用于检测特定对象是否存在内存泄漏
     * @param watchedReference 被观察的对象
     * @param referenceName 对象名称（用于标识）
     */
    fun watch(watchedReference: Any, referenceName: String) {
        if (!isInitialized) {
            OomLog.w(TAG, "LeakCanaryManager not initialized, cannot watch object: $referenceName")
            return
        }

        try {
            // LeakCanary 2.x 使用 AppWatcher.objectWatcher
            val appWatcherClass = Class.forName("com.squareup.leakcanary.AppWatcher")
            val objectWatcherField = appWatcherClass.getDeclaredField("objectWatcher")
            objectWatcherField.isAccessible = true
            val objectWatcher = objectWatcherField.get(null)

            val watchMethod = objectWatcher.javaClass.getMethod("watch", Any::class.java, String::class.java)
            watchMethod.invoke(objectWatcher, watchedReference, referenceName)
            
            OomLog.d(TAG, "Watching object: $referenceName")
        } catch (e: ClassNotFoundException) {
            OomLog.w(TAG, "LeakCanary not available (probably release build)")
        } catch (e: Exception) {
            OomLog.e(TAG, "Failed to watch object: $referenceName", e)
        }
    }

    /**
     * 手动观察指定对象（简化版本）
     * @param watchedReference 被观察的对象
     */
    fun watch(watchedReference: Any) {
        watch(watchedReference, watchedReference.javaClass.simpleName)
    }

    /**
     * 检查是否正在分析泄漏
     * @return true 如果正在分析，false 否则
     */
    fun isAnalyzing(): Boolean {
        return try {
            val leakCanaryClass = Class.forName("com.squareup.leakcanary.LeakCanary")
            val method = leakCanaryClass.getMethod("isInAnalyzerProcess", Context::class.java)
            method.invoke(null, com.example.stability.MyApplication.instance) as Boolean
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 获取 LeakCanary 配置信息
     * @return 配置信息字符串
     */
    fun getConfigInfo(): String {
        if (!isInitialized) {
            return "LeakCanary not initialized"
        }

        return try {
            val leakCanaryClass = Class.forName("com.squareup.leakcanary.LeakCanary")
            val configField = leakCanaryClass.getDeclaredField("config")
            configField.isAccessible = true
            val config = configField.get(null)
            
            buildString {
                append("=== LeakCanary Configuration ===\n")
                append("Config: $config")
            }
        } catch (e: ClassNotFoundException) {
            "LeakCanary not available (release build)"
        } catch (e: Exception) {
            "Failed to get config: ${e.message}"
        }
    }

    /**
     * 获取管理器状态
     * @return 状态字符串
     */
    fun getStatus(): String {
        return buildString {
            append("=== LeakCanaryManager Status ===\n")
            append("Initialized: $isInitialized\n")
            append("Is Analyzing: ${isAnalyzing()}")
        }
    }
}

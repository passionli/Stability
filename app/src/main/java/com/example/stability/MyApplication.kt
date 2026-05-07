package com.example.stability

import android.app.Application
import com.example.nativelib.NativeLib
import com.example.stability.oom.OomMain

/**
 * 应用程序入口类
 * 负责初始化应用级别的组件和服务
 */
class MyApplication : Application() {

    companion object {
        /**
         * 应用实例引用
         */
        lateinit var instance: MyApplication
            private set

        init {
            // 在 static 语句块中调用 NativeLib().stringFromJNI()
            // NativeLib().stringFromJNI()
        }
    }

    override fun onCreate() {
        super.onCreate()
        
        // 保存应用实例
        instance = this
        
        // 初始化 LeakCanary（仅在 debug 版本，通过反射避免编译错误）
        initLeakCanary()
        
        // 初始化 OOM 模块
        initOomModule()
    }

    /**
     * 初始化 LeakCanary 内存泄漏检测框架
     * LeakCanary 会自动检测 Activity、Fragment 等组件的内存泄漏
     * 并在检测到泄漏时显示通知，提供详细的泄漏路径分析
     */
    private fun initLeakCanary() {
        try {
            // 通过反射调用 LeakCanary.install()，避免 release 版本编译错误
            val leakCanaryClass = Class.forName("com.squareup.leakcanary.LeakCanary")
            
            // 检查是否是分析进程
            val isAnalyzerProcessMethod = leakCanaryClass.getMethod("isInAnalyzerProcess", Application::class.java)
            val isAnalyzerProcess = isAnalyzerProcessMethod.invoke(null, this) as Boolean
            
            if (isAnalyzerProcess) {
                // 此进程是 LeakCanary 的分析进程，不应初始化应用
                return
            }
            
            // 获取并配置 Config
            val configField = leakCanaryClass.getDeclaredField("config")
            configField.isAccessible = true
            val currentConfig = configField.get(null)
            
            // 使用反射调用 copy 方法更新配置
            val configClass = Class.forName("com.squareup.leakcanary.Config")
            val copyMethod = configClass.getMethod("copy",
                Boolean::class.java,
                Long::class.java,
                Boolean::class.java
            )
            
            val customConfig = copyMethod.invoke(currentConfig,
                true,     // dumpHeap
                5000L,    // watchDurationMillis
                true      // showLeakDisplayActivityLauncherIcon
            )
            
            configField.set(null, customConfig)
            
            // 安装 LeakCanary
            val installMethod = leakCanaryClass.getMethod("install", Application::class.java)
            installMethod.invoke(null, this)
            
            // 打印初始化日志
            android.util.Log.i("MyApplication", "LeakCanary initialized successfully")
            
        } catch (e: ClassNotFoundException) {
            // LeakCanary 在 release 版本中不可用，这是正常的
            android.util.Log.i("MyApplication", "LeakCanary not available (release build)")
        } catch (e: Exception) {
            android.util.Log.e("MyApplication", "Failed to initialize LeakCanary", e)
        }
    }

    /**
     * 初始化 OOM 监控模块
     * 提供内存监控、OOM 异常捕获等功能
     */
    private fun initOomModule() {
        try {
            OomMain.initialize(this)
            android.util.Log.i("MyApplication", "OOM module initialized successfully")
        } catch (e: Exception) {
            android.util.Log.e("MyApplication", "Failed to initialize OOM module", e)
        }
    }
}

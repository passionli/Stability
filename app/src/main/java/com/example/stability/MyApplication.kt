package com.example.stability

import android.app.Application
import android.util.Log
import com.alibaba.android.arouter.launcher.ARouter
import com.example.nativelib.NativeLib
import com.example.stability.oom.OomMain

class MyApplication : Application() {

    companion object {
        lateinit var instance: MyApplication
            private set
        init {
            NativeLib().stringFromJNI()
        }
    }

    override fun onCreate() {
        super.onCreate()

        instance = this

        initARouter()
        initLeakCanary()
        initOomModule()
    }

    private fun initARouter() {
        try {
            Log.d("ARouterInit", "=== ARouter Initialization Started ===")
            Log.d("ARouterInit", "Package: $packageName")
            Log.d("ARouterInit", "isDebugMode: ${isDebugMode()}")

            if (isDebugMode()) {
                Log.d("ARouterInit", "Opening ARouter debug mode and logs")
                ARouter.openDebug()
                ARouter.openLog()
            }

            Log.d("ARouterInit", "Calling ARouter.init(this)")
            ARouter.init(this)

            Log.i("MyApplication", "ARouter initialized successfully")
            Log.d("ARouterInit", "ARouter initialization completed")
        } catch (e: Exception) {
            Log.e("MyApplication", "Failed to initialize ARouter", e)
            e.printStackTrace()
        }
    }

    private fun isDebugMode(): Boolean {
        return try {
            val buildConfigClass = Class.forName("${packageName}.BuildConfig")
            val debugField = buildConfigClass.getField("DEBUG")
            debugField.getBoolean(null)
        } catch (e: Exception) {
            Log.d("ARouterInit", "isDebugMode() exception: ${e.message}")
            false
        }
    }

    private fun initLeakCanary() {
        try {
            val leakCanaryClass = Class.forName("com.squareup.leakcanary.LeakCanary")

            val isAnalyzerProcessMethod = leakCanaryClass.getMethod("isInAnalyzerProcess", Application::class.java)
            val isAnalyzerProcess = isAnalyzerProcessMethod.invoke(null, this) as Boolean

            if (isAnalyzerProcess) {
                return
            }

            val configField = leakCanaryClass.getDeclaredField("config")
            configField.isAccessible = true
            val currentConfig = configField.get(null)

            val configClass = Class.forName("com.squareup.leakcanary.Config")
            val copyMethod = configClass.getMethod("copy",
                Boolean::class.java,
                Long::class.java,
                Boolean::class.java
            )

            val customConfig = copyMethod.invoke(currentConfig,
                true,
                5000L,
                true
            )

            configField.set(null, customConfig)

            val installMethod = leakCanaryClass.getMethod("install", Application::class.java)
            installMethod.invoke(null, this)

            Log.i("MyApplication", "LeakCanary initialized successfully")

        } catch (e: ClassNotFoundException) {
            Log.i("MyApplication", "LeakCanary not available (release build)")
        } catch (e: Exception) {
            Log.e("MyApplication", "Failed to initialize LeakCanary", e)
        }
    }

    private fun initOomModule() {
        try {
            OomMain.initialize(this)
            Log.i("MyApplication", "OOM module initialized successfully")
        } catch (e: Exception) {
            Log.e("MyApplication", "Failed to initialize OOM module", e)
        }
    }
}

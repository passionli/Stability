package com.example.stability

import android.app.Application
import android.app.Activity
import android.util.Log
import com.alibaba.android.arouter.launcher.ARouter
import com.example.nativelib.NativeLib
import com.example.stability.oom.OomMain
import com.example.stability.hook.ProxyThread
import java.lang.reflect.Executable

class MyActivity:Activity() {
    override fun toString(): String {
        return "MyActivity: " + super.toString()
    }
}

class MyApplication : Application() {

    companion object {
        lateinit var instance: MyApplication
            private set
        init {
            // 反射获取 Thread.start 和 ProxyThread.start 方法对应的 ArtMethod 指针
            val threadStartMethod = Thread::class.java.getDeclaredMethod("start")
            val proxyThreadStartMethod = ProxyThread::class.java.getDeclaredMethod("start")
            threadStartMethod.isAccessible = true
            proxyThreadStartMethod.isAccessible = true
            // 获取 Thread.start 的 artMethod 指针
            val threadArtMethodField = Executable::class.java.getDeclaredField("artMethod")
            threadArtMethodField.isAccessible = true
            val threadArtMethod = threadArtMethodField.get(threadStartMethod)
            
            // 获取 ProxyThread.start 的 artMethod 指针
            val proxyThreadArtMethodField = Executable::class.java.getDeclaredField("artMethod")
            proxyThreadArtMethodField.isAccessible = true
            val proxyThreadArtMethod = proxyThreadArtMethodField.get(proxyThreadStartMethod)
            
            Log.d("MyApplication", "Thread.start artMethod: $threadArtMethod")
            Log.d("MyApplication", "ProxyThread.start artMethod: $proxyThreadArtMethod")
            NativeLib.proxyMethod = proxyThreadArtMethod as Long
            NativeLib.originMethod = threadArtMethod as Long
            
            NativeLib().stringFromJNI(proxyThreadArtMethod as Long, threadArtMethod as Long)

            NativeLib.deoptimize(threadArtMethod as Long)

            try {
//                val measureClass = Class.forName("com.example.stability.MeasureArtMethodSize")
//                // 获取 invoke 方法
//                val method = measureClass.getDeclaredMethod("invoke", Object::class.java, Array::class.java)
//                method.isAccessible = true
//                val obj = Any()
//                val objWrapper : Array<Any> = arrayOf(method, obj)
//                val args : Array<Any> = arrayOf(true, 1, 'c', Any())
//                method.invoke(objWrapper, args)
//                Log.d("MyApplication", "dummyInvokeForRunnableState invoked via reflection")
//
//                val activity = MyActivity()
//                val toStringMethod = Activity::class.java.getDeclaredMethod("toString")
//                toStringMethod.isAccessible = true
//                val shorty : Array<Char> = getShorty(toStringMethod as Executable)
//                val result = method.invoke(arrayOf(shorty, toStringMethod, activity))
//                Log.d("MyApplication", "MyActivity toString: $result")
//                Log.d("MyApplication", "MyActivity: " + result)
            } catch (e: Exception) {
                Log.e("MyApplication", "Failed to invoke dummyInvokeForRunnableState", e)
            }
        }

        private fun getShorty(method: Executable): Array<Char> {
            val shorty = ArrayList<Char>()
            
            // 处理返回值类型
            // 如果是 Method 类型
            if (method is java.lang.reflect.Method) {
                val returnType = method.returnType
                shorty.add(getTypeChar(returnType))
            }
            
            // 处理参数类型
            for (paramType in method.parameterTypes) {
                shorty.add(getTypeChar(paramType))
            }
            
            return shorty.toTypedArray()
        }

        private fun getTypeChar(type: Class<*>): Char {
            return when {
                type == Void.TYPE -> 'V'
                type == Boolean::class.javaPrimitiveType || type == Boolean::class.java -> 'Z'
                type == Byte::class.javaPrimitiveType || type == Byte::class.java -> 'B'
                type == Char::class.javaPrimitiveType || type == Char::class.java -> 'C'
                type == Short::class.javaPrimitiveType || type == Short::class.java -> 'S'
                type == Int::class.javaPrimitiveType || type == Int::class.java -> 'I'
                type == Long::class.javaPrimitiveType || type == Long::class.java -> 'J'
                type == Float::class.javaPrimitiveType || type == Float::class.java -> 'F'
                type == Double::class.javaPrimitiveType || type == Double::class.java -> 'D'
                type.isArray -> '['  // 数组类型简化为 [
                else -> 'L'  // 对象类型简化为 L
            }
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

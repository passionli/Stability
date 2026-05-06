package com.example.stability.oom.prevention

import android.app.Activity
import android.content.Context
import android.os.Handler
import com.example.stability.oom.utils.OomLog
import java.lang.ref.WeakReference

/**
 * 内存泄漏检测器
 * 提供常见内存泄漏模式的检测功能
 */
object LeakDetector {
    
    private const val TAG = "LeakDetector"
    
    /**
     * 检查 Context 是否可能泄漏
     * @param context 需要检查的 Context
     * @param tag 标识标签，用于日志输出
     */
    fun checkContextLeak(context: Context, tag: String) {
        if (context is Activity && context.isFinishing) {
            OomLog.w(TAG, "Potential context leak detected: $tag - Activity is finishing")
        }
    }
    
    /**
     * 检查 Handler 是否可能导致泄漏
     * @param handler 需要检查的 Handler
     * @param context 关联的 Context
     */
    fun checkHandlerLeak(handler: Handler, context: Context) {
        try {
            // 通过反射获取 Handler 的 mCallback 字段
            val callbackField = Handler::class.java.getDeclaredField("mCallback")
            callbackField.isAccessible = true
            val callback = callbackField.get(handler)
            
            if (callback != null) {
                val callbackClass = callback.javaClass
                // 如果回调是内部类，可能存在泄漏风险
                if (callbackClass.enclosingClass != null) {
                    OomLog.w(TAG, "Handler has inner class callback, potential leak risk")
                }
            }
        } catch (e: Exception) {
            OomLog.e(TAG, "Failed to check handler leak", e)
        }
    }
    
    /**
     * 检查静态字段是否持有 Activity 引用
     * @param clazz 需要检查的类
     */
    fun checkStaticFieldLeak(clazz: Class<*>) {
        try {
            val fields = clazz.declaredFields
            
            for (field in fields) {
                if (java.lang.reflect.Modifier.isStatic(field.modifiers)) {
                    field.isAccessible = true
                    val value = field.get(null)
                    
                    if (value is Context && value is Activity) {
                        OomLog.e(TAG, "Static field '${field.name}' holds Activity reference!")
                    }
                }
            }
        } catch (e: Exception) {
            OomLog.e(TAG, "Failed to check static field leak", e)
        }
    }
    
    /**
     * 创建安全的 Handler（使用 WeakReference 避免泄漏）
     * @param context 关联的 Context
     * @param callback 回调函数
     * @return 使用 WeakReference 的 Handler
     */
    fun createSafeHandler(context: Context, callback: Handler.Callback): SafeHandler {
        return SafeHandler(context, callback)
    }
    
    /**
     * 安全的 Handler 包装类
     * 使用 WeakReference 持有 Context，避免内存泄漏
     */
    class SafeHandler(context: Context, private val callback: Handler.Callback) : Handler() {
        
        // 使用 WeakReference 持有 Context
        private val contextRef = WeakReference(context)
        
        override fun handleMessage(msg: android.os.Message) {
            // 检查 Context 是否还存在
            val context = contextRef.get()
            if (context != null) {
                callback.handleMessage(msg)
            } else {
                OomLog.w(TAG, "Context has been garbage collected, message ignored")
            }
        }
    }
    
    /**
     * 创建安全的 Runnable（使用 WeakReference 避免泄漏）
     * @param context 关联的 Context
     * @param action 执行动作
     * @return 使用 WeakReference 的 Runnable
     */
    fun createSafeRunnable(context: Context, action: (Context) -> Unit): SafeRunnable {
        return SafeRunnable(context, action)
    }
    
    /**
     * 安全的 Runnable 包装类
     * 使用 WeakReference 持有 Context，避免内存泄漏
     */
    class SafeRunnable(context: Context, private val action: (Context) -> Unit) : Runnable {
        
        // 使用 WeakReference 持有 Context
        private val contextRef = WeakReference(context)
        
        override fun run() {
            // 检查 Context 是否还存在
            val context = contextRef.get()
            if (context != null) {
                action(context)
            } else {
                OomLog.w(TAG, "Context has been garbage collected, runnable ignored")
            }
        }
    }
    
    /**
     * 检查监听器是否正确注册/注销
     * 这是一个辅助方法，用于提醒开发者检查监听器管理
     * @param registered 是否已注册
     * @param listenerName 监听器名称
     */
    fun checkListenerRegistration(registered: Boolean, listenerName: String) {
        if (registered) {
            OomLog.w(TAG, "Listener '$listenerName' is still registered, remember to unregister!")
        }
    }
}

package com.example.stability.oom.examples

import android.content.Context
import android.os.Handler
import android.os.Looper
import com.example.stability.oom.utils.OomLog
import java.lang.ref.WeakReference

/**
 * 内存泄漏示例类
 * 包含多种常见的内存泄漏场景，用于演示和测试 LeakCanary 的检测能力
 */
object LeakExamples {

    /**
     * 日志标签
     */
    private const val TAG = "LeakExamples"

    // ==================== 泄漏场景1：静态引用持有 Activity ====================

    /**
     * 静态变量持有 Activity 引用
     * 这是最常见的内存泄漏场景之一
     */
    object StaticReferenceLeak {
        // 静态变量持有 Context，导致内存泄漏
        private var leakedContext: Context? = null

        /**
         * 模拟静态引用泄漏
         * @param context Activity 上下文（传入 Activity 会导致泄漏）
         */
        fun simulateLeak(context: Context) {
            OomLog.w(TAG, "Simulating static reference leak...")
            // 将 Activity 上下文保存到静态变量中
            // 如果传入的是 Activity，即使 Activity 销毁，这个静态引用仍会持有它
            leakedContext = context
            OomLog.d(TAG, "Static reference leak simulated: ${context.javaClass.simpleName}")
        }

        /**
         * 修复泄漏：清除静态引用
         */
        fun fixLeak() {
            OomLog.i(TAG, "Fixing static reference leak...")
            leakedContext = null
            OomLog.d(TAG, "Static reference leak fixed")
        }
    }

    // ==================== 泄漏场景2：Handler 匿名内部类泄漏 ====================

    /**
     * Handler 匿名内部类泄漏
     * Handler 隐式持有外部类（如 Activity）的引用，延迟消息会导致泄漏
     */
    class HandlerLeakActivityHelper(private val context: Context) {
        
        // 匿名内部类 Handler，隐式持有外部类引用
        private val handler = object : Handler(Looper.getMainLooper()) {
            override fun handleMessage(msg: android.os.Message) {
                // 处理消息时使用 context
                OomLog.d(TAG, "Handler received message")
            }
        }

        /**
         * 模拟 Handler 泄漏
         * 发送延迟消息，即使 Activity 销毁，消息仍在队列中，持有引用
         */
        fun simulateLeak() {
            OomLog.w(TAG, "Simulating Handler leak...")
            // 发送一个延迟 1 分钟的消息
            // 如果 Activity 在这期间销毁，Handler 仍持有 Activity 引用
            handler.postDelayed({
                OomLog.d(TAG, "Delayed task executed")
            }, 60000) // 60秒后执行
            OomLog.d(TAG, "Handler leak simulated: posted delayed task")
        }

        /**
         * 修复泄漏：移除所有消息
         */
        fun fixLeak() {
            OomLog.i(TAG, "Fixing Handler leak...")
            handler.removeCallbacksAndMessages(null)
            OomLog.d(TAG, "Handler leak fixed: removed all callbacks")
        }
    }

    // ==================== 泄漏场景3：监听器未注销 ====================

    /**
     * 监听器泄漏
     * 全局事件总线注册监听器但未注销，导致持有 Activity 引用
     */
    object ListenerLeak {
        
        // 模拟全局事件总线
        private val listeners = mutableListOf<EventListener>()

        /**
         * 事件监听器接口
         */
        interface EventListener {
            fun onEvent(event: String)
        }

        /**
         * 注册监听器（模拟泄漏）
         */
        fun registerListener(listener: EventListener) {
            OomLog.w(TAG, "Simulating listener leak...")
            listeners.add(listener)
            OomLog.d(TAG, "Listener registered, total: ${listeners.size}")
        }

        /**
         * 注销监听器（修复泄漏）
         */
        fun unregisterListener(listener: EventListener) {
            OomLog.i(TAG, "Fixing listener leak...")
            listeners.remove(listener)
            OomLog.d(TAG, "Listener unregistered, total: ${listeners.size}")
        }

        /**
         * 触发事件
         */
        fun triggerEvent(event: String) {
            listeners.forEach { it.onEvent(event) }
        }
    }

    // ==================== 泄漏场景4：静态内部类持有外部引用 ====================

    /**
     * 静态内部类泄漏
     * 静态内部类持有外部类的引用（如 Context）
     */
    object StaticInnerClassLeak {
        
        /**
         * 静态内部类，持有外部引用
         */
        class DataManager private constructor(private val context: Context) {
            
            init {
                OomLog.d(TAG, "DataManager created with context: ${context.javaClass.simpleName}")
            }

            companion object {
                // 静态实例，持有 Context 引用
                private var instance: DataManager? = null

                /**
                 * 获取单例实例（可能导致泄漏）
                 */
                fun getInstance(context: Context): DataManager {
                    OomLog.w(TAG, "Simulating static inner class leak...")
                    if (instance == null) {
                        // 如果传入 Activity，会导致泄漏
                        instance = DataManager(context)
                    }
                    return instance!!
                }

                /**
                 * 销毁实例（修复泄漏）
                 */
                fun destroy() {
                    OomLog.i(TAG, "Fixing static inner class leak...")
                    instance = null
                }
            }
        }
    }

    // ==================== 泄漏场景5：线程持有 Activity 引用 ====================

    /**
     * 线程泄漏
     * 后台线程持有 Activity 引用，即使 Activity 销毁线程仍在运行
     */
    class ThreadLeakHelper(private val context: Context) {
        
        private var backgroundThread: Thread? = null

        /**
         * 模拟线程泄漏
         */
        fun simulateLeak() {
            OomLog.w(TAG, "Simulating thread leak...")
            
            // 创建后台线程，匿名内部类持有外部类引用（包含 context）
            backgroundThread = Thread {
                try {
                    // 模拟长时间运行的任务
                    for (i in 0..100) {
                        Thread.sleep(1000)
                        // 使用 context，持有引用
                        OomLog.d(TAG, "Thread working: $i/100")
                    }
                } catch (e: InterruptedException) {
                    Thread.currentThread().interrupt()
                }
            }
            
            backgroundThread?.start()
            OomLog.d(TAG, "Thread leak simulated: background thread started")
        }

        /**
         * 修复泄漏：中断线程
         */
        fun fixLeak() {
            OomLog.i(TAG, "Fixing thread leak...")
            backgroundThread?.interrupt()
            backgroundThread = null
            OomLog.d(TAG, "Thread leak fixed: thread interrupted")
        }
    }

    // ==================== 泄漏场景6：错误的单例模式 ====================

    /**
     * 错误的单例模式导致泄漏
     * 单例持有 Activity 而不是 Application Context
     */
    class WrongSingletonLeak private constructor() {
        
        companion object {
            private var instance: WrongSingletonLeak? = null
            private var context: Context? = null

            /**
             * 获取单例（错误方式，传入 Activity 会泄漏）
             */
            fun getInstance(ctx: Context): WrongSingletonLeak {
                OomLog.w(TAG, "Simulating wrong singleton leak...")
                if (instance == null) {
                    instance = WrongSingletonLeak()
                    // ❌ 错误：持有 Activity 引用
                    context = ctx
                }
                return instance!!
            }

            /**
             * 正确的单例获取方式
             */
            fun getInstanceCorrect(ctx: Context): WrongSingletonLeak {
                OomLog.i(TAG, "Getting singleton with application context...")
                if (instance == null) {
                    instance = WrongSingletonLeak()
                    // ✅ 正确：使用 Application Context
                    context = ctx.applicationContext
                }
                return instance!!
            }

            /**
             * 销毁单例
             */
            fun destroy() {
                OomLog.i(TAG, "Fixing singleton leak...")
                context = null
                instance = null
            }
        }
    }

    // ==================== 泄漏场景7：安全的 Handler 实现（正确示例） ====================

    /**
     * 安全的 Handler 实现
     * 使用 WeakReference 避免内存泄漏
     */
    class SafeHandler(context: Context) {
        
        // 使用 WeakReference 持有 Context
        private val contextRef = WeakReference(context)
        
        // Handler 使用静态内部类，不持有外部类引用
        private val handler = SafeStaticHandler()

        /**
         * 静态内部类 Handler，不持有外部类引用
         */
        private class SafeStaticHandler : Handler(Looper.getMainLooper()) {
            override fun handleMessage(msg: android.os.Message) {
                // 通过 msg.obj 获取 WeakReference
                val ref = msg.obj as WeakReference<Context>
                val context = ref.get()
                if (context != null) {
                    OomLog.d(TAG, "SafeHandler received message")
                } else {
                    OomLog.d(TAG, "SafeHandler: Context already garbage collected")
                }
            }
        }

        /**
         * 安全地发送延迟消息
         */
        fun postDelayedTask(delayMs: Long) {
            val msg = handler.obtainMessage()
            // 将 WeakReference 作为消息的 obj 传递
            msg.obj = contextRef
            handler.sendMessageDelayed(msg, delayMs)
            OomLog.d(TAG, "SafeHandler: posted delayed task")
        }

        /**
         * 清理 Handler
         */
        fun cleanup() {
            handler.removeCallbacksAndMessages(null)
            OomLog.d(TAG, "SafeHandler cleaned up")
        }
    }

    // ==================== 辅助方法 ====================

    /**
     * 获取所有泄漏示例名称
     */
    fun getLeakExampleNames(): List<String> {
        return listOf(
            "静态引用泄漏",
            "Handler 泄漏",
            "监听器泄漏",
            "静态内部类泄漏",
            "线程泄漏",
            "单例模式泄漏"
        )
    }

    /**
     * 根据名称执行泄漏示例
     */
    fun runLeakExample(name: String, context: Context): Boolean {
        return when (name) {
            "静态引用泄漏" -> {
                StaticReferenceLeak.simulateLeak(context)
                true
            }
            "Handler 泄漏" -> {
                HandlerLeakActivityHelper(context).simulateLeak()
                true
            }
            "监听器泄漏" -> {
                ListenerLeak.registerListener(object : ListenerLeak.EventListener {
                    override fun onEvent(event: String) {
                        OomLog.d(TAG, "Event received: $event")
                    }
                })
                true
            }
            "静态内部类泄漏" -> {
                StaticInnerClassLeak.DataManager.getInstance(context)
                true
            }
            "线程泄漏" -> {
                ThreadLeakHelper(context).simulateLeak()
                true
            }
            "单例模式泄漏" -> {
                WrongSingletonLeak.getInstance(context)
                true
            }
            else -> false
        }
    }

    /**
     * 修复所有泄漏示例
     */
    fun fixAllLeaks() {
        OomLog.i(TAG, "Fixing all leaks...")
        
        StaticReferenceLeak.fixLeak()
        StaticInnerClassLeak.DataManager.destroy()
        WrongSingletonLeak.Companion.destroy()
        
        OomLog.i(TAG, "All leaks fixed")
    }
}

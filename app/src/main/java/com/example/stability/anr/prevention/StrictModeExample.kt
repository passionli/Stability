package com.example.stability.anr.prevention

import android.os.Build
import android.os.StrictMode
import com.example.stability.anr.utils.AnrLog

/**
 * StrictMode 配置示例
 * StrictMode 是 Android 提供的一个开发者工具，用于检测潜在的性能问题
 */
object StrictModeExample {
    
    /**
     * 启用 StrictMode（开发阶段）
     * 建议只在 DEBUG 模式下启用，避免影响生产环境性能
     */
    fun enableStrictMode() {
        if (!isDebugMode()) {
            AnrLog.i("StrictMode not enabled in release build")
            return
        }
        
        AnrLog.i("Enabling StrictMode...")
        
        // ========== 线程策略 ==========
        val threadPolicyBuilder = StrictMode.ThreadPolicy.Builder()
            .detectDiskReads()           // 检测主线程磁盘读取
            .detectDiskWrites()          // 检测主线程磁盘写入
            .detectNetwork()             // 检测主线程网络请求
            .detectCustomSlowCalls()     // 检测自定义慢调用
            .detectResourceMismatches()  // 检测资源不匹配
        
        // Android 11+ 新增的检测项
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            threadPolicyBuilder.detectUnbufferedIo()  // 检测无缓冲 I/O
        }
        
        // 设置违规处理方式
        threadPolicyBuilder.penaltyLog()  // 输出日志
        // threadPolicyBuilder.penaltyDeath()  // 崩溃（可选，用于严重问题）
        // threadPolicyBuilder.penaltyDialog() // 显示对话框
        
        StrictMode.setThreadPolicy(threadPolicyBuilder.build())
        
        // ========== VM 策略 ==========
        val vmPolicyBuilder = StrictMode.VmPolicy.Builder()
            .detectLeakedSqlLiteObjects()     // 检测 SQLite 对象泄漏
            .detectLeakedClosableObjects()    // 检测可关闭对象泄漏
            .detectActivityLeaks()            // 检测 Activity 泄漏
            .detectLeakedRegistrationObjects() // 检测注册对象泄漏
            .detectFileUriExposure()          // 检测文件 URI 暴露
        
        // Android 8.0+ 新增
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vmPolicyBuilder.detectContentUriWithoutPermission() // 检测内容 URI 权限问题
        }
        
        // Android 10+ 新增
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            vmPolicyBuilder.detectCredentialProtectedWhileLocked() // 检测锁定时的凭证保护
        }
        
        // 设置违规处理方式
        vmPolicyBuilder.penaltyLog()
        
        StrictMode.setVmPolicy(vmPolicyBuilder.build())
        
        AnrLog.i("StrictMode enabled successfully")
    }
    
    /**
     * 禁用 StrictMode
     */
    fun disableStrictMode() {
        StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.LAX)
        StrictMode.setVmPolicy(StrictMode.VmPolicy.LAX)
        AnrLog.i("StrictMode disabled")
    }
    
    /**
     * 仅启用线程策略（轻量级检测）
     */
    fun enableThreadPolicyOnly() {
        if (!isDebugMode()) return
        
        val threadPolicy = StrictMode.ThreadPolicy.Builder()
            .detectAll()
            .penaltyLog()
            .build()
        
        StrictMode.setThreadPolicy(threadPolicy)
        AnrLog.i("StrictMode thread policy enabled")
    }
    
    /**
     * 仅启用 VM 策略
     */
    fun enableVmPolicyOnly() {
        if (!isDebugMode()) return
        
        val vmPolicy = StrictMode.VmPolicy.Builder()
            .detectAll()
            .penaltyLog()
            .build()
        
        StrictMode.setVmPolicy(vmPolicy)
        AnrLog.i("StrictMode VM policy enabled")
    }
    
    /**
     * 检查是否是调试模式
     */
    private fun isDebugMode(): Boolean {
        return try {
            val buildConfigClass = Class.forName("com.example.stability.BuildConfig")
            val debugField = buildConfigClass.getField("DEBUG")
            debugField.getBoolean(null)
        } catch (e: Exception) {
            AnrLog.e("Failed to check debug mode", e)
            false
        }
    }
    
    /**
     * 获取当前 StrictMode 策略信息
     */
    fun getPolicyInfo(): String {
        val sb = StringBuilder()
        sb.append("=== StrictMode Policy Info ===\n")
        
        val threadPolicy = StrictMode.getThreadPolicy()
        sb.append("Thread Policy: ${threadPolicy.javaClass.simpleName}\n")
        
        val vmPolicy = StrictMode.getVmPolicy()
        sb.append("VM Policy: ${vmPolicy.javaClass.simpleName}\n")
        
        return sb.toString()
    }
}

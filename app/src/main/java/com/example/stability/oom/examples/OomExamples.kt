package com.example.stability.oom.examples

import android.content.Context
import android.graphics.BitmapFactory
import android.util.Log
import com.example.stability.oom.utils.OomLog
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

/**
 * OOM 经典示例类
 * 包含各种触发 OOM 的场景示例
 */
object OomExamples {
    
    private const val TAG = "OomExamples"
    
    /**
     * 示例1：大数组分配 OOM
     * 尝试分配超出堆内存限制的超大数组
     */
    fun triggerArrayOom(): Boolean {
        return try {
            OomLog.w(TAG, "Attempting to allocate huge array...")
            // 请求分配超大数组，会触发 OOM
            val hugeArray = ByteArray(Int.MAX_VALUE)
            OomLog.e(TAG, "Array allocation succeeded (unexpected)")
            false
        } catch (e: OutOfMemoryError) {
            OomLog.e(TAG, "Array allocation OOM triggered!", e)
            true
        } catch (e: IllegalArgumentException) {
            // 某些情况下会先抛出 IllegalArgumentException
            OomLog.e(TAG, "Array size too large: ${e.message}")
            true
        }
    }
    
    /**
     * 示例2：循环分配数组导致 OOM
     * 不断分配 10MB 数组直到内存耗尽
     */
    fun triggerMultipleArrayOom(): Boolean {
        return try {
            OomLog.w(TAG, "Attempting multiple array allocation...")
            val list = mutableListOf<ByteArray>()
            var count = 0
            
            while (true) {
                // 每次分配 10MB
                list.add(ByteArray(10 * 1024 * 1024))
                count++
                if (count % 10 == 0) {
                    OomLog.d(TAG, "Allocated $count arrays (${count * 10} MB)")
                }
            }
            false
        } catch (e: OutOfMemoryError) {
            OomLog.e(TAG, "Multiple array allocation OOM triggered!", e)
            true
        }
    }
    
    /**
     * 示例3：大图片加载 OOM
     * 直接加载大图不进行采样
     */
    fun triggerBitmapOom(context: Context): Boolean {
        return try {
            OomLog.w(TAG, "Attempting to load large bitmap...")
            // 直接加载资源中的图片，不进行采样
            val bitmap = BitmapFactory.decodeResource(context.resources, android.R.drawable.ic_menu_gallery)
            OomLog.d(TAG, "Bitmap loaded: ${bitmap.width}x${bitmap.height}")
            false
        } catch (e: OutOfMemoryError) {
            OomLog.e(TAG, "Bitmap OOM triggered!", e)
            true
        }
    }
    
    /**
     * 示例4：循环加载大量图片导致 OOM
     */
    fun triggerMultipleBitmapOom(context: Context): Boolean {
        return try {
            OomLog.w(TAG, "Attempting multiple bitmap allocation...")
            val bitmapList = mutableListOf<android.graphics.Bitmap>()
            
            for (i in 0..100) {
                val bitmap = BitmapFactory.decodeResource(context.resources, android.R.drawable.ic_menu_gallery)
                bitmapList.add(bitmap)
                if (i % 10 == 0) {
                    OomLog.d(TAG, "Loaded $i bitmaps")
                }
            }
            OomLog.d(TAG, "All bitmaps loaded successfully")
            false
        } catch (e: OutOfMemoryError) {
            OomLog.e(TAG, "Multiple bitmap OOM triggered!", e)
            true
        }
    }
    
    /**
     * 示例5：字符串拼接导致 OOM
     * 大量字符串拼接产生大量中间对象
     */
    fun triggerStringConcatOom(): Boolean {
        return try {
            OomLog.w(TAG, "Attempting string concatenation...")
            var result = ""
            
            for (i in 0..100000) {
                // 每次拼接都会创建新的 String 对象
                result += "Item $i\n"
                
                if (i % 10000 == 0) {
                    OomLog.d(TAG, "Concatenated $i items")
                }
            }
            OomLog.d(TAG, "String concatenation completed: ${result.length} chars")
            false
        } catch (e: OutOfMemoryError) {
            OomLog.e(TAG, "String concatenation OOM triggered!", e)
            true
        }
    }
    
    /**
     * 示例6：集合无限制增长
     * 不断向集合添加元素直到内存耗尽
     */
    fun triggerCollectionGrowthOom(): Boolean {
        return try {
            OomLog.w(TAG, "Attempting collection growth...")
            val list = mutableListOf<DataObject>()
            
            while (true) {
                list.add(DataObject())
                
                if (list.size % 10000 == 0) {
                    OomLog.d(TAG, "Added ${list.size} objects")
                }
            }
            false
        } catch (e: OutOfMemoryError) {
            OomLog.e(TAG, "Collection growth OOM triggered!", e)
            true
        }
    }
    
    /**
     * 示例7：创建大量线程
     * 创建大量线程导致内存不足
     */
    fun triggerThreadCreationOom(): Boolean {
        return try {
            OomLog.w(TAG, "Attempting thread creation...")
            val threads = mutableListOf<Thread>()
            
            for (i in 0..100) {
                val thread = Thread {
                    try {
                        Thread.sleep(60000)
                    } catch (e: InterruptedException) {
                        // ignored
                    }
                }
                thread.start()
                threads.add(thread)
                
                if (i % 20 == 0) {
                    OomLog.d(TAG, "Created $i threads")
                }
            }
            OomLog.d(TAG, "All threads created successfully")
            false
        } catch (e: OutOfMemoryError) {
            OomLog.e(TAG, "Thread creation OOM triggered!", e)
            true
        } catch (e: Exception) {
            OomLog.e(TAG, "Thread creation failed: ${e.message}", e)
            true
        }
    }
    
    /**
     * 示例8：线程池配置不合理
     * 提交大量任务到线程池
     */
    fun triggerThreadPoolOom(): Boolean {
        return try {
            OomLog.w(TAG, "Attempting thread pool overload...")
            
            val executor = ThreadPoolExecutor(
                10,      // corePoolSize
                100,     // maximumPoolSize
                60L,
                TimeUnit.SECONDS,
                LinkedBlockingQueue()
            )
            
            for (i in 0..1000) {
                executor.execute {
                    try {
                        Thread.sleep(1000)
                    } catch (e: InterruptedException) {
                        // ignored
                    }
                }
                
                if (i % 200 == 0) {
                    OomLog.d(TAG, "Submitted $i tasks")
                }
            }
            OomLog.d(TAG, "All tasks submitted successfully")
            false
        } catch (e: OutOfMemoryError) {
            OomLog.e(TAG, "Thread pool OOM triggered!", e)
            true
        } catch (e: Exception) {
            OomLog.e(TAG, "Thread pool error: ${e.message}", e)
            true
        }
    }
    
    /**
     * 示例9：递归深度过大
     * 递归调用深度过大导致栈溢出或堆内存耗尽
     */
    fun triggerDeepRecursion(): Boolean {
        return try {
            OomLog.w(TAG, "Attempting deep recursion...")
            deepRecursion(10000)
            false
        } catch (e: StackOverflowError) {
            OomLog.e(TAG, "Stack overflow triggered!", e)
            true
        } catch (e: OutOfMemoryError) {
            OomLog.e(TAG, "Recursion OOM triggered!", e)
            true
        }
    }
    
    /**
     * 深度递归方法
     */
    private fun deepRecursion(depth: Int) {
        if (depth > 0) {
            // 创建对象并递归
            val data = DataObject()
            deepRecursion(depth - 1)
        }
    }
    
    /**
     * 测试数据对象
     */
    class DataObject {
        // 包含一定大小的数据
        val array = ByteArray(1024)
        val name = "DataObject"
    }
    
    /**
     * 获取所有可用的示例名称
     */
    fun getExampleNames(): List<String> {
        return listOf(
            "大数组分配 OOM",
            "循环分配数组 OOM",
            "大图片加载 OOM",
            "循环加载图片 OOM",
            "字符串拼接 OOM",
            "集合无限增长 OOM",
            "创建大量线程 OOM",
            "线程池过载 OOM",
            "深递归 OOM"
        )
    }
    
    /**
     * 根据名称执行对应的示例
     */
    fun runExampleByName(name: String, context: Context): Boolean {
        return when (name) {
            "大数组分配 OOM" -> triggerArrayOom()
            "循环分配数组 OOM" -> triggerMultipleArrayOom()
            "大图片加载 OOM" -> triggerBitmapOom(context)
            "循环加载图片 OOM" -> triggerMultipleBitmapOom(context)
            "字符串拼接 OOM" -> triggerStringConcatOom()
            "集合无限增长 OOM" -> triggerCollectionGrowthOom()
            "创建大量线程 OOM" -> triggerThreadCreationOom()
            "线程池过载 OOM" -> triggerThreadPoolOom()
            "深递归 OOM" -> triggerDeepRecursion()
            else -> false
        }
    }
}

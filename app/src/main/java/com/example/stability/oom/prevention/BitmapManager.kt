package com.example.stability.oom.prevention

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import com.example.stability.oom.utils.OomLog

/**
 * Bitmap 内存管理器
 * 提供 Bitmap 加载、回收等内存优化功能
 */
object BitmapManager {
    
    /**
     * 计算 Bitmap 占用的内存大小
     * @param bitmap 目标 Bitmap
     * @return 内存大小（字节）
     */
    fun calculateBitmapMemory(bitmap: Bitmap): Long {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
            bitmap.allocationByteCount.toLong()
        } else {
            bitmap.byteCount.toLong()
        }
    }
    
    /**
     * 安全回收 Bitmap
     * @param bitmap 需要回收的 Bitmap，可为 null
     */
    fun recycleBitmap(bitmap: Bitmap?) {
        bitmap?.let {
            if (!it.isRecycled) {
                it.recycle()
                OomLog.d("BitmapManager", "Bitmap recycled")
            }
        }
    }
    
    /**
     * 批量回收 Bitmap
     * @param bitmaps 需要回收的 Bitmap 数组
     */
    fun recycleBitmaps(vararg bitmaps: Bitmap?) {
        bitmaps.forEach { recycleBitmap(it) }
    }
    
    /**
     * 从资源文件加载采样后的 Bitmap
     * @param resources Resources 对象
     * @param resId 资源 ID
     * @param reqWidth 目标宽度
     * @param reqHeight 目标高度
     * @return 采样后的 Bitmap
     */
    fun decodeSampledBitmapFromResource(
        resources: Resources,
        resId: Int,
        reqWidth: Int,
        reqHeight: Int
    ): Bitmap {
        // 第一步：只获取图片尺寸，不加载到内存
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        BitmapFactory.decodeResource(resources, resId, options)
        
        // 第二步：计算采样率
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)
        
        // 第三步：加载图片
        options.inJustDecodeBounds = false
        options.inPreferredConfig = Bitmap.Config.RGB_565  // 使用 RGB_565 减少内存占用
        options.inDither = true
        
        return BitmapFactory.decodeResource(resources, resId, options)
    }
    
    /**
     * 从文件路径加载采样后的 Bitmap
     * @param path 文件路径
     * @param reqWidth 目标宽度
     * @param reqHeight 目标高度
     * @return 采样后的 Bitmap
     */
    fun decodeSampledBitmapFromFile(
        path: String,
        reqWidth: Int,
        reqHeight: Int
    ): Bitmap? {
        return try {
            // 第一步：只获取图片尺寸
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeFile(path, options)
            
            // 第二步：计算采样率
            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)
            
            // 第三步：加载图片
            options.inJustDecodeBounds = false
            options.inPreferredConfig = Bitmap.Config.RGB_565
            options.inDither = true
            
            BitmapFactory.decodeFile(path, options)
        } catch (e: Exception) {
            OomLog.e("BitmapManager", "Failed to decode bitmap from file: $path", e)
            null
        }
    }
    
    /**
     * 计算采样率
     * @param options BitmapFactory.Options 对象（已设置 inJustDecodeBounds = true）
     * @param reqWidth 目标宽度
     * @param reqHeight 目标高度
     * @return 采样率（2 的幂次方）
     */
    fun calculateInSampleSize(
        options: BitmapFactory.Options,
        reqWidth: Int,
        reqHeight: Int
    ): Int {
        val (height: Int, width: Int) = options.run { outHeight to outWidth }
        var inSampleSize = 1
        
        // 如果原图尺寸大于目标尺寸，计算采样率
        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2
            
            // 找到最大的采样率，同时保证采样后的尺寸不小于目标尺寸
            while (halfHeight / inSampleSize >= reqHeight && 
                   halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        
        OomLog.d("BitmapManager", "Calculated inSampleSize: $inSampleSize")
        return inSampleSize
    }
    
    /**
     * 创建空白 Bitmap
     * @param width 宽度
     * @param height 高度
     * @param config 像素格式，默认为 RGB_565
     * @return 创建的 Bitmap
     */
    fun createBitmap(width: Int, height: Int, config: Bitmap.Config = Bitmap.Config.RGB_565): Bitmap {
        return Bitmap.createBitmap(width, height, config)
    }
    
    /**
     * 获取 Bitmap 占用内存的格式化字符串
     * @param bitmap 目标 Bitmap
     * @return 格式化的内存大小字符串
     */
    fun getBitmapMemoryInfo(bitmap: Bitmap): String {
        val memory = calculateBitmapMemory(bitmap)
        return buildString {
            append("Bitmap Info:\n")
            append("  Width: ${bitmap.width}\n")
            append("  Height: ${bitmap.height}\n")
            append("  Config: ${bitmap.config}\n")
            append("  Memory: ${formatSize(memory)}\n")
            append("  Is Recycled: ${bitmap.isRecycled}")
        }
    }
    
    /**
     * 格式化内存大小
     */
    private fun formatSize(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> String.format("%.2f KB", bytes / 1024.0)
            bytes < 1024 * 1024 * 1024 -> String.format("%.2f MB", bytes / (1024.0 * 1024))
            else -> String.format("%.2f GB", bytes / (1024.0 * 1024 * 1024))
        }
    }
}

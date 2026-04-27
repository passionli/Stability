package com.example.stability.opengl

import android.content.Context
import android.util.Log

/**
 * OpenGL 学习主类，用于管理和启动不同级别的 OpenGL 示例
 */
class OpenGLMain(private val context: Context) {
    
    /**
     * 运行所有 OpenGL 示例
     */
    fun runAllExamples() {
        Log.d("OpenGL", "=== OpenGLMain.runAllExamples called ===")
        Log.d("OpenGL", "Thread ID: ${Thread.currentThread().id}")
        
        // 运行初级 OpenGL 示例
        runBasicExample()
        
        // 运行中级 OpenGL 示例
        runIntermediateExample()
        
        // 运行高级 OpenGL 示例
        runAdvancedExample()
        
        Log.d("OpenGL", "=== OpenGLMain.runAllExamples completed ===")
    }
    
    /**
     * 运行初级 OpenGL 示例
     */
    private fun runBasicExample() {
        Log.d("OpenGL", "=== 运行初级 OpenGL 示例 ===")
        Log.d("OpenGL", "初级示例：绘制一个简单的三角形")
        Log.d("OpenGL", "使用基础的 GLSurfaceView 和 Renderer")
        Log.d("OpenGL", "=== 初级 OpenGL 示例运行完成 ===")
    }
    
    /**
     * 运行中级 OpenGL 示例
     */
    private fun runIntermediateExample() {
        Log.d("OpenGL", "=== 运行中级 OpenGL 示例 ===")
        Log.d("OpenGL", "中级示例：绘制彩色四边形并实现旋转效果")
        Log.d("OpenGL", "使用矩阵变换和彩色顶点")
        Log.d("OpenGL", "=== 中级 OpenGL 示例运行完成 ===")
    }
    
    /**
     * 运行高级 OpenGL 示例
     */
    private fun runAdvancedExample() {
        Log.d("OpenGL", "=== 运行高级 OpenGL 示例 ===")
        Log.d("OpenGL", "高级示例：绘制带有纹理映射的立方体和实现光照效果")
        Log.d("OpenGL", "使用纹理映射、光照和深度测试")
        Log.d("OpenGL", "=== 高级 OpenGL 示例运行完成 ===")
    }
}
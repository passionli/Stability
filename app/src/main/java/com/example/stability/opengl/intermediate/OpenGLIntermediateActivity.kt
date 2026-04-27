package com.example.stability.opengl.intermediate

import android.opengl.GLSurfaceView
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity

/**
 * OpenGL 中级示例 Activity
 * 用于显示带有彩色四边形和矩阵变换的场景
 */
class OpenGLIntermediateActivity : AppCompatActivity() {
    
    private lateinit var glSurfaceView: GLSurfaceView
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        Log.d("OpenGL", "=== OpenGLIntermediateActivity.onCreate called ===")
        Log.d("OpenGL", "Thread ID: ${Thread.currentThread().id}")
        
        // 创建 GLSurfaceView
        glSurfaceView = GLSurfaceView(this)
        
        // 设置 OpenGL 版本为 2.0
        glSurfaceView.setEGLContextClientVersion(2)
        
        // 设置渲染器
        glSurfaceView.setRenderer(IntermediateRenderer())
        
        // 设置渲染模式为连续渲染
        glSurfaceView.renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY
        
        // 设置内容视图
        setContentView(glSurfaceView)
        
        Log.d("OpenGL", "=== OpenGLIntermediateActivity.onCreate completed ===")
    }
    
    override fun onResume() {
        super.onResume()
        // 恢复 GLSurfaceView 的渲染
        glSurfaceView.onResume()
        Log.d("OpenGL", "OpenGLIntermediateActivity.onResume called")
    }
    
    override fun onPause() {
        super.onPause()
        // 暂停 GLSurfaceView 的渲染
        glSurfaceView.onPause()
        Log.d("OpenGL", "OpenGLIntermediateActivity.onPause called")
    }
}
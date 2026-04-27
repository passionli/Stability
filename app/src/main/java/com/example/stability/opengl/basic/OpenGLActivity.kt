package com.example.stability.opengl.basic

import android.opengl.GLSurfaceView
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity

/**
 * OpenGL 初级示例 Activity
 * 用于显示一个简单的 OpenGL 场景
 */
class OpenGLActivity : AppCompatActivity() {
    
    private lateinit var glSurfaceView: GLSurfaceView
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        Log.d("OpenGL", "=== OpenGLActivity.onCreate called ===")
        Log.d("OpenGL", "Thread ID: ${Thread.currentThread().id}")
        
        // 创建 GLSurfaceView
        glSurfaceView = GLSurfaceView(this)
        
        // 设置 OpenGL 版本为 2.0
        glSurfaceView.setEGLContextClientVersion(2)
        
        // 设置渲染器
        glSurfaceView.setRenderer(BasicRenderer())
        
        // 设置渲染模式为连续渲染
        // RENDERMODE_CONTINUOUSLY: 持续渲染
        // RENDERMODE_WHEN_DIRTY: 仅在需要时渲染
        glSurfaceView.renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY
        
        // 设置内容视图
        setContentView(glSurfaceView)
        
        Log.d("OpenGL", "=== OpenGLActivity.onCreate completed ===")
    }
    
    override fun onResume() {
        super.onResume()
        // 恢复 GLSurfaceView 的渲染
        glSurfaceView.onResume()
        Log.d("OpenGL", "OpenGLActivity.onResume called")
    }
    
    override fun onPause() {
        super.onPause()
        // 暂停 GLSurfaceView 的渲染
        glSurfaceView.onPause()
        Log.d("OpenGL", "OpenGLActivity.onPause called")
    }
}
package com.example.stability.opengl.advanced

import android.opengl.GLSurfaceView
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity

/**
 * OpenGL 高级示例 Activity
 * 用于显示带有纹理映射的立方体和实现光照效果
 */
class OpenGLAdvancedActivity : AppCompatActivity() {
    
    private lateinit var glSurfaceView: GLSurfaceView
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        Log.d("OpenGL", "=== OpenGLAdvancedActivity.onCreate called ===")
        Log.d("OpenGL", "Thread ID: ${Thread.currentThread().id}")
        
        // 创建 GLSurfaceView
        glSurfaceView = GLSurfaceView(this)
        
        // 设置 OpenGL 版本为 2.0
        glSurfaceView.setEGLContextClientVersion(2)
        
        // 设置渲染器
        glSurfaceView.setRenderer(AdvancedRenderer(this))
        
        // 设置渲染模式为连续渲染
        glSurfaceView.renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY
        
        // 设置内容视图
        setContentView(glSurfaceView)
        
        Log.d("OpenGL", "=== OpenGLAdvancedActivity.onCreate completed ===")
    }
    
    override fun onResume() {
        super.onResume()
        // 恢复 GLSurfaceView 的渲染
        glSurfaceView.onResume()
        Log.d("OpenGL", "OpenGLAdvancedActivity.onResume called")
    }
    
    override fun onPause() {
        super.onPause()
        // 暂停 GLSurfaceView 的渲染
        glSurfaceView.onPause()
        Log.d("OpenGL", "OpenGLAdvancedActivity.onPause called")
    }
}
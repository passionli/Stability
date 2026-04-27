package com.example.stability.opengl.intermediate

import android.opengl.GLSurfaceView
import android.os.Bundle
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import androidx.appcompat.app.AppCompatActivity

/**
 * OpenGL 变换组合示例 Activity
 * 用于显示带有平移、旋转、缩放组合变换的场景
 * 支持双击屏幕放大和缩小功能
 */
class OpenGLTransformActivity : AppCompatActivity() {
    
    private lateinit var glSurfaceView: GLSurfaceView
    private lateinit var renderer: TransformRenderer
    private lateinit var gestureDetector: GestureDetector
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        Log.d("OpenGL", "=== OpenGLTransformActivity.onCreate called ===")
        Log.d("OpenGL", "Thread ID: ${Thread.currentThread().id}")
        
        // 创建 GLSurfaceView
        glSurfaceView = GLSurfaceView(this)
        
        // 设置 OpenGL 版本为 2.0
        glSurfaceView.setEGLContextClientVersion(2)
        
        // 创建并设置渲染器
        renderer = TransformRenderer()
        glSurfaceView.setRenderer(renderer)
        
        // 设置渲染模式为连续渲染
        glSurfaceView.renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY
        
        // 创建手势检测器，启用双击检测
        gestureDetector = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
            override fun onSingleTapUp(e: MotionEvent): Boolean {
                Log.d("OpenGL", "onSingleTapUp detected")
                return super.onSingleTapUp(e)
            }
            
            override fun onDoubleTap(e: MotionEvent): Boolean {
                // 处理双击事件
                Log.d("OpenGL", "onDoubleTap detected - zoom in/out")
                // 第一次双击放大，第二次双击缩小，交替进行
                if (renderer.scaleX < 2.0f) {
                    Log.d("OpenGL", "Double tap detected - zoom in")
                    renderer.zoomIn()
                } else {
                    Log.d("OpenGL", "Double tap detected - zoom out")
                    renderer.zoomOut()
                }
                return true
            }
            
            override fun onDoubleTapEvent(e: MotionEvent): Boolean {
                Log.d("OpenGL", "onDoubleTapEvent detected")
                return super.onDoubleTapEvent(e)
            }
        })
        
        // 确保手势检测器能够检测双击事件
        gestureDetector.setIsLongpressEnabled(false)
        
        // 设置触摸监听器
        glSurfaceView.setOnTouchListener { v, event ->
            Log.d("OpenGL", "onTouchEvent: action=${event.action}, x=${event.x}, y=${event.y}")
            // 确保所有触摸事件都传递给手势检测器
            val handled = gestureDetector.onTouchEvent(event)
            Log.d("OpenGL", "GestureDetector handled: $handled")
            // 返回 true 表示事件已处理
            return@setOnTouchListener true
        }
        
        // 设置内容视图
        setContentView(glSurfaceView)
        
        Log.d("OpenGL", "=== OpenGLTransformActivity.onCreate completed ===")
    }
    
    override fun onResume() {
        super.onResume()
        // 恢复 GLSurfaceView 的渲染
        glSurfaceView.onResume()
        Log.d("OpenGL", "OpenGLTransformActivity.onResume called")
    }
    
    override fun onPause() {
        super.onPause()
        // 暂停 GLSurfaceView 的渲染
        glSurfaceView.onPause()
        Log.d("OpenGL", "OpenGLTransformActivity.onPause called")
    }
}

package com.example.stability.opengl.basic

import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.util.Log
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * 基础 OpenGL 渲染器
 * 用于渲染一个简单的三角形
 */
class BasicRenderer : GLSurfaceView.Renderer {
    
    // 顶点着色器代码
    private val vertexShaderCode = """
        attribute vec4 vPosition; // 顶点位置属性
        void main() {
            gl_Position = vPosition; // 设置顶点位置
        }
    """
    
    // 片段着色器代码
    private val fragmentShaderCode = """
        precision mediump float; // 精度限定符
        void main() {
            gl_FragColor = vec4(1.0, 0.0, 0.0, 1.0); // 设置片段颜色为红色
        }
    """
    
    // 三角形顶点数据
    private val triangleVertices = floatArrayOf(
        0.0f, 0.5f, 0.0f,   // 顶部顶点
        -0.5f, -0.5f, 0.0f,  // 左下角顶点
        0.5f, -0.5f, 0.0f    // 右下角顶点
    )
    
    // 程序 ID
    private var programId: Int = 0
    // 顶点位置属性 ID
    private var positionHandle: Int = 0
    // 顶点缓冲区 ID
    private var vertexBufferId: Int = 0
    
    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        Log.d("OpenGL", "=== BasicRenderer.onSurfaceCreated called ===")
        Log.d("OpenGL", "Thread ID: ${Thread.currentThread().id}")
        
        // 设置背景颜色为黑色
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)
        
        // 创建顶点着色器
        val vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode)
        // 创建片段着色器
        val fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode)
        
        // 创建 OpenGL 程序
        programId = GLES20.glCreateProgram()
        // 向程序中添加着色器
        GLES20.glAttachShader(programId, vertexShader)
        GLES20.glAttachShader(programId, fragmentShader)
        // 链接程序
        GLES20.glLinkProgram(programId)
        
        // 检查链接状态
        val linkStatus = IntArray(1)
        GLES20.glGetProgramiv(programId, GLES20.GL_LINK_STATUS, linkStatus, 0)
        if (linkStatus[0] != GLES20.GL_TRUE) {
            Log.e("OpenGL", "无法链接程序: " + GLES20.glGetProgramInfoLog(programId))
            GLES20.glDeleteProgram(programId)
            return
        }
        
        // 获取顶点位置属性 ID
        positionHandle = GLES20.glGetAttribLocation(programId, "vPosition")
        
        // 创建顶点缓冲区
        val buffers = IntArray(1)
        GLES20.glGenBuffers(1, buffers, 0)
        vertexBufferId = buffers[0]
        
        // 绑定顶点缓冲区
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vertexBufferId)
        
        // 将顶点数据复制到缓冲区
        val vertexBuffer = java.nio.FloatBuffer.allocate(triangleVertices.size)
        vertexBuffer.put(triangleVertices)
        vertexBuffer.position(0)
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, triangleVertices.size * 4, vertexBuffer, GLES20.GL_STATIC_DRAW)
        
        // 解绑缓冲区
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0)
        
        Log.d("OpenGL", "=== BasicRenderer.onSurfaceCreated completed ===")
    }
    
    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        Log.d("OpenGL", "=== BasicRenderer.onSurfaceChanged called ===")
        Log.d("OpenGL", "Width: $width, Height: $height")
        
        // 设置视口大小
        GLES20.glViewport(0, 0, width, height)
        
        Log.d("OpenGL", "=== BasicRenderer.onSurfaceChanged completed ===")
    }
    
    override fun onDrawFrame(gl: GL10?) {
        // 清除颜色缓冲区
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        
        // 使用程序
        GLES20.glUseProgram(programId)
        
        // 启用顶点位置属性
        GLES20.glEnableVertexAttribArray(positionHandle)
        
        // 绑定顶点缓冲区
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vertexBufferId)
        
        // 设置顶点位置属性
        GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false, 0, 0)
        
        // 绘制三角形
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 3)
        
        // 禁用顶点位置属性
        GLES20.glDisableVertexAttribArray(positionHandle)
        
        // 解绑缓冲区
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0)
    }
    
    /**
     * 加载着色器
     */
    private fun loadShader(type: Int, shaderCode: String): Int {
        // 创建着色器
        val shader = GLES20.glCreateShader(type)
        
        // 设置着色器代码
        GLES20.glShaderSource(shader, shaderCode)
        
        // 编译着色器
        GLES20.glCompileShader(shader)
        
        // 检查编译状态
        val compileStatus = IntArray(1)
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compileStatus, 0)
        if (compileStatus[0] != GLES20.GL_TRUE) {
            Log.e("OpenGL", "无法编译着色器: " + GLES20.glGetShaderInfoLog(shader))
            GLES20.glDeleteShader(shader)
            return 0
        }
        
        return shader
    }
}
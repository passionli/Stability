package com.example.stability.opengl.intermediate

import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.util.Log
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * 中级 OpenGL 渲染器
 * 用于渲染彩色四边形和使用矩阵变换
 */
class IntermediateRenderer : GLSurfaceView.Renderer {
    
    // 顶点着色器代码
    private val vertexShaderCode = """
        attribute vec4 vPosition; // 顶点位置属性
        attribute vec4 vColor; // 顶点颜色属性
        uniform mat4 uMVPMatrix; // 模型视图投影矩阵
        varying vec4 aColor; // 传递给片段着色器的颜色
        void main() {
            gl_Position = uMVPMatrix * vPosition; // 应用矩阵变换
            aColor = vColor; // 传递颜色
        }
    """
    
    // 片段着色器代码
    private val fragmentShaderCode = """
        precision mediump float; // 精度限定符
        varying vec4 aColor; // 从顶点着色器接收的颜色
        void main() {
            gl_FragColor = aColor; // 使用传递过来的颜色
        }
    """
    
    // 四边形顶点数据（包含位置和颜色）
    private val quadVertices = floatArrayOf(
        // 位置                颜色
        -0.5f,  0.5f, 0.0f,   1.0f, 0.0f, 0.0f, 1.0f, // 左上 红色
         0.5f,  0.5f, 0.0f,   0.0f, 1.0f, 0.0f, 1.0f, // 右上 绿色
        -0.5f, -0.5f, 0.0f,   0.0f, 0.0f, 1.0f, 1.0f, // 左下 蓝色
         0.5f, -0.5f, 0.0f,   1.0f, 1.0f, 0.0f, 1.0f  // 右下 黄色
    )
    
    // 绘制顺序
    private val drawOrder = shortArrayOf(0, 1, 2, 1, 2, 3)
    
    // 程序 ID
    private var programId: Int = 0
    // 顶点位置属性 ID
    private var positionHandle: Int = 0
    // 顶点颜色属性 ID
    private var colorHandle: Int = 0
    // 模型视图投影矩阵 uniform ID
    private var mvpMatrixHandle: Int = 0
    // 顶点缓冲区 ID
    private var vertexBufferId: Int = 0
    // 索引缓冲区 ID
    private var indexBufferId: Int = 0
    
    // 模型视图投影矩阵
    private val mvpMatrix = FloatArray(16)
    private val projectionMatrix = FloatArray(16)
    private val viewMatrix = FloatArray(16)
    private val modelMatrix = FloatArray(16)
    
    // 旋转角度
    private var angle = 0f
    
    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        Log.d("OpenGL", "=== IntermediateRenderer.onSurfaceCreated called ===")
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
        
        // 获取属性和 uniform 的 ID
        positionHandle = GLES20.glGetAttribLocation(programId, "vPosition")
        colorHandle = GLES20.glGetAttribLocation(programId, "vColor")
        mvpMatrixHandle = GLES20.glGetUniformLocation(programId, "uMVPMatrix")
        
        // 创建顶点缓冲区
        val vertexBuffers = IntArray(1)
        GLES20.glGenBuffers(1, vertexBuffers, 0)
        vertexBufferId = vertexBuffers[0]
        
        // 绑定顶点缓冲区
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vertexBufferId)
        
        // 将顶点数据复制到缓冲区
        val vertexBuffer = java.nio.FloatBuffer.allocate(quadVertices.size)
        vertexBuffer.put(quadVertices)
        vertexBuffer.position(0)
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, quadVertices.size * 4, vertexBuffer, GLES20.GL_STATIC_DRAW)
        
        // 创建索引缓冲区
        val indexBuffers = IntArray(1)
        GLES20.glGenBuffers(1, indexBuffers, 0)
        indexBufferId = indexBuffers[0]
        
        // 绑定索引缓冲区
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, indexBufferId)
        
        // 将索引数据复制到缓冲区
        val indexBuffer = java.nio.ShortBuffer.allocate(drawOrder.size)
        indexBuffer.put(drawOrder)
        indexBuffer.position(0)
        GLES20.glBufferData(GLES20.GL_ELEMENT_ARRAY_BUFFER, drawOrder.size * 2, indexBuffer, GLES20.GL_STATIC_DRAW)
        
        // 解绑缓冲区
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0)
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0)
        
        Log.d("OpenGL", "=== IntermediateRenderer.onSurfaceCreated completed ===")
    }
    
    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        Log.d("OpenGL", "=== IntermediateRenderer.onSurfaceChanged called ===")
        Log.d("OpenGL", "Width: $width, Height: $height")
        
        // 设置视口大小
        GLES20.glViewport(0, 0, width, height)
        
        // 计算宽高比
        val aspectRatio = width.toFloat() / height.toFloat()
        
        // 设置正交投影矩阵
        Matrix.orthoM(projectionMatrix, 0, -aspectRatio, aspectRatio, -1f, 1f, -1f, 1f)
        
        Log.d("OpenGL", "=== IntermediateRenderer.onSurfaceChanged completed ===")
    }
    
    override fun onDrawFrame(gl: GL10?) {
        // 清除颜色缓冲区
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        
        // 使用程序
        GLES20.glUseProgram(programId)
        
        // 启用顶点位置属性
        GLES20.glEnableVertexAttribArray(positionHandle)
        // 启用顶点颜色属性
        GLES20.glEnableVertexAttribArray(colorHandle)
        
        // 绑定顶点缓冲区
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vertexBufferId)
        
        // 设置顶点位置属性
        GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false, 7 * 4, 0)
        // 设置顶点颜色属性
        GLES20.glVertexAttribPointer(colorHandle, 4, GLES20.GL_FLOAT, false, 7 * 4, 3 * 4)
        
        // 绑定索引缓冲区
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, indexBufferId)
        
        // 更新旋转角度
        angle += 0.5f
        if (angle >= 360f) {
            angle = 0f
        }
        
        // 计算模型视图投影矩阵
        // 重置模型矩阵
        Matrix.setIdentityM(modelMatrix, 0)
        // 应用旋转
        Matrix.rotateM(modelMatrix, 0, angle, 0f, 0f, 1f)
        // 重置视图矩阵
        Matrix.setIdentityM(viewMatrix, 0)
        // 计算模型视图矩阵
        val mvMatrix = FloatArray(16)
        Matrix.multiplyMM(mvMatrix, 0, viewMatrix, 0, modelMatrix, 0)
        // 计算模型视图投影矩阵
        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, mvMatrix, 0)
        
        // 设置模型视图投影矩阵
        GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, mvpMatrix, 0)
        
        // 绘制四边形
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, drawOrder.size, GLES20.GL_UNSIGNED_SHORT, 0)
        
        // 禁用顶点属性
        GLES20.glDisableVertexAttribArray(positionHandle)
        GLES20.glDisableVertexAttribArray(colorHandle)
        
        // 解绑缓冲区
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0)
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0)
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
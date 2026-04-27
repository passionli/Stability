package com.example.stability.opengl.intermediate

import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.util.Log
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * 变换组合渲染器
 * 用于演示平移、旋转、缩放三种基本变换的组合使用
 * 通过动画展示不同变换顺序和参数对渲染结果的影响
 */
class TransformRenderer : GLSurfaceView.Renderer {

    // ============================ 常量定义 ============================

    // 着色器类型常量
    private val VERTEX_SHADER_TYPE = GLES20.GL_VERTEX_SHADER
    private val FRAGMENT_SHADER_TYPE = GLES20.GL_FRAGMENT_SHADER

    // 缓冲区目标常量
    private val ARRAY_BUFFER = GLES20.GL_ARRAY_BUFFER
    private val ELEMENT_ARRAY_BUFFER = GLES20.GL_ELEMENT_ARRAY_BUFFER

    // 缓冲区使用模式
    private val STATIC_DRAW = GLES20.GL_STATIC_DRAW

    // 绘制模式
    private val TRIANGLES = GLES20.GL_TRIANGLES

    // 清除缓冲区
    private val COLOR_BUFFER_BIT = GLES20.GL_COLOR_BUFFER_BIT

    // 数据类型
    private val FLOAT = GLES20.GL_FLOAT
    private val UNSIGNED_SHORT = GLES20.GL_UNSIGNED_SHORT

    // 状态查询
    private val COMPILE_STATUS = GLES20.GL_COMPILE_STATUS
    private val LINK_STATUS = GLES20.GL_LINK_STATUS
    private val TRUE = GLES20.GL_TRUE

    // 日志标签
    private val TAG = "OpenGL"

    // ============================ 着色器代码 ============================

    // 顶点着色器代码
    // 设计原因：使用字符串定义着色器代码，便于修改和维护
    // 技术目的：实现顶点位置变换和颜色传递
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

    // ============================ 顶点数据 ============================

    // 四边形顶点数据（包含位置和颜色）
    // 设计原因：使用数组存储顶点数据，便于一次性传递给 OpenGL
    // 技术目的：定义四边形的几何形状和颜色
    private val quadVertices = floatArrayOf(
        // 位置                颜色
        -0.5f,  0.5f, 0.0f,   1.0f, 0.0f, 0.0f, 1.0f, // 左上 红色
         0.5f,  0.5f, 0.0f,   0.0f, 1.0f, 0.0f, 1.0f, // 右上 绿色
        -0.5f, -0.5f, 0.0f,   0.0f, 0.0f, 1.0f, 1.0f, // 左下 蓝色
         0.5f, -0.5f, 0.0f,   1.0f, 1.0f, 0.0f, 1.0f  // 右下 黄色
    )

    // 绘制顺序（索引）
    private val drawOrder = shortArrayOf(0, 1, 2, 1, 2, 3)

    // ============================ 成员变量 ============================

    // OpenGL 对象 ID
    private var programId: Int = 0
    private var positionHandle: Int = 0
    private var colorHandle: Int = 0
    private var mvpMatrixHandle: Int = 0
    private var vertexBufferId: Int = 0
    private var indexBufferId: Int = 0

    // 矩阵相关变量
    private val mvpMatrix = FloatArray(16)
    private val projectionMatrix = FloatArray(16)
    private val viewMatrix = FloatArray(16)
    private val modelMatrix = FloatArray(16)

    // 变换参数
    private var angle = 0f
    private var translateX = 0f
    private var translateY = 0f
    var scaleX = 1f
    var scaleY = 1f
    
    // 缩放因子增量
    private val SCALE_FACTOR = 1.2f
    // 最小缩放比例
    private val MIN_SCALE = 0.5f
    // 最大缩放比例
    private val MAX_SCALE = 3.0f

    // 动画参数
    private val POSITION_COMPONENT_COUNT = 3
    private val COLOR_COMPONENT_COUNT = 4
    private val VERTEX_STRIDE = (3 + 4) * 4
    private val POSITION_OFFSET = 0
    private val COLOR_OFFSET = 3 * 4
    private val NORMALIZED = false
    private val DRAW_VERTEX_COUNT = drawOrder.size

    // ============================ 生命周期方法 ============================

    /**
     * 当 Surface 创建时调用
     */
    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        Log.d(TAG, "=== TransformRenderer.onSurfaceCreated called ===")

        // 设置背景颜色
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)

        // 创建着色器
        val vertexShader = loadShader(VERTEX_SHADER_TYPE, vertexShaderCode)
        val fragmentShader = loadShader(FRAGMENT_SHADER_TYPE, fragmentShaderCode)

        // 创建程序
        programId = GLES20.glCreateProgram()
        GLES20.glAttachShader(programId, vertexShader)
        GLES20.glAttachShader(programId, fragmentShader)
        GLES20.glLinkProgram(programId)

        // 检查链接状态
        val linkStatus = IntArray(1)
        GLES20.glGetProgramiv(programId, LINK_STATUS, linkStatus, 0)
        if (linkStatus[0] != TRUE) {
            Log.e(TAG, "无法链接程序: " + GLES20.glGetProgramInfoLog(programId))
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

        // 绑定顶点缓冲区并上传数据
        GLES20.glBindBuffer(ARRAY_BUFFER, vertexBufferId)
        val vertexBuffer = java.nio.FloatBuffer.allocate(quadVertices.size)
        vertexBuffer.put(quadVertices)
        vertexBuffer.position(0)
        GLES20.glBufferData(ARRAY_BUFFER, quadVertices.size * 4, vertexBuffer, STATIC_DRAW)

        // 创建索引缓冲区
        val indexBuffers = IntArray(1)
        GLES20.glGenBuffers(1, indexBuffers, 0)
        indexBufferId = indexBuffers[0]

        // 绑定索引缓冲区并上传数据
        GLES20.glBindBuffer(ELEMENT_ARRAY_BUFFER, indexBufferId)
        val indexBuffer = java.nio.ShortBuffer.allocate(drawOrder.size)
        indexBuffer.put(drawOrder)
        indexBuffer.position(0)
        GLES20.glBufferData(ELEMENT_ARRAY_BUFFER, drawOrder.size * 2, indexBuffer, STATIC_DRAW)

        // 解绑缓冲区
        GLES20.glBindBuffer(ARRAY_BUFFER, 0)
        GLES20.glBindBuffer(ELEMENT_ARRAY_BUFFER, 0)

        Log.d(TAG, "=== TransformRenderer.onSurfaceCreated completed ===")
    }

    /**
     * 当 Surface 尺寸发生变化时调用
     */
    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        Log.d(TAG, "=== TransformRenderer.onSurfaceChanged called ===")
        Log.d(TAG, "Width: $width, Height: $height")

        // 设置视口大小
        GLES20.glViewport(0, 0, width, height)

        // 计算宽高比
        val aspectRatio = width.toFloat() / height.toFloat()

        // 设置正交投影矩阵
        Matrix.orthoM(projectionMatrix, 0, -aspectRatio, aspectRatio, -1f, 1f, -1f, 1f)

        // 设置视图矩阵（单位矩阵）
        Matrix.setIdentityM(viewMatrix, 0)

        Log.d(TAG, "=== TransformRenderer.onSurfaceChanged completed ===")
    }

    /**
     * 绘制每一帧时调用
     */
    override fun onDrawFrame(gl: GL10?) {
        // 清除颜色缓冲区
        GLES20.glClear(COLOR_BUFFER_BIT)

        // 使用程序
        GLES20.glUseProgram(programId)

        // 启用顶点属性
        GLES20.glEnableVertexAttribArray(positionHandle)
        GLES20.glEnableVertexAttribArray(colorHandle)

        // 绑定顶点缓冲区
        GLES20.glBindBuffer(ARRAY_BUFFER, vertexBufferId)

        // 设置顶点属性
        GLES20.glVertexAttribPointer(positionHandle, POSITION_COMPONENT_COUNT, FLOAT, NORMALIZED, VERTEX_STRIDE, POSITION_OFFSET)
        GLES20.glVertexAttribPointer(colorHandle, COLOR_COMPONENT_COUNT, FLOAT, NORMALIZED, VERTEX_STRIDE, COLOR_OFFSET)

        // 绑定索引缓冲区
        GLES20.glBindBuffer(ELEMENT_ARRAY_BUFFER, indexBufferId)

        // 更新变换参数
        updateTransformParameters()

        // ============================ 矩阵变换计算 ============================
        
        // 1. 重置模型矩阵
        Matrix.setIdentityM(modelMatrix, 0)
        Log.d(TAG, "[Matrix.setIdentityM] modelMatrix reset")

        // 2. 应用平移变换
        // 设计原因：将物体移动到指定位置
        // 技术目的：实现物体的位置变化
        // 参数含义：
        // - x: X 轴平移距离（正数向右，负数向左）
        // - y: Y 轴平移距离（正数向上，负数向下）
        // - z: Z 轴平移距离（正数向屏幕外，负数向屏幕内）
        Log.d(TAG, "[Matrix.translateM] x=$translateX, y=$translateY, z=0.0f")
        Matrix.translateM(modelMatrix, 0, translateX, translateY, 0.0f)

        // 3. 应用旋转变换
        // 设计原因：将物体绕指定轴旋转
        // 技术目的：实现物体的旋转动画
        // 参数含义：
        // - angle: 旋转角度（度数）
        // - x, y, z: 旋转轴的方向向量
        // 注意：旋转是在平移之后进行的，所以是绕平移后的位置旋转
        val rotationX = 0f
        val rotationY = 0f
        val rotationZ = 1f
        Log.d(TAG, "[Matrix.rotateM] angle=$angle, x=$rotationX, y=$rotationY, z=$rotationZ")
        Matrix.rotateM(modelMatrix, 0, angle, rotationX, rotationY, rotationZ)

        // 4. 应用缩放变换
        // 设计原因：改变物体的大小
        // 技术目的：实现物体的缩放效果
        // 参数含义：
        // - x: X 轴缩放因子（1.0 为原始大小，>1.0 放大，<1.0 缩小）
        // - y: Y 轴缩放因子
        // - z: Z 轴缩放因子
        // 注意：缩放是在旋转之后进行的，所以是旋转后的物体被缩放
        Log.d(TAG, "[Matrix.scaleM] x=$scaleX, y=$scaleY, z=1.0f")
        Matrix.scaleM(modelMatrix, 0, scaleX, scaleY, 1.0f)

        // 5. 计算模型视图矩阵
        val mvMatrix = FloatArray(16)
        Matrix.multiplyMM(mvMatrix, 0, viewMatrix, 0, modelMatrix, 0)

        // 6. 计算模型视图投影矩阵
        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, mvMatrix, 0)

        // 7. 设置矩阵到着色器
        GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, mvpMatrix, 0)

        // ===================================================================

        // 绘制四边形
        GLES20.glDrawElements(TRIANGLES, DRAW_VERTEX_COUNT, UNSIGNED_SHORT, 0)

        // 禁用顶点属性
        GLES20.glDisableVertexAttribArray(positionHandle)
        GLES20.glDisableVertexAttribArray(colorHandle)

        // 解绑缓冲区
        GLES20.glBindBuffer(ARRAY_BUFFER, 0)
        GLES20.glBindBuffer(ELEMENT_ARRAY_BUFFER, 0)
    }

    /**
     * 更新变换参数
     * 设计原因：通过动画更新平移、旋转、缩放参数
     * 技术目的：实现动态变换效果
     */
    private fun updateTransformParameters() {
        // 更新旋转角度（持续旋转）
        angle += 1.0f
        if (angle >= 360f) {
            angle = 0f
        }

        // 更新平移位置（左右摆动）
        // 使用正弦函数创建平滑的左右移动效果
        translateX = kotlin.math.sin(angle * Math.PI / 180f).toFloat() * 0.5f
        translateY = kotlin.math.cos(angle * Math.PI / 180f).toFloat() * 0.3f

        // 更新缩放因子（周期性缩放）
        // 使用正弦函数创建平滑的缩放效果
        val scaleFactor = ((kotlin.math.sin(angle * Math.PI / 180f * 2) + 1.5f).toFloat() / 2f)
//        scaleX = scaleFactor
//        scaleY = scaleFactor

        Log.d(TAG, "[updateTransformParameters] angle=$angle, translateX=$translateX, translateY=$translateY, scaleX=$scaleX, scaleY=$scaleY")
    }

    // ============================ 辅助方法 ============================

    /**
     * 放大物体
     * 设计原因：实现双击屏幕放大功能
     * 技术目的：增加物体的缩放比例
     */
    fun zoomIn() {
        // 计算新的缩放比例
        val newScale = scaleX * SCALE_FACTOR
        // 确保缩放比例不超过最大值
        if (newScale <= MAX_SCALE) {
            scaleX = newScale
            scaleY = newScale
            Log.d(TAG, "[zoomIn] scaleX=$scaleX, scaleY=$scaleY")
        }
    }

    /**
     * 缩小物体
     * 设计原因：实现双击屏幕缩小功能
     * 技术目的：减小物体的缩放比例
     */
    fun zoomOut() {
        // 计算新的缩放比例
        val newScale = scaleX / SCALE_FACTOR
        // 确保缩放比例不小于最小值
        if (newScale >= MIN_SCALE) {
            scaleX = newScale
            scaleY = newScale
            Log.d(TAG, "[zoomOut] scaleX=$scaleX, scaleY=$scaleY")
        }
    }

    /**
     * 加载着色器
     */
    private fun loadShader(type: Int, shaderCode: String): Int {
        val shader = GLES20.glCreateShader(type)
        GLES20.glShaderSource(shader, shaderCode)
        GLES20.glCompileShader(shader)

        val compileStatus = IntArray(1)
        GLES20.glGetShaderiv(shader, COMPILE_STATUS, compileStatus, 0)
        if (compileStatus[0] != TRUE) {
            Log.e(TAG, "无法编译着色器: " + GLES20.glGetShaderInfoLog(shader))
            GLES20.glDeleteShader(shader)
            return 0
        }

        return shader
    }
}

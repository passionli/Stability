package com.example.stability.opengl.advanced

import android.content.Context
import android.graphics.BitmapFactory
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.opengl.GLUtils
import android.util.Log
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * 高级 OpenGL 渲染器
 * 用于渲染带有纹理映射的立方体和实现光照效果
 */
class AdvancedRenderer(private val context: Context) : GLSurfaceView.Renderer {
    
    // 顶点着色器代码
    private val vertexShaderCode = """
        attribute vec4 vPosition; // 顶点位置属性
        attribute vec3 vNormal; // 顶点法线属性
        attribute vec2 vTexCoord; // 顶点纹理坐标属性
        uniform mat4 uMVPMatrix; // 模型视图投影矩阵
        uniform mat4 uMVMatrix; // 模型视图矩阵
        uniform mat3 uNormalMatrix; // 法线矩阵
        uniform vec3 uLightDirection; // 光源方向
        uniform vec4 uLightColor; // 光源颜色
        uniform vec4 uAmbientColor; // 环境光颜色
        varying vec2 aTexCoord; // 传递给片段着色器的纹理坐标
        varying vec4 aColor; // 传递给片段着色器的颜色
        void main() {
            // 应用矩阵变换
            gl_Position = uMVPMatrix * vPosition;
            
            // 计算变换后的法线
            vec3 transformedNormal = uNormalMatrix * vNormal;
            
            // 计算光照
            float dotProduct = max(dot(transformedNormal, normalize(uLightDirection)), 0.0);
            vec4 lightFactor = uAmbientColor + uLightColor * dotProduct;
            
            // 传递纹理坐标和颜色
            aTexCoord = vTexCoord;
            aColor = lightFactor;
        }
    """
    
    // 片段着色器代码
    private val fragmentShaderCode = """
        precision mediump float; // 精度限定符
        varying vec2 aTexCoord; // 从顶点着色器接收的纹理坐标
        varying vec4 aColor; // 从顶点着色器接收的颜色
        uniform sampler2D uTexture; // 纹理采样器
        void main() {
            // 采样纹理并应用光照
            vec4 textureColor = texture2D(uTexture, aTexCoord);
            gl_FragColor = textureColor * aColor;
        }
    """
    
    // 立方体顶点数据（包含位置、法线和纹理坐标）
    private val cubeVertices = floatArrayOf(
        // 前面
        -0.5f, -0.5f,  0.5f,  0.0f, 0.0f, 1.0f, 0.0f, 0.0f, // 左下
         0.5f, -0.5f,  0.5f,  0.0f, 0.0f, 1.0f, 1.0f, 0.0f, // 右下
         0.5f,  0.5f,  0.5f,  0.0f, 0.0f, 1.0f, 1.0f, 1.0f, // 右上
        -0.5f,  0.5f,  0.5f,  0.0f, 0.0f, 1.0f, 0.0f, 1.0f, // 左上
        // 后面
        -0.5f, -0.5f, -0.5f,  0.0f, 0.0f, -1.0f, 1.0f, 0.0f, // 左下
         0.5f, -0.5f, -0.5f,  0.0f, 0.0f, -1.0f, 0.0f, 0.0f, // 右下
         0.5f,  0.5f, -0.5f,  0.0f, 0.0f, -1.0f, 0.0f, 1.0f, // 右上
        -0.5f,  0.5f, -0.5f,  0.0f, 0.0f, -1.0f, 1.0f, 1.0f, // 左上
        // 左面
        -0.5f, -0.5f, -0.5f, -1.0f, 0.0f, 0.0f, 0.0f, 0.0f, // 左下
        -0.5f, -0.5f,  0.5f, -1.0f, 0.0f, 0.0f, 1.0f, 0.0f, // 右下
        -0.5f,  0.5f,  0.5f, -1.0f, 0.0f, 0.0f, 1.0f, 1.0f, // 右上
        -0.5f,  0.5f, -0.5f, -1.0f, 0.0f, 0.0f, 0.0f, 1.0f, // 左上
        // 右面
         0.5f, -0.5f, -0.5f,  1.0f, 0.0f, 0.0f, 1.0f, 0.0f, // 左下
         0.5f, -0.5f,  0.5f,  1.0f, 0.0f, 0.0f, 0.0f, 0.0f, // 右下
         0.5f,  0.5f,  0.5f,  1.0f, 0.0f, 0.0f, 0.0f, 1.0f, // 右上
         0.5f,  0.5f, -0.5f,  1.0f, 0.0f, 0.0f, 1.0f, 1.0f, // 左上
        // 下面
        -0.5f, -0.5f, -0.5f,  0.0f, -1.0f, 0.0f, 0.0f, 1.0f, // 左下
         0.5f, -0.5f, -0.5f,  0.0f, -1.0f, 0.0f, 1.0f, 1.0f, // 右下
         0.5f, -0.5f,  0.5f,  0.0f, -1.0f, 0.0f, 1.0f, 0.0f, // 右上
        -0.5f, -0.5f,  0.5f,  0.0f, -1.0f, 0.0f, 0.0f, 0.0f, // 左上
        // 上面
        -0.5f,  0.5f, -0.5f,  0.0f, 1.0f, 0.0f, 0.0f, 0.0f, // 左下
         0.5f,  0.5f, -0.5f,  0.0f, 1.0f, 0.0f, 1.0f, 0.0f, // 右下
         0.5f,  0.5f,  0.5f,  0.0f, 1.0f, 0.0f, 1.0f, 1.0f, // 右上
        -0.5f,  0.5f,  0.5f,  0.0f, 1.0f, 0.0f, 0.0f, 1.0f  // 左上
    )
    
    // 绘制顺序
    private val drawOrder = shortArrayOf(
        0, 1, 2, 0, 2, 3,    // 前面
        4, 5, 6, 4, 6, 7,    // 后面
        8, 9, 10, 8, 10, 11,  // 左面
        12, 13, 14, 12, 14, 15, // 右面
        16, 17, 18, 16, 18, 19, // 下面
        20, 21, 22, 20, 22, 23  // 上面
    )
    
    // 程序 ID
    private var programId: Int = 0
    // 顶点位置属性 ID
    private var positionHandle: Int = 0
    // 顶点法线属性 ID
    private var normalHandle: Int = 0
    // 顶点纹理坐标属性 ID
    private var texCoordHandle: Int = 0
    // 模型视图投影矩阵 uniform ID
    private var mvpMatrixHandle: Int = 0
    // 模型视图矩阵 uniform ID
    private var mvMatrixHandle: Int = 0
    // 法线矩阵 uniform ID
    private var normalMatrixHandle: Int = 0
    // 光源方向 uniform ID
    private var lightDirectionHandle: Int = 0
    // 光源颜色 uniform ID
    private var lightColorHandle: Int = 0
    // 环境光颜色 uniform ID
    private var ambientColorHandle: Int = 0
    // 纹理采样器 uniform ID
    private var textureHandle: Int = 0
    // 顶点缓冲区 ID
    private var vertexBufferId: Int = 0
    // 索引缓冲区 ID
    private var indexBufferId: Int = 0
    // 纹理 ID
    private var textureId: Int = 0
    
    // 模型视图投影矩阵
    private val mvpMatrix = FloatArray(16)
    private val projectionMatrix = FloatArray(16)
    private val viewMatrix = FloatArray(16)
    private val modelMatrix = FloatArray(16)
    private val normalMatrix = FloatArray(9)
    
    // 旋转角度
    private var angleX = 0f
    private var angleY = 0f
    
    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        Log.d("OpenGL", "=== AdvancedRenderer.onSurfaceCreated called ===")
        Log.d("OpenGL", "Thread ID: ${Thread.currentThread().id}")
        
        // 设置背景颜色为黑色
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)
        
        // 启用深度测试
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)
        
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
        normalHandle = GLES20.glGetAttribLocation(programId, "vNormal")
        texCoordHandle = GLES20.glGetAttribLocation(programId, "vTexCoord")
        mvpMatrixHandle = GLES20.glGetUniformLocation(programId, "uMVPMatrix")
        mvMatrixHandle = GLES20.glGetUniformLocation(programId, "uMVMatrix")
        normalMatrixHandle = GLES20.glGetUniformLocation(programId, "uNormalMatrix")
        lightDirectionHandle = GLES20.glGetUniformLocation(programId, "uLightDirection")
        lightColorHandle = GLES20.glGetUniformLocation(programId, "uLightColor")
        ambientColorHandle = GLES20.glGetUniformLocation(programId, "uAmbientColor")
        textureHandle = GLES20.glGetUniformLocation(programId, "uTexture")
        
        // 创建顶点缓冲区
        val vertexBuffers = IntArray(1)
        GLES20.glGenBuffers(1, vertexBuffers, 0)
        vertexBufferId = vertexBuffers[0]
        
        // 绑定顶点缓冲区
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vertexBufferId)
        
        // 将顶点数据复制到缓冲区
        val vertexBuffer = java.nio.FloatBuffer.allocate(cubeVertices.size)
        vertexBuffer.put(cubeVertices)
        vertexBuffer.position(0)
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, cubeVertices.size * 4, vertexBuffer, GLES20.GL_STATIC_DRAW)
        
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
        
        // 加载纹理
        textureId = loadTexture()
        
        // 解绑缓冲区
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0)
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0)
        
        Log.d("OpenGL", "=== AdvancedRenderer.onSurfaceCreated completed ===")
    }
    
    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        Log.d("OpenGL", "=== AdvancedRenderer.onSurfaceChanged called ===")
        Log.d("OpenGL", "Width: $width, Height: $height")
        
        // 设置视口大小
        GLES20.glViewport(0, 0, width, height)
        
        // 计算宽高比
        val aspectRatio = width.toFloat() / height.toFloat()
        
        // 设置透视投影矩阵
        Matrix.frustumM(projectionMatrix, 0, -aspectRatio, aspectRatio, -1f, 1f, 3f, 7f)
        
        // 设置相机位置
        Matrix.setLookAtM(viewMatrix, 0, 0f, 0f, 5f, 0f, 0f, 0f, 0f, 1f, 0f)
        
        Log.d("OpenGL", "=== AdvancedRenderer.onSurfaceChanged completed ===")
    }
    
    override fun onDrawFrame(gl: GL10?) {
        // 清除颜色和深度缓冲区
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
        
        // 使用程序
        GLES20.glUseProgram(programId)
        
        // 启用顶点属性
        GLES20.glEnableVertexAttribArray(positionHandle)
        GLES20.glEnableVertexAttribArray(normalHandle)
        GLES20.glEnableVertexAttribArray(texCoordHandle)
        
        // 绑定顶点缓冲区
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vertexBufferId)
        
        // 设置顶点位置属性
        GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false, 8 * 4, 0)
        // 设置顶点法线属性
        GLES20.glVertexAttribPointer(normalHandle, 3, GLES20.GL_FLOAT, false, 8 * 4, 3 * 4)
        // 设置顶点纹理坐标属性
        GLES20.glVertexAttribPointer(texCoordHandle, 2, GLES20.GL_FLOAT, false, 8 * 4, 6 * 4)
        
        // 绑定索引缓冲区
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, indexBufferId)
        
        // 绑定纹理
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId)
        GLES20.glUniform1i(textureHandle, 0)
        
        // 更新旋转角度
        angleX += 0.5f
        angleY += 0.3f
        if (angleX >= 360f) angleX = 0f
        if (angleY >= 360f) angleY = 0f
        
        // 计算模型视图投影矩阵
        // 重置模型矩阵
        Matrix.setIdentityM(modelMatrix, 0)
        // 应用旋转
        Matrix.rotateM(modelMatrix, 0, angleX, 1f, 0f, 0f)
        Matrix.rotateM(modelMatrix, 0, angleY, 0f, 1f, 0f)
        // 计算模型视图矩阵
        val mvMatrix = FloatArray(16)
        Matrix.multiplyMM(mvMatrix, 0, viewMatrix, 0, modelMatrix, 0)
        // 计算模型视图投影矩阵
        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, mvMatrix, 0)
        // 计算法线矩阵
        Matrix.invertM(normalMatrix, 0, mvMatrix, 0)
        Matrix.transposeM(normalMatrix, 0, normalMatrix, 0)
        
        // 设置矩阵
        GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, mvpMatrix, 0)
        GLES20.glUniformMatrix4fv(mvMatrixHandle, 1, false, mvMatrix, 0)
        GLES20.glUniformMatrix3fv(normalMatrixHandle, 1, false, normalMatrix, 0)
        
        // 设置光照参数
        GLES20.glUniform3f(lightDirectionHandle, 0f, 0f, -1f)
        GLES20.glUniform4f(lightColorHandle, 1.0f, 1.0f, 1.0f, 1.0f)
        GLES20.glUniform4f(ambientColorHandle, 0.2f, 0.2f, 0.2f, 1.0f)
        
        // 绘制立方体
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, drawOrder.size, GLES20.GL_UNSIGNED_SHORT, 0)
        
        // 禁用顶点属性
        GLES20.glDisableVertexAttribArray(positionHandle)
        GLES20.glDisableVertexAttribArray(normalHandle)
        GLES20.glDisableVertexAttribArray(texCoordHandle)
        
        // 解绑缓冲区和纹理
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0)
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)
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
    
    /**
     * 加载纹理
     */
    private fun loadTexture(): Int {
        val textures = IntArray(1)
        GLES20.glGenTextures(1, textures, 0)
        
        if (textures[0] != 0) {
            // 绑定纹理
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[0])
            
            // 设置纹理参数
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT)
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT)
            
            // 加载纹理图片
            val bitmap = BitmapFactory.decodeResource(context.resources, android.R.drawable.ic_menu_help)
            if (bitmap != null) {
                GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0)
                bitmap.recycle()
            } else {
                Log.e("OpenGL", "无法加载纹理图片")
                GLES20.glDeleteTextures(1, textures, 0)
                return 0
            }
        } else {
            Log.e("OpenGL", "无法生成纹理")
        }
        
        return textures[0]
    }
}
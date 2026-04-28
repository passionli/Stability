package com.example.stability.video_edit

import android.content.Context
import android.graphics.SurfaceTexture
import android.opengl.GLES11Ext
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import android.view.Surface
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import java.nio.ByteBuffer
import java.nio.ByteOrder

class GrayscaleTextureView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : GLSurfaceView(context, attrs) {

    private var renderer: GrayscaleRenderer? = null
    private var onRendererReadyListener: OnRendererReadyListener? = null
    private var isRendererReady = false

    interface OnRendererReadyListener {
        fun onRendererReady()
    }

    fun setOnRendererReadyListener(listener: OnRendererReadyListener?) {
        this.onRendererReadyListener = listener
        if (isRendererReady) {
            listener?.onRendererReady()
        }
    }

    init {
        setEGLContextClientVersion(2)
        renderer = GrayscaleRenderer(object : GrayscaleRenderer.RendererCallback {
            override fun onSurfaceCreated() {
                isRendererReady = true
                onRendererReadyListener?.onRendererReady()
            }
        })
        setRenderer(renderer)
        renderMode = RENDERMODE_CONTINUOUSLY
    }

    fun getSurface(): Surface {
        return renderer?.getSurface() ?: throw IllegalStateException("Renderer not ready")
    }

    fun getSurfaceTexture(): SurfaceTexture? {
        return renderer?.getSurfaceTexture()
    }

    private class GrayscaleRenderer(
        private val callback: RendererCallback
    ) : GLSurfaceView.Renderer {
        
        interface RendererCallback {
            fun onSurfaceCreated()
        }

        var textureId = -1
        private var programId = -1
        private var positionHandle = -1
        private var texCoordHandle = -1
        private var textureHandle = -1
        private var surfaceTexture: SurfaceTexture? = null

        private val vertexShaderCode = """
            #version 100
            attribute vec4 aPosition;
            attribute vec2 aTexCoord;
            varying vec2 vTexCoord;
            void main() {
                gl_Position = aPosition;
                vTexCoord = aTexCoord;
            }
        """.trimIndent()

        private val fragmentShaderCode = """
            #version 100
            #extension GL_OES_EGL_image_external : require
            precision mediump float;
            varying vec2 vTexCoord;
            uniform samplerExternalOES uTexture;
            void main() {
                vec4 color = texture2D(uTexture, vTexCoord);
                float gray = 0.299 * color.r + 0.587 * color.g + 0.114 * color.b;
                gl_FragColor = vec4(gray, gray, gray, color.a);
            }
        """.trimIndent()

        private val vertices = floatArrayOf(
            -1.0f, -1.0f,
             1.0f, -1.0f,
            -1.0f,  1.0f,
             1.0f,  1.0f
        )

        private val texCoords = floatArrayOf(
            0.0f, 1.0f,
            1.0f, 1.0f,
            0.0f, 0.0f,
            1.0f, 0.0f
        )

        override fun onSurfaceCreated(gl: GL10, config: EGLConfig) {
            GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)

            val vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode)
            val fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode)

            programId = GLES20.glCreateProgram()
            GLES20.glAttachShader(programId, vertexShader)
            GLES20.glAttachShader(programId, fragmentShader)
            GLES20.glLinkProgram(programId)

            positionHandle = GLES20.glGetAttribLocation(programId, "aPosition")
            texCoordHandle = GLES20.glGetAttribLocation(programId, "aTexCoord")
            textureHandle = GLES20.glGetUniformLocation(programId, "uTexture")

            val textures = IntArray(1)
            GLES20.glGenTextures(1, textures, 0)
            textureId = textures[0]
            GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId)
            GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
            GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)
            GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE)
            GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE)

            surfaceTexture = SurfaceTexture(textureId)
            
            callback.onSurfaceCreated()
        }

        override fun onSurfaceChanged(gl: GL10, width: Int, height: Int) {
            GLES20.glViewport(0, 0, width, height)
        }

        override fun onDrawFrame(gl: GL10) {
            // 1. 清除颜色缓冲区，将画布填充为 glClearColor 设定的颜色（黑色）
            // GL_COLOR_BUFFER_BIT 表示清除颜色缓冲区
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)

            // 2. 更新 SurfaceTexture 中的纹理图像
            // 从视频解码器获取最新的视频帧并更新到 OpenGL 纹理中
            // 这是实现实时视频流渲染的关键步骤
            surfaceTexture?.updateTexImage()

            // 3. 设置当前使用的着色器程序
            // 将之前编译链接好的 programId 绑定到当前渲染状态
            // 后续的绘制命令将使用此程序中的顶点着色器和片段着色器
            GLES20.glUseProgram(programId)

            // 4. 创建顶点位置数据的缓冲区
            // ByteBuffer.allocateDirect: 分配本地直接内存，避免 JVM 堆内存管理
            // vertices.size * 4: 每个 float 占 4 字节，计算总字节数
            // order(ByteOrder.nativeOrder()): 设置字节序为本地字节序（大端/小端）
            // asFloatBuffer(): 转换为 FloatBuffer
            // put(vertices): 将顶点数组数据写入缓冲区
            // position(0): 将缓冲区位置指针重置到起始位置
            val vertexBuffer = ByteBuffer.allocateDirect(vertices.size * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(vertices)
                .position(0)

            // 5. 创建纹理坐标数据的缓冲区
            // 流程与顶点缓冲区相同，存储纹理坐标数据
            val texCoordBuffer = ByteBuffer.allocateDirect(texCoords.size * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(texCoords)
                .position(0)

            // 6. 启用顶点位置属性数组
            // positionHandle 是顶点着色器中 aPosition 属性的位置索引
            // 启用后，后续的 glVertexAttribPointer 调用会更新此属性
            GLES20.glEnableVertexAttribArray(positionHandle)

            // 7. 设置顶点位置属性指针
            // 参数说明：
            //   positionHandle: 属性位置
            //   2: 每个顶点的分量数（x, y）
            //   GLES20.GL_FLOAT: 数据类型为 float
            //   false: 不进行归一化
            //   0: 步长（stride），连续顶点之间的字节偏移，0 表示紧密排列
            //   vertexBuffer: 数据缓冲区
            GLES20.glVertexAttribPointer(positionHandle, 2, GLES20.GL_FLOAT, false, 0, vertexBuffer)

            // 8. 启用纹理坐标属性数组
            // texCoordHandle 是顶点着色器中 aTexCoord 属性的位置索引
            GLES20.glEnableVertexAttribArray(texCoordHandle)

            // 9. 设置纹理坐标属性指针
            // 参数说明同顶点位置属性
            GLES20.glVertexAttribPointer(texCoordHandle, 2, GLES20.GL_FLOAT, false, 0, texCoordBuffer)

            // 10. 激活纹理单元 0
            // OpenGL 支持多个纹理单元，这里选择 GL_TEXTURE0
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0)

            // 11. 绑定外部纹理到当前纹理单元
            // GL_OES_EGL_image_external 扩展用于支持来自外部源（如视频解码器）的纹理
            // textureId 是之前创建的纹理对象 ID
            GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId)

            // 12. 设置片段着色器中的纹理采样器 uniform 变量
            // textureHandle 是片段着色器中 uTexture 变量的位置
            // 0 表示使用 GL_TEXTURE0 纹理单元
            GLES20.glUniform1i(textureHandle, 0)

            // 13. 使用三角形带方式绘制 4 个顶点
            // GL_TRIANGLE_STRIP: 三角形带绘制模式，通过相邻顶点形成三角形
            // 0: 起始顶点索引
            // 4: 顶点数量
            // 4 个顶点将形成 2 个三角形，覆盖整个屏幕
            GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)

            // 14. 禁用顶点位置属性数组
            // 绘制完成后禁用，避免影响后续操作
            GLES20.glDisableVertexAttribArray(positionHandle)

            // 15. 禁用纹理坐标属性数组
            GLES20.glDisableVertexAttribArray(texCoordHandle)
        }

        private fun loadShader(type: Int, shaderCode: String): Int {
            val shader = GLES20.glCreateShader(type)
            GLES20.glShaderSource(shader, shaderCode)
            GLES20.glCompileShader(shader)
            return shader
        }

        fun getSurfaceTexture(): SurfaceTexture? {
            return surfaceTexture
        }

        fun getSurface(): Surface {
            return Surface(surfaceTexture)
        }
    }
}
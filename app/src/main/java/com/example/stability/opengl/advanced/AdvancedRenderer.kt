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
    // 设计原因：使用字符串定义着色器代码，便于修改和维护
    // 技术目的：实现顶点位置变换、法线变换和光照计算
    // 最佳实践：使用多行字符串格式，提高代码可读性
    private val vertexShaderCode = """
        attribute vec4 vPosition; // 顶点位置属性 - 从应用程序接收的顶点位置数据
        attribute vec3 vNormal; // 顶点法线属性 - 用于光照计算
        attribute vec2 vTexCoord; // 顶点纹理坐标属性 - 用于纹理映射
        uniform mat4 uMVPMatrix; // 模型视图投影矩阵 - 用于顶点位置变换
        uniform mat4 uMVMatrix; // 模型视图矩阵 - 用于计算法线矩阵
        uniform mat3 uNormalMatrix; // 法线矩阵 - 用于法线变换
        uniform vec3 uLightDirection; // 光源方向 - 平行光的方向向量
        uniform vec4 uLightColor; // 光源颜色 - 平行光的颜色
        uniform vec4 uAmbientColor; // 环境光颜色 - 环境光的强度和颜色
        varying vec2 aTexCoord; // 传递给片段着色器的纹理坐标 - 插值后的纹理坐标
        varying vec4 aColor; // 传递给片段着色器的颜色 - 计算后的光照颜色
        void main() {
            // 应用矩阵变换 - 将局部坐标转换为裁剪空间坐标
            gl_Position = uMVPMatrix * vPosition;
            
            // 计算变换后的法线 - 确保法线在变换后仍然保持单位长度
            vec3 transformedNormal = uNormalMatrix * vNormal;
            
            // 计算光照 - 使用兰伯特光照模型
            float dotProduct = max(dot(transformedNormal, normalize(uLightDirection)), 0.0);
            vec4 lightFactor = uAmbientColor + uLightColor * dotProduct;
            
            // 传递纹理坐标和颜色 - 将计算结果传递给片段着色器
            aTexCoord = vTexCoord;
            aColor = lightFactor;
        }
    """
    
    // 片段着色器代码
    // 设计原因：使用字符串定义着色器代码，便于修改和维护
    // 技术目的：实现纹理采样和光照应用
    // 最佳实践：使用多行字符串格式，提高代码可读性
    private val fragmentShaderCode = """
        precision mediump float; // 精度限定符 - 设置浮点数精度为中等
        varying vec2 aTexCoord; // 从顶点着色器接收的纹理坐标 - 插值后的纹理坐标
        varying vec4 aColor; // 从顶点着色器接收的颜色 - 计算后的光照颜色
        uniform sampler2D uTexture; // 纹理采样器 - 用于采样纹理
        void main() {
            // 采样纹理并应用光照 - 将纹理颜色与光照颜色相乘
            vec4 textureColor = texture2D(uTexture, aTexCoord);
            gl_FragColor = textureColor * aColor;
        }
    """
    
    // 立方体顶点数据（包含位置、法线和纹理坐标）
    // 设计原因：使用数组存储顶点数据，便于一次性传递给 OpenGL
    // 技术目的：定义立方体的几何形状、法线和纹理坐标
    // 最佳实践：按顺序组织顶点数据，包含位置、法线和纹理坐标信息
    private val cubeVertices = floatArrayOf(
        // 前面
        -0.5f, -0.5f,  0.5f,  0.0f, 0.0f, 1.0f, 0.0f, 0.0f, // 左下 - (x, y, z, nx, ny, nz, s, t)
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
    // 设计原因：使用索引绘制，减少重复顶点数据，提高性能
    // 技术目的：定义如何使用顶点数据绘制三角形
    // 最佳实践：按三角形顺序组织索引，确保正确的绘制顺序
    private val drawOrder = shortArrayOf(
        0, 1, 2, 0, 2, 3,    // 前面
        4, 5, 6, 4, 6, 7,    // 后面
        8, 9, 10, 8, 10, 11,  // 左面
        12, 13, 14, 12, 14, 15, // 右面
        16, 17, 18, 16, 18, 19, // 下面
        20, 21, 22, 20, 22, 23  // 上面
    )
    
    // 程序 ID
    // 设计原因：存储 OpenGL 程序对象的 ID，用于后续操作
    // 技术目的：标识和管理 OpenGL 程序
    private var programId: Int = 0
    
    // 顶点位置属性 ID
    // 设计原因：存储顶点位置属性的索引，用于后续设置顶点数据
    // 技术目的：标识顶点着色器中的 vPosition 属性
    private var positionHandle: Int = 0
    
    // 顶点法线属性 ID
    // 设计原因：存储顶点法线属性的索引，用于后续设置顶点数据
    // 技术目的：标识顶点着色器中的 vNormal 属性
    private var normalHandle: Int = 0
    
    // 顶点纹理坐标属性 ID
    // 设计原因：存储顶点纹理坐标属性的索引，用于后续设置顶点数据
    // 技术目的：标识顶点着色器中的 vTexCoord 属性
    private var texCoordHandle: Int = 0
    
    // 模型视图投影矩阵 uniform ID
    // 设计原因：存储矩阵 uniform 的索引，用于后续设置矩阵数据
    // 技术目的：标识顶点着色器中的 uMVPMatrix uniform
    private var mvpMatrixHandle: Int = 0
    
    // 模型视图矩阵 uniform ID
    // 设计原因：存储矩阵 uniform 的索引，用于后续设置矩阵数据
    // 技术目的：标识顶点着色器中的 uMVMatrix uniform
    private var mvMatrixHandle: Int = 0
    
    // 法线矩阵 uniform ID
    // 设计原因：存储矩阵 uniform 的索引，用于后续设置矩阵数据
    // 技术目的：标识顶点着色器中的 uNormalMatrix uniform
    private var normalMatrixHandle: Int = 0
    
    // 光源方向 uniform ID
    // 设计原因：存储 uniform 的索引，用于后续设置光源参数
    // 技术目的：标识顶点着色器中的 uLightDirection uniform
    private var lightDirectionHandle: Int = 0
    
    // 光源颜色 uniform ID
    // 设计原因：存储 uniform 的索引，用于后续设置光源参数
    // 技术目的：标识顶点着色器中的 uLightColor uniform
    private var lightColorHandle: Int = 0
    
    // 环境光颜色 uniform ID
    // 设计原因：存储 uniform 的索引，用于后续设置光源参数
    // 技术目的：标识顶点着色器中的 uAmbientColor uniform
    private var ambientColorHandle: Int = 0
    
    // 纹理采样器 uniform ID
    // 设计原因：存储 uniform 的索引，用于后续设置纹理参数
    // 技术目的：标识片段着色器中的 uTexture uniform
    private var textureHandle: Int = 0
    
    // 顶点缓冲区 ID
    // 设计原因：存储顶点缓冲区对象的 ID，用于后续绑定和操作
    // 技术目的：管理顶点数据的 GPU 内存
    private var vertexBufferId: Int = 0
    
    // 索引缓冲区 ID
    // 设计原因：存储索引缓冲区对象的 ID，用于后续绑定和操作
    // 技术目的：管理索引数据的 GPU 内存
    private var indexBufferId: Int = 0
    
    // 纹理 ID
    // 设计原因：存储纹理对象的 ID，用于后续绑定和操作
    // 技术目的：管理纹理数据的 GPU 内存
    private var textureId: Int = 0
    
    // 模型视图投影矩阵
    // 设计原因：存储矩阵数据，用于顶点变换
    // 技术目的：实现 3D 变换和投影
    // 最佳实践：使用 16 元素的浮点数组存储 4x4 矩阵
    private val mvpMatrix = FloatArray(16) // 模型视图投影矩阵
    private val projectionMatrix = FloatArray(16) // 投影矩阵
    private val viewMatrix = FloatArray(16) // 视图矩阵
    private val modelMatrix = FloatArray(16) // 模型矩阵
    private val normalMatrix = FloatArray(9) // 法线矩阵
    
    // 旋转角度
    // 设计原因：存储旋转角度，用于实现动画效果
    // 技术目的：控制模型的旋转
    private var angleX = 0f // X 轴旋转角度
    private var angleY = 0f // Y 轴旋转角度
    
    /**
     * 当 Surface 创建时调用
     * 这是渲染器的第一个回调方法，在这里进行 OpenGL 的初始化工作
     * @param gl GL10 接口，用于获取 OpenGL 信息（在 ES 2.0 中很少使用）
     * @param config EGL 配置对象，包含颜色缓冲区、深度缓冲区等配置
     */
    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        // 输出日志信息，用于调试和跟踪渲染器的生命周期
        // 设计原因：提供方法调用的详细信息，便于调试
        // 技术目的：跟踪渲染器的初始化过程
        Log.d("OpenGL", "=== AdvancedRenderer.onSurfaceCreated called ===")
        Log.d("OpenGL", "Thread ID: ${Thread.currentThread().id}")
        
        // 设置背景颜色为黑色
        // 设计原因：初始化 OpenGL 状态，设置默认背景颜色
        // 技术目的：确保渲染前缓冲区有正确的初始颜色
        // 参数含义：red=0.0f（红色分量）, green=0.0f（绿色分量）, blue=0.0f（蓝色分量）, alpha=1.0f（透明度）
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)
        
        // 启用深度测试
        // 设计原因：启用深度测试，确保物体按照正确的深度顺序绘制
        // 技术目的：避免远处的物体遮挡近处的物体
        // 最佳实践：在 3D 渲染中启用深度测试
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)
        
        // 创建顶点着色器
        // 设计原因：编译和加载顶点着色器
        // 技术目的：准备顶点处理的着色器程序
        // 最佳实践：检查编译结果，处理编译错误
        val vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode)
        
        // 创建片段着色器
        // 设计原因：编译和加载片段着色器
        // 技术目的：准备片段处理的着色器程序
        // 最佳实践：检查编译结果，处理编译错误
        val fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode)
        
        // 创建 OpenGL 程序
        // 设计原因：创建着色器程序对象，用于链接和管理着色器
        // 技术目的：将多个着色器组合成一个完整的渲染程序
        // 返回值：程序对象的 ID，如果创建失败返回 0
        programId = GLES20.glCreateProgram()
        
        // 向程序中添加着色器
        // 设计原因：将编译好的着色器附加到程序对象
        // 技术目的：为程序提供顶点和片段处理逻辑
        // 参数含义：programId=程序对象 ID, shaderId=着色器对象 ID
        GLES20.glAttachShader(programId, vertexShader)
        GLES20.glAttachShader(programId, fragmentShader)
        
        // 链接程序
        // 设计原因：链接程序对象，将多个着色器组合成可执行的程序
        // 技术目的：准备程序以供使用
        // 最佳实践：检查链接结果，处理链接错误
        GLES20.glLinkProgram(programId)
        
        // 检查链接状态
        // 设计原因：验证程序链接是否成功
        // 技术目的：确保程序可以正常使用
        // 最佳实践：获取并检查链接状态，处理链接错误
        val linkStatus = IntArray(1)
        GLES20.glGetProgramiv(programId, GLES20.GL_LINK_STATUS, linkStatus, 0)
        if (linkStatus[0] != GLES20.GL_TRUE) {
            // 链接失败，获取错误信息
            // 设计原因：处理链接错误，提供详细的错误信息
            // 技术目的：帮助开发者定位链接问题
            Log.e("OpenGL", "无法链接程序: " + GLES20.glGetProgramInfoLog(programId))
            // 删除无效的程序对象
            // 设计原因：清理资源，避免内存泄漏
            // 技术目的：释放未成功链接的程序对象
            GLES20.glDeleteProgram(programId)
            return
        }
        
        // 获取属性和 uniform 的 ID
        // 设计原因：获取着色器中属性和 uniform 的位置，用于后续设置数据
        // 技术目的：建立应用程序与着色器之间的数据通道
        // 最佳实践：检查返回值是否有效
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
        // 设计原因：创建顶点缓冲区对象，用于存储顶点数据
        // 技术目的：将顶点数据上传到 GPU，提高渲染性能
        // 最佳实践：检查返回的缓冲区 ID 是否有效
        val vertexBuffers = IntArray(1)
        GLES20.glGenBuffers(1, vertexBuffers, 0)
        vertexBufferId = vertexBuffers[0]
        
        // 绑定顶点缓冲区
        // 设计原因：将顶点缓冲区绑定到当前上下文，准备上传数据
        // 技术目的：指定当前操作的缓冲区对象
        // 参数含义：target=GL_ARRAY_BUFFER（顶点数据缓冲区）, buffer=缓冲区 ID
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vertexBufferId)
        
        // 将顶点数据复制到缓冲区
        // 设计原因：将 CPU 端的顶点数据上传到 GPU 缓冲区
        // 技术目的：让 GPU 直接访问顶点数据，提高渲染性能
        // 最佳实践：正确计算数据大小，使用合适的缓冲区使用模式
        val vertexBuffer = java.nio.FloatBuffer.allocate(cubeVertices.size)
        vertexBuffer.put(cubeVertices)
        vertexBuffer.position(0)
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, cubeVertices.size * 4, vertexBuffer, GLES20.GL_STATIC_DRAW)
        
        // 创建索引缓冲区
        // 设计原因：创建索引缓冲区对象，用于存储索引数据
        // 技术目的：使用索引绘制减少重复顶点，提高性能
        // 最佳实践：检查返回的缓冲区 ID 是否有效
        val indexBuffers = IntArray(1)
        GLES20.glGenBuffers(1, indexBuffers, 0)
        indexBufferId = indexBuffers[0]
        
        // 绑定索引缓冲区
        // 设计原因：将索引缓冲区绑定到当前上下文，准备上传数据
        // 技术目的：指定当前操作的缓冲区对象
        // 参数含义：target=GL_ELEMENT_ARRAY_BUFFER（索引数据缓冲区）, buffer=缓冲区 ID
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, indexBufferId)
        
        // 将索引数据复制到缓冲区
        // 设计原因：将 CPU 端的索引数据上传到 GPU 缓冲区
        // 技术目的：让 GPU 直接访问索引数据，提高渲染性能
        // 最佳实践：正确计算数据大小，使用合适的缓冲区使用模式
        val indexBuffer = java.nio.ShortBuffer.allocate(drawOrder.size)
        indexBuffer.put(drawOrder)
        indexBuffer.position(0)
        GLES20.glBufferData(GLES20.GL_ELEMENT_ARRAY_BUFFER, drawOrder.size * 2, indexBuffer, GLES20.GL_STATIC_DRAW)
        
        // 加载纹理
        // 设计原因：加载纹理图片，准备纹理映射
        // 技术目的：为立方体表面添加纹理
        // 最佳实践：检查纹理加载是否成功，处理加载失败的情况
        textureId = loadTexture()
        
        // 解绑缓冲区
        // 设计原因：完成缓冲区操作后解绑，避免后续操作意外修改
        // 技术目的：释放缓冲区的绑定状态，提高安全性
        // 最佳实践：操作完成后及时解绑缓冲区
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0)
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0)
        
        Log.d("OpenGL", "=== AdvancedRenderer.onSurfaceCreated completed ===")
    }
    
    /**
     * 当 Surface 尺寸发生变化时调用
     * 例如屏幕旋转、窗口大小改变时会触发此方法
     * @param gl GL10 接口
     * @param width 新的宽度（像素）
     * @param height 新的高度（像素）
     */
    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        // 输出日志信息，用于调试
        // 设计原因：提供方法调用的详细信息，便于调试
        // 技术目的：跟踪 Surface 尺寸变化
        Log.d("OpenGL", "=== AdvancedRenderer.onSurfaceChanged called ===")
        Log.d("OpenGL", "Width: $width, Height: $height")
        
        // 设置视口大小
        // 设计原因：调整 OpenGL 视口大小以匹配 Surface 尺寸
        // 技术目的：确保渲染结果正确映射到屏幕
        // 参数含义：x=视口左上角 x 坐标, y=视口左上角 y 坐标, width=视口宽度, height=视口高度
        GLES20.glViewport(0, 0, width, height)
        
        // 计算宽高比
        // 设计原因：根据屏幕尺寸计算宽高比，用于设置投影矩阵
        // 技术目的：确保渲染结果不会拉伸变形
        val aspectRatio = width.toFloat() / height.toFloat()
        
        // 设置透视投影矩阵
        // 设计原因：创建透视投影矩阵，用于 3D 渲染
        // 技术目的：模拟真实世界的透视效果
        // 参数含义：
        // - left: 左边界（标准化设备坐标）
        // - right: 右边界（标准化设备坐标）
        // - bottom: 下边界（标准化设备坐标）
        // - top: 上边界（标准化设备坐标）
        // - near: 近平面距离
        // - far: 远平面距离
        Matrix.frustumM(projectionMatrix, 0, -aspectRatio, aspectRatio, -1f, 1f, 3f, 7f)
        
        // 设置相机位置
        // 设计原因：设置相机的位置和朝向
        // 技术目的：定义观察者的视角
        // 参数含义：
        // - eyeX, eyeY, eyeZ: 相机位置
        // - centerX, centerY, centerZ: 相机看向的点
        // - upX, upY, upZ: 相机的上方向向量
        Matrix.setLookAtM(viewMatrix, 0, 0f, 0f, 5f, 0f, 0f, 0f, 0f, 1f, 0f)
        
        Log.d("OpenGL", "=== AdvancedRenderer.onSurfaceChanged completed ===")
    }
    
    /**
     * 绘制每一帧时调用
     * 这是渲染循环的核心方法，会被持续调用以更新画面
     * @param gl GL10 接口
     */
    override fun onDrawFrame(gl: GL10?) {
        // 清除颜色和深度缓冲区
        // 设计原因：在绘制新帧前清除缓冲区，避免帧之间的残留
        // 技术目的：确保每一帧都从干净的状态开始绘制
        // 参数含义：mask=需要清除的缓冲区标志（颜色缓冲区和深度缓冲区）
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
        
        // 使用程序
        // 设计原因：激活之前创建的着色器程序，准备渲染
        // 技术目的：告诉 OpenGL 使用哪个程序进行渲染
        // 参数含义：programId=程序对象 ID
        GLES20.glUseProgram(programId)
        
        // 启用顶点属性
        // 设计原因：启用顶点属性，允许 OpenGL 访问顶点数据
        // 技术目的：为顶点着色器提供数据
        // 参数含义：index=属性索引
        GLES20.glEnableVertexAttribArray(positionHandle)
        GLES20.glEnableVertexAttribArray(normalHandle)
        GLES20.glEnableVertexAttribArray(texCoordHandle)
        
        // 绑定顶点缓冲区
        // 设计原因：将顶点缓冲区绑定到当前上下文，准备读取顶点数据
        // 技术目的：让 OpenGL 能够访问 GPU 中的顶点数据
        // 参数含义：target=GL_ARRAY_BUFFER（顶点数据缓冲区）, buffer=缓冲区 ID
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vertexBufferId)
        
        // 设置顶点位置属性
        // 设计原因：告诉 OpenGL 如何解析顶点缓冲区中的位置数据
        // 技术目的：为顶点着色器的 vPosition 属性提供数据格式和位置
        // 参数含义：index=属性索引, size=每个顶点的分量数, type=数据类型, normalized=是否规范化, stride=步长, ptr=偏移量
        GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false, 8 * 4, 0)
        
        // 设置顶点法线属性
        // 设计原因：告诉 OpenGL 如何解析顶点缓冲区中的法线数据
        // 技术目的：为顶点着色器的 vNormal 属性提供数据格式和位置
        // 参数含义：index=属性索引, size=每个顶点的分量数, type=数据类型, normalized=是否规范化, stride=步长, ptr=偏移量
        GLES20.glVertexAttribPointer(normalHandle, 3, GLES20.GL_FLOAT, false, 8 * 4, 3 * 4)
        
        // 设置顶点纹理坐标属性
        // 设计原因：告诉 OpenGL 如何解析顶点缓冲区中的纹理坐标数据
        // 技术目的：为顶点着色器的 vTexCoord 属性提供数据格式和位置
        // 参数含义：index=属性索引, size=每个顶点的分量数, type=数据类型, normalized=是否规范化, stride=步长, ptr=偏移量
        GLES20.glVertexAttribPointer(texCoordHandle, 2, GLES20.GL_FLOAT, false, 8 * 4, 6 * 4)
        
        // 绑定索引缓冲区
        // 设计原因：将索引缓冲区绑定到当前上下文，准备读取索引数据
        // 技术目的：让 OpenGL 能够访问 GPU 中的索引数据
        // 参数含义：target=GL_ELEMENT_ARRAY_BUFFER（索引数据缓冲区）, buffer=缓冲区 ID
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, indexBufferId)
        
        // 绑定纹理
        // 设计原因：激活纹理单元并绑定纹理，准备纹理采样
        // 技术目的：为片段着色器提供纹理数据
        // 参数含义：
        // - GL_TEXTURE0: 激活第 0 个纹理单元
        // - textureId: 纹理对象 ID
        // - 0: 纹理单元索引
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId)
        GLES20.glUniform1i(textureHandle, 0)
        
        // 更新旋转角度
        // 设计原因：更新旋转角度，实现动画效果
        // 技术目的：使立方体产生旋转动画
        angleX += 0.5f
        angleY += 0.3f
        if (angleX >= 360f) angleX = 0f
        if (angleY >= 360f) angleY = 0f
        
        // 计算模型视图投影矩阵
        
        // 重置模型矩阵
        // 设计原因：每次绘制前重置模型矩阵，避免变换累积
        // 技术目的：确保每次绘制都从初始状态开始变换
        Matrix.setIdentityM(modelMatrix, 0)
        
        // 应用旋转
        // 设计原因：对模型应用旋转变换，实现动画效果
        // 技术目的：使模型绕 X 轴和 Y 轴旋转
        // 参数含义：
        // - angle: 旋转角度（度数）
        // - x, y, z: 旋转轴的方向向量
        Matrix.rotateM(modelMatrix, 0, angleX, 1f, 0f, 0f)
        Matrix.rotateM(modelMatrix, 0, angleY, 0f, 1f, 0f)
        
        // 计算模型视图矩阵
        // 设计原因：将模型矩阵和视图矩阵相乘，得到模型视图矩阵
        // 技术目的：将顶点从世界坐标系转换到视图坐标系
        val mvMatrix = FloatArray(16)
        Matrix.multiplyMM(mvMatrix, 0, viewMatrix, 0, modelMatrix, 0)
        
        // 计算模型视图投影矩阵
        // 设计原因：将模型视图矩阵和投影矩阵相乘，得到最终的变换矩阵
        // 技术目的：将顶点从视图坐标系转换到裁剪坐标系
        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, mvMatrix, 0)
        
        // 计算法线矩阵
        // 设计原因：计算法线矩阵，用于正确变换法线
        // 技术目的：确保法线在变换后仍然保持单位长度和正确方向
        // 数学原理：法线矩阵 = 模型视图矩阵的逆转置矩阵
        
        // 首先创建一个临时的 4x4 矩阵来存储逆矩阵
        val tempMatrix = FloatArray(16)
        // 计算模型视图矩阵的逆矩阵
        Matrix.invertM(tempMatrix, 0, mvMatrix, 0)
        
        // 从 4x4 逆矩阵中提取左上角的 3x3 部分作为法线矩阵
        // 法线矩阵只需要 3x3 部分，因为它只处理方向向量（没有平移）
        normalMatrix[0] = tempMatrix[0]  // 第一行第一列
        normalMatrix[1] = tempMatrix[1]  // 第一行第二列
        normalMatrix[2] = tempMatrix[2]  // 第一行第三列
        normalMatrix[3] = tempMatrix[4]  // 第二行第一列
        normalMatrix[4] = tempMatrix[5]  // 第二行第二列
        normalMatrix[5] = tempMatrix[6]  // 第二行第三列
        normalMatrix[6] = tempMatrix[8]  // 第三行第一列
        normalMatrix[7] = tempMatrix[9]  // 第三行第二列
        normalMatrix[8] = tempMatrix[10] // 第三行第三列
        
        // 注意：由于我们直接提取了逆矩阵的 3x3 部分，而逆矩阵的转置等于转置的逆矩阵
        // 所以这里不需要再调用 transposeM，因为法线矩阵已经是正确的
        
        // 设置矩阵
        // 设计原因：将计算好的变换矩阵传递给顶点着色器
        // 技术目的：让顶点着色器能够对顶点进行正确的变换
        // 参数含义：
        // - location: uniform 变量的位置
        // - count: 矩阵数量（这里是 1）
        // - transpose: 是否转置矩阵（OpenGL 中通常为 false）
        // - value: 矩阵数据
        // - offset: 数据偏移（从数组的哪个位置开始）
        GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, mvpMatrix, 0)
        GLES20.glUniformMatrix4fv(mvMatrixHandle, 1, false, mvMatrix, 0)
        GLES20.glUniformMatrix3fv(normalMatrixHandle, 1, false, normalMatrix, 0)
        
        // 设置光照参数
        // 设计原因：设置光照参数，实现光照效果
        // 技术目的：为顶点着色器提供光照数据
        // 参数含义：
        // - location: uniform 变量的位置
        // - x, y, z: 光源方向向量
        // - r, g, b, a: 光源颜色和环境光颜色
        GLES20.glUniform3f(lightDirectionHandle, 0f, 0f, -1f)
        GLES20.glUniform4f(lightColorHandle, 1.0f, 1.0f, 1.0f, 1.0f)
        GLES20.glUniform4f(ambientColorHandle, 0.2f, 0.2f, 0.2f, 1.0f)
        
        // 绘制立方体
        // 设计原因：使用索引绘制方式绘制立方体
        // 技术目的：根据索引数据绘制三角形组成的立方体
        // 参数含义：mode=绘制模式, count=顶点数量, type=索引数据类型, offset=索引偏移
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, drawOrder.size, GLES20.GL_UNSIGNED_SHORT, 0)
        
        // 禁用顶点属性
        // 设计原因：绘制完成后禁用顶点属性，避免影响后续操作
        // 技术目的：释放属性数组的绑定，提高性能和安全性
        // 最佳实践：绘制完成后及时禁用不再使用的属性
        GLES20.glDisableVertexAttribArray(positionHandle)
        GLES20.glDisableVertexAttribArray(normalHandle)
        GLES20.glDisableVertexAttribArray(texCoordHandle)
        
        // 解绑缓冲区和纹理
        // 设计原因：绘制完成后解绑缓冲区和纹理，避免影响后续操作
        // 技术目的：释放缓冲区和纹理的绑定状态，提高安全性
        // 最佳实践：操作完成后及时解绑缓冲区和纹理
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0)
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)
    }
    
    /**
     * 加载着色器
     * @param type 着色器类型（GL_VERTEX_SHADER 或 GL_FRAGMENT_SHADER）
     * @param shaderCode 着色器代码（GLSL 语言）
     * @return 着色器 ID，如果失败返回 0
     */
    private fun loadShader(type: Int, shaderCode: String): Int {
        // 创建着色器
        // 设计原因：创建指定类型的着色器对象
        // 技术目的：准备编译着色器代码
        // 参数含义：type=着色器类型（顶点或片段）
        // 返回值：着色器对象 ID，如果创建失败返回 0
        val shader = GLES20.glCreateShader(type)
        
        // 设置着色器代码
        // 设计原因：将着色器代码加载到着色器对象中
        // 技术目的：为编译做准备
        // 参数含义：shader=着色器对象 ID, string=着色器代码
        GLES20.glShaderSource(shader, shaderCode)
        
        // 编译着色器
        // 设计原因：编译着色器代码，生成可执行代码
        // 技术目的：将 GLSL 代码编译为 GPU 可执行的代码
        // 参数含义：shader=着色器对象 ID
        GLES20.glCompileShader(shader)
        
        // 检查编译状态
        // 设计原因：验证着色器编译是否成功
        // 技术目的：确保着色器可以正常使用
        // 最佳实践：获取并检查编译状态，处理编译错误
        val compileStatus = IntArray(1)
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compileStatus, 0)
        if (compileStatus[0] != GLES20.GL_TRUE) {
            // 编译失败，获取错误信息
            // 设计原因：处理编译错误，提供详细的错误信息
            // 技术目的：帮助开发者定位编译问题
            Log.e("OpenGL", "无法编译着色器: " + GLES20.glGetShaderInfoLog(shader))
            // 删除无效的着色器对象
            // 设计原因：清理资源，避免内存泄漏
            // 技术目的：释放未成功编译的着色器对象
            GLES20.glDeleteShader(shader)
            return 0
        }
        
        return shader
    }
    
    /**
     * 加载纹理
     * @return 纹理 ID，如果失败返回 0
     */
    private fun loadTexture(): Int {
        // 生成纹理
        // 设计原因：生成纹理对象，用于存储纹理数据
        // 技术目的：创建纹理资源
        // 返回值：纹理对象 ID 数组
        val textures = IntArray(1)
        GLES20.glGenTextures(1, textures, 0)
        
        if (textures[0] != 0) {
            // 绑定纹理
            // 设计原因：将纹理绑定到当前上下文，准备设置纹理参数
            // 技术目的：指定当前操作的纹理对象
            // 参数含义：target=GL_TEXTURE_2D（2D 纹理）, texture=纹理对象 ID
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[0])
            
            // 设置纹理参数
            // 设计原因：设置纹理的过滤和环绕模式
            // 技术目的：控制纹理的采样行为
            // 参数含义：
            // - target=GL_TEXTURE_2D
            // - pname=纹理参数名称
            // - param=参数值
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT)
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT)
            
            // 加载纹理图片
            // 设计原因：从资源中加载纹理图片
            // 技术目的：为纹理提供图像数据
            val bitmap = BitmapFactory.decodeResource(context.resources, android.R.drawable.ic_menu_help)
            if (bitmap != null) {
                // 将图片数据上传到纹理
                // 设计原因：将位图数据复制到纹理对象
                // 技术目的：为纹理提供实际的图像数据
                // 参数含义：
                // - target=GL_TEXTURE_2D
                // - level=纹理级别（0 为基础级别）
                // - bitmap=位图对象
                // - border=边框宽度（0）
                GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0)
                // 回收位图资源
                // 设计原因：释放位图占用的内存
                // 技术目的：避免内存泄漏
                // 最佳实践：使用完位图后及时回收
                bitmap.recycle()
            } else {
                // 加载失败，处理错误
                // 设计原因：处理纹理加载失败的情况
                // 技术目的：避免使用无效的纹理
                Log.e("OpenGL", "无法加载纹理图片")
                // 删除无效的纹理对象
                // 设计原因：清理资源，避免内存泄漏
                // 技术目的：释放未成功加载的纹理对象
                GLES20.glDeleteTextures(1, textures, 0)
                return 0
            }
        } else {
            // 生成纹理失败，处理错误
            // 设计原因：处理纹理生成失败的情况
            // 技术目的：避免使用无效的纹理
            Log.e("OpenGL", "无法生成纹理")
        }
        
        return textures[0]
    }
}
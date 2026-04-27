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
 *
 * 所有 GLES20 函数调用都添加了详细的参数和返回值日志
 * 日志格式: [GLES20.函数名] 参数名=参数值, 返回值=返回值
 */
class IntermediateRenderer : GLSurfaceView.Renderer {

    // ============================ 常量定义 ============================

    // 着色器类型常量
    // 设计原因：提取 OpenGL 常量为具名变量，提高代码可读性和可维护性
    // 技术目的：避免硬编码魔法数字，使代码更清晰
    // 最佳实践：将相关常量分组管理，便于后续扩展和修改
    private val VERTEX_SHADER_TYPE = GLES20.GL_VERTEX_SHADER  // 顶点着色器类型
    private val FRAGMENT_SHADER_TYPE = GLES20.GL_FRAGMENT_SHADER  // 片段着色器类型

    // 缓冲区目标常量
    // 设计原因：统一管理缓冲区相关常量，提高代码一致性
    // 技术目的：明确区分不同类型的缓冲区，避免使用错误的缓冲区类型
    private val ARRAY_BUFFER = GLES20.GL_ARRAY_BUFFER  // 顶点数据缓冲区
    private val ELEMENT_ARRAY_BUFFER = GLES20.GL_ELEMENT_ARRAY_BUFFER  // 索引数据缓冲区

    // 缓冲区使用模式
    // 设计原因：为不同使用场景的缓冲区提供明确的使用模式
    // 技术目的：帮助 OpenGL 优化内存管理和数据传输
    private val STATIC_DRAW = GLES20.GL_STATIC_DRAW  // 数据不经常变化
    private val DYNAMIC_DRAW = GLES20.GL_DYNAMIC_DRAW  // 数据会频繁变化
    private val STREAM_DRAW = GLES20.GL_STREAM_DRAW  // 数据只使用一次

    // 绘制模式
    // 设计原因：统一管理绘制模式常量，便于代码理解
    // 技术目的：明确指定几何体的绘制方式
    private val TRIANGLES = GLES20.GL_TRIANGLES  // 三角形绘制模式
    private val LINES = GLES20.GL_LINES  // 线段绘制模式
    private val POINTS = GLES20.GL_POINTS  // 点绘制模式

    // 清除缓冲区
    // 设计原因：统一管理缓冲区清除标志
    // 技术目的：明确指定需要清除的缓冲区
    private val COLOR_BUFFER_BIT = GLES20.GL_COLOR_BUFFER_BIT  // 颜色缓冲区
    private val DEPTH_BUFFER_BIT = GLES20.GL_DEPTH_BUFFER_BIT  // 深度缓冲区
    private val STENCIL_BUFFER_BIT = GLES20.GL_STENCIL_BUFFER_BIT  // 模板缓冲区

    // 数据类型
    // 设计原因：统一管理数据类型常量，提高代码可读性
    // 技术目的：明确指定数据类型，避免类型错误
    private val FLOAT = GLES20.GL_FLOAT  // 浮点型
    private val UNSIGNED_SHORT = GLES20.GL_UNSIGNED_SHORT  // 无符号短整型
    private val BYTE = GLES20.GL_BYTE  // 字节型
    private val UNSIGNED_BYTE = GLES20.GL_UNSIGNED_BYTE  // 无符号字节型
    private val SHORT = GLES20.GL_SHORT  // 短整型

    // 状态查询
    // 设计原因：统一管理状态查询常量，提高代码可读性
    // 技术目的：明确指定状态查询的类型
    private val COMPILE_STATUS = GLES20.GL_COMPILE_STATUS  // 编译状态
    private val LINK_STATUS = GLES20.GL_LINK_STATUS  // 链接状态
    private val TRUE = GLES20.GL_TRUE  // 真
    private val FALSE = GLES20.GL_FALSE  // 假
    private val NO_ERROR = GLES20.GL_NO_ERROR  // 无错误

    // 日志标签
    // 设计原因：统一日志标签，便于日志过滤和管理
    // 技术目的：在 Logcat 中清晰标识 OpenGL 相关日志
    private val TAG = "OpenGL"

    // ============================ 着色器代码 ============================

    // 顶点着色器代码
    // 设计原因：使用字符串定义着色器代码，便于修改和维护
    // 技术目的：实现顶点位置变换和颜色传递
    // 最佳实践：使用多行字符串格式，提高代码可读性
    private val vertexShaderCode = """
        attribute vec4 vPosition; // 顶点位置属性 - 从应用程序接收的顶点位置数据
        attribute vec4 vColor; // 顶点颜色属性 - 从应用程序接收的顶点颜色数据
        uniform mat4 uMVPMatrix; // 模型视图投影矩阵 - 用于顶点位置变换
        varying vec4 aColor; // 传递给片段着色器的颜色 - 插值后的颜色值
        void main() {
            gl_Position = uMVPMatrix * vPosition; // 应用矩阵变换 - 将局部坐标转换为裁剪空间坐标
            aColor = vColor; // 传递颜色 - 将顶点颜色传递给片段着色器
        }
    """

    // 片段着色器代码
    // 设计原因：使用字符串定义着色器代码，便于修改和维护
    // 技术目的：实现片段颜色计算
    // 最佳实践：使用多行字符串格式，提高代码可读性
    private val fragmentShaderCode = """
        precision mediump float; // 精度限定符 - 设置浮点数精度为中等
        varying vec4 aColor; // 从顶点着色器接收的颜色 - 插值后的颜色值
        void main() {
            gl_FragColor = aColor; // 使用传递过来的颜色 - 将插值后的颜色设置为片段颜色
        }
    """

    // ============================ 顶点数据 ============================

    // 四边形顶点数据（包含位置和颜色）
    // 设计原因：使用数组存储顶点数据，便于一次性传递给 OpenGL
    // 技术目的：定义四边形的几何形状和颜色
    // 最佳实践：按顺序组织顶点数据，包含位置和颜色信息
    private val quadVertices = floatArrayOf(
        // 位置                颜色
        -0.5f,  0.5f, 0.0f,   1.0f, 0.0f, 0.0f, 1.0f, // 左上 红色 - (x, y, z, r, g, b, a)
         0.5f,  0.5f, 0.0f,   0.0f, 1.0f, 0.0f, 1.0f, // 右上 绿色
        -0.5f, -0.5f, 0.0f,   0.0f, 0.0f, 1.0f, 1.0f, // 左下 蓝色
         0.5f, -0.5f, 0.0f,   1.0f, 1.0f, 0.0f, 1.0f  // 右下 黄色
    )

    // 绘制顺序（索引）
    // 设计原因：使用索引绘制，减少重复顶点数据，提高性能
    // 技术目的：定义如何使用顶点数据绘制三角形
    // 最佳实践：按三角形顺序组织索引，确保正确的绘制顺序
    private val drawOrder = shortArrayOf(0, 1, 2, 1, 2, 3) // 两个三角形：(0,1,2) 和 (1,2,3)

    // ============================ 成员变量 ============================

    // 程序 ID
    // 设计原因：存储 OpenGL 程序对象的 ID，用于后续操作
    // 技术目的：标识和管理 OpenGL 程序
    private var programId: Int = 0

    // 顶点位置属性 ID
    // 设计原因：存储顶点位置属性的索引，用于后续设置顶点数据
    // 技术目的：标识顶点着色器中的 vPosition 属性
    private var positionHandle: Int = 0

    // 顶点颜色属性 ID
    // 设计原因：存储顶点颜色属性的索引，用于后续设置顶点数据
    // 技术目的：标识顶点着色器中的 vColor 属性
    private var colorHandle: Int = 0

    // 模型视图投影矩阵 uniform ID
    // 设计原因：存储矩阵 uniform 的索引，用于后续设置矩阵数据
    // 技术目的：标识顶点着色器中的 uMVPMatrix uniform
    private var mvpMatrixHandle: Int = 0

    // 顶点缓冲区 ID
    // 设计原因：存储顶点缓冲区对象的 ID，用于后续绑定和操作
    // 技术目的：管理顶点数据的 GPU 内存
    private var vertexBufferId: Int = 0

    // 索引缓冲区 ID
    // 设计原因：存储索引缓冲区对象的 ID，用于后续绑定和操作
    // 技术目的：管理索引数据的 GPU 内存
    private var indexBufferId: Int = 0

    // ============================ 矩阵相关变量 ============================

    // 模型视图投影矩阵
    // 设计原因：存储矩阵数据，用于顶点变换
    // 技术目的：实现 3D 变换和投影
    // 最佳实践：使用 16 元素的浮点数组存储 4x4 矩阵
    
    // 模型矩阵 (Model Matrix)
    // 作用：描述模型在局部坐标系中的变换（平移、旋转、缩放）
    // 初始状态：单位矩阵（无变换）
    // 数学原理：4x4 矩阵，对角线为 1 时表示无变换
    // 坐标转换：局部坐标系 → 世界坐标系
    private val modelMatrix = FloatArray(16) // 模型矩阵
    
    // 视图矩阵 (View Matrix)
    // 作用：描述相机/观察者的位置和方向
    // 初始状态：单位矩阵（相机在原点，朝向 Z 轴负方向）
    // 数学原理：相机的逆变换矩阵，将世界坐标系转换为相机坐标系
    // 坐标转换：世界坐标系 → 视图（相机）坐标系
    private val viewMatrix = FloatArray(16) // 视图矩阵
    
    // 投影矩阵 (Projection Matrix)
    // 作用：将 3D 坐标投影到 2D 屏幕坐标
    // 类型：这里使用正交投影（适合 2D 渲染）
    // 数学原理：线性变换，保持物体大小比例，无透视效果
    // 坐标转换：视图坐标系 → 裁剪坐标系
    private val projectionMatrix = FloatArray(16) // 投影矩阵
    
    // 模型视图投影矩阵 (Model-View-Projection Matrix)
    // 作用：组合了模型、视图和投影变换的最终变换矩阵
    // 计算方式：P * V * M（投影 × 视图 × 模型）
    // 数学原理：矩阵乘法的结合律，先计算 V*M 得到模型视图矩阵，再与 P 相乘
    // 坐标转换：局部坐标系 → 裁剪坐标系（一步到位）
    private val mvpMatrix = FloatArray(16) // 模型视图投影矩阵

    // 旋转角度
    // 设计原因：存储旋转角度，用于实现动画效果
    // 技术目的：控制模型的旋转
    private var angle = 0f

    // 顶点属性配置
    // 设计原因：提取顶点属性配置为具名常量，提高代码可读性
    // 技术目的：明确顶点数据的组织结构
    // 最佳实践：使用具名常量代替硬编码数字
    private val POSITION_COMPONENT_COUNT = 3 // 每个顶点的位置分量数（x, y, z）
    private val COLOR_COMPONENT_COUNT = 4 // 每个顶点的颜色分量数（r, g, b, a）
    private val VERTEX_STRIDE = (3 + 4) * 4 // 步长：(位置分量数 + 颜色分量数) * 4字节
    private val POSITION_OFFSET = 0 // 位置数据在顶点中的偏移（字节）
    private val COLOR_OFFSET = 3 * 4 // 颜色数据在顶点中的偏移（字节）
    private val NORMALIZED = false // 是否规范化数据（对于浮点数不需要）

    // 绘制配置
    // 设计原因：提取绘制配置为具名常量，提高代码可读性
    // 技术目的：明确绘制的顶点数量
    private val DRAW_VERTEX_COUNT = drawOrder.size // 绘制的顶点数量

    // ============================ 生命周期方法 ============================

    /**
     * 当 Surface 创建时调用
     * 这是渲染器的第一个回调方法，在这里进行 OpenGL 的初始化工作
     * @param gl GL10 接口，用于获取 OpenGL 信息（在 ES 2.0 中很少使用）
     * @param config EGL 配置对象，包含颜色缓冲区、深度缓冲区等配置
     */
    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        // 打印调用栈
        // 设计原因：提供调用链信息，便于调试和理解渲染流程
        // 技术目的：跟踪 onSurfaceCreated 方法的调用来源
        Log.d(TAG, "=== Call Stack for onSurfaceCreated ===")
        Thread.currentThread().stackTrace.forEachIndexed { index, element ->
            if (index > 2) { // 跳过前 2 个元素（Thread.getStackTrace 和当前方法）
                Log.d(TAG, "${"  ".repeat(index - 2)}${element.className}.${element.methodName} (${element.fileName}:${element.lineNumber})")
            }
        }

        // 输出日志信息，用于调试和跟踪渲染器的生命周期
        // 设计原因：提供方法调用的详细信息，便于调试
        // 技术目的：跟踪渲染器的初始化过程
        Log.d(TAG, "=== IntermediateRenderer.onSurfaceCreated called ===")
        Log.d(TAG, "Thread ID: ${Thread.currentThread().id}")

        // 设置背景颜色为黑色
        // 设计原因：初始化 OpenGL 状态，设置默认背景颜色
        // 技术目的：确保渲染前缓冲区有正确的初始颜色
        // 参数含义：red=0.0f（红色分量）, green=0.0f（绿色分量）, blue=0.0f（蓝色分量）, alpha=1.0f（透明度）
        val clearRed = 0.0f
        val clearGreen = 0.0f
        val clearBlue = 0.0f
        val clearAlpha = 1.0f
        Log.d(TAG, "[GLES20.glClearColor] red=$clearRed, green=$clearGreen, blue=$clearBlue, alpha=$clearAlpha")
        GLES20.glClearColor(clearRed, clearGreen, clearBlue, clearAlpha)

        // 创建顶点着色器
        // 设计原因：编译和加载顶点着色器
        // 技术目的：准备顶点处理的着色器程序
        // 最佳实践：检查编译结果，处理编译错误
        Log.d(TAG, "[loadShader] type=GL_VERTEX_SHADER($VERTEX_SHADER_TYPE)")
        val vertexShader = loadShader(VERTEX_SHADER_TYPE, vertexShaderCode)
        Log.d(TAG, "[loadShader] returns shaderId=$vertexShader")

        // 创建片段着色器
        // 设计原因：编译和加载片段着色器
        // 技术目的：准备片段处理的着色器程序
        // 最佳实践：检查编译结果，处理编译错误
        Log.d(TAG, "[loadShader] type=GL_FRAGMENT_SHADER($FRAGMENT_SHADER_TYPE)")
        val fragmentShader = loadShader(FRAGMENT_SHADER_TYPE, fragmentShaderCode)
        Log.d(TAG, "[loadShader] returns shaderId=$fragmentShader")

        // 创建 OpenGL 程序
        // 设计原因：创建着色器程序对象，用于链接和管理着色器
        // 技术目的：将多个着色器组合成一个完整的渲染程序
        // 返回值：程序对象的 ID，如果创建失败返回 0
        Log.d(TAG, "[GLES20.glCreateProgram] no parameters")
        programId = GLES20.glCreateProgram()
        Log.d(TAG, "[GLES20.glCreateProgram] returns programId=$programId")

        // 向程序中添加着色器
        // 设计原因：将编译好的着色器附加到程序对象
        // 技术目的：为程序提供顶点和片段处理逻辑
        // 参数含义：programId=程序对象 ID, shaderId=着色器对象 ID
        Log.d(TAG, "[GLES20.glAttachShader] programId=$programId, shaderId=$vertexShader")
        GLES20.glAttachShader(programId, vertexShader)
        Log.d(TAG, "[GLES20.glAttachShader] programId=$programId, shaderId=$fragmentShader")
        GLES20.glAttachShader(programId, fragmentShader)

        // 链接程序
        // 设计原因：链接程序对象，将多个着色器组合成可执行的程序
        // 技术目的：准备程序以供使用
        // 最佳实践：检查链接结果，处理链接错误
        Log.d(TAG, "[GLES20.glLinkProgram] programId=$programId")
        GLES20.glLinkProgram(programId)

        // 检查链接状态
        // 设计原因：验证程序链接是否成功
        // 技术目的：确保程序可以正常使用
        // 最佳实践：获取并检查链接状态，处理链接错误
        val linkStatus = IntArray(1)
        Log.d(TAG, "[GLES20.glGetProgramiv] programId=$programId, pname=GL_LINK_STATUS($LINK_STATUS)")
        GLES20.glGetProgramiv(programId, LINK_STATUS, linkStatus, 0)
        Log.d(TAG, "[GLES20.glGetProgramiv] returns linkStatus=${linkStatus[0]} (GL_TRUE=$TRUE, GL_FALSE=$FALSE)")

        if (linkStatus[0] != TRUE) {
            // 链接失败，获取错误信息
            // 设计原因：处理链接错误，提供详细的错误信息
            // 技术目的：帮助开发者定位链接问题
            Log.e(TAG, "[GLES20.glGetProgramInfoLog] programId=$programId")
            val errorLog = GLES20.glGetProgramInfoLog(programId)
            Log.e(TAG, "[GLES20.glGetProgramInfoLog] returns errorLog=\"$errorLog\"")
            Log.e(TAG, "无法链接程序: $errorLog")
            // 删除无效的程序对象
            // 设计原因：清理资源，避免内存泄漏
            // 技术目的：释放未成功链接的程序对象
            Log.d(TAG, "[GLES20.glDeleteProgram] programId=$programId")
            GLES20.glDeleteProgram(programId)
            return
        }

        // 获取属性和 uniform 的 ID
        // 设计原因：获取着色器中属性和 uniform 的位置，用于后续设置数据
        // 技术目的：建立应用程序与着色器之间的数据通道
        // 最佳实践：检查返回值是否有效
        val positionAttributeName = "vPosition"
        Log.d(TAG, "[GLES20.glGetAttribLocation] programId=$programId, name=\"$positionAttributeName\"")
        positionHandle = GLES20.glGetAttribLocation(programId, positionAttributeName)
        Log.d(TAG, "[GLES20.glGetAttribLocation] returns positionHandle=$positionHandle (${if (positionHandle >= 0) "valid" else "invalid"})")

        val colorAttributeName = "vColor"
        Log.d(TAG, "[GLES20.glGetAttribLocation] programId=$programId, name=\"$colorAttributeName\"")
        colorHandle = GLES20.glGetAttribLocation(programId, colorAttributeName)
        Log.d(TAG, "[GLES20.glGetAttribLocation] returns colorHandle=$colorHandle (${if (colorHandle >= 0) "valid" else "invalid"})")

        val matrixUniformName = "uMVPMatrix"
        Log.d(TAG, "[GLES20.glGetUniformLocation] programId=$programId, name=\"$matrixUniformName\"")
        mvpMatrixHandle = GLES20.glGetUniformLocation(programId, matrixUniformName)
        Log.d(TAG, "[GLES20.glGetUniformLocation] returns mvpMatrixHandle=$mvpMatrixHandle (${if (mvpMatrixHandle >= 0) "valid" else "invalid"})")

        // 创建顶点缓冲区
        // 设计原因：创建顶点缓冲区对象，用于存储顶点数据
        // 技术目的：将顶点数据上传到 GPU，提高渲染性能
        // 最佳实践：检查返回的缓冲区 ID 是否有效
        val vertexBuffers = IntArray(1)
        val bufferCount = 1
        val bufferOffset = 0
        Log.d(TAG, "[GLES20.glGenBuffers] n=$bufferCount, buffers=${vertexBuffers[0]}, offset=$bufferOffset")
        GLES20.glGenBuffers(bufferCount, vertexBuffers, bufferOffset)
        vertexBufferId = vertexBuffers[0]
        Log.d(TAG, "[GLES20.glGenBuffers] returns vertexBufferId=$vertexBufferId")

        // 绑定顶点缓冲区
        // 设计原因：将顶点缓冲区绑定到当前上下文，准备上传数据
        // 技术目的：指定当前操作的缓冲区对象
        // 参数含义：target=GL_ARRAY_BUFFER（顶点数据缓冲区）, buffer=缓冲区 ID
        Log.d(TAG, "[GLES20.glBindBuffer] target=GL_ARRAY_BUFFER($ARRAY_BUFFER), buffer=$vertexBufferId")
        GLES20.glBindBuffer(ARRAY_BUFFER, vertexBufferId)

        // 将顶点数据复制到缓冲区
        // 设计原因：将 CPU 端的顶点数据上传到 GPU 缓冲区
        // 技术目的：让 GPU 直接访问顶点数据，提高渲染性能
        // 最佳实践：正确计算数据大小，使用合适的缓冲区使用模式
        val vertexBuffer = java.nio.FloatBuffer.allocate(quadVertices.size)
        vertexBuffer.put(quadVertices)
        vertexBuffer.position(0)
        val vertexDataSizeBytes = quadVertices.size * 4 // 每个 float 4 字节
        Log.d(TAG, "[GLES20.glBufferData] target=GL_ARRAY_BUFFER($ARRAY_BUFFER), size=$vertexDataSizeBytes bytes, usage=GL_STATIC_DRAW($STATIC_DRAW)")
        GLES20.glBufferData(ARRAY_BUFFER, vertexDataSizeBytes, vertexBuffer, STATIC_DRAW)

        // 创建索引缓冲区
        // 设计原因：创建索引缓冲区对象，用于存储索引数据
        // 技术目的：使用索引绘制减少重复顶点，提高性能
        // 最佳实践：检查返回的缓冲区 ID 是否有效
        val indexBuffers = IntArray(1)
        Log.d(TAG, "[GLES20.glGenBuffers] n=$bufferCount, buffers=${indexBuffers[0]}, offset=$bufferOffset")
        GLES20.glGenBuffers(bufferCount, indexBuffers, bufferOffset)
        indexBufferId = indexBuffers[0]
        Log.d(TAG, "[GLES20.glGenBuffers] returns indexBufferId=$indexBufferId")

        // 绑定索引缓冲区
        // 设计原因：将索引缓冲区绑定到当前上下文，准备上传数据
        // 技术目的：指定当前操作的缓冲区对象
        // 参数含义：target=GL_ELEMENT_ARRAY_BUFFER（索引数据缓冲区）, buffer=缓冲区 ID
        Log.d(TAG, "[GLES20.glBindBuffer] target=GL_ELEMENT_ARRAY_BUFFER($ELEMENT_ARRAY_BUFFER), buffer=$indexBufferId")
        GLES20.glBindBuffer(ELEMENT_ARRAY_BUFFER, indexBufferId)

        // 将索引数据复制到缓冲区
        // 设计原因：将 CPU 端的索引数据上传到 GPU 缓冲区
        // 技术目的：让 GPU 直接访问索引数据，提高渲染性能
        // 最佳实践：正确计算数据大小，使用合适的缓冲区使用模式
        val indexBuffer = java.nio.ShortBuffer.allocate(drawOrder.size)
        indexBuffer.put(drawOrder)
        indexBuffer.position(0)
        val indexDataSizeBytes = drawOrder.size * 2 // 每个 short 2 字节
        Log.d(TAG, "[GLES20.glBufferData] target=GL_ELEMENT_ARRAY_BUFFER($ELEMENT_ARRAY_BUFFER), size=$indexDataSizeBytes bytes, usage=GL_STATIC_DRAW($STATIC_DRAW)")
        GLES20.glBufferData(ELEMENT_ARRAY_BUFFER, indexDataSizeBytes, indexBuffer, STATIC_DRAW)

        // 解绑缓冲区
        // 设计原因：完成缓冲区操作后解绑，避免后续操作意外修改
        // 技术目的：释放缓冲区的绑定状态，提高安全性
        // 最佳实践：操作完成后及时解绑缓冲区
        val UNBIND_BUFFER = 0
        Log.d(TAG, "[GLES20.glBindBuffer] target=GL_ARRAY_BUFFER($ARRAY_BUFFER), buffer=$UNBIND_BUFFER (unbind)")
        GLES20.glBindBuffer(ARRAY_BUFFER, UNBIND_BUFFER)
        Log.d(TAG, "[GLES20.glBindBuffer] target=GL_ELEMENT_ARRAY_BUFFER($ELEMENT_ARRAY_BUFFER), buffer=$UNBIND_BUFFER (unbind)")
        GLES20.glBindBuffer(ELEMENT_ARRAY_BUFFER, UNBIND_BUFFER)

        Log.d(TAG, "=== IntermediateRenderer.onSurfaceCreated completed ===")
        Log.d(TAG, "Summary: programId=$programId, positionHandle=$positionHandle, colorHandle=$colorHandle, mvpMatrixHandle=$mvpMatrixHandle, vertexBufferId=$vertexBufferId, indexBufferId=$indexBufferId")
    }

    /**
     * 当 Surface 尺寸发生变化时调用
     * 例如屏幕旋转、窗口大小改变时会触发此方法
     * @param gl GL10 接口
     * @param width 新的宽度（像素）
     * @param height 新的高度（像素）
     */
    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        // 打印调用栈
        // 设计原因：提供调用链信息，便于调试和理解渲染流程
        // 技术目的：跟踪 onSurfaceChanged 方法的调用来源
        Log.d(TAG, "=== Call Stack for onSurfaceChanged ===")
        Thread.currentThread().stackTrace.forEachIndexed { index, element ->
            if (index > 2) { // 跳过前 2 个元素（Thread.getStackTrace 和当前方法）
                Log.d(TAG, "${"  ".repeat(index - 2)}${element.className}.${element.methodName} (${element.fileName}:${element.lineNumber})")
            }
        }

        // 输出日志信息，用于调试
        // 设计原因：提供方法调用的详细信息，便于调试
        // 技术目的：跟踪 Surface 尺寸变化
        Log.d(TAG, "=== IntermediateRenderer.onSurfaceChanged called ===")
        Log.d(TAG, "Width: $width, Height: $height")

        // 设置视口大小
        // 设计原因：调整 OpenGL 视口大小以匹配 Surface 尺寸
        // 技术目的：确保渲染结果正确映射到屏幕
        // 参数含义：x=视口左上角 x 坐标, y=视口左上角 y 坐标, width=视口宽度, height=视口高度
        val viewportX = 0
        val viewportY = 0
        Log.d(TAG, "[GLES20.glViewport] x=$viewportX, y=$viewportY, width=$width, height=$height")
        GLES20.glViewport(viewportX, viewportY, width, height)

        // 计算宽高比
        // 设计原因：根据屏幕尺寸计算宽高比，用于设置投影矩阵
        // 技术目的：确保渲染结果不会拉伸变形
        val aspectRatio = width.toFloat() / height.toFloat()
        Log.d(TAG, "[Matrix.orthoM] aspectRatio=$aspectRatio (width/height)")

        // 设置正交投影矩阵
        // 设计原因：创建正交投影矩阵，用于 2D 渲染
        // 技术目的：将标准化设备坐标映射到屏幕坐标
        // 矩阵原理：正交投影是一种平行投影，保持物体的大小比例
        // 参数含义：
        // - left: 左边界（标准化设备坐标）
        // - right: 右边界（标准化设备坐标）
        // - bottom: 下边界（标准化设备坐标）
        // - top: 上边界（标准化设备坐标）
        // - near: 近平面距离
        // - far: 远平面距离
        // 计算方式：根据屏幕宽高比调整左右边界，保持图形不失真
        val left = -aspectRatio  // 左边界 = -宽高比
        val right = aspectRatio   // 右边界 = 宽高比
        val bottom = -1f          // 下边界 = -1
        val top = 1f              // 上边界 = 1
        val near = -1f            // 近平面 = -1
        val far = 1f               // 远平面 = 1
        Log.d(TAG, "[Matrix.orthoM] left=$left, right=$right, bottom=$bottom, top=$top, near=$near, far=$far")
        Matrix.orthoM(projectionMatrix, 0, left, right, bottom, top, near, far)
        
        // 正交投影矩阵的详细原理：
        // 1. 数学公式：
        //    orthoM 生成的矩阵形式：
        //    [ 2/(right-left), 0, 0, -(right+left)/(right-left) ]
        //    [ 0, 2/(top-bottom), 0, -(top+bottom)/(top-bottom) ]
        //    [ 0, 0, -2/(far-near), -(far+near)/(far-near) ]
        //    [ 0, 0, 0, 1 ]
        // 2. 坐标转换过程：
        //    - 首先将 [left, right] 范围映射到 [-1, 1]
        //    - 然后将 [bottom, top] 范围映射到 [-1, 1]
        //    - 最后将 [near, far] 范围映射到 [-1, 1]
        // 3. 宽高比调整：
        //    - 当屏幕宽高比 > 1（横屏）时，左右边界范围更大
        //    - 当屏幕宽高比 < 1（竖屏）时，左右边界范围更小
        //    - 这样可以保持物体在不同屏幕方向下的正确比例
        // 4. 正交投影的特点：
        //    - 平行投影，无透视效果
        //    - 物体大小与距离无关
        //    - 适合 2D 游戏和 UI 渲染
        //    - 计算简单，性能较好

        Log.d(TAG, "=== IntermediateRenderer.onSurfaceChanged completed ===")
    }

    /**
     * 绘制每一帧时调用
     * 这是渲染循环的核心方法，会被持续调用以更新画面
     * @param gl GL10 接口
     */
    override fun onDrawFrame(gl: GL10?) {
        // 打印调用栈
        // 设计原因：提供调用链信息，便于调试和理解渲染流程
        // 技术目的：跟踪 onDrawFrame 方法的调用来源
        Log.d(TAG, "=== Call Stack for onDrawFrame ===")
        Thread.currentThread().stackTrace.forEachIndexed { index, element ->
            if (index > 2) { // 跳过前 2 个元素（Thread.getStackTrace 和当前方法）
                Log.d(TAG, "${"  ".repeat(index - 2)}${element.className}.${element.methodName} (${element.fileName}:${element.lineNumber})")
            }
        }

        // 清除颜色缓冲区
        // 设计原因：在绘制新帧前清除缓冲区，避免帧之间的残留
        // 技术目的：确保每一帧都从干净的状态开始绘制
        // 参数含义：mask=需要清除的缓冲区标志（这里只清除颜色缓冲区）
        val clearMask = COLOR_BUFFER_BIT
        Log.d(TAG, "[GLES20.glClear] mask=GL_COLOR_BUFFER_BIT($clearMask)")
        GLES20.glClear(clearMask)

        // 使用程序
        // 设计原因：激活之前创建的着色器程序，准备渲染
        // 技术目的：告诉 OpenGL 使用哪个程序进行渲染
        // 参数含义：programId=程序对象 ID
        Log.d(TAG, "[GLES20.glUseProgram] programId=$programId")
        GLES20.glUseProgram(programId)

        // 启用顶点位置属性
        // 设计原因：启用顶点位置属性，允许 OpenGL 访问顶点位置数据
        // 技术目的：为顶点着色器提供位置数据
        // 参数含义：index=属性索引（positionHandle）
        Log.d(TAG, "[GLES20.glEnableVertexAttribArray] index=$positionHandle (vPosition)")
        GLES20.glEnableVertexAttribArray(positionHandle)
        // 启用顶点颜色属性
        // 设计原因：启用顶点颜色属性，允许 OpenGL 访问顶点颜色数据
        // 技术目的：为顶点着色器提供颜色数据
        // 参数含义：index=属性索引（colorHandle）
        Log.d(TAG, "[GLES20.glEnableVertexAttribArray] index=$colorHandle (vColor)")
        GLES20.glEnableVertexAttribArray(colorHandle)

        // 绑定顶点缓冲区
        // 设计原因：将顶点缓冲区绑定到当前上下文，准备读取顶点数据
        // 技术目的：让 OpenGL 能够访问 GPU 中的顶点数据
        // 参数含义：target=GL_ARRAY_BUFFER（顶点数据缓冲区）, buffer=缓冲区 ID
        Log.d(TAG, "[GLES20.glBindBuffer] target=GL_ARRAY_BUFFER($ARRAY_BUFFER), buffer=$vertexBufferId")
        GLES20.glBindBuffer(ARRAY_BUFFER, vertexBufferId)

        // 设置顶点位置属性
        // 设计原因：告诉 OpenGL 如何解析顶点缓冲区中的位置数据
        // 技术目的：为顶点着色器的 vPosition 属性提供数据格式和位置
        // 参数含义：index=属性索引, size=每个顶点的分量数, type=数据类型, normalized=是否规范化, stride=步长, ptr=偏移量
        Log.d(TAG, "[GLES20.glVertexAttribPointer] index=$positionHandle, size=$POSITION_COMPONENT_COUNT, type=GL_FLOAT($FLOAT), normalized=$NORMALIZED, stride=$VERTEX_STRIDE, ptr=offset($POSITION_OFFSET)")
        GLES20.glVertexAttribPointer(positionHandle, POSITION_COMPONENT_COUNT, FLOAT, NORMALIZED, VERTEX_STRIDE, POSITION_OFFSET)

        // 设置顶点颜色属性
        // 设计原因：告诉 OpenGL 如何解析顶点缓冲区中的颜色数据
        // 技术目的：为顶点着色器的 vColor 属性提供数据格式和位置
        // 参数含义：index=属性索引, size=每个顶点的分量数, type=数据类型, normalized=是否规范化, stride=步长, ptr=偏移量
        Log.d(TAG, "[GLES20.glVertexAttribPointer] index=$colorHandle, size=$COLOR_COMPONENT_COUNT, type=GL_FLOAT($FLOAT), normalized=$NORMALIZED, stride=$VERTEX_STRIDE, ptr=offset($COLOR_OFFSET)")
        GLES20.glVertexAttribPointer(colorHandle, COLOR_COMPONENT_COUNT, FLOAT, NORMALIZED, VERTEX_STRIDE, COLOR_OFFSET)

        // 绑定索引缓冲区
        // 设计原因：将索引缓冲区绑定到当前上下文，准备读取索引数据
        // 技术目的：让 OpenGL 能够访问 GPU 中的索引数据
        // 参数含义：target=GL_ELEMENT_ARRAY_BUFFER（索引数据缓冲区）, buffer=缓冲区 ID
        Log.d(TAG, "[GLES20.glBindBuffer] target=GL_ELEMENT_ARRAY_BUFFER($ELEMENT_ARRAY_BUFFER), buffer=$indexBufferId")
        GLES20.glBindBuffer(ELEMENT_ARRAY_BUFFER, indexBufferId)

        // 更新旋转角度
        // 设计原因：更新旋转角度，实现动画效果
        // 技术目的：使四边形产生旋转动画
        angle += 0.5f
        if (angle >= 360f) {
            angle = 0f
        }
        Log.d(TAG, "[Matrix.rotateM] angle=$angle degrees")

        // ============================ 矩阵变换计算 ============================
        
        // 计算模型视图投影矩阵
        // 矩阵变换流程：局部坐标 → 世界坐标 → 视图坐标 → 裁剪坐标
        // 矩阵乘法顺序：P * V * M（投影 × 视图 × 模型）
        // 数学原理：矩阵乘法满足结合律但不满足交换律，变换顺序非常重要
        
        // 1. 重置模型矩阵
        // 设计原因：每次绘制前重置模型矩阵，避免变换累积
        // 技术目的：确保每次绘制都从初始状态开始变换
        // 单位矩阵：对角线为 1，其他为 0 的矩阵，代表无变换
        // 单位矩阵形式：
        // [ 1, 0, 0, 0 ]
        // [ 0, 1, 0, 0 ]
        // [ 0, 0, 1, 0 ]
        // [ 0, 0, 0, 1 ]
        Matrix.setIdentityM(modelMatrix, 0)
        Log.d(TAG, "[Matrix.setIdentityM] modelMatrix reset")

        // 2. 应用旋转变换
        // 设计原因：对模型应用旋转变换，实现动画效果
        // 技术目的：使模型绕 Z 轴旋转
        // 参数含义：
        // - angle: 旋转角度（度数）
        // - x, y, z: 旋转轴的方向向量
        // 旋转原理：绕指定轴旋转指定角度，这里绕 Z 轴旋转
        // 旋转矩阵数学公式（绕 Z 轴）：
        // [ cosθ, -sinθ, 0, 0 ]
        // [ sinθ,  cosθ, 0, 0 ]
        // [ 0,     0,    1, 0 ]
        // [ 0,     0,    0, 1 ]
        val rotationX = 0f  // 旋转轴 X 分量（0 表示不绕 X 轴旋转）
        val rotationY = 0f  // 旋转轴 Y 分量（0 表示不绕 Y 轴旋转）
        val rotationZ = 1f  // 旋转轴 Z 分量（1 表示绕 Z 轴旋转）
        Log.d(TAG, "[Matrix.rotateM] angle=$angle, x=$rotationX, y=$rotationY, z=$rotationZ")
        Matrix.rotateM(modelMatrix, 0, angle, rotationX, rotationY, rotationZ)
        
        // 模型矩阵现在包含了旋转变换信息
        // 作用：将顶点从局部坐标系转换到世界坐标系
        // 变换过程：局部坐标 * 模型矩阵 = 世界坐标

        // 3. 重置视图矩阵
        // 设计原因：设置视图矩阵为单位矩阵，模拟相机在原点
        // 技术目的：简化坐标变换，适用于 2D 渲染
        // 视图矩阵作用：描述相机的位置和朝向
        // 单位矩阵表示：相机在原点，朝向 Z 轴负方向
        // 注意：视图矩阵是相机变换的逆矩阵，因为我们是移动世界来模拟相机移动
        Matrix.setIdentityM(viewMatrix, 0)
        Log.d(TAG, "[Matrix.setIdentityM] viewMatrix reset")
        
        // 4. 计算模型视图矩阵 (Model-View Matrix)
        // 设计原因：将模型矩阵和视图矩阵相乘，得到模型视图矩阵
        // 技术目的：将顶点从世界坐标系转换到视图坐标系
        // 矩阵乘法顺序：V * M（视图 × 模型）
        // 注意：矩阵乘法是右乘，变换顺序是从右到左
        // 数学原理：(V * M) * 顶点 = V * (M * 顶点)，先应用模型变换，再应用视图变换
        val mvMatrix = FloatArray(16)
        Log.d(TAG, "[Matrix.multiplyMM] viewMatrix × modelMatrix")
        Matrix.multiplyMM(mvMatrix, 0, viewMatrix, 0, modelMatrix, 0)
        
        // 模型视图矩阵作用：将顶点从局部坐标系直接转换到视图坐标系
        // 变换过程：局部坐标 * 模型矩阵 * 视图矩阵 = 视图坐标

        // 5. 计算模型视图投影矩阵 (Model-View-Projection Matrix)
        // 设计原因：将模型视图矩阵和投影矩阵相乘，得到最终的变换矩阵
        // 技术目的：将顶点从视图坐标系转换到裁剪坐标系
        // 矩阵乘法顺序：P * (V * M) = P * V * M（投影 × 视图 × 模型）
        // 数学原理：矩阵乘法结合律，先计算 V*M，再与 P 相乘
        Log.d(TAG, "[Matrix.multiplyMM] projectionMatrix × mvMatrix")
        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, mvMatrix, 0)
        
        // 最终的 mvpMatrix 包含了完整的变换信息：
        // - 模型变换（旋转）：物体自身的变换
        // - 视图变换（相机位置）：观察者的位置和方向
        // - 投影变换（从 3D 到 2D 屏幕）：将 3D 坐标投影到 2D 屏幕
        // 变换过程：局部坐标 * 模型矩阵 * 视图矩阵 * 投影矩阵 = 裁剪坐标

        // 6. 设置模型视图投影矩阵到着色器
        // 设计原因：将计算好的变换矩阵传递给顶点着色器
        // 技术目的：让顶点着色器能够对顶点进行正确的变换
        // 参数含义：
        // - location: uniform 变量的位置（uMVPMatrix 在着色器中的位置）
        // - count: 矩阵数量（这里是 1）
        // - transpose: 是否转置矩阵（OpenGL 中通常为 false，因为矩阵在内存中已经是列主序）
        // - value: 矩阵数据（mvpMatrix 数组）
        // - offset: 数据偏移（从数组的哪个位置开始，这里是 0）
        val count = 1
        val transpose = false
        Log.d(TAG, "[GLES20.glUniformMatrix4fv] location=$mvpMatrixHandle, count=$count, transpose=$transpose")
        GLES20.glUniformMatrix4fv(mvpMatrixHandle, count, transpose, mvpMatrix, 0)
        
        // 在顶点着色器中，这个矩阵会被用于变换顶点：
        // gl_Position = uMVPMatrix * vPosition;
        // 这行代码将局部空间的顶点位置转换为裁剪空间的位置
        // 裁剪空间坐标范围：x, y, z ∈ [-1, 1]，w > 0
        // 最终，OpenGL 会将裁剪空间坐标转换为屏幕坐标（视口变换）

        // ===================================================================

        // 绘制四边形
        // 设计原因：使用索引绘制方式绘制四边形
        // 技术目的：根据索引数据绘制三角形组成的四边形
        // 参数含义：mode=绘制模式, count=顶点数量, type=索引数据类型, offset=索引偏移
        val drawMode = TRIANGLES
        val indexType = UNSIGNED_SHORT
        val indicesOffset = 0
        Log.d(TAG, "[GLES20.glDrawElements] mode=GL_TRIANGLES($drawMode), count=$DRAW_VERTEX_COUNT, type=GL_UNSIGNED_SHORT($indexType), offset=$indicesOffset")
        GLES20.glDrawElements(drawMode, DRAW_VERTEX_COUNT, indexType, indicesOffset)

        // 禁用顶点属性
        // 设计原因：绘制完成后禁用顶点属性，避免影响后续操作
        // 技术目的：释放属性数组的绑定，提高性能和安全性
        // 最佳实践：绘制完成后及时禁用不再使用的属性
        Log.d(TAG, "[GLES20.glDisableVertexAttribArray] index=$positionHandle (vPosition)")
        GLES20.glDisableVertexAttribArray(positionHandle)
        Log.d(TAG, "[GLES20.glDisableVertexAttribArray] index=$colorHandle (vColor)")
        GLES20.glDisableVertexAttribArray(colorHandle)

        // 解绑缓冲区
        // 设计原因：绘制完成后解绑缓冲区，避免影响后续操作
        // 技术目的：释放缓冲区的绑定状态，提高安全性
        // 最佳实践：操作完成后及时解绑缓冲区
        val UNBIND_BUFFER = 0
        Log.d(TAG, "[GLES20.glBindBuffer] target=GL_ARRAY_BUFFER($ARRAY_BUFFER), buffer=$UNBIND_BUFFER (unbind)")
        GLES20.glBindBuffer(ARRAY_BUFFER, UNBIND_BUFFER)
        Log.d(TAG, "[GLES20.glBindBuffer] target=GL_ELEMENT_ARRAY_BUFFER($ELEMENT_ARRAY_BUFFER), buffer=$UNBIND_BUFFER (unbind)")
        GLES20.glBindBuffer(ELEMENT_ARRAY_BUFFER, UNBIND_BUFFER)

        // 每帧结束后检查是否有 OpenGL 错误
        // 设计原因：及时发现和处理 OpenGL 错误
        // 技术目的：确保渲染过程没有错误，便于调试
        // 最佳实践：在关键操作后检查 OpenGL 错误
        val error = GLES20.glGetError()
        if (error != NO_ERROR) {
            Log.e(TAG, "[GLES20.glGetError] returns error=$error (${getGLErrorString(error)})")
        } else {
            Log.d(TAG, "[GLES20.glGetError] returns GL_NO_ERROR($NO_ERROR)")
        }
    }

    // ============================ 辅助方法 ============================

    /**
     * 将 OpenGL 错误代码转换为可读字符串
     * @param error OpenGL 错误代码
     * @return 可读的错误描述
     */
    private fun getGLErrorString(error: Int): String {
        // 设计原因：将 OpenGL 错误代码转换为人类可读的字符串
        // 技术目的：便于开发者理解和调试 OpenGL 错误
        // 最佳实践：覆盖常见的 OpenGL 错误类型
        return when (error) {
            GLES20.GL_INVALID_ENUM -> "GL_INVALID_ENUM - 无效的枚举常量"
            GLES20.GL_INVALID_VALUE -> "GL_INVALID_VALUE - 无效的数值参数"
            GLES20.GL_INVALID_OPERATION -> "GL_INVALID_OPERATION - 无效的操作"
            GLES20.GL_OUT_OF_MEMORY -> "GL_OUT_OF_MEMORY - 内存不足"
            else -> "UNKNOWN_ERROR($error)"
        }
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
        val shaderTypeDescription = if (type == VERTEX_SHADER_TYPE) "GL_VERTEX_SHADER" else "GL_FRAGMENT_SHADER"
        Log.d(TAG, "[GLES20.glCreateShader] type=$type ($shaderTypeDescription)")
        val shader = GLES20.glCreateShader(type)
        Log.d(TAG, "[GLES20.glCreateShader] returns shaderId=$shader (${if (shader > 0) "valid" else "invalid"})")

        // 设置着色器代码
        // 设计原因：将着色器代码加载到着色器对象中
        // 技术目的：为编译做准备
        // 参数含义：shader=着色器对象 ID, string=着色器代码
        val codeLength = shaderCode.length
        Log.d(TAG, "[GLES20.glShaderSource] shaderId=$shader, shaderCode length=$codeLength chars")
        GLES20.glShaderSource(shader, shaderCode)

        // 编译着色器
        // 设计原因：编译着色器代码，生成可执行代码
        // 技术目的：将 GLSL 代码编译为 GPU 可执行的代码
        // 参数含义：shader=着色器对象 ID
        Log.d(TAG, "[GLES20.glCompileShader] shaderId=$shader")
        GLES20.glCompileShader(shader)

        // 检查编译状态
        // 设计原因：验证着色器编译是否成功
        // 技术目的：确保着色器可以正常使用
        // 最佳实践：获取并检查编译状态，处理编译错误
        val compileStatus = IntArray(1)
        Log.d(TAG, "[GLES20.glGetShaderiv] shaderId=$shader, pname=GL_COMPILE_STATUS($COMPILE_STATUS)")
        GLES20.glGetShaderiv(shader, COMPILE_STATUS, compileStatus, 0)
        Log.d(TAG, "[GLES20.glGetShaderiv] returns compileStatus=${compileStatus[0]} (GL_TRUE=$TRUE, GL_FALSE=$FALSE)")

        if (compileStatus[0] != TRUE) {
            // 编译失败，获取错误信息
            // 设计原因：处理编译错误，提供详细的错误信息
            // 技术目的：帮助开发者定位编译问题
            Log.e(TAG, "[GLES20.glGetShaderInfoLog] shaderId=$shader")
            val shaderLog = GLES20.glGetShaderInfoLog(shader)
            Log.e(TAG, "[GLES20.glGetShaderInfoLog] returns shaderLog=\"$shaderLog\"")
            Log.e(TAG, "无法编译着色器: $shaderLog")
            // 删除无效的着色器对象
            // 设计原因：清理资源，避免内存泄漏
            // 技术目的：释放编译失败的着色器对象
            Log.d(TAG, "[GLES20.glDeleteShader] shaderId=$shader")
            GLES20.glDeleteShader(shader)
            return 0
        }

        Log.d(TAG, "[loadShader] returns shaderId=$shader")
        return shader
    }
}

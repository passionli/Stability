package com.example.stability.opengl.basic

// 导入 Android OpenGL ES 2.0 API，用于调用 OpenGL 图形渲染函数
import android.opengl.GLES20
// 导入 GLSurfaceView，这是 Android 提供的专门用于显示 OpenGL 图形的视图组件
import android.opengl.GLSurfaceView
// 导入日志工具，用于输出调试信息到 Logcat
import android.util.Log
// 导入 EGLConfig，这是 OpenGL ES 的配置对象，包含颜色缓冲区、深度缓冲区等配置信息
import javax.microedition.khronos.egl.EGLConfig
// 导入 GL10，这是 OpenGL ES 1.0 的接口，在 ES 2.0 中主要用于获取一些基本信息
import javax.microedition.khronos.opengles.GL10

/**
 * 基础 OpenGL 渲染器
 * 实现了 GLSurfaceView.Renderer 接口，这是 Android 中 OpenGL 渲染的核心接口
 * 用于渲染一个简单的三角形，是学习 OpenGL 的入门示例
 *
 * 所有 GLES20 函数调用都添加了详细的参数和返回值日志
 * 日志格式: [GLES20.函数名] 参数名=参数值, 返回值=返回值
 */
class BasicRenderer : GLSurfaceView.Renderer {

    // ============================ 常量定义 ============================

    // 着色器类型常量
    private val VERTEX_SHADER_TYPE = GLES20.GL_VERTEX_SHADER
    private val FRAGMENT_SHADER_TYPE = GLES20.GL_FRAGMENT_SHADER

    // 缓冲区目标常量
    private val ARRAY_BUFFER = GLES20.GL_ARRAY_BUFFER
    private val ELEMENT_ARRAY_BUFFER = GLES20.GL_ELEMENT_ARRAY_BUFFER

    // 缓冲区使用模式
    private val STATIC_DRAW = GLES20.GL_STATIC_DRAW
    private val DYNAMIC_DRAW = GLES20.GL_DYNAMIC_DRAW
    private val STREAM_DRAW = GLES20.GL_STREAM_DRAW

    // 绘制模式
    private val TRIANGLES = GLES20.GL_TRIANGLES
    private val LINES = GLES20.GL_LINES
    private val POINTS = GLES20.GL_POINTS

    // 清除缓冲区
    private val COLOR_BUFFER_BIT = GLES20.GL_COLOR_BUFFER_BIT
    private val DEPTH_BUFFER_BIT = GLES20.GL_DEPTH_BUFFER_BIT
    private val STENCIL_BUFFER_BIT = GLES20.GL_STENCIL_BUFFER_BIT

    // 数据类型
    private val FLOAT = GLES20.GL_FLOAT
    private val BYTE = GLES20.GL_BYTE
    private val UNSIGNED_BYTE = GLES20.GL_UNSIGNED_BYTE
    private val SHORT = GLES20.GL_SHORT
    private val UNSIGNED_SHORT = GLES20.GL_UNSIGNED_SHORT

    // 状态查询
    private val COMPILE_STATUS = GLES20.GL_COMPILE_STATUS
    private val LINK_STATUS = GLES20.GL_LINK_STATUS
    private val TRUE = GLES20.GL_TRUE
    private val FALSE = GLES20.GL_FALSE
    private val NO_ERROR = GLES20.GL_NO_ERROR

    // 日志标签
    private val TAG = "OpenGL"

    // ============================ 着色器代码 ============================

    // 顶点着色器代码（使用 Kotlin 多行字符串）
    // 顶点着色器在 GPU 上运行，负责处理每个顶点的属性（如位置、颜色等）
    // attribute 变量是从 CPU 端传递到 GPU 的数据，每个顶点都会有一个对应的值
    private val vertexShaderCode = """
        attribute vec4 vPosition; // 声明一个四维向量属性 vPosition，用于存储顶点的 x、y、z、w 坐标
        void main() {
            // main 函数是着色器的入口函数，类似于 C/C++ 的 main 函数
            // gl_Position 是 OpenGL ES 的内置变量，用于设置顶点的最终位置
            // 将输入的顶点位置直接赋值给 gl_Position，不做任何变换
            gl_Position = vPosition; // 设置顶点在裁剪空间中的位置
        }
    """

    // 片段着色器代码（也称为像素着色器）
    // 片段着色器在 GPU 上运行，负责计算每个像素（片段）的最终颜色
    // 在顶点着色器之后执行，对光栅化后的每个像素进行处理
    private val fragmentShaderCode = """
        // precision 关键字用于设置浮点数的精度
        // mediump 表示中等精度，适用于大多数颜色计算
        // float 表示浮点数类型
        precision mediump float; // 设置默认浮点数精度为中等精度，这是片段着色器必需的声明

        void main() {
            // gl_FragColor 是 OpenGL ES 的内置变量，用于设置片段的最终颜色
            // vec4 表示四维向量，分别对应 R（红）、G（绿）、B（蓝）、A（透明度）
            // 这里设置为红色：R=1.0（全红）、G=0.0（无绿）、B=0.0（无蓝）、A=1.0（完全不透明）
            gl_FragColor = vec4(1.0, 0.0, 0.0, 1.0); // 设置片段颜色为红色
        }
    """

    // ============================ 顶点数据 ============================

    // 三角形顶点数据（在 OpenGL 中，所有 3D 模型都由三角形组成）
    // 这些坐标是在标准化设备坐标（NDC）系统中，范围从 -1 到 1
    // X 轴：-1（左）到 1（右），Y 轴：-1（下）到 1（上），Z 轴：-1（里）到 1（外）
    private val triangleVertices = floatArrayOf(
        // 每个顶点由 3 个浮点数组成：x、y、z 坐标
        0.0f, 0.5f, 0.0f,   // 顶部顶点：X=0（居中），Y=0.5（上半部分），Z=0（在屏幕上）
        -0.5f, -0.5f, 0.0f,  // 左下角顶点：X=-0.5（偏左），Y=-0.5（下半部分），Z=0
        0.5f, -0.5f, 0.0f    // 右下角顶点：X=0.5（偏右），Y=-0.5（下半部分），Z=0
    )

    // ============================ 成员变量 ============================

    // 程序 ID：OpenGL 程序是着色器的容器，包含顶点着色器和片段着色器
    // 这个 ID 用于在后续渲染中引用和使用这个程序
    private var programId: Int = 0

    // 顶点位置属性 ID：用于在 GPU 中找到顶点着色器中的 vPosition 变量
    // 通过这个 ID，CPU 可以将顶点数据传递给 GPU 的着色器
    private var positionHandle: Int = 0

    // 顶点缓冲区 ID：用于存储顶点数据的 GPU 内存区域
    // 使用缓冲区可以提高性能，因为数据直接存储在 GPU 中，不需要每次都从 CPU 传输
    private var vertexBufferId: Int = 0

    // 顶点属性配置
    private val POSITION_COMPONENT_COUNT = 3 // 每个顶点的位置分量数（x, y, z）
    private val POSITION_STRIDE = 0 // 步长，0 表示数据紧密排列
    private val POSITION_OFFSET = 0 // 偏移量，0 表示从缓冲区开头开始
    private val NORMALIZED = false // 是否规范化数据

    // 绘制配置
    private val DRAW_FIRST_VERTEX = 0 // 从第几个顶点开始绘制
    private val DRAW_VERTEX_COUNT = 3 // 绘制的顶点数量

    // ============================ 生命周期方法 ============================

    /**
     * 当 Surface 创建时调用
     * 这是渲染器的第一个回调方法，在这里进行 OpenGL 的初始化工作
     * @param gl GL10 接口，用于获取 OpenGL 信息（在 ES 2.0 中很少使用）
     * @param config EGL 配置对象，包含颜色缓冲区、深度缓冲区等配置
     */
    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        // 打印调用栈
        Log.d(TAG, "=== Call Stack for onSurfaceCreated ===")
        Thread.currentThread().stackTrace.forEachIndexed { index, element ->
            if (index > 2) { // 跳过前 2 个元素（Thread.getStackTrace 和当前方法）
                Log.d(TAG, "${"  ".repeat(index - 2)}${element.className}.${element.methodName} (${element.fileName}:${element.lineNumber})")
            }
        }

        // 输出日志信息，用于调试和跟踪渲染器的生命周期
        // 这对于理解 OpenGL 的执行流程非常重要
        Log.d(TAG, "=== BasicRenderer.onSurfaceCreated called ===")
        Log.d(TAG, "Thread ID: ${Thread.currentThread().id}")

        // 设置 OpenGL 的背景清除颜色（清屏颜色）
        // glClearColor 的参数是 RGBA 格式，范围都是 0.0 到 1.0
        // 这里设置为黑色：R=0, G=0, B=0, A=1（完全不透明）
        // 这个颜色会在每次调用 glClear 时使用
        val clearRed = 0.0f
        val clearGreen = 0.0f
        val clearBlue = 0.0f
        val clearAlpha = 1.0f
        Log.d(TAG, "[GLES20.glClearColor] red=$clearRed, green=$clearGreen, blue=$clearBlue, alpha=$clearAlpha")
        GLES20.glClearColor(clearRed, clearGreen, clearBlue, clearAlpha)

        // 创建顶点着色器对象
        // loadShader 是自定义方法，负责编译着色器代码
        // GLES20.GL_VERTEX_SHADER 指定创建的是顶点着色器
        Log.d(TAG, "[loadShader] type=GL_VERTEX_SHADER($VERTEX_SHADER_TYPE)")
        val vertexShader = loadShader(VERTEX_SHADER_TYPE, vertexShaderCode)
        Log.d(TAG, "[loadShader] returns shaderId=$vertexShader")

        // 创建片段着色器对象
        // GLES20.GL_FRAGMENT_SHADER 指定创建的是片段着色器
        Log.d(TAG, "[loadShader] type=GL_FRAGMENT_SHADER($FRAGMENT_SHADER_TYPE)")
        val fragmentShader = loadShader(FRAGMENT_SHADER_TYPE, fragmentShaderCode)
        Log.d(TAG, "[loadShader] returns shaderId=$fragmentShader")

        // 创建 OpenGL 程序对象
        // 程序是着色器的容器，一个完整的渲染程序至少需要一个顶点着色器和一个片段着色器
        // glCreateProgram 返回程序的 ID，后续操作都使用这个 ID
        Log.d(TAG, "[GLES20.glCreateProgram] no parameters")
        programId = GLES20.glCreateProgram()
        Log.d(TAG, "[GLES20.glCreateProgram] returns programId=$programId")

        // 将顶点着色器添加到程序中
        // 一个程序可以有多个着色器，但必须至少有一个顶点着色器和一个片段着色器
        Log.d(TAG, "[GLES20.glAttachShader] programId=$programId, shaderId=$vertexShader")
        GLES20.glAttachShader(programId, vertexShader)

        // 将片段着色器添加到程序中
        Log.d(TAG, "[GLES20.glAttachShader] programId=$programId, shaderId=$fragmentShader")
        GLES20.glAttachShader(programId, fragmentShader)

        // 链接程序
        // 链接过程会将多个着色器组合成一个可执行的 GPU 程序
        // 链接器会检查着色器之间的兼容性，比如变量名称是否匹配
        Log.d(TAG, "[GLES20.glLinkProgram] programId=$programId")
        GLES20.glLinkProgram(programId)

        // 检查程序链接状态
        // OpenGL 操作可能会失败，所以必须检查重要操作的结果
        val linkStatus = IntArray(1) // 创建一个数组来存储链接状态
        // glGetProgramiv 获取程序的信息，GL_LINK_STATUS 表示获取链接状态
        Log.d(TAG, "[GLES20.glGetProgramiv] programId=$programId, pname=GL_LINK_STATUS($LINK_STATUS)")
        GLES20.glGetProgramiv(programId, LINK_STATUS, linkStatus, 0)
        Log.d(TAG, "[GLES20.glGetProgramiv] returns linkStatus=${linkStatus[0]} (GL_TRUE=$TRUE, GL_FALSE=$FALSE)")

        // 如果链接失败（GL_TRUE 表示成功，其他值表示失败）
        if (linkStatus[0] != TRUE) {
            // 输出错误日志，包含详细的错误信息
            // glGetProgramInfoLog 返回链接过程中的错误信息
            Log.e(TAG, "[GLES20.glGetProgramInfoLog] programId=$programId")
            val errorLog = GLES20.glGetProgramInfoLog(programId)
            Log.e(TAG, "[GLES20.glGetProgramInfoLog] returns errorLog=\"$errorLog\"")
            Log.e(TAG, "无法链接程序：$errorLog")
            // 删除失败的程序，释放资源
            Log.d(TAG, "[GLES20.glDeleteProgram] programId=$programId")
            GLES20.glDeleteProgram(programId)
            // 直接返回，不进行后续操作
            return
        }

        // 获取顶点位置属性在程序中的位置
        // glGetAttribLocation 返回着色器中指定变量的索引（ID）
        // "vPosition" 必须与顶点着色器中的 attribute 变量名称完全一致
        val attributeName = "vPosition"
        Log.d(TAG, "[GLES20.glGetAttribLocation] programId=$programId, name=\"$attributeName\"")
        positionHandle = GLES20.glGetAttribLocation(programId, attributeName)
        Log.d(TAG, "[GLES20.glGetAttribLocation] returns positionHandle=$positionHandle (${if (positionHandle >= 0) "valid" else "invalid"})")

        // 创建顶点缓冲区对象（VBO）
        // VBO 是存储在 GPU 内存中的顶点数据，可以提高渲染性能
        val buffers = IntArray(1) // 用于存储生成的缓冲区 ID

        // 生成缓冲区 ID
        // glGenBuffers 第一个参数是要生成的缓冲区数量，第二个参数是存储 ID 的数组，第三个参数是数组偏移量
        val bufferCount = 1
        val bufferOffset = 0
        Log.d(TAG, "[GLES20.glGenBuffers] n=$bufferCount, buffers=${buffers[0]}, offset=$bufferOffset")
        GLES20.glGenBuffers(bufferCount, buffers, bufferOffset)
        vertexBufferId = buffers[0] // 获取生成的第一个缓冲区 ID
        Log.d(TAG, "[GLES20.glGenBuffers] returns vertexBufferId=$vertexBufferId")

        // 绑定顶点缓冲区
        // glBindBuffer 将缓冲区 ID 与目标类型关联
        // GL_ARRAY_BUFFER 表示这是一个存储顶点数据的缓冲区
        // 绑定后，后续的缓冲区操作都会作用于这个缓冲区
        Log.d(TAG, "[GLES20.glBindBuffer] target=GL_ARRAY_BUFFER($ARRAY_BUFFER), buffer=$vertexBufferId")
        GLES20.glBindBuffer(ARRAY_BUFFER, vertexBufferId)

        // 创建 FloatBuffer 用于存储顶点数据
        // 在 Android 中，需要使用 NIO 的 Buffer 类来与 OpenGL 共享数据
        // allocate 分配与顶点数据大小相同的内存空间
        val vertexBuffer = java.nio.FloatBuffer.allocate(triangleVertices.size)

        // 将顶点数据复制到缓冲区
        // put 方法将 float 数组的数据复制到 FloatBuffer 中
        vertexBuffer.put(triangleVertices)

        // 设置缓冲区的读取位置
        // 在写入数据后，缓冲区的位置指针在末尾，需要重置为 0 才能被 OpenGL 读取
        vertexBuffer.position(0)

        // 将顶点数据上传到 GPU 缓冲区
        // 第一个参数：目标缓冲区类型（GL_ARRAY_BUFFER）
        // 第二个参数：数据大小（字节数），float 占 4 字节
        // 第三个参数：包含数据的 FloatBuffer
        // 第四个参数：使用模式，GL_STATIC_DRAW 表示数据设置后不会频繁改变
        val dataSizeBytes = triangleVertices.size * 4
        Log.d(TAG, "[GLES20.glBufferData] target=GL_ARRAY_BUFFER($ARRAY_BUFFER), size=$dataSizeBytes bytes, usage=GL_STATIC_DRAW($STATIC_DRAW)")
        GLES20.glBufferData(ARRAY_BUFFER, dataSizeBytes, vertexBuffer, STATIC_DRAW)

        // 解绑缓冲区
        // 将缓冲区目标绑定到 0，表示不再使用当前缓冲区
        // 这是一个好习惯，可以避免意外修改缓冲区数据
        val UNBIND_BUFFER = 0
        Log.d(TAG, "[GLES20.glBindBuffer] target=GL_ARRAY_BUFFER($ARRAY_BUFFER), buffer=$UNBIND_BUFFER (unbind)")
        GLES20.glBindBuffer(ARRAY_BUFFER, UNBIND_BUFFER)

        // 输出完成日志
        Log.d(TAG, "=== BasicRenderer.onSurfaceCreated completed ===")
        Log.d(TAG, "Summary: programId=$programId, positionHandle=$positionHandle, vertexBufferId=$vertexBufferId")
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
        Log.d(TAG, "=== Call Stack for onSurfaceChanged ===")
        Thread.currentThread().stackTrace.forEachIndexed { index, element ->
            if (index > 2) { // 跳过前 2 个元素（Thread.getStackTrace 和当前方法）
                Log.d(TAG, "${"  ".repeat(index - 2)}${element.className}.${element.methodName} (${element.fileName}:${element.lineNumber})")
            }
        }

        // 输出日志信息，用于调试
        Log.d(TAG, "=== BasicRenderer.onSurfaceChanged called ===")
        Log.d(TAG, "Width: $width, Height: $height")

        // 设置 OpenGL 的视口（Viewport）
        // 视口定义了 OpenGL 渲染结果在屏幕上的显示区域
        // 参数：x、y（视口左下角坐标）、width、height（视口大小）
        // 这里设置为整个 Surface 的大小，确保渲染结果填满整个屏幕
        val viewportX = 0
        val viewportY = 0
        Log.d(TAG, "[GLES20.glViewport] x=$viewportX, y=$viewportY, width=$width, height=$height")
        GLES20.glViewport(viewportX, viewportY, width, height)

        // 计算并输出宽高比（用于调试投影矩阵）
        val aspectRatio = width.toFloat() / height.toFloat()
        Log.d(TAG, "[GLES20.glViewport] aspectRatio=$aspectRatio (width/height)")

        // 输出完成日志
        Log.d(TAG, "=== BasicRenderer.onSurfaceChanged completed ===")
    }

    /**
     * 绘制每一帧时调用
     * 这是渲染循环的核心方法，会被持续调用以更新画面
     * @param gl GL10 接口
     */
    override fun onDrawFrame(gl: GL10?) {
        // 打印调用栈
        Log.d(TAG, "=== Call Stack for onDrawFrame ===")
        Thread.currentThread().stackTrace.forEachIndexed { index, element ->
            if (index > 2) { // 跳过前 2 个元素（Thread.getStackTrace 和当前方法）
                Log.d(TAG, "${"  ".repeat(index - 2)}${element.className}.${element.methodName} (${element.fileName}:${element.lineNumber})")
            }
        }

        // 清除颜色缓冲区
        // glClear 使用 glClearColor 设置的颜色清除屏幕
        // GL_COLOR_BUFFER_BIT 表示清除颜色缓冲区，还可以清除深度缓冲区等
        val clearMask = COLOR_BUFFER_BIT
        Log.d(TAG, "[GLES20.glClear] mask=GL_COLOR_BUFFER_BIT($clearMask)")
        GLES20.glClear(clearMask)

        // 使用之前创建的程序
        // glUseProgram 激活指定的程序，后续的绘制操作都会使用这个程序中的着色器
        Log.d(TAG, "[GLES20.glUseProgram] programId=$programId")
        GLES20.glUseProgram(programId)

        // 启用顶点位置属性数组
        // glEnableVertexAttribArray 告诉 OpenGL 我们要使用顶点属性数据
        // positionHandle 指定要启用的属性（vPosition）
        Log.d(TAG, "[GLES20.glEnableVertexAttribArray] index=$positionHandle (vPosition)")
        GLES20.glEnableVertexAttribArray(positionHandle)

        // 绑定顶点缓冲区
        // 在绘制前需要重新绑定缓冲区，因为 onSurfaceCreated 结束后解绑了
        Log.d(TAG, "[GLES20.glBindBuffer] target=GL_ARRAY_BUFFER($ARRAY_BUFFER), buffer=$vertexBufferId")
        GLES20.glBindBuffer(ARRAY_BUFFER, vertexBufferId)

        // 设置顶点位置属性的数据格式
        // 告诉 OpenGL 如何解释缓冲区中的顶点数据
        // 参数说明：
        // 1. index: 属性位置（positionHandle）
        // 2. size: 每个顶点有 3 个分量（x、y、z）
        // 3. type: 数据类型是 GL_FLOAT
        // 4. normalized: 是否规范化（false 表示不规范化）
        // 5. stride: 步长（0 表示数据是紧密排列的）
        // 6. ptr: 数据在缓冲区中的起始偏移（0 表示从开头开始）
        Log.d(TAG, "[GLES20.glVertexAttribPointer] index=$positionHandle, size=$POSITION_COMPONENT_COUNT, type=GL_FLOAT($FLOAT), normalized=$NORMALIZED, stride=$POSITION_STRIDE, ptr=offset($POSITION_OFFSET)")
        GLES20.glVertexAttribPointer(positionHandle, POSITION_COMPONENT_COUNT, FLOAT, NORMALIZED, POSITION_STRIDE, POSITION_OFFSET)

        // 绘制三角形
        // glDrawArrays 使用当前绑定的顶点数据绘制图形
        // 参数说明：
        // 1. mode: 绘制模式，GL_TRIANGLES 表示每 3 个顶点组成一个三角形
        // 2. first: 从第几个顶点开始（0 表示从第一个顶点开始）
        // 3. count: 使用多少个顶点（3 表示使用 3 个顶点，绘制 1 个三角形）
        val drawMode = TRIANGLES
        Log.d(TAG, "[GLES20.glDrawArrays] mode=GL_TRIANGLES($drawMode), first=$DRAW_FIRST_VERTEX, count=$DRAW_VERTEX_COUNT")
        GLES20.glDrawArrays(drawMode, DRAW_FIRST_VERTEX, DRAW_VERTEX_COUNT)

        // 禁用顶点位置属性数组
        // 绘制完成后禁用属性，这是一个好习惯，可以避免影响后续的绘制
        Log.d(TAG, "[GLES20.glDisableVertexAttribArray] index=$positionHandle (vPosition)")
        GLES20.glDisableVertexAttribArray(positionHandle)

        // 解绑缓冲区
        // 恢复默认状态，避免意外修改缓冲区数据
        val UNBIND_BUFFER = 0
        Log.d(TAG, "[GLES20.glBindBuffer] target=GL_ARRAY_BUFFER($ARRAY_BUFFER), buffer=$UNBIND_BUFFER (unbind)")
        GLES20.glBindBuffer(ARRAY_BUFFER, UNBIND_BUFFER)

        // 每帧结束后检查是否有 OpenGL 错误
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
        return when (error) {
            GLES20.GL_INVALID_ENUM -> "GL_INVALID_ENUM - 无效的枚举常量"
            GLES20.GL_INVALID_VALUE -> "GL_INVALID_VALUE - 无效的数值参数"
            GLES20.GL_INVALID_OPERATION -> "GL_INVALID_OPERATION - 无效的操作"
            GLES20.GL_OUT_OF_MEMORY -> "GL_OUT_OF_MEMORY - 内存不足"
            else -> "UNKNOWN_ERROR($error)"
        }
    }

    /**
     * 加载并编译着色器
     * 这是一个辅助方法，用于减少代码重复
     * @param type 着色器类型（GL_VERTEX_SHADER 或 GL_FRAGMENT_SHADER）
     * @param shaderCode 着色器代码（GLSL 语言）
     * @return 着色器 ID，如果失败返回 0
     */
    private fun loadShader(type: Int, shaderCode: String): Int {
        // 创建着色器对象
        // glCreateShader 根据类型创建着色器，返回着色器 ID
        val shaderTypeDescription = if (type == VERTEX_SHADER_TYPE) "GL_VERTEX_SHADER" else "GL_FRAGMENT_SHADER"
        Log.d(TAG, "[GLES20.glCreateShader] type=$type ($shaderTypeDescription)")
        val shader = GLES20.glCreateShader(type)
        Log.d(TAG, "[GLES20.glCreateShader] returns shaderId=$shader (${if (shader > 0) "valid" else "invalid"})")

        // 设置着色器源代码
        // glShaderSource 将 GLSL 代码上传到 GPU
        val codeLength = shaderCode.length
        Log.d(TAG, "[GLES20.glShaderSource] shaderId=$shader, shaderCode length=$codeLength chars")
        GLES20.glShaderSource(shader, shaderCode)

        // 编译着色器
        // glCompileShader 将 GLSL 代码编译成 GPU 可执行的机器码
        Log.d(TAG, "[GLES20.glCompileShader] shaderId=$shader")
        GLES20.glCompileShader(shader)

        // 检查编译状态
        // 编译可能失败（语法错误、不支持的特性等），必须检查结果
        val compileStatus = IntArray(1) // 创建数组存储编译状态

        // glGetShaderiv 获取着色器的信息
        // GL_COMPILE_STATUS 表示获取编译状态
        Log.d(TAG, "[GLES20.glGetShaderiv] shaderId=$shader, pname=GL_COMPILE_STATUS($COMPILE_STATUS)")
        GLES20.glGetShaderiv(shader, COMPILE_STATUS, compileStatus, 0)
        Log.d(TAG, "[GLES20.glGetShaderiv] returns compileStatus=${compileStatus[0]} (GL_TRUE=$TRUE, GL_FALSE=$FALSE)")

        // 如果编译失败（GL_TRUE 表示成功）
        if (compileStatus[0] != TRUE) {
            // 输出错误日志，包含详细的编译错误信息
            // glGetShaderInfoLog 返回编译错误的具体信息
            Log.e(TAG, "[GLES20.glGetShaderInfoLog] shaderId=$shader")
            val shaderLog = GLES20.glGetShaderInfoLog(shader)
            Log.e(TAG, "[GLES20.glGetShaderInfoLog] returns shaderLog=\"$shaderLog\"")
            Log.e(TAG, "无法编译着色器：$shaderLog")
            // 删除失败的着色器，释放资源
            Log.d(TAG, "[GLES20.glDeleteShader] shaderId=$shader")
            GLES20.glDeleteShader(shader)
            // 返回 0 表示失败
            return 0
        }

        // 返回着色器 ID，供后续使用
        Log.d(TAG, "[loadShader] returns shaderId=$shader")
        return shader
    }
}

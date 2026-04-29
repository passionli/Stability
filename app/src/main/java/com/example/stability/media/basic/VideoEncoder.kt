package com.example.stability.media.basic

import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.MediaMuxer
import android.os.Build
import android.util.Log
import android.view.Surface
import java.io.File
import java.io.IOException
import java.nio.ByteBuffer

/**
 * ================================================
 * 视频编码器示例
 * ================================================
 * 这是一个完整的视频编码器实现，演示了如何使用 MediaCodec
 * 将原始视频帧编码为 H.264 格式并保存为 MP4 文件。
 *
 * 核心流程：
 * 1. 创建并配置 MediaCodec 编码器
 * 2. 创建 MediaMuxer 用于封装 MP4 文件
 * 3. 获取输入 Surface 用于绘制帧数据
 * 4. 循环处理编码输出
 * 5. 将编码后的数据写入 MP4 文件
 *
 * @author MediaCodec Learning
 * @version 1.0
 */
class VideoEncoder {

    private val TAG = "VideoEncoder"

    // MediaCodec 编码器实例
    private var encoder: MediaCodec? = null

    // MediaMuxer 用于封装输出文件
    private var muxer: MediaMuxer? = null

    // 视频轨道索引
    private var trackIndex = -1

    // 编码是否已开始
    private var isStarted = false

    // 是否正在编码
    private var isEncoding = false

    /**
     * 初始化编码器
     *
     * @param outputPath 输出 MP4 文件路径
     * @param width 视频宽度
     * @param height 视频高度
     * @param bitRate 比特率（bps），默认 5Mbps
     * @param frameRate 帧率（fps），默认 30fps
     * @param iFrameInterval 关键帧间隔（秒），默认 2 秒
     * @throws IOException 如果文件无法创建或编码器无法初始化
     */
    @Throws(IOException::class)
    fun initialize(
        outputPath: String,
        width: Int = 1280,
        height: Int = 720,
        bitRate: Int = 5_000_000,
        frameRate: Int = 30,
        iFrameInterval: Int = 2
    ) {
        Log.d(TAG, "初始化编码器，输出路径: $outputPath")
        Log.d(TAG, "参数: width=$width, height=$height, bitRate=$bitRate, frameRate=$frameRate")

        // 确保输出目录存在
        val outputFile = File(outputPath)
        outputFile.parentFile?.mkdirs()

        // 步骤1: 创建编码格式
        val format = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, width, height).apply {
            // 设置比特率（越高视频质量越好，但文件越大）
            setInteger(MediaFormat.KEY_BIT_RATE, bitRate)

            // 设置帧率
            setInteger(MediaFormat.KEY_FRAME_RATE, frameRate)

            // 设置关键帧间隔（I帧间隔）
            // 关键帧是完整帧，可用于随机访问
            setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, iFrameInterval)

            // 设置颜色格式
            // COLOR_FormatSurface 表示使用 Surface 作为输入，零拷贝
            setInteger(
                MediaFormat.KEY_COLOR_FORMAT,
                MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface
            )

            // 可选：设置码率控制模式
            // MediaCodecInfo.EncoderCapabilities.BITRATE_MODE_CBR - 恒定码率
            // MediaCodecInfo.EncoderCapabilities.BITRATE_MODE_VBR - 可变码率
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                setInteger(MediaFormat.KEY_BITRATE_MODE, MediaCodecInfo.EncoderCapabilities.BITRATE_MODE_CBR)
            }
        }

        // 步骤2: 创建编码器
        try {
            encoder = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC)
        } catch (e: IOException) {
            Log.e(TAG, "创建编码器失败", e)
            throw e
        }

        // 步骤3: 配置编码器
        // 参数说明：
        // - format: 输出媒体格式
        // - surface: 输出 Surface，编码器不需要输出 Surface（null）
        // - crypto: 用于加密的 MediaCrypto，null 表示不加密
        // - flags: MediaCodec.CONFIGURE_FLAG_ENCODE 表示编码模式
        encoder!!.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)

        // 步骤4: 创建 MediaMuxer
        // MediaMuxer 用于将编码后的音视频数据混合成 MP4 文件
        muxer = MediaMuxer(outputPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)

        // 步骤5: 启动编码器
        encoder!!.start()

        Log.d(TAG, "编码器初始化完成")
    }

    /**
     * 获取输入 Surface
     *
     * 调用者可以通过此 Surface 绘制帧数据，编码器会自动读取并编码。
     *
     * @return 用于绘制的 Surface，如果编码器未初始化则返回 null
     */
    fun getInputSurface(): Surface? {
        return encoder?.createInputSurface()
    }

    /**
     * 编码一帧数据
     *
     * @param presentationTimeUs 帧的显示时间戳（微秒）
     */
    fun encodeFrame(presentationTimeUs: Long) {
        if (!isEncoding) {
            isEncoding = true
        }

        // 通知编码器有新帧可用
        // 此方法用于 Surface 输入模式，表示已完成一帧的绘制
        encoder?.signalEndOfInputStream()

        // 处理编码输出
        drainEncoder(false)
    }

    /**
     * 完成编码
     *
     * 调用此方法后，编码器会处理所有剩余的数据并写入文件。
     */
    fun finish() {
        Log.d(TAG, "完成编码")

        // 发送 EOS 信号，表示没有更多输入数据
        encoder?.signalEndOfInputStream()

        // 处理所有剩余的编码数据
        drainEncoder(true)

        isEncoding = false
        Log.d(TAG, "编码完成")
    }

    /**
     * 处理编码器输出
     *
     * @param endOfStream 是否到达流末尾
     */
    private fun drainEncoder(endOfStream: Boolean) {
        val bufferInfo = MediaCodec.BufferInfo()
        var outputBufferIndex = encoder!!.dequeueOutputBuffer(bufferInfo, 10000)

        Log.v(TAG, "处理编码器输出，索引: $outputBufferIndex")

        while (outputBufferIndex >= 0) {
            val outputBuffer = encoder!!.getOutputBuffer(outputBufferIndex)

            if (outputBuffer != null) {
                // 检查是否是编解码器配置数据（SPS/PPS）
                if (bufferInfo.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG != 0) {
                    // 配置数据不需要写入文件，它会在第一次输出格式改变时自动处理
                    Log.v(TAG, "跳过配置数据")
                    bufferInfo.size = 0
                }

                if (bufferInfo.size > 0) {
                    // 调整缓冲区位置，确保读取正确的数据
                    outputBuffer.position(bufferInfo.offset)
                    outputBuffer.limit(bufferInfo.offset + bufferInfo.size)

                    if (!isStarted) {
                        // 第一次输出有效数据，需要添加轨道
                        val format = encoder!!.outputFormat
                        trackIndex = muxer!!.addTrack(format)
                        muxer!!.start()
                        isStarted = true

                        Log.d(TAG, "添加视频轨道，索引: $trackIndex")
                        logFormatInfo(format)
                    }

                    // 将编码后的数据写入 MP4 文件
                    muxer!!.writeSampleData(trackIndex, outputBuffer, bufferInfo)

                    Log.v(TAG, "写入帧: 大小=${bufferInfo.size}, " +
                            "时间戳=${bufferInfo.presentationTimeUs}, " +
                            "标志=${bufferInfo.flags}")
                }

                // 释放输出缓冲区
                // 参数说明：
                // - index: 缓冲区索引
                // - render: 是否渲染到 Surface（编码器不需要渲染，设为 false）
                encoder!!.releaseOutputBuffer(outputBufferIndex, false)
            }

            // 获取下一个输出缓冲区
            outputBufferIndex = encoder!!.dequeueOutputBuffer(bufferInfo, 0)
        }

        // 检查是否到达末尾
        if (endOfStream) {
            // 刷新编码器，确保所有数据都被处理
            encoder!!.flush()
        }
    }

    /**
     * 释放所有资源
     */
    fun release() {
        Log.d(TAG, "释放编码器资源")

        // 停止并释放编码器
        encoder?.apply {
            try {
                stop()
                release()
            } catch (e: Exception) {
                Log.e(TAG, "释放编码器失败", e)
            }
        }
        encoder = null

        // 停止并释放 muxer
        muxer?.apply {
            try {
                stop()
                release()
            } catch (e: Exception) {
                Log.e(TAG, "释放 muxer 失败", e)
            }
        }
        muxer = null

        trackIndex = -1
        isStarted = false

        Log.d(TAG, "资源释放完成")
    }

    /**
     * 打印 MediaFormat 的详细信息
     */
    private fun logFormatInfo(format: MediaFormat) {
        Log.d(TAG, "=== 输出格式信息 ===")
        Log.d(TAG, "MIME: ${format.getString(MediaFormat.KEY_MIME)}")
        Log.d(TAG, "宽度: ${format.getInteger(MediaFormat.KEY_WIDTH)}")
        Log.d(TAG, "高度: ${format.getInteger(MediaFormat.KEY_HEIGHT)}")
        Log.d(TAG, "比特率: ${format.getInteger(MediaFormat.KEY_BIT_RATE)}")
        Log.d(TAG, "帧率: ${format.getInteger(MediaFormat.KEY_FRAME_RATE)}")
        Log.d(TAG, "=== 格式信息结束 ===")
    }

    /**
     * 获取编码器的输出格式
     *
     * @return 当前编码器的输出格式，如果编码器未初始化则返回 null
     */
    fun getOutputFormat(): MediaFormat? {
        return encoder?.outputFormat
    }

    /**
     * 检查编码器是否正在运行
     *
     * @return 如果正在编码返回 true，否则返回 false
     */
    fun isEncoding(): Boolean {
        return isEncoding
    }
}
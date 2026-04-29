package com.example.stability.media.basic

import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.os.Build
import android.util.Log
import android.view.Surface
import java.io.IOException
import java.nio.ByteBuffer

/**
 * ================================================
 * 视频解码器示例
 * ================================================
 * 这是一个完整的视频解码器实现，演示了如何使用 MediaCodec
 * 解码视频文件并渲染到 Surface。
 *
 * 核心流程：
 * 1. 创建 MediaExtractor 提取视频数据
 * 2. 找到视频轨道并获取格式信息
 * 3. 创建并配置 MediaCodec 解码器
 * 4. 循环处理输入输出缓冲区
 * 5. 渲染解码后的帧到 Surface
 *
 * @author MediaCodec Learning
 * @version 1.0
 */
class VideoDecoder {

    private val TAG = "VideoDecoder"

    // MediaCodec 解码器实例
    private var decoder: MediaCodec? = null

    // MediaExtractor 用于提取音视频数据
    private var extractor: MediaExtractor? = null

    // 用于渲染的 Surface
    private var surface: Surface? = null

    // 是否到达流末尾
    private var isEOS = false

    /**
     * 初始化解码器
     *
     * @param inputPath 输入视频文件路径
     * @param outputSurface 用于渲染解码后视频帧的 Surface
     * @throws IllegalArgumentException 如果未找到视频轨道
     * @throws IOException 如果文件无法读取
     */
    @Throws(IllegalArgumentException::class, IOException::class)
    fun initialize(inputPath: String, outputSurface: Surface) {
        Log.d(TAG, "初始化解码器，输入路径: $inputPath")

        // 保存 Surface 引用
        this.surface = outputSurface

        // 步骤1: 创建 MediaExtractor 并设置数据源
        extractor = MediaExtractor().apply {
            setDataSource(inputPath)
        }

        // 步骤2: 遍历所有轨道，找到视频轨道
        var videoTrackIndex = -1
        for (i in 0 until extractor!!.trackCount) {
            val format = extractor!!.getTrackFormat(i)
            val mimeType = format.getString(MediaFormat.KEY_MIME)

            Log.d(TAG, "轨道 $i: $mimeType")

            if (mimeType?.startsWith("video/") == true) {
                videoTrackIndex = i
                break
            }
        }

        // 检查是否找到视频轨道
        if (videoTrackIndex < 0) {
            release()
            throw IllegalArgumentException("未找到视频轨道")
        }

        // 步骤3: 选择视频轨道
        extractor!!.selectTrack(videoTrackIndex)
        val videoFormat = extractor!!.getTrackFormat(videoTrackIndex)

        // 打印视频格式信息
        logFormatInfo(videoFormat)

        // 步骤4: 获取 MIME 类型并创建解码器
        val mimeType = videoFormat.getString(MediaFormat.KEY_MIME) ?: "video/avc"

        try {
            decoder = MediaCodec.createDecoderByType(mimeType)
        } catch (e: IOException) {
            Log.e(TAG, "创建解码器失败: $mimeType", e)
            release()
            throw e
        }

        // 步骤5: 配置解码器
        // 参数说明：
        // - format: 输入媒体格式
        // - surface: 输出 Surface，解码后的帧将渲染到这里
        // - crypto: 用于解密的 MediaCrypto，null 表示不解密
        // - flags: 0 表示解码模式
        decoder!!.configure(videoFormat, surface, null, 0)

        // 步骤6: 启动解码器
        decoder!!.start()

        Log.d(TAG, "解码器初始化完成")
    }

    /**
     * 开始解码循环
     *
     * 这是解码器的核心方法，包含两个主要阶段：
     * 1. 输入阶段：从提取器读取数据并送入解码器
     * 2. 输出阶段：获取解码后的帧并渲染到 Surface
     */
    fun startDecoding() {
        Log.d(TAG, "开始解码")

        // BufferInfo 用于存储输出缓冲区的元数据
        val bufferInfo = MediaCodec.BufferInfo()
        isEOS = false

        // 解码循环，直到到达流末尾
        while (!isEOS) {
            // ========== 阶段1: 输入处理 ==========
            if (!isEOS) {
                // 获取可用的输入缓冲区索引
                // 参数: timeoutUs - 超时时间（微秒），-1 表示无限等待
                val inputBufferIndex = decoder!!.dequeueInputBuffer(10000) // 10ms 超时

                Log.v(TAG, "输入缓冲区索引: $inputBufferIndex")

                if (inputBufferIndex >= 0) {
                    // 获取实际的输入缓冲区
                    val inputBuffer = decoder!!.getInputBuffer(inputBufferIndex)

                    if (inputBuffer != null) {
                        // 从提取器读取数据到缓冲区
                        val sampleSize = extractor!!.readSampleData(inputBuffer, 0)

                        if (sampleSize < 0) {
                            // 数据读取完毕，发送 EOS（End of Stream）信号
                            Log.d(TAG, "输入数据读取完毕，发送 EOS")
                            decoder!!.queueInputBuffer(
                                inputBufferIndex,
                                0,      // offset - 数据在缓冲区中的偏移量
                                0,      // size - 数据大小
                                0,      // presentationTimeUs - 显示时间戳
                                MediaCodec.BUFFER_FLAG_END_OF_STREAM // flags - EOS 标志
                            )
                            isEOS = true
                        } else {
                            // 获取当前样本的时间戳
                            val presentationTimeUs = extractor!!.sampleTime

                            // 将数据送入解码器
                            decoder!!.queueInputBuffer(
                                inputBufferIndex,
                                0,
                                sampleSize,
                                presentationTimeUs,
                                0 // 无特殊标志
                            )

                            // 移动到下一个样本
                            extractor!!.advance()

                            Log.v(TAG, "送入解码器: 大小=$sampleSize, 时间戳=$presentationTimeUs")
                        }
                    }
                }
            }

            // ========== 阶段2: 输出处理 ==========
            // 获取解码后的输出缓冲区索引
            val outputBufferIndex = decoder!!.dequeueOutputBuffer(bufferInfo, 10000)

            Log.v(TAG, "输出缓冲区索引: $outputBufferIndex")

            when (outputBufferIndex) {
                // 没有可用的输出缓冲区，稍后重试
                MediaCodec.INFO_TRY_AGAIN_LATER -> {
                    Log.v(TAG, "暂无输出，稍后重试")
                }

                // 输出格式发生变化（通常在第一次输出前触发）
                MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> {
                    val newFormat = decoder!!.outputFormat
                    Log.d(TAG, "输出格式变化: $newFormat")
                    logFormatInfo(newFormat)
                }

                // 其他负值为未定义状态
                else -> {
                    if (outputBufferIndex >= 0) {
                        // 检查是否到达流末尾
                        if (bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) {
                            Log.d(TAG, "输出数据读取完毕")
                            isEOS = true
                        }

                        // 渲染到 Surface
                        // 参数说明：
                        // - index: 缓冲区索引
                        // - render: 是否渲染到 Surface（true = 渲染并自动释放，false = 不渲染但释放）
                        // - timestamp: 显示时间戳（可选，通常使用 bufferInfo 中的时间戳）
                        decoder!!.releaseOutputBuffer(outputBufferIndex, true)

                        Log.v(TAG, "渲染帧: 时间戳=${bufferInfo.presentationTimeUs}, " +
                                "大小=${bufferInfo.size}, 标志=${bufferInfo.flags}")
                    }
                }
            }
        }

        Log.d(TAG, "解码完成")
    }

    /**
     * 停止解码并释放所有资源
     *
     * 必须在使用完解码器后调用此方法，否则会导致资源泄漏。
     */
    fun release() {
        Log.d(TAG, "释放解码器资源")

        // 停止并释放解码器
        decoder?.apply {
            try {
                stop()
                release()
            } catch (e: Exception) {
                Log.e(TAG, "释放解码器失败", e)
            }
        }
        decoder = null

        // 释放提取器
        extractor?.apply {
            try {
                release()
            } catch (e: Exception) {
                Log.e(TAG, "释放提取器失败", e)
            }
        }
        extractor = null

        // 清除 Surface 引用
        surface = null

        Log.d(TAG, "资源释放完成")
    }

    /**
     * 打印 MediaFormat 的详细信息
     *
     * @param format 要打印的媒体格式
     */
    private fun logFormatInfo(format: MediaFormat) {
        Log.d(TAG, "=== 媒体格式信息 ===")
        Log.d(TAG, "MIME: ${format.getString(MediaFormat.KEY_MIME)}")
        Log.d(TAG, "宽度: ${format.getInteger(MediaFormat.KEY_WIDTH)}")
        Log.d(TAG, "高度: ${format.getInteger(MediaFormat.KEY_HEIGHT)}")
        Log.d(TAG, "比特率: ${format.getInteger(MediaFormat.KEY_BIT_RATE)}")
        Log.d(TAG, "帧率: ${format.getInteger(MediaFormat.KEY_FRAME_RATE)}")
        Log.d(TAG, "关键帧间隔: ${format.getInteger(MediaFormat.KEY_I_FRAME_INTERVAL)}")
        Log.d(TAG, "时长: ${format.getLong(MediaFormat.KEY_DURATION)} us")
        Log.d(TAG, "=== 格式信息结束 ===")
    }

    /**
     * 检查设备是否支持指定的编解码器
     *
     * @param mimeType MIME 类型，如 "video/avc"
     * @param isEncoder 是否检查编码器（true）还是解码器（false）
     * @return 如果支持返回 true，否则返回 false
     */
    fun isCodecSupported(mimeType: String, isEncoder: Boolean): Boolean {
        return try {
            val codecList = android.media.MediaCodecList(android.media.MediaCodecList.ALL_CODECS)
            for (codecInfo in codecList.codecInfos) {
                if (codecInfo.isEncoder == isEncoder && codecInfo.supportedTypes.contains(mimeType)) {
                    return true
                }
            }
            false
        } catch (e: IllegalArgumentException) {
            false
        }
    }
}
package com.example.stability.video_edit

import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMuxer
import android.os.Build
import android.util.Log
import java.io.File
import java.nio.ByteBuffer

/**
 * ================================================
 * MP4 文件解码和编码详细教程
 * ================================================
 * 本教程演示如何使用 Android 的 MediaCodec API
 * 进行 MP4 文件的解码和编码操作
 *
 * 作者：新手学习
 * 版本：1.0
 * ================================================
 */

/**
 * 步骤 1：了解 MP4 文件结构
 * MP4 文件是一个容器格式，通常包含：
 * - 视频轨道（通常是 H.264 或 H.265 编码）
 * - 音频轨道（通常是 AAC 编码）
 * - 元数据（如时长、帧率等信息）
 */

/**
 * 步骤 2：MediaExtractor 的作用
 * MediaExtractor 用于从 MP4 文件中提取各个轨道的数据
 */

/**
 * 步骤 3：MediaCodec 的作用
 * MediaCodec 是 Android 的编解码器 API，用于：
 * - 解码（Decode）：将压缩的视频/音频数据转换为原始数据
 * - 编码（Encode）：将原始数据转换为压缩格式
 */

/**
 * 步骤 4：完整的解码示例
 */
class Mp4DecoderTutorial {

    private val TAG = "Mp4DecoderTutorial"

    /**
     * 解码 MP4 文件的完整流程
     * @param inputFilePath 输入的 MP4 文件路径
     */
    fun decodeMp4File(inputFilePath: String) {
        Log.d(TAG, "开始解码 MP4 文件: $inputFilePath")

        var mediaExtractor: MediaExtractor? = null
        var videoCodec: MediaCodec? = null
        var audioCodec: MediaCodec? = null

        try {
            // ================================================
            // 第 1 步：创建 MediaExtractor 并设置数据源
            // ================================================
            mediaExtractor = MediaExtractor()
            mediaExtractor.setDataSource(inputFilePath)

            Log.d(TAG, "轨道数量: ${mediaExtractor.trackCount}")

            // ================================================
            // 第 2 步：遍历轨道，找到视频和音频轨道
            // ================================================
            var videoTrackIndex = -1
            var audioTrackIndex = -1

            for (i in 0 until mediaExtractor.trackCount) {
                val format = mediaExtractor.getTrackFormat(i)
                val mime = format.getString(MediaFormat.KEY_MIME) ?: ""

                Log.d(TAG, "轨道 $i: MIME 类型 = $mime")

                // 判断是视频轨道还是音频轨道
                if (mime.startsWith("video/")) {
                    videoTrackIndex = i
                    Log.d(TAG, "找到视频轨道: $i")
                } else if (mime.startsWith("audio/")) {
                    audioTrackIndex = i
                    Log.d(TAG, "找到音频轨道: $i")
                }
            }

            // ================================================
            // 第 3 步：处理视频轨道
            // ================================================
            if (videoTrackIndex >= 0) {
                mediaExtractor.selectTrack(videoTrackIndex)
                val videoFormat = mediaExtractor.getTrackFormat(videoTrackIndex)

                Log.d(TAG, "视频格式: $videoFormat")

                // 创建视频解码器
                videoCodec = createVideoDecoder(videoFormat)

                // 开始解码视频
                decodeVideoTrack(mediaExtractor, videoCodec, videoTrackIndex)
            }

            // ================================================
            // 第 4 步：处理音频轨道
            // ================================================
            if (audioTrackIndex >= 0) {
                mediaExtractor.selectTrack(audioTrackIndex)
                val audioFormat = mediaExtractor.getTrackFormat(audioTrackIndex)

                Log.d(TAG, "音频格式: $audioFormat")

                // 创建音频解码器
                audioCodec = createAudioDecoder(audioFormat)

                // 开始解码音频
                decodeAudioTrack(mediaExtractor, audioCodec, audioTrackIndex)
            }

            Log.d(TAG, "MP4 文件解码完成！")

        } catch (e: Exception) {
            Log.e(TAG, "解码过程中发生错误", e)
        } finally {
            // ================================================
            // 第 5 步：释放资源（非常重要！）
            // ================================================
            videoCodec?.stop()
            videoCodec?.release()
            audioCodec?.stop()
            audioCodec?.release()
            mediaExtractor?.release()
        }
    }

    /**
     * 创建视频解码器
     */
    private fun createVideoDecoder(format: MediaFormat): MediaCodec {
        // 获取 MIME 类型（如 "video/avc" 表示 H.264）
        val mime = format.getString(MediaFormat.KEY_MIME) ?: throw IllegalArgumentException("MIME 类型为空")

        // 创建解码器
        val decoder = MediaCodec.createDecoderByType(mime)

        // 配置解码器
        decoder.configure(format, null, null, 0)

        // 启动解码器
        decoder.start()

        Log.d(TAG, "视频解码器创建成功: $mime")

        return decoder
    }

    /**
     * 创建音频解码器
     */
    private fun createAudioDecoder(format: MediaFormat): MediaCodec {
        // 获取 MIME 类型（如 "audio/mp4a-latm" 表示 AAC）
        val mime = format.getString(MediaFormat.KEY_MIME) ?: throw IllegalArgumentException("MIME 类型为空")

        // 创建解码器
        val decoder = MediaCodec.createDecoderByType(mime)

        // 配置解码器
        decoder.configure(format, null, null, 0)

        // 启动解码器
        decoder.start()

        Log.d(TAG, "音频解码器创建成功: $mime")

        return decoder
    }

    /**
     * 解码视频轨道
     */
    private fun decodeVideoTrack(
        extractor: MediaExtractor,
        codec: MediaCodec,
        trackIndex: Int
    ) {
        Log.d(TAG, "开始解码视频轨道...")

        val bufferInfo = MediaCodec.BufferInfo()
        var frameCount = 0
        val timeoutUs = 10000L // 超时时间 10 毫秒

        try {
            var inputDone = false
            var outputDone = false

            while (!outputDone) {
                // ================================================
                // 第一部分：将数据送入解码器（输入缓冲区）
                // ================================================
                if (!inputDone) {
                    // 获取一个可用的输入缓冲区索引
                    val inputBufferIndex = codec.dequeueInputBuffer(timeoutUs)

                    if (inputBufferIndex >= 0) {
                        // 获取输入缓冲区
                        val inputBuffer = codec.getInputBuffer(inputBufferIndex)

                        if (inputBuffer != null) {
                            // 从 MediaExtractor 读取数据
                            val sampleSize = extractor.readSampleData(inputBuffer, 0)

                            if (sampleSize < 0) {
                                // 没有更多数据了，通知解码器
                                codec.queueInputBuffer(
                                    inputBufferIndex,
                                    0,
                                    0,
                                    0L,
                                    MediaCodec.BUFFER_FLAG_END_OF_STREAM
                                )
                                inputDone = true
                                Log.d(TAG, "视频输入结束")
                            } else {
                                // 将数据送入解码器
                                codec.queueInputBuffer(
                                    inputBufferIndex,
                                    0,
                                    sampleSize,
                                    extractor.sampleTime,
                                    extractor.sampleFlags
                                )

                                // 移动到下一个样本
                                extractor.advance()
                            }
                        }
                    }
                }

                // ================================================
                // 第二部分：从解码器获取解码后的数据（输出缓冲区）
                // ================================================
                val outputBufferIndex = codec.dequeueOutputBuffer(bufferInfo, timeoutUs)

                when (outputBufferIndex) {
                    MediaCodec.INFO_TRY_AGAIN_LATER -> {
                        // 没有可用的输出，稍后再试
                        // 如果输入完成且没有输出，说明解码完成
                    }
                    MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> {
                        // 输出格式改变了（通常发生在开始时）
                        val newFormat = codec.outputFormat
                        Log.d(TAG, "视频输出格式改变: $newFormat")
                    }
                    MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED -> {
                        // 输出缓冲区改变了（旧版本 Android 可能会遇到）
                        Log.d(TAG, "视频输出缓冲区改变")
                    }
                    else -> {
                        // 有解码好的帧可以处理了
                        val outputBuffer = codec.getOutputBuffer(outputBufferIndex)

                        if (outputBuffer != null) {
                            // 在这里处理解码后的视频帧
                            // 通常是渲染到 Surface 或保存为 YUV 数据
                            frameCount++

                            if (frameCount % 30 == 0) {
                                Log.d(TAG, "已解码 $frameCount 帧视频")
                            }
                        }

                        // 释放输出缓冲区
                        codec.releaseOutputBuffer(outputBufferIndex, false)

                        // 检查是否到达流末尾
                        if ((bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                            outputDone = true
                            Log.d(TAG, "视频解码完成，共 $frameCount 帧")
                        }
                    }
                }
            }

        } catch (e: Exception) {
            Log.e(TAG, "视频解码过程出错", e)
        }
    }

    /**
     * 解码音频轨道（流程与视频类似）
     */
    private fun decodeAudioTrack(
        extractor: MediaExtractor,
        codec: MediaCodec,
        trackIndex: Int
    ) {
        Log.d(TAG, "开始解码音频轨道...")

        val bufferInfo = MediaCodec.BufferInfo()
        var sampleCount = 0
        val timeoutUs = 10000L

        try {
            var inputDone = false
            var outputDone = false

            while (!outputDone) {
                // 处理输入
                if (!inputDone) {
                    val inputBufferIndex = codec.dequeueInputBuffer(timeoutUs)
                    if (inputBufferIndex >= 0) {
                        val inputBuffer = codec.getInputBuffer(inputBufferIndex)
                        if (inputBuffer != null) {
                            val sampleSize = extractor.readSampleData(inputBuffer, 0)
                            if (sampleSize < 0) {
                                codec.queueInputBuffer(
                                    inputBufferIndex, 0, 0, 0L,
                                    MediaCodec.BUFFER_FLAG_END_OF_STREAM
                                )
                                inputDone = true
                            } else {
                                codec.queueInputBuffer(
                                    inputBufferIndex, 0, sampleSize,
                                    extractor.sampleTime, extractor.sampleFlags
                                )
                                extractor.advance()
                            }
                        }
                    }
                }

                // 处理输出
                val outputBufferIndex = codec.dequeueOutputBuffer(bufferInfo, timeoutUs)
                when (outputBufferIndex) {
                    MediaCodec.INFO_TRY_AGAIN_LATER -> {}
                    MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> {
                        Log.d(TAG, "音频输出格式改变: ${codec.outputFormat}")
                    }
                    MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED -> {}
                    else -> {
                        val outputBuffer = codec.getOutputBuffer(outputBufferIndex)
                        if (outputBuffer != null) {
                            // 处理解码后的音频数据
                            sampleCount++
                        }
                        codec.releaseOutputBuffer(outputBufferIndex, false)

                        if ((bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                            outputDone = true
                            Log.d(TAG, "音频解码完成")
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "音频解码过程出错", e)
        }
    }
}

/**
 * ================================================
 * MP4 文件编码教程
 * ================================================
 * 将原始视频/音频数据编码为 MP4 文件
 */
class Mp4EncoderTutorial {

    private val TAG = "Mp4EncoderTutorial"

    /**
     * 编码 MP4 文件的完整流程
     * @param outputFilePath 输出的 MP4 文件路径
     * @param width 视频宽度
     * @param height 视频高度
     * @param durationMs 视频时长（毫秒）
     */
    fun encodeMp4File(
        outputFilePath: String,
        width: Int,
        height: Int,
        durationMs: Long
    ) {
        Log.d(TAG, "开始编码 MP4 文件: $outputFilePath")

        var videoCodec: MediaCodec? = null
        var audioCodec: MediaCodec? = null
        var muxer: MediaMuxer? = null

        try {
            // ================================================
            // 第 1 步：创建 MediaMuxer（混合器）
            // ================================================
            muxer = MediaMuxer(outputFilePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)

            // ================================================
            // 第 2 步：创建视频编码器
            // ================================================
            val videoFormat = createVideoFormat(width, height)
            videoCodec = createVideoEncoder(videoFormat)

            // 获取视频轨道索引（在编码器输出格式改变后）
            var videoTrackIndex = -1

            // ================================================
            // 第 3 步：创建音频编码器
            // ================================================
            val audioFormat = createAudioFormat()
            audioCodec = createAudioEncoder(audioFormat)

            // 获取音频轨道索引
            var audioTrackIndex = -1

            // ================================================
            // 第 4 步：开始编码
            // ================================================
            encodeVideoAndAudio(
                videoCodec,
                audioCodec,
                muxer,
                durationMs,
                onVideoFormatChanged = { format ->
                    videoTrackIndex = muxer.addTrack(format)
                    Log.d(TAG, "添加视频轨道，索引: $videoTrackIndex")
                },
                onAudioFormatChanged = { format ->
                    audioTrackIndex = muxer.addTrack(format)
                    Log.d(TAG, "添加音频轨道，索引: $audioTrackIndex")
                },
                onAllFormatsReady = {
                    // 所有轨道格式都准备好了，启动混合器
                    muxer.start()
                    Log.d(TAG, "MediaMuxer 已启动")
                },
                onVideoSampleReady = { buffer, info ->
                    // 写入视频样本
                    if (videoTrackIndex >= 0) {
                        muxer.writeSampleData(videoTrackIndex, buffer, info)
                    }
                },
                onAudioSampleReady = { buffer, info ->
                    // 写入音频样本
                    if (audioTrackIndex >= 0) {
                        muxer.writeSampleData(audioTrackIndex, buffer, info)
                    }
                }
            )

            Log.d(TAG, "MP4 文件编码完成！")

        } catch (e: Exception) {
            Log.e(TAG, "编码过程中发生错误", e)
        } finally {
            // ================================================
            // 第 5 步：释放资源
            // ================================================
            videoCodec?.stop()
            videoCodec?.release()
            audioCodec?.stop()
            audioCodec?.release()
            muxer?.stop()
            muxer?.release()
        }
    }

    /**
     * 创建视频格式
     */
    private fun createVideoFormat(width: Int, height: Int): MediaFormat {
        // 使用 H.264 编码
        val format = MediaFormat.createVideoFormat(
            MediaFormat.MIMETYPE_VIDEO_AVC,
            width,
            height
        )

        // 设置比特率（每秒位数）
        format.setInteger(MediaFormat.KEY_BIT_RATE, 5000000) // 5 Mbps

        // 设置帧率
        format.setInteger(MediaFormat.KEY_FRAME_RATE, 30)

        // 设置 I 帧间隔（关键帧间隔）
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 2) // 每 2 秒一个关键帧

        // 设置颜色格式（使用 YUV 格式）
        format.setInteger(
            MediaFormat.KEY_COLOR_FORMAT,
            MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface
        )

        Log.d(TAG, "视频格式创建完成: $format")

        return format
    }

    /**
     * 创建音频格式
     */
    private fun createAudioFormat(): MediaFormat {
        // 使用 AAC 编码
        val format = MediaFormat.createAudioFormat(
            MediaFormat.MIMETYPE_AUDIO_AAC,
            44100, // 采样率 44.1kHz
            2 // 立体声
        )

        // 设置比特率
        format.setInteger(MediaFormat.KEY_BIT_RATE, 128000) // 128 kbps

        // 设置 AAC 配置文件
        format.setInteger(
            MediaFormat.KEY_AAC_PROFILE,
            MediaCodecInfo.CodecProfileLevel.AACObjectLC
        )

        Log.d(TAG, "音频格式创建完成: $format")

        return format
    }

    /**
     * 创建视频编码器
     */
    private fun createVideoEncoder(format: MediaFormat): MediaCodec {
        val mime = format.getString(MediaFormat.KEY_MIME) ?: throw IllegalArgumentException("MIME 类型为空")

        // 创建编码器
        val encoder = MediaCodec.createEncoderByType(mime)

        // 配置编码器
        encoder.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)

        // 启动编码器
        encoder.start()

        Log.d(TAG, "视频编码器创建成功: $mime")

        return encoder
    }

    /**
     * 创建音频编码器
     */
    private fun createAudioEncoder(format: MediaFormat): MediaCodec {
        val mime = format.getString(MediaFormat.KEY_MIME) ?: throw IllegalArgumentException("MIME 类型为空")

        // 创建编码器
        val encoder = MediaCodec.createEncoderByType(mime)

        // 配置编码器
        encoder.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)

        // 启动编码器
        encoder.start()

        Log.d(TAG, "音频编码器创建成功: $mime")

        return encoder
    }

    /**
     * 编码视频和音频
     * 注意：在实际应用中，你需要提供真实的视频帧和音频数据
     * 这里演示的是编码的流程
     */
    private fun encodeVideoAndAudio(
        videoCodec: MediaCodec,
        audioCodec: MediaCodec,
        muxer: MediaMuxer,
        durationMs: Long,
        onVideoFormatChanged: (MediaFormat) -> Unit,
        onAudioFormatChanged: (MediaFormat) -> Unit,
        onAllFormatsReady: () -> Unit,
        onVideoSampleReady: (ByteBuffer, MediaCodec.BufferInfo) -> Unit,
        onAudioSampleReady: (ByteBuffer, MediaCodec.BufferInfo) -> Unit
    ) {
        Log.d(TAG, "开始编码视频和音频...")

        val videoBufferInfo = MediaCodec.BufferInfo()
        val audioBufferInfo = MediaCodec.BufferInfo()
        val timeoutUs = 10000L

        // 标志位
        var videoInputDone = false
        var videoOutputDone = false
        var audioInputDone = false
        var audioOutputDone = false
        var videoFormatReady = false
        var audioFormatReady = false
        var muxerStarted = false

        // 模拟的帧计数
        var videoFrameCount = 0L
        val targetVideoFrameCount = (durationMs / 1000L) * 30L // 30fps

        var audioSampleCount = 0L

        try {
            while (!videoOutputDone || !audioOutputDone) {
                // ================================================
                // 处理视频编码
                // ================================================
                if (!videoOutputDone) {
                    // 输入视频数据
                    if (!videoInputDone) {
                        val inputBufferIndex = videoCodec.dequeueInputBuffer(timeoutUs)
                        if (inputBufferIndex >= 0) {
                            val inputBuffer = videoCodec.getInputBuffer(inputBufferIndex)

                            if (inputBuffer != null) {
                                if (videoFrameCount < targetVideoFrameCount) {
                                    // 这里应该填入真实的视频帧数据
                                    // 为了演示，我们模拟输入
                                    val presentationTimeUs = (videoFrameCount * 1000000L) / 30L

                                    // 在实际应用中，这里需要：
                                    // 1. 从某个地方获取原始视频帧（YUV 数据）
                                    // 2. 将数据复制到 inputBuffer
                                    // 3. 设置正确的大小和时间戳

                                    // 这里我们模拟写入一些数据（仅作演示）
                                    val dummyData = ByteArray(0) // 实际应用中这里是真实的视频数据
                                    inputBuffer.put(dummyData)

                                    videoCodec.queueInputBuffer(
                                        inputBufferIndex,
                                        0,
                                        dummyData.size,
                                        presentationTimeUs,
                                        0
                                    )

                                    videoFrameCount++
                                } else {
                                    // 视频输入结束
                                    videoCodec.queueInputBuffer(
                                        inputBufferIndex,
                                        0,
                                        0,
                                        0L,
                                        MediaCodec.BUFFER_FLAG_END_OF_STREAM
                                    )
                                    videoInputDone = true
                                    Log.d(TAG, "视频输入结束")
                                }
                            }
                        }
                    }

                    // 获取视频输出
                    val outputBufferIndex = videoCodec.dequeueOutputBuffer(videoBufferInfo, timeoutUs)
                    when (outputBufferIndex) {
                        MediaCodec.INFO_TRY_AGAIN_LATER -> {}
                        MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> {
                            val newFormat = videoCodec.outputFormat
                            Log.d(TAG, "视频输出格式改变: $newFormat")
                            onVideoFormatChanged(newFormat)
                            videoFormatReady = true
                            if (videoFormatReady && audioFormatReady && !muxerStarted) {
                                onAllFormatsReady()
                                muxerStarted = true
                            }
                        }
                        MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED -> {}
                        else -> {
                            val outputBuffer = videoCodec.getOutputBuffer(outputBufferIndex)
                            if (outputBuffer != null && muxerStarted) {
                                onVideoSampleReady(outputBuffer, videoBufferInfo)
                            }
                            videoCodec.releaseOutputBuffer(outputBufferIndex, false)

                            if ((videoBufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                                videoOutputDone = true
                                Log.d(TAG, "视频编码完成")
                            }
                        }
                    }
                }

                // ================================================
                // 处理音频编码（流程与视频类似）
                // ================================================
                if (!audioOutputDone) {
                    // 输入音频数据
                    if (!audioInputDone) {
                        val inputBufferIndex = audioCodec.dequeueInputBuffer(timeoutUs)
                        if (inputBufferIndex >= 0) {
                            val inputBuffer = audioCodec.getInputBuffer(inputBufferIndex)

                            if (inputBuffer != null) {
                                // 这里应该填入真实的音频数据
                                // 为了演示，我们简化处理
                                audioInputDone = true
                                audioCodec.queueInputBuffer(
                                    inputBufferIndex,
                                    0,
                                    0,
                                    0L,
                                    MediaCodec.BUFFER_FLAG_END_OF_STREAM
                                )
                            }
                        }
                    }

                    // 获取音频输出
                    val outputBufferIndex = audioCodec.dequeueOutputBuffer(audioBufferInfo, timeoutUs)
                    when (outputBufferIndex) {
                        MediaCodec.INFO_TRY_AGAIN_LATER -> {}
                        MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> {
                            val newFormat = audioCodec.outputFormat
                            Log.d(TAG, "音频输出格式改变: $newFormat")
                            onAudioFormatChanged(newFormat)
                            audioFormatReady = true
                            if (videoFormatReady && audioFormatReady && !muxerStarted) {
                                onAllFormatsReady()
                                muxerStarted = true
                            }
                        }
                        MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED -> {}
                        else -> {
                            val outputBuffer = audioCodec.getOutputBuffer(outputBufferIndex)
                            if (outputBuffer != null && muxerStarted) {
                                onAudioSampleReady(outputBuffer, audioBufferInfo)
                            }
                            audioCodec.releaseOutputBuffer(outputBufferIndex, false)

                            if ((audioBufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                                audioOutputDone = true
                                Log.d(TAG, "音频编码完成")
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "编码过程出错", e)
        }
    }
}

/**
 * ================================================
 * 完整的 MP4 文件转码示例
 * ================================================
 * 将一个 MP4 文件转换为另一个 MP4 文件（重新编码）
 * 这个示例结合了解码和编码两个过程
 */
class Mp4TranscoderTutorial {

    private val TAG = "Mp4TranscoderTutorial"

    /**
     * 转码 MP4 文件
     * @param inputFilePath 输入文件路径
     * @param outputFilePath 输出文件路径
     */
    fun transcodeMp4File(inputFilePath: String, outputFilePath: String) {
        Log.d(TAG, "开始转码 MP4 文件")
        Log.d(TAG, "输入文件: $inputFilePath")
        Log.d(TAG, "输出文件: $outputFilePath")

        // 在实际应用中，转码的流程是：
        // 1. 读取输入文件（MediaExtractor）
        // 2. 解码视频和音频（MediaCodec 解码）
        // 3. 可能的处理（如裁剪、滤镜、音频调整等）
        // 4. 重新编码视频和音频（MediaCodec 编码）
        // 5. 混合到输出文件（MediaMuxer）

        Log.d(TAG, "转码过程完成！")

        // 注意：完整的转码实现比较复杂，需要：
        // - 正确处理 PTS（时间戳）
        // - 同步视频和音频
        // - 处理各种格式转换
        // - 性能优化等
    }

    /**
     * 重要提示
     */
    fun getImportantTips(): List<String> {
        return listOf(
            "1. MediaCodec API 在不同设备上的行为可能有差异",
            "2. 确保正确释放所有资源，避免内存泄漏",
            "3. 处理 MediaCodec 的各种状态转换",
            "4. 注意处理 BufferFlags（特别是 END_OF_STREAM）",
            "5. 时间戳的正确处理对于同步很重要",
            "6. 考虑使用 Surface 进行视频渲染，性能更好",
            "7. 对于复杂场景，可以考虑使用第三方库如 FFmpeg",
            "8. 总是在后台线程进行编解码操作",
            "9. 注意检查 MediaFormat 的兼容性",
            "10. 多测试不同设备和不同格式的文件"
        )
    }
}

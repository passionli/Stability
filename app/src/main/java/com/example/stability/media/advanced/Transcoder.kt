package com.example.stability.media.advanced

import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMuxer
import android.util.Log
import java.io.File
import java.io.IOException
import java.nio.ByteBuffer

/**
 * ================================================
 * 视频转码器示例
 * ================================================
 * 这是一个完整的视频转码器实现，支持：
 * 1. 格式转换（如 H.264 转 H.265）
 * 2. 分辨率调整
 * 3. 比特率调整
 * 4. 帧率调整
 * 5. 音视频同步转码
 *
 * 转码流程：
 * 1. 解码输入视频
 * 2. 解码输入音频（如果有）
 * 3. 重新编码视频
 * 4. 重新编码音频（如果有）
 * 5. 混合音视频到输出文件
 *
 * @author MediaCodec Learning
 * @version 1.0
 */
class Transcoder {

    private val TAG = "Transcoder"

    // 转码回调接口
    interface Callback {
        fun onProgress(progress: Float)
        fun onComplete()
        fun onError(error: Exception)
    }

    // 转码配置
    data class Config(
        val inputPath: String,
        val outputPath: String,
        val videoWidth: Int = 1280,
        val videoHeight: Int = 720,
        val videoBitRate: Int = 5_000_000,
        val videoFrameRate: Int = 30,
        val audioBitRate: Int = 128_000,
        val audioSampleRate: Int = 44100,
        val callback: Callback? = null
    )

    /**
     * 执行转码
     */
    fun transcode(config: Config) {
        Log.d(TAG, "开始转码: ${config.inputPath} -> ${config.outputPath}")

        try {
            // 创建提取器
            val extractor = MediaExtractor().apply {
                setDataSource(config.inputPath)
            }

            // 找到音视频轨道
            var videoTrackIndex = -1
            var audioTrackIndex = -1
            var videoFormat: MediaFormat? = null
            var audioFormat: MediaFormat? = null

            for (i in 0 until extractor.trackCount) {
                val format = extractor.getTrackFormat(i)
                val mimeType = format.getString(MediaFormat.KEY_MIME)

                if (mimeType?.startsWith("video/") == true) {
                    videoTrackIndex = i
                    videoFormat = format
                } else if (mimeType?.startsWith("audio/") == true) {
                    audioTrackIndex = i
                    audioFormat = format
                }
            }

            // 创建输出目录
            val outputFile = File(config.outputPath)
            outputFile.parentFile?.mkdirs()

            // 创建 muxer
            val muxer = MediaMuxer(config.outputPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)

            var outputVideoTrackIndex = -1
            var outputAudioTrackIndex = -1

            // 视频转码
            if (videoTrackIndex >= 0 && videoFormat != null) {
                outputVideoTrackIndex = transcodeVideo(
                    extractor,
                    videoTrackIndex,
                    videoFormat,
                    muxer,
                    config
                )
            }

            // 音频转码
            if (audioTrackIndex >= 0 && audioFormat != null) {
                outputAudioTrackIndex = transcodeAudio(
                    extractor,
                    audioTrackIndex,
                    audioFormat,
                    muxer,
                    config
                )
            }

            // 完成
            muxer.stop()
            muxer.release()
            extractor.release()

            config.callback?.onComplete()
            Log.d(TAG, "转码完成")

        } catch (e: Exception) {
            Log.e(TAG, "转码失败", e)
            config.callback?.onError(e)
        }
    }

    /**
     * 转码视频轨道
     */
    private fun transcodeVideo(
        extractor: MediaExtractor,
        inputTrackIndex: Int,
        inputFormat: MediaFormat,
        muxer: MediaMuxer,
        config: Config
    ): Int {
        Log.d(TAG, "开始视频转码")

        // 选择视频轨道
        extractor.selectTrack(inputTrackIndex)

        // 创建视频解码器
        val decoderMimeType = inputFormat.getString(MediaFormat.KEY_MIME) ?: "video/avc"
        val decoder = MediaCodec.createDecoderByType(decoderMimeType)
        decoder.configure(inputFormat, null, null, 0)
        decoder.start()

        // 创建视频编码器
        val encoderFormat = MediaFormat.createVideoFormat(
            MediaFormat.MIMETYPE_VIDEO_AVC,
            config.videoWidth,
            config.videoHeight
        ).apply {
            setInteger(MediaFormat.KEY_BIT_RATE, config.videoBitRate)
            setInteger(MediaFormat.KEY_FRAME_RATE, config.videoFrameRate)
            setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 2)
            setInteger(
                MediaFormat.KEY_COLOR_FORMAT,
                MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface
            )
        }

        val encoder = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC)
        encoder.configure(encoderFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
        encoder.start()

        // 转码循环
        val decoderBufferInfo = MediaCodec.BufferInfo()
        val encoderBufferInfo = MediaCodec.BufferInfo()
        var isEOS = false
        var outputTrackIndex = -1
        var isEncoderStarted = false

        while (!isEOS) {
            // 解码输入
            if (!isEOS) {
                val inputBufferIndex = decoder.dequeueInputBuffer(10000)
                if (inputBufferIndex >= 0) {
                    val inputBuffer = decoder.getInputBuffer(inputBufferIndex)
                    if (inputBuffer != null) {
                        val sampleSize = extractor.readSampleData(inputBuffer, 0)
                        val presentationTimeUs = extractor.sampleTime

                        if (sampleSize < 0) {
                            decoder.queueInputBuffer(
                                inputBufferIndex, 0, 0, 0,
                                MediaCodec.BUFFER_FLAG_END_OF_STREAM
                            )
                            isEOS = true
                        } else {
                            decoder.queueInputBuffer(
                                inputBufferIndex, 0, sampleSize, presentationTimeUs, 0
                            )
                            extractor.advance()
                        }
                    }
                }
            }

            // 获取解码输出
            val decoderOutputIndex = decoder.dequeueOutputBuffer(decoderBufferInfo, 10000)

            when (decoderOutputIndex) {
                MediaCodec.INFO_TRY_AGAIN_LATER -> {
                    // 继续循环
                }
                MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> {
                    // 格式变化
                }
                else -> {
                    if (decoderOutputIndex >= 0) {
                        // 获取解码后的帧
                        val decoderOutputBuffer = decoder.getOutputBuffer(decoderOutputIndex)

                        // 编码输入
                        if (decoderOutputBuffer != null && decoderBufferInfo.size > 0) {
                            val encoderInputIndex = encoder.dequeueInputBuffer(10000)
                            if (encoderInputIndex >= 0) {
                                val encoderInputBuffer = encoder.getInputBuffer(encoderInputIndex)
                                if (encoderInputBuffer != null) {
                                    encoderInputBuffer.clear()
                                    encoderInputBuffer.put(decoderOutputBuffer)

                                    encoder.queueInputBuffer(
                                        encoderInputIndex,
                                        0,
                                        decoderBufferInfo.size,
                                        decoderBufferInfo.presentationTimeUs,
                                        0
                                    )
                                }
                            }
                        }

                        decoder.releaseOutputBuffer(decoderOutputIndex, false)

                        // 获取编码输出
                        var encoderOutputIndex = encoder.dequeueOutputBuffer(encoderBufferInfo, 0)
                        while (encoderOutputIndex >= 0) {
                            val encoderOutputBuffer = encoder.getOutputBuffer(encoderOutputIndex)

                            if (encoderOutputBuffer != null) {
                                if (encoderBufferInfo.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG != 0) {
                                    encoderBufferInfo.size = 0
                                }

                                if (encoderBufferInfo.size > 0) {
                                    encoderOutputBuffer.position(encoderBufferInfo.offset)
                                    encoderOutputBuffer.limit(encoderBufferInfo.offset + encoderBufferInfo.size)

                                    if (!isEncoderStarted) {
                                        val outputFormat = encoder.outputFormat
                                        outputTrackIndex = muxer.addTrack(outputFormat)
                                        muxer.start()
                                        isEncoderStarted = true
                                    }

                                    muxer.writeSampleData(outputTrackIndex, encoderOutputBuffer, encoderBufferInfo)
                                }

                                encoder.releaseOutputBuffer(encoderOutputIndex, false)
                            }

                            encoderOutputIndex = encoder.dequeueOutputBuffer(encoderBufferInfo, 0)
                        }

                        if (decoderBufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) {
                            // 发送 EOS 到编码器
                            val encoderInputIndex = encoder.dequeueInputBuffer(10000)
                            if (encoderInputIndex >= 0) {
                                encoder.queueInputBuffer(
                                    encoderInputIndex, 0, 0, 0,
                                    MediaCodec.BUFFER_FLAG_END_OF_STREAM
                                )
                            }

                            // 处理编码器剩余数据
                            var encoderOutputIndex: Int
                            do {
                                encoderOutputIndex = encoder.dequeueOutputBuffer(encoderBufferInfo, 10000)
                                if (encoderOutputIndex >= 0) {
                                    val encoderOutputBuffer = encoder.getOutputBuffer(encoderOutputIndex)
                                    if (encoderOutputBuffer != null && encoderBufferInfo.size > 0) {
                                        encoderOutputBuffer.position(encoderBufferInfo.offset)
                                        encoderOutputBuffer.limit(encoderBufferInfo.offset + encoderBufferInfo.size)
                                        muxer.writeSampleData(outputTrackIndex, encoderOutputBuffer, encoderBufferInfo)
                                    }
                                    encoder.releaseOutputBuffer(encoderOutputIndex, false)
                                }
                            } while (encoderOutputIndex >= 0)

                            isEOS = true
                        }
                    }
                }
            }
        }

        decoder.stop()
        decoder.release()
        encoder.stop()
        encoder.release()

        Log.d(TAG, "视频转码完成")
        return outputTrackIndex
    }

    /**
     * 转码音频轨道
     */
    private fun transcodeAudio(
        extractor: MediaExtractor,
        inputTrackIndex: Int,
        inputFormat: MediaFormat,
        muxer: MediaMuxer,
        config: Config
    ): Int {
        Log.d(TAG, "开始音频转码")

        // 选择音频轨道
        extractor.selectTrack(inputTrackIndex)

        // 创建音频解码器
        val decoderMimeType = inputFormat.getString(MediaFormat.KEY_MIME) ?: "audio/mp4a-latm"
        val decoder = MediaCodec.createDecoderByType(decoderMimeType)
        decoder.configure(inputFormat, null, null, 0)
        decoder.start()

        // 创建音频编码器
        val encoderFormat = MediaFormat.createAudioFormat(
            MediaFormat.MIMETYPE_AUDIO_AAC,
            config.audioSampleRate,
            2
        ).apply {
            setInteger(MediaFormat.KEY_BIT_RATE, config.audioBitRate)
            setInteger(MediaFormat.KEY_AAC_PROFILE, 2)
        }

        val encoder = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_AUDIO_AAC)
        encoder.configure(encoderFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
        encoder.start()

        // 转码循环
        val decoderBufferInfo = MediaCodec.BufferInfo()
        val encoderBufferInfo = MediaCodec.BufferInfo()
        var isEOS = false
        var outputTrackIndex = -1
        var isEncoderStarted = false

        while (!isEOS) {
            // 解码输入
            if (!isEOS) {
                val inputBufferIndex = decoder.dequeueInputBuffer(10000)
                if (inputBufferIndex >= 0) {
                    val inputBuffer = decoder.getInputBuffer(inputBufferIndex)
                    if (inputBuffer != null) {
                        val sampleSize = extractor.readSampleData(inputBuffer, 0)
                        val presentationTimeUs = extractor.sampleTime

                        if (sampleSize < 0) {
                            decoder.queueInputBuffer(
                                inputBufferIndex, 0, 0, 0,
                                MediaCodec.BUFFER_FLAG_END_OF_STREAM
                            )
                            isEOS = true
                        } else {
                            decoder.queueInputBuffer(
                                inputBufferIndex, 0, sampleSize, presentationTimeUs, 0
                            )
                            extractor.advance()
                        }
                    }
                }
            }

            // 获取解码输出
            val decoderOutputIndex = decoder.dequeueOutputBuffer(decoderBufferInfo, 10000)

            when (decoderOutputIndex) {
                MediaCodec.INFO_TRY_AGAIN_LATER -> {
                    // 继续循环
                }
                MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> {
                    // 格式变化
                }
                else -> {
                    if (decoderOutputIndex >= 0) {
                        // 获取解码后的音频数据
                        val decoderOutputBuffer = decoder.getOutputBuffer(decoderOutputIndex)

                        // 编码输入
                        if (decoderOutputBuffer != null && decoderBufferInfo.size > 0) {
                            val encoderInputIndex = encoder.dequeueInputBuffer(10000)
                            if (encoderInputIndex >= 0) {
                                val encoderInputBuffer = encoder.getInputBuffer(encoderInputIndex)
                                if (encoderInputBuffer != null) {
                                    encoderInputBuffer.clear()
                                    encoderInputBuffer.put(decoderOutputBuffer)

                                    encoder.queueInputBuffer(
                                        encoderInputIndex,
                                        0,
                                        decoderBufferInfo.size,
                                        decoderBufferInfo.presentationTimeUs,
                                        0
                                    )
                                }
                            }
                        }

                        decoder.releaseOutputBuffer(decoderOutputIndex, false)

                        // 获取编码输出
                        var encoderOutputIndex = encoder.dequeueOutputBuffer(encoderBufferInfo, 0)
                        while (encoderOutputIndex >= 0) {
                            val encoderOutputBuffer = encoder.getOutputBuffer(encoderOutputIndex)

                            if (encoderOutputBuffer != null) {
                                if (encoderBufferInfo.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG != 0) {
                                    encoderBufferInfo.size = 0
                                }

                                if (encoderBufferInfo.size > 0) {
                                    encoderOutputBuffer.position(encoderBufferInfo.offset)
                                    encoderOutputBuffer.limit(encoderBufferInfo.offset + encoderBufferInfo.size)

                                    if (!isEncoderStarted) {
                                        val outputFormat = encoder.outputFormat
                                        outputTrackIndex = muxer.addTrack(outputFormat)
                                        // muxer 已经在视频转码中启动了
                                        isEncoderStarted = true
                                    }

                                    muxer.writeSampleData(outputTrackIndex, encoderOutputBuffer, encoderBufferInfo)
                                }

                                encoder.releaseOutputBuffer(encoderOutputIndex, false)
                            }

                            encoderOutputIndex = encoder.dequeueOutputBuffer(encoderBufferInfo, 0)
                        }

                        if (decoderBufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) {
                            // 发送 EOS 到编码器
                            val encoderInputIndex = encoder.dequeueInputBuffer(10000)
                            if (encoderInputIndex >= 0) {
                                encoder.queueInputBuffer(
                                    encoderInputIndex, 0, 0, 0,
                                    MediaCodec.BUFFER_FLAG_END_OF_STREAM
                                )
                            }

                            // 处理编码器剩余数据
                            var encoderOutputIndex: Int
                            do {
                                encoderOutputIndex = encoder.dequeueOutputBuffer(encoderBufferInfo, 10000)
                                if (encoderOutputIndex >= 0) {
                                    val encoderOutputBuffer = encoder.getOutputBuffer(encoderOutputIndex)
                                    if (encoderOutputBuffer != null && encoderBufferInfo.size > 0) {
                                        encoderOutputBuffer.position(encoderBufferInfo.offset)
                                        encoderOutputBuffer.limit(encoderBufferInfo.offset + encoderBufferInfo.size)
                                        muxer.writeSampleData(outputTrackIndex, encoderOutputBuffer, encoderBufferInfo)
                                    }
                                    encoder.releaseOutputBuffer(encoderOutputIndex, false)
                                }
                            } while (encoderOutputIndex >= 0)

                            isEOS = true
                        }
                    }
                }
            }
        }

        decoder.stop()
        decoder.release()
        encoder.stop()
        encoder.release()

        Log.d(TAG, "音频转码完成")
        return outputTrackIndex
    }
}
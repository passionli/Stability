package com.example.stability.media.intermediate

import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMuxer
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.ByteBuffer

/**
 * ================================================
 * 音频编解码器示例
 * ================================================
 * 这是一个完整的音频编解码实现，支持：
 * 1. 音频解码：将压缩音频（如 AAC）解码为 PCM
 * 2. 音频编码：将 PCM 编码为 AAC
 *
 * @author MediaCodec Learning
 * @version 1.0
 */
class AudioCodec {

    private val TAG = "AudioCodec"

    /**
     * ================================================
     * 音频解码器
     * ================================================
     */
    class Decoder {

        private val TAG = "AudioDecoder"

        private var decoder: MediaCodec? = null
        private var extractor: MediaExtractor? = null

        /**
         * 初始化解码器
         *
         * @param inputPath 输入音频文件路径
         * @throws IOException 如果文件无法读取
         */
        @Throws(IOException::class)
        fun initialize(inputPath: String) {
            Log.d(TAG, "初始化音频解码器，输入路径: $inputPath")

            // 创建提取器
            extractor = MediaExtractor().apply {
                setDataSource(inputPath)
            }

            // 找到音频轨道
            var audioTrackIndex = -1
            for (i in 0 until extractor!!.trackCount) {
                val format = extractor!!.getTrackFormat(i)
                val mimeType = format.getString(MediaFormat.KEY_MIME)

                if (mimeType?.startsWith("audio/") == true) {
                    audioTrackIndex = i
                    break
                }
            }

            if (audioTrackIndex < 0) {
                release()
                throw IllegalArgumentException("未找到音频轨道")
            }

            extractor!!.selectTrack(audioTrackIndex)
            val format = extractor!!.getTrackFormat(audioTrackIndex)

            // 打印格式信息
            logFormatInfo(format)

            // 创建解码器
            val mimeType = format.getString(MediaFormat.KEY_MIME) ?: "audio/mp4a-latm"
            decoder = MediaCodec.createDecoderByType(mimeType)

            // 配置解码器
            decoder!!.configure(format, null, null, 0)

            // 启动解码器
            decoder!!.start()

            Log.d(TAG, "音频解码器初始化完成")
        }

        /**
         * 解码音频并保存为 PCM 文件
         *
         * @param outputPcmPath 输出 PCM 文件路径
         */
        fun decodeToPcm(outputPcmPath: String) {
            Log.d(TAG, "开始解码到 PCM，输出路径: $outputPcmPath")

            // 确保输出目录存在
            val outputFile = File(outputPcmPath)
            outputFile.parentFile?.mkdirs()

            val outputStream = FileOutputStream(outputPcmPath)
            val bufferInfo = MediaCodec.BufferInfo()
            var isEOS = false

            while (!isEOS) {
                // 输入阶段
                if (!isEOS) {
                    val inputBufferIndex = decoder!!.dequeueInputBuffer(10000)

                    if (inputBufferIndex >= 0) {
                        val inputBuffer = decoder!!.getInputBuffer(inputBufferIndex)

                        if (inputBuffer != null) {
                            val sampleSize = extractor!!.readSampleData(inputBuffer, 0)

                            if (sampleSize < 0) {
                                // 发送 EOS
                                decoder!!.queueInputBuffer(
                                    inputBufferIndex, 0, 0, 0,
                                    MediaCodec.BUFFER_FLAG_END_OF_STREAM
                                )
                                isEOS = true
                            } else {
                                decoder!!.queueInputBuffer(
                                    inputBufferIndex, 0, sampleSize,
                                    extractor!!.sampleTime, 0
                                )
                                extractor!!.advance()
                            }
                        }
                    }
                }

                // 输出阶段
                val outputBufferIndex = decoder!!.dequeueOutputBuffer(bufferInfo, 10000)

                when (outputBufferIndex) {
                    MediaCodec.INFO_TRY_AGAIN_LATER -> {
                        // 暂无输出
                    }
                    MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> {
                        val newFormat = decoder!!.outputFormat
                        Log.d(TAG, "输出格式变化: $newFormat")
                    }
                    else -> {
                        if (outputBufferIndex >= 0) {
                            val outputBuffer = decoder!!.getOutputBuffer(outputBufferIndex)

                            if (outputBuffer != null && bufferInfo.size > 0) {
                                // 读取解码后的 PCM 数据
                                val pcmData = ByteArray(bufferInfo.size)
                                outputBuffer.position(bufferInfo.offset)
                                outputBuffer.get(pcmData)

                                // 写入文件
                                outputStream.write(pcmData)

                                Log.v(TAG, "解码 PCM: 大小=${bufferInfo.size}")
                            }

                            decoder!!.releaseOutputBuffer(outputBufferIndex, false)

                            if (bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) {
                                isEOS = true
                            }
                        }
                    }
                }
            }

            outputStream.close()
            Log.d(TAG, "PCM 解码完成")
        }

        /**
         * 释放资源
         */
        fun release() {
            decoder?.stop()
            decoder?.release()
            extractor?.release()
            decoder = null
            extractor = null
        }

        private fun logFormatInfo(format: MediaFormat) {
            Log.d(TAG, "=== 音频格式信息 ===")
            Log.d(TAG, "MIME: ${format.getString(MediaFormat.KEY_MIME)}")
            Log.d(TAG, "采样率: ${format.getInteger(MediaFormat.KEY_SAMPLE_RATE)} Hz")
            Log.d(TAG, "声道数: ${format.getInteger(MediaFormat.KEY_CHANNEL_COUNT)}")
            Log.d(TAG, "比特率: ${format.getInteger(MediaFormat.KEY_BIT_RATE)} bps")
            Log.d(TAG, "时长: ${format.getLong(MediaFormat.KEY_DURATION)} us")
            Log.d(TAG, "=== 格式信息结束 ===")
        }
    }

    /**
     * ================================================
     * 音频编码器
     * ================================================
     */
    class Encoder {

        private val TAG = "AudioEncoder"

        private var encoder: MediaCodec? = null
        private var muxer: MediaMuxer? = null
        private var trackIndex = -1
        private var isStarted = false

        /**
         * 初始化编码器
         *
         * @param outputPath 输出 AAC 文件路径（MP4 容器）
         * @param sampleRate 采样率，默认 44100Hz
         * @param channelCount 声道数，默认 2（立体声）
         * @param bitRate 比特率，默认 128000 bps
         * @throws IOException 如果文件无法创建
         */
        @Throws(IOException::class)
        fun initialize(
            outputPath: String,
            sampleRate: Int = 44100,
            channelCount: Int = 2,
            bitRate: Int = 128_000
        ) {
            Log.d(TAG, "初始化音频编码器，输出路径: $outputPath")

            // 确保输出目录存在
            val outputFile = File(outputPath)
            outputFile.parentFile?.mkdirs()

            // 创建编码格式
            val format = MediaFormat.createAudioFormat(
                MediaFormat.MIMETYPE_AUDIO_AAC,
                sampleRate,
                channelCount
            ).apply {
                setInteger(MediaFormat.KEY_BIT_RATE, bitRate)
                // 设置 AAC 配置参数
                setInteger(MediaFormat.KEY_AAC_PROFILE, 2) // AAC LC
            }

            // 创建编码器
            encoder = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_AUDIO_AAC)

            // 配置编码器
            encoder!!.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)

            // 创建 muxer
            muxer = MediaMuxer(outputPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)

            // 启动编码器
            encoder!!.start()

            Log.d(TAG, "音频编码器初始化完成")
        }

        /**
         * 编码 PCM 数据
         *
         * @param pcmData PCM 数据
         * @param presentationTimeUs 时间戳（微秒）
         */
        fun encodePcm(pcmData: ByteArray, presentationTimeUs: Long) {
            // 输入阶段
            val inputBufferIndex = encoder!!.dequeueInputBuffer(10000)

            if (inputBufferIndex >= 0) {
                val inputBuffer = encoder!!.getInputBuffer(inputBufferIndex)

                if (inputBuffer != null) {
                    inputBuffer.clear()
                    inputBuffer.put(pcmData)

                    encoder!!.queueInputBuffer(
                        inputBufferIndex,
                        0,
                        pcmData.size,
                        presentationTimeUs,
                        0
                    )
                }
            }

            // 输出阶段
            drainEncoder(false)
        }

        /**
         * 完成编码
         */
        fun finish() {
            Log.d(TAG, "完成音频编码")

            // 发送 EOS
            val inputBufferIndex = encoder!!.dequeueInputBuffer(10000)
            if (inputBufferIndex >= 0) {
                encoder!!.queueInputBuffer(
                    inputBufferIndex, 0, 0, 0,
                    MediaCodec.BUFFER_FLAG_END_OF_STREAM
                )
            }

            // 处理剩余数据
            drainEncoder(true)

            Log.d(TAG, "音频编码完成")
        }

        /**
         * 处理编码器输出
         */
        private fun drainEncoder(endOfStream: Boolean) {
            val bufferInfo = MediaCodec.BufferInfo()
            var outputBufferIndex = encoder!!.dequeueOutputBuffer(bufferInfo, 10000)

            while (outputBufferIndex >= 0) {
                val outputBuffer = encoder!!.getOutputBuffer(outputBufferIndex)

                if (outputBuffer != null) {
                    if (bufferInfo.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG != 0) {
                        bufferInfo.size = 0
                    }

                    if (bufferInfo.size > 0) {
                        outputBuffer.position(bufferInfo.offset)
                        outputBuffer.limit(bufferInfo.offset + bufferInfo.size)

                        if (!isStarted) {
                            val format = encoder!!.outputFormat
                            trackIndex = muxer!!.addTrack(format)
                            muxer!!.start()
                            isStarted = true
                            Log.d(TAG, "添加音频轨道，索引: $trackIndex")
                        }

                        muxer!!.writeSampleData(trackIndex, outputBuffer, bufferInfo)
                        Log.v(TAG, "写入音频帧: 大小=${bufferInfo.size}")
                    }

                    encoder!!.releaseOutputBuffer(outputBufferIndex, false)
                }

                outputBufferIndex = encoder!!.dequeueOutputBuffer(bufferInfo, 0)
            }
        }

        /**
         * 释放资源
         */
        fun release() {
            encoder?.stop()
            encoder?.release()
            muxer?.stop()
            muxer?.release()
            encoder = null
            muxer = null
        }
    }
}
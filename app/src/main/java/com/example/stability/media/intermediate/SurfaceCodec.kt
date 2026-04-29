package com.example.stability.media.intermediate

import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMuxer
import android.util.Log
import android.view.Surface
import java.io.File
import java.io.IOException

/**
 * ================================================
 * Surface 模式编解码器示例
 * ================================================
 * 演示如何使用 Surface 模式进行零拷贝编解码，这是最推荐的方式。
 *
 * Surface 模式的优势：
 * - 零拷贝：数据不需要从用户空间拷贝到内核空间
 * - 性能优异：直接使用 GPU 进行处理
 * - 适合实时场景：摄像头预览、屏幕录制等
 *
 * @author MediaCodec Learning
 * @version 1.0
 */
class SurfaceCodec {

    private val TAG = "SurfaceCodec"

    /**
     * ================================================
     * Surface 解码器
     * ================================================
     * 将视频文件解码并渲染到 Surface
     */
    class Decoder {

        private val TAG = "SurfaceDecoder"

        private var decoder: MediaCodec? = null
        private var extractor: MediaExtractor? = null
        private var surface: Surface? = null

        /**
         * 初始化 Surface 解码器
         *
         * @param inputPath 输入视频文件路径
         * @param outputSurface 用于渲染的 Surface
         * @throws IOException 如果文件无法读取
         */
        @Throws(IOException::class)
        fun initialize(inputPath: String, outputSurface: Surface) {
            Log.d(TAG, "初始化 Surface 解码器，输入路径: $inputPath")

            this.surface = outputSurface

            // 创建提取器
            extractor = MediaExtractor().apply {
                setDataSource(inputPath)
            }

            // 找到视频轨道
            var videoTrackIndex = -1
            for (i in 0 until extractor!!.trackCount) {
                val format = extractor!!.getTrackFormat(i)
                val mimeType = format.getString(MediaFormat.KEY_MIME)

                if (mimeType?.startsWith("video/") == true) {
                    videoTrackIndex = i
                    break
                }
            }

            if (videoTrackIndex < 0) {
                release()
                throw IllegalArgumentException("未找到视频轨道")
            }

            extractor!!.selectTrack(videoTrackIndex)
            val format = extractor!!.getTrackFormat(videoTrackIndex)

            // 打印格式信息
            logFormatInfo(format)

            // 创建解码器
            val mimeType = format.getString(MediaFormat.KEY_MIME) ?: "video/avc"
            decoder = MediaCodec.createDecoderByType(mimeType)

            // 配置解码器（关键：传入 Surface）
            decoder!!.configure(format, surface, null, 0)

            // 启动解码器
            decoder!!.start()

            Log.d(TAG, "Surface 解码器初始化完成")
        }

        /**
         * 开始解码
         */
        fun startDecoding() {
            Log.d(TAG, "开始 Surface 解码")

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
                        Log.d(TAG, "输出格式变化")
                    }
                    else -> {
                        if (outputBufferIndex >= 0) {
                            // 渲染到 Surface（第二个参数为 true）
                            decoder!!.releaseOutputBuffer(outputBufferIndex, true)

                            if (bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) {
                                isEOS = true
                            }
                        }
                    }
                }
            }

            Log.d(TAG, "Surface 解码完成")
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
            surface = null
        }

        private fun logFormatInfo(format: MediaFormat) {
            Log.d(TAG, "=== 视频格式信息 ===")
            Log.d(TAG, "MIME: ${format.getString(MediaFormat.KEY_MIME)}")
            Log.d(TAG, "宽度: ${format.getInteger(MediaFormat.KEY_WIDTH)}")
            Log.d(TAG, "高度: ${format.getInteger(MediaFormat.KEY_HEIGHT)}")
            Log.d(TAG, "帧率: ${format.getInteger(MediaFormat.KEY_FRAME_RATE)}")
            Log.d(TAG, "比特率: ${format.getInteger(MediaFormat.KEY_BIT_RATE)}")
            Log.d(TAG, "=== 格式信息结束 ===")
        }
    }

    /**
     * ================================================
     * Surface 编码器
     * ================================================
     * 从 Surface 读取帧数据并编码为视频文件
     */
    class Encoder {

        private val TAG = "SurfaceEncoder"

        private var encoder: MediaCodec? = null
        private var muxer: MediaMuxer? = null
        private var trackIndex = -1
        private var isStarted = false

        /**
         * 初始化 Surface 编码器
         *
         * @param outputPath 输出视频文件路径
         * @param width 视频宽度
         * @param height 视频高度
         * @param bitRate 比特率
         * @param frameRate 帧率
         * @throws IOException 如果文件无法创建
         */
        @Throws(IOException::class)
        fun initialize(
            outputPath: String,
            width: Int = 1280,
            height: Int = 720,
            bitRate: Int = 5_000_000,
            frameRate: Int = 30
        ) {
            Log.d(TAG, "初始化 Surface 编码器，输出路径: $outputPath")

            // 确保输出目录存在
            val outputFile = File(outputPath)
            outputFile.parentFile?.mkdirs()

            // 创建编码格式
            val format = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, width, height).apply {
                setInteger(MediaFormat.KEY_BIT_RATE, bitRate)
                setInteger(MediaFormat.KEY_FRAME_RATE, frameRate)
                setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 2)
                setInteger(
                    MediaFormat.KEY_COLOR_FORMAT,
                    MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface
                )
            }

            // 创建编码器
            encoder = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC)

            // 配置编码器
            encoder!!.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)

            // 创建 muxer
            muxer = MediaMuxer(outputPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)

            // 启动编码器
            encoder!!.start()

            Log.d(TAG, "Surface 编码器初始化完成")
        }

        /**
         * 获取输入 Surface
         *
         * 调用者可以通过此 Surface 绘制帧数据
         */
        fun getInputSurface(): Surface? {
            return encoder?.createInputSurface()
        }

        /**
         * 处理编码输出
         */
        fun drainOutput() {
            val bufferInfo = MediaCodec.BufferInfo()
            var outputBufferIndex = encoder!!.dequeueOutputBuffer(bufferInfo, 0)

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
                        }

                        muxer!!.writeSampleData(trackIndex, outputBuffer, bufferInfo)
                    }

                    encoder!!.releaseOutputBuffer(outputBufferIndex, false)
                }

                outputBufferIndex = encoder!!.dequeueOutputBuffer(bufferInfo, 0)
            }
        }

        /**
         * 完成编码
         */
        fun finish() {
            Log.d(TAG, "完成 Surface 编码")

            // 发送 EOS
            encoder?.signalEndOfInputStream()

            // 处理剩余数据
            val bufferInfo = MediaCodec.BufferInfo()
            var outputBufferIndex: Int

            do {
                outputBufferIndex = encoder!!.dequeueOutputBuffer(bufferInfo, 10000)

                if (outputBufferIndex >= 0) {
                    val outputBuffer = encoder!!.getOutputBuffer(outputBufferIndex)

                    if (outputBuffer != null && bufferInfo.size > 0) {
                        outputBuffer.position(bufferInfo.offset)
                        outputBuffer.limit(bufferInfo.offset + bufferInfo.size)
                        muxer?.writeSampleData(trackIndex, outputBuffer, bufferInfo)
                    }

                    encoder!!.releaseOutputBuffer(outputBufferIndex, false)
                }
            } while (outputBufferIndex >= 0)

            Log.d(TAG, "Surface 编码完成")
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
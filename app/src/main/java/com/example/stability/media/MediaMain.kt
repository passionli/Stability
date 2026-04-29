package com.example.stability.media

import android.util.Log
import com.example.stability.media.advanced.Transcoder
import com.example.stability.media.basic.VideoDecoder
import com.example.stability.media.basic.VideoEncoder
import com.example.stability.media.intermediate.AudioCodec
import com.example.stability.media.intermediate.SurfaceCodec

/**
 * ================================================
 * MediaCodec 学习模块入口
 * ================================================
 * 提供所有 MediaCodec 相关示例的入口点和使用示例
 *
 * @author MediaCodec Learning
 * @version 1.0
 */
object MediaMain {

    private val TAG = "MediaMain"

    /**
     * 模块初始化
     */
    fun initialize() {
        Log.d(TAG, "MediaCodec 学习模块初始化")
        logAvailableCodecs()
    }

    /**
     * 打印可用的编解码器信息
     */
    private fun logAvailableCodecs() {
        Log.d(TAG, "=== 可用编解码器列表 ===")

        // 视频编解码器
        Log.d(TAG, "视频解码器:")
        listCodecs("video/", isEncoder = false)

        Log.d(TAG, "视频编码器:")
        listCodecs("video/", isEncoder = true)

        // 音频编解码器
        Log.d(TAG, "音频解码器:")
        listCodecs("audio/", isEncoder = false)

        Log.d(TAG, "音频编码器:")
        listCodecs("audio/", isEncoder = true)

        Log.d(TAG, "=== 编解码器列表结束 ===")
    }

    /**
     * 列出指定类型的编解码器
     */
    private fun listCodecs(mimeTypePrefix: String, isEncoder: Boolean) {
        try {
            val codecList = android.media.MediaCodecList(android.media.MediaCodecList.ALL_CODECS)
            val codecs = codecList.codecInfos.filter {
                it.isEncoder == isEncoder && it.supportedTypes.any { type ->
                    type.startsWith(mimeTypePrefix)
                }
            }

            if (codecs.isEmpty()) {
                Log.d(TAG, "  无")
            } else {
                codecs.forEach { codec ->
                    Log.d(TAG, "  - ${codec.name}")
                    codec.supportedTypes.forEach { type ->
                        if (type.startsWith(mimeTypePrefix)) {
                            Log.d(TAG, "    $type")
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "获取编解码器列表失败", e)
        }
    }

    /**
     * ================================================
     * 使用示例
     * ================================================
     */

    /**
     * 视频解码示例
     */
    fun exampleVideoDecode(inputPath: String, surface: android.view.Surface) {
        Log.d(TAG, "开始视频解码示例")

        val decoder = VideoDecoder()

        try {
            decoder.initialize(inputPath, surface)
            decoder.startDecoding()
        } catch (e: Exception) {
            Log.e(TAG, "视频解码失败", e)
        } finally {
            decoder.release()
        }

        Log.d(TAG, "视频解码示例完成")
    }

    /**
     * 视频编码示例
     */
    fun exampleVideoEncode(outputPath: String) {
        Log.d(TAG, "开始视频编码示例")

        val encoder = VideoEncoder()

        try {
            encoder.initialize(
                outputPath = outputPath,
                width = 1280,
                height = 720,
                bitRate = 5_000_000,
                frameRate = 30
            )

            // 获取输入 Surface 用于绘制
            val inputSurface = encoder.getInputSurface()
            if (inputSurface != null) {
                // 在这里绘制帧数据到 Surface
                // 例如：使用 Canvas 绘制或从 Camera 预览获取数据
                Log.d(TAG, "输入 Surface 已准备好")
            }

            // 完成编码
            encoder.finish()
        } catch (e: Exception) {
            Log.e(TAG, "视频编码失败", e)
        } finally {
            encoder.release()
        }

        Log.d(TAG, "视频编码示例完成")
    }

    /**
     * 音频解码示例
     */
    fun exampleAudioDecode(inputPath: String, outputPcmPath: String) {
        Log.d(TAG, "开始音频解码示例")

        val decoder = AudioCodec.Decoder()

        try {
            decoder.initialize(inputPath)
            decoder.decodeToPcm(outputPcmPath)
        } catch (e: Exception) {
            Log.e(TAG, "音频解码失败", e)
        } finally {
            decoder.release()
        }

        Log.d(TAG, "音频解码示例完成")
    }

    /**
     * 音频编码示例
     */
    fun exampleAudioEncode(outputPath: String) {
        Log.d(TAG, "开始音频编码示例")

        val encoder = AudioCodec.Encoder()

        try {
            encoder.initialize(
                outputPath = outputPath,
                sampleRate = 44100,
                channelCount = 2,
                bitRate = 128_000
            )

            // 这里应该提供 PCM 数据进行编码
            // 例如：从麦克风采集或从文件读取 PCM 数据
            Log.d(TAG, "编码器已准备好，等待 PCM 数据")

            encoder.finish()
        } catch (e: Exception) {
            Log.e(TAG, "音频编码失败", e)
        } finally {
            encoder.release()
        }

        Log.d(TAG, "音频编码示例完成")
    }

    /**
     * Surface 模式解码示例
     */
    fun exampleSurfaceDecode(inputPath: String, surface: android.view.Surface) {
        Log.d(TAG, "开始 Surface 解码示例")

        val decoder = SurfaceCodec.Decoder()

        try {
            decoder.initialize(inputPath, surface)
            decoder.startDecoding()
        } catch (e: Exception) {
            Log.e(TAG, "Surface 解码失败", e)
        } finally {
            decoder.release()
        }

        Log.d(TAG, "Surface 解码示例完成")
    }

    /**
     * Surface 模式编码示例
     */
    fun exampleSurfaceEncode(outputPath: String) {
        Log.d(TAG, "开始 Surface 编码示例")

        val encoder = SurfaceCodec.Encoder()

        try {
            encoder.initialize(
                outputPath = outputPath,
                width = 1280,
                height = 720,
                bitRate = 5_000_000,
                frameRate = 30
            )

            // 获取输入 Surface
            val inputSurface = encoder.getInputSurface()
            if (inputSurface != null) {
                Log.d(TAG, "输入 Surface 已准备好")
            }

            // 处理输出
            encoder.drainOutput()
            encoder.finish()
        } catch (e: Exception) {
            Log.e(TAG, "Surface 编码失败", e)
        } finally {
            encoder.release()
        }

        Log.d(TAG, "Surface 编码示例完成")
    }

    /**
     * 视频转码示例
     */
    fun exampleTranscode(inputPath: String, outputPath: String) {
        Log.d(TAG, "开始转码示例")

        val callback = object : Transcoder.Callback {
            override fun onProgress(progress: Float) {
                Log.d(TAG, "转码进度: ${progress * 100}%")
            }

            override fun onComplete() {
                Log.d(TAG, "转码完成")
            }

            override fun onError(error: Exception) {
                Log.e(TAG, "转码失败", error)
            }
        }

        val config = Transcoder.Config(
            inputPath = inputPath,
            outputPath = outputPath,
            videoWidth = 1280,
            videoHeight = 720,
            videoBitRate = 5_000_000,
            videoFrameRate = 30,
            audioBitRate = 128_000,
            audioSampleRate = 44100,
            callback = callback
        )

        val transcoder = Transcoder()
        transcoder.transcode(config)

        Log.d(TAG, "转码示例完成")
    }

    /**
     * 获取所有可用示例列表
     */
    fun getExampleList(): List<String> {
        return listOf(
            "1. 视频解码 (VideoDecoder)",
            "2. 视频编码 (VideoEncoder)",
            "3. 音频解码 (AudioDecoder)",
            "4. 音频编码 (AudioEncoder)",
            "5. Surface 解码 (SurfaceDecoder)",
            "6. Surface 编码 (SurfaceEncoder)",
            "7. 视频转码 (Transcoder)"
        )
    }
}
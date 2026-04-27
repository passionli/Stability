package com.example.stability.video_edit

import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

/**
 * ================================================
 * 编解码器教程使用示例
 * ================================================
 * 演示如何在实际项目中使用 Mp4CodecTutorial
 * 中的类来进行 MP4 文件的解码和编码
 */
class CodecUsageExample : AppCompatActivity() {

    private val TAG = "CodecUsageExample"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 这里可以创建布局，或者直接在现有页面中添加按钮
        
        // 示例：如何使用解码器
        val decoder = Mp4DecoderTutorial()
        val encoder = Mp4EncoderTutorial()
        val transcoder = Mp4TranscoderTutorial()

        // 显示重要提示
        val tips = transcoder.getImportantTips()
        tips.forEachIndexed { index, tip ->
            Log.d(TAG, "提示 ${index + 1}: $tip")
        }

        Log.d(TAG, "编解码器教程使用示例加载完成！")
    }

    /**
     * 示例：解码一个 MP4 文件
     */
    fun exampleDecodeMp4(filePath: String) {
        Log.d(TAG, "开始示例：解码 MP4 文件")

        try {
            val decoder = Mp4DecoderTutorial()
            decoder.decodeMp4File(filePath)
        } catch (e: Exception) {
            Log.e(TAG, "解码示例失败", e)
            Toast.makeText(this, "解码失败: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * 示例：编码一个 MP4 文件
     */
    fun exampleEncodeMp4(filePath: String) {
        Log.d(TAG, "开始示例：编码 MP4 文件")

        try {
            val encoder = Mp4EncoderTutorial()
            encoder.encodeMp4File(
                outputFilePath = filePath,
                width = 1280,
                height = 720,
                durationMs = 10000L // 10秒
            )
        } catch (e: Exception) {
            Log.e(TAG, "编码示例失败", e)
            Toast.makeText(this, "编码失败: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * 示例：转码一个 MP4 文件
     */
    fun exampleTranscodeMp4(inputPath: String, outputPath: String) {
        Log.d(TAG, "开始示例：转码 MP4 文件")

        try {
            val transcoder = Mp4TranscoderTutorial()
            transcoder.transcodeMp4File(inputPath, outputPath)
        } catch (e: Exception) {
            Log.e(TAG, "转码示例失败", e)
            Toast.makeText(this, "转码失败: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}

/**
 * ================================================
 * 快速参考手册
 * ================================================
 * 这个类提供了常用操作的快速参考
 */
object QuickReference {

    /**
     * 常用 MIME 类型
     */
    object MimeTypes {
        // 视频
        const val VIDEO_H264 = "video/avc"
        const val VIDEO_H265 = "video/hevc"
        const val VIDEO_MP4V = "video/mp4v-es"

        // 音频
        const val AUDIO_AAC = "audio/mp4a-latm"
        const val AUDIO_MP3 = "audio/mpeg"
        const val AUDIO_RAW = "audio/raw"
    }

    /**
     * 常用颜色格式
     */
    object ColorFormats {
        const val FORMAT_SURFACE = MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface
        const val FORMAT_YUV420P = MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar
        const val FORMAT_YUV420SP = MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar
        const val FORMAT_YUV422P = MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV422Planar
    }

    /**
     * 常见配置参数
     */
    object CommonConfigs {
        // 视频
        const val FRAME_RATE_24 = 24
        const val FRAME_RATE_30 = 30
        const val FRAME_RATE_60 = 60

        const val I_FRAME_INTERVAL_DEFAULT = 2 // 2秒

        // 比特率 (bps)
        const val BITRATE_5Mbps = 5_000_000
        const val BITRATE_8Mbps = 8_000_000
        const val BITRATE_10Mbps = 10_000_000

        // 音频
        const val SAMPLE_RATE_44100 = 44100
        const val SAMPLE_RATE_48000 = 48000

        const val AUDIO_BITRATE_128K = 128_000
        const val AUDIO_BITRATE_192K = 192_000
        const val AUDIO_BITRATE_256K = 256_000
    }

    /**
     * MediaCodec 状态码参考
     */
    object MediaCodecStates {
        const val INFO_TRY_AGAIN_LATER = -1
        const val INFO_OUTPUT_FORMAT_CHANGED = -2
        const val INFO_OUTPUT_BUFFERS_CHANGED = -3
    }

    /**
     * 缓冲区标志
     */
    object BufferFlags {
        const val FLAG_END_OF_STREAM = MediaCodec.BUFFER_FLAG_END_OF_STREAM
        const val FLAG_KEY_FRAME = MediaCodec.BUFFER_FLAG_KEY_FRAME
        const val FLAG_CODEC_CONFIG = MediaCodec.BUFFER_FLAG_CODEC_CONFIG
        const val FLAG_PARTIAL_FRAME = MediaCodec.BUFFER_FLAG_PARTIAL_FRAME
    }

    /**
     * 常用分辨率
     */
    object CommonResolutions {
        data class Resolution(val width: Int, val height: Int, val name: String)

        val HD = Resolution(1280, 720, "720p HD")
        val FULL_HD = Resolution(1920, 1080, "1080p Full HD")
        val TWO_K = Resolution(2560, 1440, "2K QHD")
        val FOUR_K = Resolution(3840, 2160, "4K UHD")
    }
}

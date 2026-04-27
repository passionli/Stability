package com.example.stability.video_edit.audio

import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer

class Mp3ToPcmConverter {
    
    companion object {
        private const val TAG = "Mp3ToPcmConverter"
        private const val TIMEOUT_US = 10000L
    }
    
    data class ConversionResult(
        val success: Boolean,
        val outputPath: String?,
        val errorMessage: String? = null,
        val durationMs: Long = 0
    )
    
    /**
     * 同步版本：将 MP3 转换为 PCM（用于从协程外调用）
     */
    fun convertMp3ToPcmSync(
        inputPath: String,
        outputDir: String
    ): ConversionResult {
        return kotlinx.coroutines.runBlocking {
            convertMp3ToPcm(inputPath, outputDir)
        }
    }
    
    suspend fun convertMp3ToPcm(
        inputPath: String,
        outputDir: String
    ): ConversionResult = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        var outputPath: String? = null
        var extractor: android.media.MediaExtractor? = null
        var decoder: android.media.MediaCodec? = null
        var outputStream: FileOutputStream? = null
        
        try {
            android.util.Log.d(TAG, "开始转换 MP3 到 PCM")
            android.util.Log.d(TAG, "输入文件: $inputPath")
            android.util.Log.d(TAG, "输出目录: $outputDir")
            
            // 验证输入文件
            val inputFile = File(inputPath)
            if (!inputFile.exists()) {
                return@withContext ConversionResult(
                    success = false,
                    outputPath = null,
                    errorMessage = "输入文件不存在: $inputPath"
                )
            }
            
            // 创建输出目录
            val outputDirectory = File(outputDir)
            if (!outputDirectory.exists()) {
                outputDirectory.mkdirs()
            }
            
            // 生成输出文件名
            val outputFileName = "${inputFile.nameWithoutExtension}_${System.currentTimeMillis()}.pcm"
            outputPath = File(outputDir, outputFileName).absolutePath
            
            // 初始化 MediaExtractor
            extractor = android.media.MediaExtractor()
            extractor.setDataSource(inputPath)
            
            // 查找音频轨道
            var audioTrackIndex = -1
            var inputFormat: android.media.MediaFormat? = null
            
            for (i in 0 until extractor.trackCount) {
                val format = extractor.getTrackFormat(i)
                val mime = format.getString(android.media.MediaFormat.KEY_MIME)
                android.util.Log.d(TAG, "轨道 $i: $mime")
                
                if (mime?.startsWith("audio/") == true) {
                    audioTrackIndex = i
                    inputFormat = format
                    break
                }
            }
            
            if (audioTrackIndex == -1 || inputFormat == null) {
                return@withContext ConversionResult(
                    success = false,
                    outputPath = null,
                    errorMessage = "未找到音频轨道"
                )
            }
            
            // 打印音频格式信息
            val mime = inputFormat.getString(android.media.MediaFormat.KEY_MIME)
            val sampleRate = inputFormat.getInteger(android.media.MediaFormat.KEY_SAMPLE_RATE)
            val channelCount = inputFormat.getInteger(android.media.MediaFormat.KEY_CHANNEL_COUNT)
            val duration = inputFormat.getLong(android.media.MediaFormat.KEY_DURATION)
            
            android.util.Log.d(TAG, "音频格式: $mime")
            android.util.Log.d(TAG, "采样率: $sampleRate Hz")
            android.util.Log.d(TAG, "声道数: $channelCount")
            android.util.Log.d(TAG, "时长: ${duration / 1000} ms")
            
            // 选择音频轨道
            extractor.selectTrack(audioTrackIndex)
            
            // 创建解码器
            decoder = android.media.MediaCodec.createDecoderByType(mime!!)
            decoder.configure(inputFormat, null, null, 0)
            decoder.start()
            
            // 创建输出文件
            outputStream = FileOutputStream(outputPath)
            
            // 分配缓冲区
            val bufferSize = 1024 * 1024 // 1MB
            val buffer = ByteBuffer.allocate(bufferSize)
            val bufferInfo = android.media.MediaCodec.BufferInfo()
            
            var inputDone = false
            var outputDone = false
            var totalBytesWritten = 0L
            
            android.util.Log.d(TAG, "开始解码...")
            
            while (!outputDone) {
                // 向解码器输入数据
                if (!inputDone) {
                    val inputBufferIndex = decoder.dequeueInputBuffer(TIMEOUT_US)
                    if (inputBufferIndex >= 0) {
                        val inputBuffer = decoder.getInputBuffer(inputBufferIndex)
                        if (inputBuffer != null) {
                            val sampleSize = extractor.readSampleData(inputBuffer, 0)
                            if (sampleSize < 0) {
                                decoder.queueInputBuffer(
                                    inputBufferIndex, 
                                    0, 
                                    0, 
                                    0, 
                                    android.media.MediaCodec.BUFFER_FLAG_END_OF_STREAM
                                )
                                inputDone = true
                                android.util.Log.d(TAG, "输入数据读取完成")
                            } else {
                                decoder.queueInputBuffer(
                                    inputBufferIndex,
                                    0,
                                    sampleSize,
                                    extractor.sampleTime,
                                    0
                                )
                                extractor.advance()
                            }
                        }
                    }
                }
                
                // 从解码器获取输出
                val outputBufferIndex = decoder.dequeueOutputBuffer(bufferInfo, TIMEOUT_US)
                when (outputBufferIndex) {
                    android.media.MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED -> {
                        android.util.Log.d(TAG, "输出缓冲区改变")
                    }
                    android.media.MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> {
                        val newFormat = decoder.outputFormat
                        android.util.Log.d(TAG, "输出格式改变: $newFormat")
                    }
                    android.media.MediaCodec.INFO_TRY_AGAIN_LATER -> {
                        // 没有可用的输出，等待一下
                    }
                    else -> {
                        if (outputBufferIndex >= 0) {
                            val outputBuffer = decoder.getOutputBuffer(outputBufferIndex)
                            if (outputBuffer != null && bufferInfo.size > 0) {
                                // 保存解码后的 PCM 数据
                                outputBuffer.position(bufferInfo.offset)
                                outputBuffer.limit(bufferInfo.offset + bufferInfo.size)
                                
                                val pcmData = ByteArray(bufferInfo.size)
                                outputBuffer.get(pcmData)
                                outputStream.write(pcmData)
                                totalBytesWritten += bufferInfo.size
                                
                                // 每秒打印一次进度
                                if (bufferInfo.presentationTimeUs % 1000000 < TIMEOUT_US) {
                                    val progressMs = bufferInfo.presentationTimeUs / 1000
                                    val totalMs = duration / 1000
                                    android.util.Log.d(TAG, "进度: ${progressMs}/${totalMs} ms, 已写入: $totalBytesWritten bytes")
                                }
                            }
                            decoder.releaseOutputBuffer(outputBufferIndex, false)
                            
                            if (bufferInfo.flags and android.media.MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) {
                                outputDone = true
                                android.util.Log.d(TAG, "输出完成")
                            }
                        }
                    }
                }
            }
            
            val endTime = System.currentTimeMillis()
            val durationMs = endTime - startTime
            
            android.util.Log.d(TAG, "转换完成!")
            android.util.Log.d(TAG, "输出文件: $outputPath")
            android.util.Log.d(TAG, "总字节数: $totalBytesWritten")
            android.util.Log.d(TAG, "耗时: ${durationMs}ms")
            
            return@withContext ConversionResult(
                success = true,
                outputPath = outputPath,
                durationMs = durationMs
            )
            
        } catch (e: Exception) {
            android.util.Log.e(TAG, "转换失败: ${e.message}", e)
            return@withContext ConversionResult(
                success = false,
                outputPath = null,
                errorMessage = e.message,
                durationMs = System.currentTimeMillis() - startTime
            )
        } finally {
            try {
                outputStream?.close()
            } catch (e: Exception) {
                android.util.Log.e(TAG, "关闭输出流失败: ${e.message}")
            }
            
            try {
                decoder?.stop()
                decoder?.release()
            } catch (e: Exception) {
                android.util.Log.e(TAG, "释放解码器失败: ${e.message}")
            }
            
            try {
                extractor?.release()
            } catch (e: Exception) {
                android.util.Log.e(TAG, "释放提取器失败: ${e.message}")
            }
        }
    }
    
    fun getAudioInfo(path: String): AudioInfo? {
        var extractor: android.media.MediaExtractor? = null
        
        try {
            extractor = android.media.MediaExtractor()
            extractor.setDataSource(path)
            
            for (i in 0 until extractor.trackCount) {
                val format = extractor.getTrackFormat(i)
                val mime = format.getString(android.media.MediaFormat.KEY_MIME)
                
                if (mime?.startsWith("audio/") == true) {
                    return AudioInfo(
                        mime = mime,
                        sampleRate = format.getInteger(android.media.MediaFormat.KEY_SAMPLE_RATE),
                        channelCount = format.getInteger(android.media.MediaFormat.KEY_CHANNEL_COUNT),
                        duration = format.getLong(android.media.MediaFormat.KEY_DURATION),
                        bitRate = if (format.containsKey(android.media.MediaFormat.KEY_BIT_RATE)) 
                            format.getInteger(android.media.MediaFormat.KEY_BIT_RATE) else null
                    )
                }
            }
            return null
        } catch (e: Exception) {
            android.util.Log.e(TAG, "获取音频信息失败: ${e.message}", e)
            return null
        } finally {
            extractor?.release()
        }
    }
    
    data class AudioInfo(
        val mime: String,
        val sampleRate: Int,
        val channelCount: Int,
        val duration: Long,
        val bitRate: Int?
    ) {
        fun getDurationSeconds(): Float = duration / 1000000f
        
        override fun toString(): String {
            return """
                |Audio Info:
                |  Format: $mime
                |  Sample Rate: $sampleRate Hz
                |  Channels: $channelCount
                |  Duration: ${getDurationSeconds()}s
                |  Bit Rate: ${bitRate ?: "N/A"} kbps
            """.trimMargin()
        }
    }
}

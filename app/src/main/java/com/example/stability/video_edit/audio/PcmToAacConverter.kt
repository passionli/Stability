package com.example.stability.video_edit.audio

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.ByteBuffer

class PcmToAacConverter {
    
    companion object {
        private const val TAG = "PcmToAacConverter"
        private const val TIMEOUT_US = 10000L
        private const val DEFAULT_BIT_RATE = 128000
    }
    
    data class ConversionResult(
        val success: Boolean,
        val outputPath: String?,
        val errorMessage: String? = null,
        val durationMs: Long = 0
    )
    
    data class PcmConfig(
        val sampleRate: Int = 44100,
        val channelCount: Int = 2,
        val bitRate: Int = DEFAULT_BIT_RATE
    )
    
    /**
     * 同步版本：将 PCM 转换为 AAC（用于从协程外调用）
     */
    fun convertPcmToAacSync(
        inputPath: String,
        outputPath: String,
        config: PcmConfig = PcmConfig()
    ): ConversionResult {
        return kotlinx.coroutines.runBlocking {
            convertPcmToAac(inputPath, outputPath, config)
        }
    }
    
    suspend fun convertPcmToAac(
        inputPath: String,
        outputPath: String,
        config: PcmConfig = PcmConfig()
    ): ConversionResult = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        var encoder: android.media.MediaCodec? = null
        var inputStream: FileInputStream? = null
        var outputStream: FileOutputStream? = null
        
        try {
            android.util.Log.d(TAG, "开始转换 PCM 到 AAC")
            android.util.Log.d(TAG, "输入文件: $inputPath")
            android.util.Log.d(TAG, "输出文件: $outputPath")
            android.util.Log.d(TAG, "配置: 采样率=${config.sampleRate}Hz, 声道数=${config.channelCount}, 比特率=${config.bitRate}")
            
            // 验证输入文件
            val inputFile = File(inputPath)
            if (!inputFile.exists()) {
                return@withContext ConversionResult(
                    success = false,
                    outputPath = null,
                    errorMessage = "输入文件不存在: $inputPath"
                )
            }
            
            android.util.Log.d(TAG, "输入文件大小: ${inputFile.length()} bytes")
            
            // 创建输出目录
            val outputFile = File(outputPath)
            val outputDir = outputFile.parentFile
            if (outputDir != null && !outputDir.exists()) {
                android.util.Log.d(TAG, "创建输出目录: ${outputDir.absolutePath}")
                outputDir.mkdirs()
            }
            
            // 创建 AAC 编码器
            android.util.Log.d(TAG, "创建 AAC 编码器...")
            val outputFormat = android.media.MediaFormat.createAudioFormat(
                "audio/mp4a-latm",
                config.sampleRate,
                config.channelCount
            )
            outputFormat.setInteger(android.media.MediaFormat.KEY_BIT_RATE, config.bitRate)
            outputFormat.setInteger(android.media.MediaFormat.KEY_AAC_PROFILE, android.media.MediaCodecInfo.CodecProfileLevel.AACObjectLC)
            outputFormat.setInteger(android.media.MediaFormat.KEY_MAX_INPUT_SIZE, 1024 * 1024)
            
            android.util.Log.d(TAG, "编码器格式: $outputFormat")
            
            encoder = android.media.MediaCodec.createEncoderByType("audio/mp4a-latm")
            android.util.Log.d(TAG, "编码器创建成功")
            
            encoder.configure(outputFormat, null, null, android.media.MediaCodec.CONFIGURE_FLAG_ENCODE)
            android.util.Log.d(TAG, "编码器配置成功")
            
            encoder.start()
            android.util.Log.d(TAG, "编码器启动成功")
            
            // 打开输入输出流
            android.util.Log.d(TAG, "打开输入流...")
            inputStream = FileInputStream(inputPath)
            android.util.Log.d(TAG, "输入流打开成功")
            
            android.util.Log.d(TAG, "打开输出流...")
            outputStream = FileOutputStream(outputPath)
            android.util.Log.d(TAG, "输出流打开成功")
            
            // 分配缓冲区
            val bufferSize = 1024 * 1024 // 1MB
            val inputBuffer = ByteBuffer.allocate(bufferSize)
            val bufferInfo = android.media.MediaCodec.BufferInfo()
            android.util.Log.d(TAG, "缓冲区分配成功")
            
            var inputDone = false
            var outputDone = false
            var totalBytesWritten = 0L
            var presentationTimeUs = 0L
            
            // 计算每帧的时间戳增量
            val frameSize = config.channelCount * 2 // 16bit = 2 bytes per sample
            val samplesPerFrame = 1024 // AAC 通常一帧 1024 个样本
            val timePerFrame = (samplesPerFrame * 1000000L) / config.sampleRate
            
            android.util.Log.d(TAG, "开始编码...")
            
            try {
                while (!outputDone) {
                    // 向编码器输入数据
                    if (!inputDone) {
                        try {
                            val inputBufferIndex = encoder.dequeueInputBuffer(TIMEOUT_US)
                            android.util.Log.d(TAG, "dequeueInputBuffer 返回: $inputBufferIndex")
                            
                            if (inputBufferIndex >= 0) {
                                val encoderInputBuffer = encoder.getInputBuffer(inputBufferIndex)
                                if (encoderInputBuffer != null) {
                                    encoderInputBuffer.clear()
                                    
                                    val bytesRead = inputStream.read(inputBuffer.array())
                                    android.util.Log.d(TAG, "读取输入数据: $bytesRead bytes")
                                    
                                    if (bytesRead < 0) {
                                        // 输入结束，发送结束标记
                                        android.util.Log.d(TAG, "输入数据结束，发送 EOS 标记")
                                        encoder.queueInputBuffer(
                                            inputBufferIndex,
                                            0,
                                            0,
                                            presentationTimeUs,
                                            android.media.MediaCodec.BUFFER_FLAG_END_OF_STREAM
                                        )
                                        inputDone = true
                                        android.util.Log.d(TAG, "输入数据读取完成")
                                    } else {
                                        // 处理实际读取的数据
                                        inputBuffer.position(0)
                                        inputBuffer.limit(bytesRead)
                                        
                                        encoderInputBuffer.put(inputBuffer)
                                        
                                        encoder.queueInputBuffer(
                                            inputBufferIndex,
                                            0,
                                            bytesRead,
                                            presentationTimeUs,
                                            0
                                        )
                                        
                                        // 更新时间戳
                                        val samplesProcessed = bytesRead / frameSize
                                        presentationTimeUs += (samplesProcessed * 1000000L) / config.sampleRate
                                        android.util.Log.d(TAG, "处理 $samplesProcessed 个样本，时间戳: $presentationTimeUs us")
                                    }
                                } else {
                                    android.util.Log.e(TAG, "获取输入缓冲区失败")
                                }
                            } else if (inputBufferIndex == android.media.MediaCodec.INFO_TRY_AGAIN_LATER) {
                                android.util.Log.d(TAG, "没有可用的输入缓冲区，稍后再试")
                            }
                        } catch (e: Exception) {
                            android.util.Log.e(TAG, "处理输入缓冲区失败: ${e.message}", e)
                            break
                        }
                    }
                    
                    // 从编码器获取输出
                    try {
                        val encoderOutputIndex = encoder.dequeueOutputBuffer(bufferInfo, TIMEOUT_US)
                        android.util.Log.d(TAG, "dequeueOutputBuffer 返回: $encoderOutputIndex")
                        
                        when (encoderOutputIndex) {
                            android.media.MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED -> {
                                android.util.Log.d(TAG, "输出缓冲区改变")
                            }
                            android.media.MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> {
                                val newFormat = encoder.outputFormat
                                android.util.Log.d(TAG, "输出格式改变: $newFormat")
                            }
                            android.media.MediaCodec.INFO_TRY_AGAIN_LATER -> {
                                // 没有可用的输出，等待一下
                                android.util.Log.d(TAG, "没有可用的输出缓冲区，稍后再试")
                            }
                            else -> {
                                if (encoderOutputIndex >= 0) {
                                    val encoderOutputBuffer = encoder.getOutputBuffer(encoderOutputIndex)
                                    if (encoderOutputBuffer != null) {
                                        if (bufferInfo.flags and android.media.MediaCodec.BUFFER_FLAG_CODEC_CONFIG != 0) {
                                            // 跳过编解码器配置数据
                                            android.util.Log.d(TAG, "跳过编解码器配置数据")
                                            bufferInfo.size = 0
                                        }
                                        
                                        if (bufferInfo.size > 0) {
                                            // 保存编码后的 AAC 数据
                                            val aacData = ByteArray(bufferInfo.size)
                                            encoderOutputBuffer.position(bufferInfo.offset)
                                            encoderOutputBuffer.limit(bufferInfo.offset + bufferInfo.size)
                                            encoderOutputBuffer.get(aacData)

                                            // 打印写入的 AAC 数据大小和时间戳
//                                            android.util.Log.d(TAG, "写入 AAC 数据大小: ${bufferInfo.size}, 时间戳: ${bufferInfo.presentationTimeUs} us")
                                            outputStream.write(aacData)
                                            totalBytesWritten += bufferInfo.size
                                            
                                            // 每秒打印一次进度
                                            if (bufferInfo.presentationTimeUs % 1000000 < TIMEOUT_US * 10) {
                                                val progressMs = bufferInfo.presentationTimeUs / 1000
                                                android.util.Log.d(TAG, "进度: $progressMs ms, 已写入: $totalBytesWritten bytes")
                                            }
                                        }
                                        
                                        encoder.releaseOutputBuffer(encoderOutputIndex, false)
                                        
                                        if (bufferInfo.flags and android.media.MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) {
                                            outputDone = true
                                            android.util.Log.d(TAG, "输出完成")
                                        }
                                    } else {
                                        android.util.Log.e(TAG, "获取输出缓冲区失败")
                                    }
                                }
                            }
                        }
                    } catch (e: Exception) {
                        android.util.Log.e(TAG, "处理输出缓冲区失败: ${e.message}", e)
                        break
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e(TAG, "编码过程失败: ${e.message}", e)
                throw e
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
                inputStream?.close()
            } catch (e: Exception) {
                android.util.Log.e(TAG, "关闭输入流失败: ${e.message}")
            }
            
            try {
                encoder?.stop()
                encoder?.release()
            } catch (e: Exception) {
                android.util.Log.e(TAG, "释放编码器失败: ${e.message}")
            }
        }
    }
    
    suspend fun convertAllPcmInDirectory(
        inputDir: String,
        outputDir: String,
        config: PcmConfig = PcmConfig()
    ): List<ConversionResult> = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        val results = mutableListOf<ConversionResult>()
        
        val dir = File(inputDir)
        if (!dir.exists() || !dir.isDirectory) {
            return@withContext listOf(ConversionResult(
                success = false,
                outputPath = null,
                errorMessage = "输入目录不存在或不是目录: $inputDir"
            ))
        }
        
        // 创建输出目录
        val outputDirectory = File(outputDir)
        if (!outputDirectory.exists()) {
            outputDirectory.mkdirs()
        }
        
        // 获取所有 PCM 文件
        val pcmFiles = dir.listFiles { file ->
            file.extension.equals("pcm", ignoreCase = true)
        }?.sortedBy { it.name } ?: emptyList()
        
        android.util.Log.d(TAG, "找到 ${pcmFiles.size} 个 PCM 文件")
        
        for (pcmFile in pcmFiles) {
            android.util.Log.d(TAG, "处理文件: ${pcmFile.name}")
            
            val outputFileName = "${pcmFile.nameWithoutExtension}.aac"
            val outputPath = File(outputDir, outputFileName).absolutePath
            
            val result = convertPcmToAac(pcmFile.absolutePath, outputPath, config)
            results.add(result)
            
            if (result.success) {
                android.util.Log.d(TAG, "成功转换: ${pcmFile.name} -> $outputFileName")
            } else {
                android.util.Log.e(TAG, "转换失败: ${pcmFile.name}, 错误: ${result.errorMessage}")
            }
        }
        
        return@withContext results
    }
    
    fun getPcmInfo(path: String): PcmInfo? {
        val file = File(path)
        if (!file.exists()) {
            return null
        }
        
        return PcmInfo(
            fileName = file.name,
            fileSize = file.length(),
            lastModified = file.lastModified()
        )
    }
    
    data class PcmInfo(
        val fileName: String,
        val fileSize: Long,
        val lastModified: Long
    ) {
        fun getFileSizeKb(): Float = fileSize / 1024f
        fun getFileSizeMb(): Float = fileSize / (1024f * 1024f)
        
        override fun toString(): String {
            return """
                |PCM Info:
                |  File Name: $fileName
                |  File Size: ${getFileSizeKb()} KB (${getFileSizeMb()} MB)
                |  Last Modified: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(java.util.Date(lastModified))}
            """.trimMargin()
        }
    }
}

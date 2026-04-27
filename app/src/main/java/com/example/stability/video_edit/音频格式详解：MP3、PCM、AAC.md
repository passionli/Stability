# 音频格式详解：MP3、PCM、AAC

## 1. 音频格式概述

### 1.1 音频格式分类

| 类型 | 特点 | 代表格式 |
|------|------|----------|
| 无损压缩 | 保持原始音质，文件较大 | PCM、FLAC、WAV |
| 有损压缩 | 牺牲部分音质，文件较小 | MP3、AAC、OGG |

### 1.2 基本概念

- **采样率**：每秒采集的样本数，常见值为 44.1kHz、48kHz
- **位深度**：每个样本的位数，常见值为 16bit、24bit
- **声道数**：单声道(1)、立体声(2)、多声道
- **比特率**：每秒传输的数据量，单位为 kbps

## 2. PCM 格式

### 2.1 基本概念

PCM（Pulse Code Modulation，脉冲编码调制）是一种数字音频的原始表示形式，是未经压缩的音频数据。

### 2.2 特点

- **无损**：保留完整的音频信息
- **原始**：直接从模拟信号转换而来
- **无压缩**：文件体积较大
- **高质量**：音质最好

### 2.3 技术参数

| 参数 | 常见值 | 说明 |
|------|--------|------|
| 采样率 | 44.1kHz, 48kHz, 96kHz | 标准 CD 为 44.1kHz |
| 位深度 | 16bit, 24bit, 32bit | 标准 CD 为 16bit |
| 声道 | 1(单声道), 2(立体声) | 标准 CD 为 2 声道 |
| 数据量 | 计算公式：采样率 × 位深度 × 声道数 | 44.1kHz/16bit/2声道 = 176.4kbps |

### 2.4 PCM 在 Android 中的使用

```kotlin
// 读取 PCM 数据
val inputStream = FileInputStream("audio.pcm")
val buffer = ByteArray(1024)
var bytesRead: Int
while (inputStream.read(buffer).also { bytesRead = it } != -1) {
    // 处理 PCM 数据
}

// 使用 AudioTrack 播放 PCM
val audioTrack = AudioTrack.Builder()
    .setAudioAttributes(AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_MEDIA)
        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
        .build())
    .setAudioFormat(AudioFormat.Builder()
        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
        .setSampleRate(44100)
        .setChannelMask(AudioFormat.CHANNEL_OUT_STEREO)
        .build())
    .setBufferSizeInBytes(bufferSize)
    .build()

audioTrack.play()
audioTrack.write(buffer, 0, buffer.size)
```

## 3. MP3 格式

### 3.1 基本概念

MP3（MPEG-1 Audio Layer 3）是一种广泛使用的有损音频压缩格式，由 Fraunhofer IIS 开发。

### 3.2 特点

- **有损压缩**：牺牲部分音质以减小文件体积
- **广泛兼容**：几乎所有设备都支持
- **可变比特率**：可以根据音频复杂度动态调整比特率
- **文件小**：通常比 PCM 小 10-12 倍

### 3.3 技术参数

| 参数 | 常见值 | 说明 |
|------|--------|------|
| 比特率 | 128kbps, 192kbps, 320kbps | 320kbps 为 MP3 的最高质量 |
| 采样率 | 44.1kHz, 48kHz | 与原始音频保持一致 |
| 编码质量 | VBR, CBR, ABR | VBR 可变比特率效果最佳 |
| 压缩比 | 10:1 到 12:1 | 取决于比特率设置 |

### 3.4 MP3 编码标准

| 版本 | 特点 | 比特率范围 |
|------|------|------------|
| MPEG-1 Layer 3 | 标准 MP3 | 32-320 kbps |
| MPEG-2 Layer 3 | 低比特率 | 8-160 kbps |
| MPEG-2.5 Layer 3 | 更低比特率 | 8-128 kbps |

### 3.5 MP3 在 Android 中的限制

- **MediaMuxer 不支持**：无法直接将 MP3 写入 MP4 文件
- **需要转换**：必须转换为 AAC 才能与视频合成
- **播放支持**：ExoPlayer 和 MediaPlayer 都支持播放 MP3

## 4. AAC 格式

### 4.1 基本概念

AAC（Advanced Audio Coding）是一种专为数字音频压缩设计的有损音频格式，是 MP3 的后继者。

### 4.2 特点

- **更高压缩效率**：相同比特率下音质优于 MP3
- **多声道支持**：最多支持 48 声道
- **采样率范围广**：8kHz-96kHz
- **支持可变比特率**：VBR 模式
- **硬件加速**：大多数现代设备支持硬件解码

### 4.3 技术参数

| 参数 | 常见值 | 说明 |
|------|--------|------|
| 比特率 | 64kbps-320kbps | 通常 128kbps 即可达到良好音质 |
| 采样率 | 44.1kHz, 48kHz | 与原始音频保持一致 |
| 编码配置文件 | LC, HE-AAC, HE-AAC v2 | LC 为标准配置文件 |
| 压缩比 | 15:1 到 20:1 | 比 MP3 更高效 |

### 4.4 AAC 编码配置文件

| 配置文件 | 特点 | 适用场景 |
|----------|------|----------|
| LC (Low Complexity) | 标准配置，平衡质量和复杂度 | 大多数应用场景 |
| HE-AAC (High Efficiency) | 低比特率下表现更好 | 流媒体、移动设备 |
| HE-AAC v2 | 更低比特率，增加 SBR 和 PS 技术 | 极低比特率场景 |
| LD (Low Delay) | 低延迟，适合实时通信 | 语音通话、游戏 |

### 4.5 AAC 在 Android 中的使用

- **MediaMuxer 支持**：可以直接写入 MP4 文件
- **ExoPlayer 支持**：原生支持 AAC 播放
- **硬件加速**：大多数设备支持硬件解码
- **推荐格式**：Android 官方推荐的音频格式

## 5. 格式转换

### 5.1 MP3 到 AAC 转换

```kotlin
fun convertMp3ToAac(mp3Path: String, aacPath: String) {
    try {
        // 创建 MP3 解码器
        val extractor = MediaExtractor()
        extractor.setDataSource(mp3Path)
        
        var inputFormat: MediaFormat? = null
        for (i in 0 until extractor.trackCount) {
            val format = extractor.getTrackFormat(i)
            val mime = format.getString(MediaFormat.KEY_MIME)
            if (mime?.startsWith("audio/") == true) {
                inputFormat = format
                extractor.selectTrack(i)
                break
            }
        }
        
        if (inputFormat == null) {
            throw Exception("No audio track found")
        }
        
        val sampleRate = inputFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE)
        val channelCount = inputFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT)
        
        // 创建 AAC 编码器
        val encoder = MediaCodec.createEncoderByType("audio/mp4a-latm")
        val outputFormat = MediaFormat.createAudioFormat(
            "audio/mp4a-latm",
            sampleRate,
            channelCount
        )
        outputFormat.setInteger(MediaFormat.KEY_BIT_RATE, 128000)
        outputFormat.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC)
        
        encoder.configure(outputFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
        encoder.start()
        
        // 创建 AAC 解码器
        val decoder = MediaCodec.createDecoderByType(inputFormat.getString(MediaFormat.KEY_MIME)!!)
        decoder.configure(inputFormat, null, null, 0)
        decoder.start()
        
        // 处理数据
        val outputStream = FileOutputStream(aacPath)
        
        val inputBufferSize = 1024 * 1024
        val outputBufferSize = 1024 * 1024
        val inputBuffer = ByteBuffer.allocate(inputBufferSize)
        val outputBuffer = ByteBuffer.allocate(outputBufferSize)
        val bufferInfo = MediaCodec.BufferInfo()
        
        var inputDone = false
        var outputDone = false
        
        while (!outputDone) {
            // 输入数据到解码器
            if (!inputDone) {
                val inputIndex = decoder.dequeueInputBuffer(10000)
                if (inputIndex >= 0) {
                    val decoderInputBuffer = decoder.getInputBuffer(inputIndex)
                    if (decoderInputBuffer != null) {
                        val sampleSize = extractor.readSampleData(decoderInputBuffer, 0)
                        if (sampleSize < 0) {
                            decoder.queueInputBuffer(inputIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                            inputDone = true
                        } else {
                            decoder.queueInputBuffer(inputIndex, 0, sampleSize, extractor.sampleTime, 0)
                            extractor.advance()
                        }
                    }
                }
            }
            
            // 从解码器获取输出
            val decoderOutputIndex = decoder.dequeueOutputBuffer(bufferInfo, 10000)
            when (decoderOutputIndex) {
                MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED -> {}
                MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> {}
                MediaCodec.INFO_TRY_AGAIN_LATER -> {}
                else -> {
                    val decoderOutputBuffer = decoder.getOutputBuffer(decoderOutputIndex)
                    if (decoderOutputBuffer != null) {
                        // 输入数据到编码器
                        val encoderInputIndex = encoder.dequeueInputBuffer(10000)
                        if (encoderInputIndex >= 0) {
                            val encoderInputBuffer = encoder.getInputBuffer(encoderInputIndex)
                            if (encoderInputBuffer != null) {
                                encoderInputBuffer.clear()
                                encoderInputBuffer.put(decoderOutputBuffer)
                                encoder.queueInputBuffer(encoderInputIndex, 0, bufferInfo.size, bufferInfo.presentationTimeUs, bufferInfo.flags)
                            }
                        }
                        decoder.releaseOutputBuffer(decoderOutputIndex, false)
                    }
                }
            }
            
            // 从编码器获取输出
            val encoderOutputIndex = encoder.dequeueOutputBuffer(bufferInfo, 10000)
            when (encoderOutputIndex) {
                MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED -> {}
                MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> {}
                MediaCodec.INFO_TRY_AGAIN_LATER -> {}
                else -> {
                    val encoderOutputBuffer = encoder.getOutputBuffer(encoderOutputIndex)
                    if (encoderOutputBuffer != null) {
                        if (bufferInfo.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG != 0) {
                            // 跳过配置数据
                            bufferInfo.size = 0
                        }
                        if (bufferInfo.size > 0) {
                            // 写入 AAC 数据
                            val data = ByteArray(bufferInfo.size)
                            encoderOutputBuffer.position(bufferInfo.offset)
                            encoderOutputBuffer.get(data)
                            outputStream.write(data)
                        }
                        encoder.releaseOutputBuffer(encoderOutputIndex, false)
                        if (bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) {
                            outputDone = true
                        }
                    }
                }
            }
        }
        
        outputStream.close()
        decoder.stop()
        decoder.release()
        encoder.stop()
        encoder.release()
        extractor.release()
        
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
```

### 5.2 PCM 到 AAC 转换

```kotlin
fun convertPcmToAac(pcmPath: String, aacPath: String, sampleRate: Int, channelCount: Int) {
    try {
        val inputStream = FileInputStream(pcmPath)
        val outputStream = FileOutputStream(aacPath)
        
        // 创建 AAC 编码器
        val encoder = MediaCodec.createEncoderByType("audio/mp4a-latm")
        val outputFormat = MediaFormat.createAudioFormat(
            "audio/mp4a-latm",
            sampleRate,
            channelCount
        )
        outputFormat.setInteger(MediaFormat.KEY_BIT_RATE, 128000)
        outputFormat.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC)
        
        encoder.configure(outputFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
        encoder.start()
        
        val bufferSize = 1024 * 1024
        val inputBuffer = ByteBuffer.allocate(bufferSize)
        val outputBuffer = ByteBuffer.allocate(bufferSize)
        val bufferInfo = MediaCodec.BufferInfo()
        
        var inputDone = false
        var outputDone = false
        
        while (!outputDone) {
            // 读取 PCM 数据
            if (!inputDone) {
                val bytesRead = inputStream.read(inputBuffer.array())
                if (bytesRead < 0) {
                    inputDone = true
                    val inputIndex = encoder.dequeueInputBuffer(10000)
                    if (inputIndex >= 0) {
                        encoder.queueInputBuffer(inputIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                    }
                } else {
                    val inputIndex = encoder.dequeueInputBuffer(10000)
                    if (inputIndex >= 0) {
                        val encoderInputBuffer = encoder.getInputBuffer(inputIndex)
                        if (encoderInputBuffer != null) {
                            encoderInputBuffer.clear()
                            encoderInputBuffer.put(inputBuffer.array(), 0, bytesRead)
                            encoder.queueInputBuffer(inputIndex, 0, bytesRead, 0, 0)
                        }
                    }
                }
            }
            
            // 从编码器获取输出
            val encoderOutputIndex = encoder.dequeueOutputBuffer(bufferInfo, 10000)
            when (encoderOutputIndex) {
                MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED -> {}
                MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> {}
                MediaCodec.INFO_TRY_AGAIN_LATER -> {}
                else -> {
                    val encoderOutputBuffer = encoder.getOutputBuffer(encoderOutputIndex)
                    if (encoderOutputBuffer != null) {
                        if (bufferInfo.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG != 0) {
                            // 跳过配置数据
                            bufferInfo.size = 0
                        }
                        if (bufferInfo.size > 0) {
                            // 写入 AAC 数据
                            val data = ByteArray(bufferInfo.size)
                            encoderOutputBuffer.position(bufferInfo.offset)
                            encoderOutputBuffer.get(data)
                            outputStream.write(data)
                        }
                        encoder.releaseOutputBuffer(encoderOutputIndex, false)
                        if (bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) {
                            outputDone = true
                        }
                    }
                }
            }
        }
        
        inputStream.close()
        outputStream.close()
        encoder.stop()
        encoder.release()
        
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
```

## 6. 音频格式比较

### 6.1 质量对比

| 格式 | 比特率 | 音质 | 文件大小 | 适用场景 |
|------|--------|------|----------|----------|
| PCM | 1411kbps (44.1kHz/16bit) | 最好 | 最大 | 专业录音、音频编辑 |
| AAC | 128-256kbps | 很好 | 小 | 移动设备、流媒体 |
| MP3 | 128-320kbps | 好 | 较小 | 兼容设备、旧系统 |

### 6.2 技术参数对比

| 特性 | PCM | MP3 | AAC |
|------|-----|-----|-----|
| 压缩方式 | 无压缩 | 有损压缩 | 有损压缩 |
| 压缩比 | 1:1 | 10:1-12:1 | 15:1-20:1 |
| 延迟 | 最小 | 中等 | 低 |
| 硬件支持 | 所有设备 | 几乎所有设备 | 现代设备 |
| 编解码复杂度 | 低 | 中等 | 高 |
| 多声道支持 | 支持 | 最多 2 声道 | 最多 48 声道 |

### 6.3 优缺点分析

#### PCM
- **优点**：无损音质、编解码简单、延迟小
- **缺点**：文件体积大、不适合网络传输

#### MP3
- **优点**：广泛兼容、成熟稳定、文件较小
- **缺点**：压缩效率低、音质不如 AAC、不支持多声道

#### AAC
- **优点**：压缩效率高、音质好、支持多声道
- **缺点**：编解码复杂度高、旧设备兼容性差

## 7. Android 中的音频处理

### 7.1 音频播放

```kotlin
// 使用 ExoPlayer 播放音频
val player = ExoPlayer.Builder(context).build()
val mediaItem = MediaItem.fromUri(audioUri)
player.setMediaItem(mediaItem)
player.prepare()
player.play()

// 播放完成后释放
player.release()
```

### 7.2 音频录制

```kotlin
// 录制 PCM 音频
val recorder = AudioRecord.Builder()
    .setAudioSource(MediaRecorder.AudioSource.MIC)
    .setAudioFormat(AudioFormat.Builder()
        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
        .setSampleRate(44100)
        .setChannelMask(AudioFormat.CHANNEL_IN_STEREO)
        .build())
    .setBufferSizeInBytes(bufferSize)
    .build()

recorder.startRecording()
// 读取 PCM 数据
val buffer = ByteArray(1024)
recorder.read(buffer, 0, buffer.size)
recorder.stop()
recorder.release()
```

### 7.3 音频合成

```kotlin
// 使用 MediaMuxer 合成视频和音频
val muxer = MediaMuxer(outputPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)

// 添加视频轨道
val videoTrackIndex = muxer.addTrack(videoFormat)

// 添加 AAC 音频轨道
val audioTrackIndex = muxer.addTrack(aacFormat)

muxer.start()

// 写入数据
muxer.writeSampleData(videoTrackIndex, videoBuffer, videoBufferInfo)
muxer.writeSampleData(audioTrackIndex, audioBuffer, audioBufferInfo)

muxer.stop()
muxer.release()
```

## 8. 最佳实践

### 8.1 选择合适的音频格式

| 场景 | 推荐格式 | 推荐比特率 |
|------|----------|------------|
| 音乐播放 | AAC | 192-256 kbps |
| 语音记录 | AAC | 64-128 kbps |
| 视频配音 | AAC | 128-192 kbps |
| 专业录音 | PCM | 44.1kHz/16bit |
| 网络流媒体 | AAC | 128-160 kbps |

### 8.2 性能优化

1. **使用硬件编解码**：
   ```kotlin
   val codec = MediaCodec.createEncoderByType("audio/mp4a-latm")
   // 硬件编码器会自动选择
   ```

2. **合理设置缓冲区大小**：
   ```kotlin
   val bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelMask, encoding)
   ```

3. **异步处理**：
   ```kotlin
   CoroutineScope(Dispatchers.IO).launch {
       // 处理音频数据
   }
   ```

4. **使用直接缓冲区**：
   ```kotlin
   val buffer = ByteBuffer.allocateDirect(bufferSize)
   ```

### 8.3 错误处理

```kotlin
try {
    // 音频处理操作
} catch (e: MediaCodec.CodecException) {
    Log.e(TAG, "Codec error: ${e.errorCode}")
} catch (e: IOException) {
    Log.e(TAG, "IO error: ${e.message}")
} catch (e: Exception) {
    Log.e(TAG, "Unknown error: ${e.message}")
}
```

### 8.4 兼容性考虑

1. **检测设备支持**：
   ```kotlin
   val codecList = MediaCodecList(MediaCodecList.ALL_CODECS)
   val supported = codecList.findEncoderForFormat(format) != null
   ```

2. **降级策略**：
   ```kotlin
   if (!isAacSupported) {
       // 降级为 MP3 或其他格式
   }
   ```

## 9. 常见问题与解决方案

### 9.1 音频不同步

**原因**：时间戳错误或缓冲区问题

**解决方案**：
- 确保音视频时间戳一致
- 正确设置缓冲区大小
- 使用固定的时间戳增量

### 9.2 音频质量差

**原因**：比特率过低或编码参数不当

**解决方案**：
- 提高比特率（推荐 128kbps 以上）
- 使用合适的编码配置文件
- 确保采样率与原始音频一致

### 9.3 编码速度慢

**原因**：软件编码或设备性能不足

**解决方案**：
- 使用硬件编码器
- 降低编码复杂度
- 在后台线程处理编码

### 9.4 内存占用高

**原因**：缓冲区过大或频繁创建对象

**解决方案**：
- 合理设置缓冲区大小
- 复用缓冲区对象
- 使用直接缓冲区

## 10. 总结

### 10.1 格式选择指南

- **PCM**：需要最高音质、专业音频处理
- **AAC**：现代应用、移动设备、流媒体
- **MP3**：需要广泛兼容性、旧设备支持

### 10.2 技术趋势

- **AAC** 成为主流：更高的压缩效率和音质
- **Opus** 崛起：低延迟、高音质，适合实时通信
- **硬件加速**：几乎所有现代设备都支持硬件编解码
- **AI 音频编码**：基于机器学习的音频压缩技术

### 10.3 未来发展

- **空间音频**：支持 3D 环绕声
- **个性化编码**：根据设备和用户偏好自动调整
- **无损压缩**：FLAC、ALAC 等无损格式的普及
- **多声道音频**：支持 5.1、7.1 等多声道格式

通过理解这些音频格式的特性和使用方法，开发者可以在 Android 应用中实现高质量的音频处理功能，提供更好的用户体验。

# 🎬 Android MediaCodec 完全指南

> 深入理解 Android 原生编解码 API，掌握音视频处理核心技术！

---

## 📚 目录

1. [MediaCodec 是什么？](#mediacodec-是什么)
2. [核心概念](#核心概念)
3. [MediaCodec 工作原理](#mediacodec-工作原理)
4. [视频解码流程](#视频解码流程)
5. [视频编码流程](#视频编码流程)
6. [Surface 模式编解码](#surface-模式编解码)
7. [音频编解码](#音频编解码)
8. [常见问题与调试](#常见问题与调试)
9. [最佳实践](#最佳实践)

---

## 🎯 MediaCodec 是什么？

### 官方定义

**MediaCodec** 是 Android 提供的用于音视频编解码的底层 API，允许应用程序访问设备硬件加速的编解码器。

### 简单理解

```
MediaCodec 就像一个黑盒子：
┌────────────────────────────────────────┐
│          MediaCodec                    │
│  ┌─────────────┐      ┌─────────────┐  │
│  │   编码器    │      │   解码器    │  │
│  │ Encoder     │      │ Decoder     │  │
│  │             │      │             │  │
│  │ 原始数据 →  │      │ 压缩数据 →  │  │
│  │  压缩数据   │      │  原始数据   │  │
│  └─────────────┘      └─────────────┘  │
└────────────────────────────────────────┘
```

### 为什么需要 MediaCodec？

| 场景 | 说明 |
|------|------|
| 📹 **视频播放** | 解码网络视频、本地视频文件 |
| 🎥 **视频录制** | 编码摄像头采集的视频 |
| 🔄 **视频转码** | 格式转换、分辨率调整 |
| 📞 **视频通话** | 实时编解码 |

---

## 🎯 核心概念

### 1. 编解码器（Codec）

编解码器是 MediaCodec 的核心组件：

```kotlin
// 获取可用的编解码器列表
val mediaCodecList = MediaCodecList(MediaCodecList.ALL_CODECS)
for (codecInfo in mediaCodecList.codecInfos) {
    // 过滤视频编码器
    if (codecInfo.isEncoder && codecInfo.supportedTypes.contains("video/avc")) {
        println("编码器: ${codecInfo.name}")
    }
}
```

### 2. MediaFormat

`MediaFormat` 描述了音视频数据的格式信息：

```kotlin
// 创建视频格式
val videoFormat = MediaFormat.createVideoFormat("video/avc", 1280, 720).apply {
    setInteger(MediaFormat.KEY_BIT_RATE, 5_000_000)      // 比特率
    setInteger(MediaFormat.KEY_FRAME_RATE, 30)           // 帧率
    setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 2)      // 关键帧间隔
    setInteger(
        MediaFormat.KEY_COLOR_FORMAT,
        MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface
    )
}

// 创建音频格式
val audioFormat = MediaFormat.createAudioFormat("audio/mp4a-latm", 44100, 2).apply {
    setInteger(MediaFormat.KEY_BIT_RATE, 128_000)        // 音频比特率
}
```

### 3. 缓冲区（Buffer）

MediaCodec 使用缓冲区来处理数据：

```
缓冲区模型：
┌─────────────────────────────────────────────────────┐
│                   MediaCodec                        │
│                                                     │
│   ┌─────────────┐              ┌─────────────┐     │
│   │  Input      │  处理过程    │  Output     │     │
│   │  Buffers    │ ─────────→   │  Buffers    │     │
│   │  (压缩数据) │              │  (原始数据) │     │
│   └─────────────┘              └─────────────┘     │
│         ↑                              │            │
│         │                              ↓            │
│   dequeueInputBuffer            dequeueOutputBuffer │
│   queueInputBuffer              releaseOutputBuffer │
│                                                     │
└─────────────────────────────────────────────────────┘
```

### 4. 缓冲区信息（BufferInfo）

`MediaCodec.BufferInfo` 包含缓冲区的关键信息：

```kotlin
val bufferInfo = MediaCodec.BufferInfo()
// bufferInfo.offset    - 数据在缓冲区中的偏移量
// bufferInfo.size      - 数据大小
// bufferInfo.presentationTimeUs - 显示时间戳（微秒）
// bufferInfo.flags     - 标志位（是否关键帧、是否结束等）
```

---

## 🎯 MediaCodec 工作原理

### 状态机

MediaCodec 有严格的状态转换：

```
                    ┌─────────────────────────┐
                    │                         │
                    ▼                         │
┌──────────┐     ┌──────────┐     ┌──────────┐│     ┌──────────┐
│  Uninitialized │ → configure() →│  Configured │→ start() →│  Started  │
└──────────┘     └──────────┘     └──────────┘│     └────┬─────┘
       │                                      │          │
       │                ┌──────────────────────┘          │
       │                │                                │
       │                ▼                                ▼
       │         ┌──────────┐                    ┌──────────┐
       └──────→  │  Released │←───────── stop() ─│  Flushed │
                 └──────────┘                    └──────────┘
```

### 状态说明

| 状态 | 说明 | 允许操作 |
|------|------|----------|
| **Uninitialized** | 初始状态 | `configure()` |
| **Configured** | 已配置 | `start()` |
| **Started** | 运行中 | 编解码操作 |
| **Flushed** | 刷新中 | `stop()` |
| **Released** | 已释放 | 无 |

---

## 🎯 视频解码流程

### 完整解码流程

```
┌─────────────────┐     ┌─────────────────┐     ┌─────────────────┐
│  1. 创建解码器  │ →   │  2. 配置解码器  │ →   │  3. 启动解码器  │
└────────┬────────┘     └────────┬────────┘     └────────┬────────┘
         │                       │                       │
         ▼                       ▼                       ▼
┌─────────────────┐     ┌─────────────────┐     ┌─────────────────┐
│  4. 获取输入    │ →   │  5. 处理输出    │ →   │  6. 释放资源    │
│  缓冲区并填充   │     │  缓冲区并渲染   │     │                 │
└─────────────────┘     └─────────────────┘     └─────────────────┘
```

### 代码实现

```kotlin
class VideoDecoder {

    private var decoder: MediaCodec? = null
    private var extractor: MediaExtractor? = null
    private var surface: Surface? = null

    /**
     * 初始化解码器
     * @param inputPath 输入视频文件路径
     * @param outputSurface 用于渲染的 Surface
     */
    fun initialize(inputPath: String, outputSurface: Surface) {
        this.surface = outputSurface

        // 步骤1：创建 MediaExtractor 提取视频数据
        extractor = MediaExtractor().apply {
            setDataSource(inputPath)
        }

        // 步骤2：找到视频轨道
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
            throw IllegalArgumentException("未找到视频轨道")
        }

        extractor!!.selectTrack(videoTrackIndex)
        val videoFormat = extractor!!.getTrackFormat(videoTrackIndex)

        // 步骤3：创建解码器
        val mimeType = videoFormat.getString(MediaFormat.KEY_MIME) ?: "video/avc"
        decoder = MediaCodec.createDecoderByType(mimeType)

        // 步骤4：配置解码器
        decoder!!.configure(videoFormat, surface, null, 0)

        // 步骤5：启动解码器
        decoder!!.start()
    }

    /**
     * 开始解码循环
     */
    fun startDecoding() {
        val bufferInfo = MediaCodec.BufferInfo()
        var isEOS = false

        while (!isEOS) {
            // ========== 输入阶段 ==========
            if (!isEOS) {
                // 获取可用的输入缓冲区
                val inputBufferIndex = decoder!!.dequeueInputBuffer(10000) // 10ms 超时

                if (inputBufferIndex >= 0) {
                    // 获取输入缓冲区
                    val inputBuffer = decoder!!.getInputBuffer(inputBufferIndex)

                    if (inputBuffer != null) {
                        // 从 extractor 读取数据到缓冲区
                        val sampleSize = extractor!!.readSampleData(inputBuffer, 0)

                        if (sampleSize < 0) {
                            // 数据读取完毕，发送 EOS 信号
                            decoder!!.queueInputBuffer(
                                inputBufferIndex,
                                0,
                                0,
                                0,
                                MediaCodec.BUFFER_FLAG_END_OF_STREAM
                            )
                            isEOS = true
                        } else {
                            // 发送数据到解码器
                            decoder!!.queueInputBuffer(
                                inputBufferIndex,
                                0,
                                sampleSize,
                                extractor!!.sampleTime,
                                0
                            )
                            // 移动到下一个样本
                            extractor!!.advance()
                        }
                    }
                }
            }

            // ========== 输出阶段 ==========
            val outputBufferIndex = decoder!!.dequeueOutputBuffer(bufferInfo, 10000)

            when (outputBufferIndex) {
                MediaCodec.INFO_TRY_AGAIN_LATER -> {
                    // 没有可用输出，稍后重试
                }
                MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> {
                    // 输出格式改变（第一次输出前触发）
                    val newFormat = decoder!!.outputFormat
                    println("输出格式: $newFormat")
                }
                else -> {
                    if (outputBufferIndex >= 0) {
                        // 渲染到 Surface
                        // 第二个参数 true 表示渲染后自动释放缓冲区
                        decoder!!.releaseOutputBuffer(outputBufferIndex, true)

                        // 检查是否到达末尾
                        if (bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) {
                            isEOS = true
                        }
                    }
                }
            }
        }
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
}
```

---

## 🎯 视频编码流程

### 完整编码流程

```
┌─────────────────┐     ┌─────────────────┐     ┌─────────────────┐
│  1. 创建编码器  │ →   │  2. 配置编码器  │ →   │  3. 启动编码器  │
└────────┬────────┘     └────────┬────────┘     └────────┬────────┘
         │                       │                       │
         ▼                       ▼                       ▼
┌─────────────────┐     ┌─────────────────┐     ┌─────────────────┐
│  4. 输入原始    │ →   │  5. 获取编码    │ →   │  6. 释放资源    │
│  视频数据      │     │  后的数据      │     │                 │
└─────────────────┘     └─────────────────┘     └─────────────────┘
```

### 代码实现

```kotlin
class VideoEncoder {

    private var encoder: MediaCodec? = null
    private var muxer: MediaMuxer? = null
    private var trackIndex = -1
    private var isStarted = false

    /**
     * 初始化编码器
     * @param outputPath 输出文件路径
     * @param width 视频宽度
     * @param height 视频高度
     * @param bitRate 比特率（bps）
     * @param frameRate 帧率
     */
    fun initialize(
        outputPath: String,
        width: Int = 1280,
        height: Int = 720,
        bitRate: Int = 5_000_000,
        frameRate: Int = 30
    ) {
        // 步骤1：创建编码格式
        val format = MediaFormat.createVideoFormat("video/avc", width, height).apply {
            setInteger(MediaFormat.KEY_BIT_RATE, bitRate)
            setInteger(MediaFormat.KEY_FRAME_RATE, frameRate)
            setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 2)
            setInteger(
                MediaFormat.KEY_COLOR_FORMAT,
                MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface
            )
        }

        // 步骤2：创建编码器
        encoder = MediaCodec.createEncoderByType("video/avc")

        // 步骤3：配置编码器（CONFIGURE_FLAG_ENCODE 表示这是编码器）
        encoder!!.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)

        // 步骤4：创建 MediaMuxer
        muxer = MediaMuxer(outputPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)

        // 步骤5：启动编码器
        encoder!!.start()
    }

    /**
     * 获取输入 Surface（用于绘制帧数据）
     */
    fun getInputSurface(): Surface? {
        return encoder?.createInputSurface()
    }

    /**
     * 编码一帧
     * @param presentationTimeUs 时间戳（微秒）
     */
    fun encodeFrame(presentationTimeUs: Long) {
        // 通知编码器有新帧可用
        encoder?.signalEndOfInputStream()

        val bufferInfo = MediaCodec.BufferInfo()
        var outputBufferIndex = encoder!!.dequeueOutputBuffer(bufferInfo, 10000)

        while (outputBufferIndex >= 0) {
            val outputBuffer = encoder!!.getOutputBuffer(outputBufferIndex)

            if (outputBuffer != null) {
                // 处理输出数据
                if (bufferInfo.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG != 0) {
                    // 这是编解码器配置数据（SPS/PPS），不写入文件
                    bufferInfo.size = 0
                }

                if (bufferInfo.size > 0) {
                    // 调整缓冲区位置
                    outputBuffer.position(bufferInfo.offset)
                    outputBuffer.limit(bufferInfo.offset + bufferInfo.size)

                    if (!isStarted) {
                        // 第一次输出，添加轨道
                        val format = encoder!!.outputFormat
                        trackIndex = muxer!!.addTrack(format)
                        muxer!!.start()
                        isStarted = true
                    }

                    // 写入 muxer
                    muxer!!.writeSampleData(trackIndex, outputBuffer, bufferInfo)
                }

                // 释放输出缓冲区
                encoder!!.releaseOutputBuffer(outputBufferIndex, false)
            }

            outputBufferIndex = encoder!!.dequeueOutputBuffer(bufferInfo, 0)
        }
    }

    /**
     * 完成编码
     */
    fun finish() {
        // 发送 EOS 信号
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
```

---

## 🎯 Surface 模式编解码

### Surface 模式的优势

Surface 模式是一种零拷贝的高效编解码方式：

```
传统模式（数据拷贝）：
┌──────────┐   copy   ┌──────────┐   copy   ┌──────────┐
│ Camera   │ ──────→ │  ByteBuffer │ ──────→ │ MediaCodec│
└──────────┘          └──────────┘          └──────────┘

Surface 模式（零拷贝）：
┌──────────┐           ┌──────────┐
│ Camera   │ ──────→   │ MediaCodec│
│ (直接渲染)│           │ (直接读取)│
└──────────┘           └──────────┘
```

### Surface 解码示例

```kotlin
class SurfaceDecoder {

    fun decodeToSurface(inputPath: String, surface: Surface) {
        val extractor = MediaExtractor().apply {
            setDataSource(inputPath)
        }

        // 找到视频轨道
        var videoTrackIndex = -1
        for (i in 0 until extractor.trackCount) {
            val format = extractor.getTrackFormat(i)
            if (format.getString(MediaFormat.KEY_MIME)?.startsWith("video/") == true) {
                videoTrackIndex = i
                break
            }
        }

        extractor.selectTrack(videoTrackIndex)
        val format = extractor.getTrackFormat(videoTrackIndex)
        val mimeType = format.getString(MediaFormat.KEY_MIME) ?: "video/avc"

        // 创建解码器并配置到 Surface
        val decoder = MediaCodec.createDecoderByType(mimeType)
        decoder.configure(format, surface, null, 0)
        decoder.start()

        // 解码循环...
        // （省略，与前面的解码器类似）
    }
}
```

### Surface 编码示例

```kotlin
class SurfaceEncoder {

    fun encodeFromSurface(outputPath: String, width: Int, height: Int): Surface {
        val format = MediaFormat.createVideoFormat("video/avc", width, height).apply {
            setInteger(MediaFormat.KEY_BIT_RATE, 5_000_000)
            setInteger(MediaFormat.KEY_FRAME_RATE, 30)
            setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 2)
            setInteger(
                MediaFormat.KEY_COLOR_FORMAT,
                MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface
            )
        }

        val encoder = MediaCodec.createEncoderByType("video/avc")
        encoder.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
        encoder.start()

        // 返回用于绘制的 Surface
        return encoder.createInputSurface()
    }
}
```

---

## 🎯 音频编解码

### 音频格式参数

| 参数 | 说明 | 常用值 |
|------|------|--------|
| **MIME 类型** | 音频编码格式 | `audio/mp4a-latm` (AAC) |
| **采样率** | 每秒采样次数 | 44100Hz, 48000Hz |
| **声道数** | 单声道/立体声 | 1 (单), 2 (立体声) |
| **比特率** | 每秒数据量 | 128000 (128kbps) |

### 音频解码示例

```kotlin
class AudioDecoder {

    fun decodeAudio(inputPath: String, outputPath: String) {
        val extractor = MediaExtractor().apply {
            setDataSource(inputPath)
        }

        // 找到音频轨道
        var audioTrackIndex = -1
        for (i in 0 until extractor.trackCount) {
            val format = extractor.getTrackFormat(i)
            if (format.getString(MediaFormat.KEY_MIME)?.startsWith("audio/") == true) {
                audioTrackIndex = i
                break
            }
        }

        extractor.selectTrack(audioTrackIndex)
        val format = extractor.getTrackFormat(audioTrackIndex)
        val mimeType = format.getString(MediaFormat.KEY_MIME) ?: "audio/mp4a-latm"

        // 创建解码器
        val decoder = MediaCodec.createDecoderByType(mimeType)
        decoder.configure(format, null, null, 0)
        decoder.start()

        // 解码循环
        val bufferInfo = MediaCodec.BufferInfo()
        var isEOS = false

        while (!isEOS) {
            // 输入
            val inputBufferIndex = decoder.dequeueInputBuffer(10000)
            if (inputBufferIndex >= 0) {
                val inputBuffer = decoder.getInputBuffer(inputBufferIndex)
                if (inputBuffer != null) {
                    val size = extractor.readSampleData(inputBuffer, 0)
                    if (size < 0) {
                        decoder.queueInputBuffer(
                            inputBufferIndex, 0, 0, 0,
                            MediaCodec.BUFFER_FLAG_END_OF_STREAM
                        )
                        isEOS = true
                    } else {
                        decoder.queueInputBuffer(
                            inputBufferIndex, 0, size, extractor.sampleTime, 0
                        )
                        extractor.advance()
                    }
                }
            }

            // 输出
            val outputBufferIndex = decoder.dequeueOutputBuffer(bufferInfo, 10000)
            if (outputBufferIndex >= 0) {
                val outputBuffer = decoder.getOutputBuffer(outputBufferIndex)
                if (outputBuffer != null) {
                    // 处理解码后的 PCM 数据
                    // outputBuffer 包含原始 PCM 音频数据
                }
                decoder.releaseOutputBuffer(outputBufferIndex, false)
            }
        }

        decoder.stop()
        decoder.release()
        extractor.release()
    }
}
```

### 音频编码示例

```kotlin
class AudioEncoder {

    fun encodeAudio(inputPcmPath: String, outputPath: String) {
        // 创建编码格式
        val format = MediaFormat.createAudioFormat("audio/mp4a-latm", 44100, 2).apply {
            setInteger(MediaFormat.KEY_BIT_RATE, 128_000)
        }

        // 创建编码器
        val encoder = MediaCodec.createEncoderByType("audio/mp4a-latm")
        encoder.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
        encoder.start()

        // 创建 muxer
        val muxer = MediaMuxer(outputPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)

        // 读取 PCM 数据并编码
        // ... (省略文件读取逻辑)

        encoder.stop()
        encoder.release()
        muxer.stop()
        muxer.release()
    }
}
```

---

## 🎯 常见问题与调试

### 问题1：编解码器创建失败

```kotlin
// 错误：
// java.lang.IllegalArgumentException: Failed to find configured codec

// 解决方案：检查 MIME 类型是否正确
val mimeType = "video/avc"  // 正确
// val mimeType = "video/h264" // 错误！
```

### 问题2：缓冲区操作异常

```kotlin
// 错误：
// java.lang.IllegalStateException: Buffer is not owned by codec

// 解决方案：确保正确处理缓冲区生命周期
// 输入缓冲区：queueInputBuffer 后自动释放
// 输出缓冲区：releaseOutputBuffer 后释放
```

### 问题3：视频花屏或绿边

```kotlin
// 原因：颜色格式不匹配

// 解决方案：使用正确的颜色格式
val colorFormat = MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface
// 或使用编解码器支持的格式
```

### 问题4：时间戳错误

```kotlin
// 错误：视频播放速度异常

// 解决方案：使用正确的时间戳单位
// presentationTimeUs 必须是微秒（microseconds）
val timeUs = System.nanoTime() / 1000 // 纳秒转微秒
```

### 调试技巧

```kotlin
// 1. 打印编解码器信息
val codecList = MediaCodecList(MediaCodecList.ALL_CODECS)
for (codec in codecList.codecInfos) {
    Log.d("Codec", "Name: ${codec.name}, Types: ${codec.supportedTypes.joinToString()}")
}

// 2. 打印格式信息
val format = extractor.getTrackFormat(trackIndex)
for (key in format.keySet()) {
    Log.d("Format", "$key: ${format.get(key)}")
}

// 3. 使用 MediaCodecInfo 检查能力
val codecInfo = codecList.findCodecForMimeType("video/avc")
val capabilities = codecInfo?.getCapabilitiesForType("video/avc")
```

---

## 🎯 最佳实践

### 1. 异步处理

```kotlin
// 使用线程池处理编解码
val executor = Executors.newSingleThreadExecutor()

executor.execute {
    // 编解码操作
    decoder.startDecoding()
}
```

### 2. 资源管理

```kotlin
// 使用 try-finally 确保资源释放
var decoder: MediaCodec? = null

try {
    decoder = MediaCodec.createDecoderByType("video/avc")
    // 使用解码器...
} finally {
    decoder?.stop()
    decoder?.release()
}
```

### 3. 版本适配

```kotlin
// 处理不同 Android 版本的差异
if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
    // 新版本 API
} else {
    // 旧版本兼容
}
```

### 4. 错误处理

```kotlin
try {
    // 编解码操作
} catch (e: IOException) {
    Log.e("Codec", "编解码失败", e)
} catch (e: IllegalStateException) {
    Log.e("Codec", "状态错误", e)
}
```

### 5. 性能优化

| 优化项 | 说明 |
|--------|------|
| **使用 Surface 模式** | 避免数据拷贝 |
| **合理设置缓冲区大小** | 平衡内存和性能 |
| **批量处理** | 减少缓冲区操作次数 |
| **硬件加速** | 优先使用硬件编解码器 |

---

## 📝 总结

### MediaCodec 核心要点

```
1️⃣ MediaCodec 是 Android 音视频编解码的核心 API
   └── 支持硬件加速，性能优异

2️⃣ 状态机管理至关重要
   ├── configure() → start() → stop() → release()
   └── 严格遵循状态转换

3️⃣ 两种工作模式
   ├── ByteBuffer 模式：灵活但有拷贝开销
   └── Surface 模式：零拷贝，推荐使用

4️⃣ 缓冲区操作是关键
   ├── dequeueInputBuffer / queueInputBuffer
   └── dequeueOutputBuffer / releaseOutputBuffer

5️⃣ 时间戳必须正确
   └── presentationTimeUs 使用微秒单位
```

### 学习路径

```
第 1 步：理解状态机和基本概念
第 2 步：实现简单的视频解码
第 3 步：实现简单的视频编码
第 4 步：掌握 Surface 模式
第 5 步：实现完整的转码器
```

### 代码示例

查看 `basic/`、`intermediate/` 和 `advanced/` 目录中的代码示例，包含完整的可运行代码！

---

> 💡 **提示**：MediaCodec API 相对底层，需要深入理解才能正确使用。建议先从简单的解码示例开始，逐步掌握编码和高级特性。
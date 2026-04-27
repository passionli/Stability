# MediaMuxer 使用指南

## 1. MediaMuxer 简介

MediaMuxer 是 Android SDK 提供的一个用于混合（合成）音视频流的类。它可以将多个音视频轨道合并成一个多媒体文件，支持的输出格式包括 MP4 和 WebM。

### 主要功能
- 将视频轨道和音频轨道合并成一个 MP4 文件
- 支持添加多个音视频轨道
- 支持设置视频方向（rotation）
- 支持 MPEG4、WebM 等输出格式

## 2. 基本用法

### 2.1 创建 MediaMuxer

```kotlin
// 创建 MediaMuxer 实例
// 参数1: 输出文件路径
// 参数2: 输出格式
val muxer = MediaMuxer(outputPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
```

### 2.2 添加轨道

使用 `addTrack()` 方法添加音视频轨道：

```kotlin
// 从 MediaExtractor 获取轨道格式
val videoExtractor = MediaExtractor()
videoExtractor.setDataSource(videoPath)

for (i in 0 until videoExtractor.trackCount) {
    val format = videoExtractor.getTrackFormat(i)
    val mime = format.getString(MediaFormat.KEY_MIME)

    when {
        mime?.startsWith("video/") == true -> {
            // 添加视频轨道
            val videoTrackIndex = muxer.addTrack(format)
            videoExtractor.selectTrack(i)
        }
        mime?.startsWith("audio/") == true -> {
            // 添加音频轨道
            val audioTrackIndex = muxer.addTrack(format)
            audioExtractor.selectTrack(i)
        }
    }
}
```

### 2.3 开始和停止混合

```kotlin
// 开始混合
muxer.start()

// ... 写入数据 ...

// 停止混合
muxer.stop()

// 释放资源
muxer.release()
```

### 2.4 写入媒体数据

使用 `writeSampleData()` 方法写入媒体数据：

```kotlin
val buffer = ByteBuffer.allocate(1024 * 1024)
val bufferInfo = MediaCodec.BufferInfo()

// 设置缓冲区信息
bufferInfo.offset = 0
bufferInfo.size = sampleSize
bufferInfo.presentationTimeUs = presentationTimeUs
bufferInfo.flags = sampleFlags

// 写入数据到指定轨道
muxer.writeSampleData(videoTrackIndex, buffer, bufferInfo)
```

## 3. 完整示例代码

### 3.1 视频音频合成

```kotlin
private fun muxVideoAndAudio(
    videoPath: String,
    audioPath: String,
    outputPath: String
): Boolean {
    var muxer: MediaMuxer? = null
    var videoExtractor: MediaExtractor? = null
    var audioExtractor: MediaExtractor? = null

    try {
        // 创建 MediaMuxer
        muxer = MediaMuxer(outputPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)

        // 初始化视频提取器
        videoExtractor = MediaExtractor()
        videoExtractor.setDataSource(videoPath)

        var videoTrackIndex = -1
        for (i in 0 until videoExtractor.trackCount) {
            val format = videoExtractor.getTrackFormat(i)
            val mime = format.getString(MediaFormat.KEY_MIME)
            if (mime?.startsWith("video/") == true) {
                videoTrackIndex = muxer.addTrack(format)
                videoExtractor.selectTrack(i)
                break
            }
        }

        // 初始化音频提取器
        audioExtractor = MediaExtractor()
        audioExtractor.setDataSource(audioPath)

        var audioTrackIndex = -1
        for (i in 0 until audioExtractor.trackCount) {
            val format = audioExtractor.getTrackFormat(i)
            val mime = format.getString(MediaFormat.KEY_MIME)
            if (mime?.startsWith("audio/") == true) {
                audioTrackIndex = muxer.addTrack(format)
                audioExtractor.selectTrack(i)
                break
            }
        }

        // 开始混合
        muxer.start()

        // 写入视频数据
        val buffer = ByteBuffer.allocate(1024 * 1024)
        val bufferInfo = MediaCodec.BufferInfo()

        // 写入视频轨道
        while (true) {
            val sampleSize = videoExtractor.readSampleData(buffer, 0)
            if (sampleSize < 0) break

            bufferInfo.offset = 0
            bufferInfo.size = sampleSize
            bufferInfo.presentationTimeUs = videoExtractor.sampleTime
            bufferInfo.flags = videoExtractor.sampleFlags

            muxer.writeSampleData(videoTrackIndex, buffer, bufferInfo)
            videoExtractor.advance()
        }

        // 写入音频轨道
        while (true) {
            val sampleSize = audioExtractor.readSampleData(buffer, 0)
            if (sampleSize < 0) break

            bufferInfo.offset = 0
            bufferInfo.size = sampleSize
            bufferInfo.presentationTimeUs = audioExtractor.sampleTime
            bufferInfo.flags = audioExtractor.sampleFlags

            muxer.writeSampleData(audioTrackIndex, buffer, bufferInfo)
            audioExtractor.advance()
        }

        return true
    } catch (e: Exception) {
        e.printStackTrace()
        return false
    } finally {
        muxer?.stop()
        muxer?.release()
        videoExtractor?.release()
        audioExtractor?.release()
    }
}
```

## 4. 支持的输出格式

MediaMuxer 支持以下输出格式：

| 格式 | 常量 | 说明 |
|------|------|------|
| MPEG4 | MUXER_OUTPUT_MPEG_4 | MP4 容器格式 |
| WebM | MUXER_OUTPUT_WEBM | WebM 容器格式（仅支持 Android 26+）|

### 音频编解码器支持

MediaMuxer 对音频格式有严格要求：

| 音频格式 | 支持情况 | 说明 |
|----------|----------|------|
| AAC | 支持 | Android 推荐使用的音频格式 |
| MP3 | 不支持 | 需要转换才能使用 |
| OGG | 部分支持 | 取决于设备 |
| FLAC | 部分支持 | 取决于设备 |

### 视频编解码器支持

| 视频格式 | 支持情况 | 说明 |
|----------|----------|------|
| H.264 | 支持 | 最常用的视频格式 |
| H.265/HEVC | 支持 | 需要设备支持 |
| VP8/VP9 | 支持 | WebM 格式使用 |
| AV1 | 部分支持 | 需要 Android 29+ |

## 5. 常见问题与解决方案

### 5.1 "Failed to instantiate extractor"

**问题原因**：无法创建 MediaExtractor 实例

**解决方案**：
1. 检查文件路径是否正确
2. 确保文件存在且可读
3. 验证文件格式是否被支持

### 5.2 "Only the original thread that created a view hierarchy can touch its views"

**问题原因**：在非主线程更新 UI

**解决方案**：
```kotlin
// 使用 Handler 在主线程更新 UI
Handler(Looper.getMainLooper()).post {
    progressBar.progress = progress
    statusText.text = "正在处理..."
}
```

### 5.3 "audio/mpeg is not supported"

**问题原因**：MediaMuxer 不支持 MP3 格式

**解决方案**：
1. 将 MP3 转换为 AAC 格式
2. 使用 MediaCodec 进行解码和重新编码

```kotlin
// MP3 到 AAC 的转换流程
val decoder = MediaCodec.createDecoderByType("audio/mpeg")
decoder.configure(inputFormat, null, null, 0)
decoder.start()

val encoder = MediaCodec.createEncoderByType("audio/mp4a-latm")
encoder.configure(outputFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
encoder.start()

// 解码 -> 编码 -> 写入 Muxer
```

### 5.4 "Missing codec specific data"

**问题原因**：缺少编解码器配置数据

**解决方案**：
1. 确保在开始混合前获取编码器输出格式
2. 跳过编解码器配置数据（BUFFER_FLAG_CODEC_CONFIG）

```kotlin
if (bufferInfo.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG != 0) {
    bufferInfo.size = 0
}
```

### 5.5 "Track event err/info msg"

**问题原因**：音视频轨道同步问题或格式不支持

**解决方案**：
1. 确保音视频时间戳正确
2. 检查设备是否支持该音视频格式
3. 尝试只导出视频来确认视频轨道正常

## 6. 最佳实践

### 6.1 资源管理

```kotlin
try {
    // 初始化和操作
} finally {
    // 确保资源释放
    muxer?.stop()
    muxer?.release()
    videoExtractor?.release()
    audioExtractor?.release()
}
```

### 6.2 错误处理

```kotlin
try {
    // 操作
} catch (e: Exception) {
    Log.e(TAG, "Error: ${e.message}")
    e.printStackTrace()
    // 清理临时文件
    outputFile.delete()
    throw e
}
```

### 6.3 进度跟踪

```kotlin
val totalDuration = videoDuration + audioDuration
var currentProgress = 0

while (processing) {
    val progress = (currentPosition * 100 / totalDuration).toInt()
    Handler(Looper.getMainLooper()).post {
        progressBar.progress = progress
    }
}
```

### 6.4 缓冲区管理

```kotlin
// 使用足够大的缓冲区
val bufferSize = 1024 * 1024 // 1MB
val buffer = ByteBuffer.allocate(bufferSize)

// 确保缓冲区容量足够
if (sampleSize > buffer.capacity()) {
    // 处理异常或使用更大的缓冲区
}
```

### 6.5 时间戳处理

```kotlin
// 确保时间戳单调递增
var lastPresentationTimeUs = 0L
if (presentationTimeUs > lastPresentationTimeUs) {
    lastPresentationTimeUs = presentationTimeUs
} else {
    presentationTimeUs = lastPresentationTimeUs
}
```

## 7. MediaMuxer 与 MediaMultiplexer 的区别

Android 14 引入了 MediaMultiplexer，它是 MediaMuxer 的现代替代品：

| 特性 | MediaMuxer | MediaMultiplexer |
|------|------------|------------------|
| API 级别 | 18+ | 35+ |
| 异步操作 | 不支持 | 支持 |
| 错误处理 | 手动 | 内置 |
| 多轨道支持 | 基础 | 增强 |

### 迁移到 MediaMultiplexer

```kotlin
// 旧代码 (MediaMuxer)
val muxer = MediaMuxer(outputPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
muxer.start()
// ... 同步写入 ...
muxer.stop()
muxer.release()

// 新代码 (MediaMultiplexer - Android 35+)
val muxer = MediaMultiplexer.Builder(outputPath).build()
muxer.start()
// ... 异步操作 ...
muxer.stop()
muxer.release()
```

## 8. 性能优化建议

### 8.1 缓冲区大小

- 使用 1MB 缓冲区通常是一个好的选择
- 过小的缓冲区会导致频繁的 I/O 操作
- 过大的缓冲区会占用过多内存

### 8.2 避免频繁创建对象

```kotlin
// 推荐：在循环外创建缓冲区
val buffer = ByteBuffer.allocate(BUFFER_SIZE)
val bufferInfo = MediaCodec.BufferInfo()

while (processing) {
    // 使用同一个缓冲区
    videoExtractor.readSampleData(buffer, 0)
}
```

### 8.3 使用直接缓冲区

```kotlin
// 使用直接缓冲区减少内存拷贝
val buffer = ByteBuffer.allocateDirect(BUFFER_SIZE)
```

### 8.4 多线程处理

对于大量文件处理，可以考虑使用线程池：

```kotlin
val executor = Executors.newFixedThreadPool(4)
executor.submit { processVideo(videoPath) }
executor.submit { processAudio(audioPath) }
executor.shutdown()
```

## 9. 总结

MediaMuxer 是 Android 音视频处理的核心组件之一，主要用于：
- 视频音频合成
- 多轨道混合
- 格式转换

使用时需要注意：
1. **格式支持**：确保音视频格式被 MediaMuxer 支持
2. **资源管理**：正确释放所有资源
3. **错误处理**：做好异常捕获和清理
4. **时间戳**：确保音视频同步正确
5. **线程安全**：UI 操作必须在主线程

如果遇到格式不支持的问题，可以使用 MediaCodec 进行解码和重新编码，将不支持的格式（如 MP3）转换为支持的格式（如 AAC）。

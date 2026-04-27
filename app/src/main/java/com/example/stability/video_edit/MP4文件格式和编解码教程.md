# 🎬 MP4 文件格式和编解码完整教程

> 专为新手编写，通俗易懂！

---

## 📚 目录

1. [什么是 MP4 文件？](#-什么是-mp4-文件)
2. [MP4 文件结构详解](#-mp4-文件结构详解)
3. [视频编解码基础](#-视频编解码基础)
4. [音频编解码基础](#-音频编解码基础)
5. [Android 编解码 API 介绍](#-android-编解码-api-介绍)
6. [完整的解码流程](#-完整的解码流程)
7. [完整的编码流程](#-完整的编码流程)
8. [实际应用示例](#-实际应用示例)

---

## 🎯 什么是 MP4 文件？

### 简单理解
**MP4 就像是一个"文件盒子"**，里面装着两样东西：
- 🎞️ **视频轨道**：视频画面
- 🔊 **音频轨道**：声音

### 正式定义
- MP4 是 **MPEG-4 Part 14** 的缩写
- 它是一种 **容器格式**（Container Format）
- 可以同时容纳多种类型的媒体数据
- 文件扩展名通常是 `.mp4`

### 生活中的比喻
想象 MP4 文件是一个**快递包裹**：
```
📦 包裹 = MP4 文件
├── 📽️ 视频文件 = 视频轨道
├── 🎵 音频文件 = 音频轨道
└── 📋 清单 = 元数据（时长、分辨率等）
```

---

## 📦 MP4 文件结构详解

### 原子（Atoms/Boxes）概念

MP4 文件由一个个 **原子（Atom）** 组成，就像积木一样！

#### 原子结构
每个原子都有相同的结构：
```
┌─────────────────────────────────────┐
│  大小 (4字节)  │  类型 (4字节)       │
├─────────────────────────────────────┤
│           数据 (n 字节)              │
└─────────────────────────────────────┘
```

### 重要的原子类型

| 原子类型 | 英文名称 | 中文说明 | 作用 |
|---------|---------|---------|------|
| `ftyp` | File Type Box | 文件类型盒子 | 告诉播放器这是 MP4 文件 |
| `moov` | Movie Box | 电影盒子 | 包含所有元数据的根盒子 |
| `mdat` | Media Data Box | 媒体数据盒子 | 存放实际的音视频数据 |
| `moof` | Movie Fragment Box | 电影片段盒子 | 用于流媒体的片段 |
| `trak` | Track Box | 轨道盒子 | 一个视频或音频轨道 |
| `mdia` | Media Box | 媒体盒子 | 轨道的媒体信息 |
| `stbl` | Sample Table Box | 样本表盒子 | 描述样本位置和时间 |

### 完整的 MP4 文件结构树

```
📁 MP4 文件
├── 📦 ftyp (文件类型)
├── 📦 moov (电影元数据)
│   ├── 📦 mvhd (电影头)
│   ├── 📦 trak (视频轨道 1)
│   │   ├── 📦 tkhd (轨道头)
│   │   └── 📦 mdia (媒体信息)
│   │       ├── 📦 mdhd (媒体头)
│   │       ├── 📦 hdlr (处理者)
│   │       └── 📦 minf (媒体信息)
│   │           ├── 📦 smhd (声音媒体头)
│   │           ├── 📦 dinf (数据信息)
│   │           └── 📦 stbl (样本表)
│   │               ├── 📦 stsd (样本描述)
│   │               ├── 📦 stts (时间-样本)
│   │               ├── 📦 stsc (样本-块)
│   │               ├── 📦 stsz (样本大小)
│   │               └── 📦 stco (块偏移)
│   └── 📦 trak (音频轨道 2)
│       └── (类似视频轨道结构...)
└── 📦 mdat (实际媒体数据)
    ├── 视频帧 1
    ├── 视频帧 2
    ├── 音频帧 1
    ├── ...
```

### 实际例子

假设我们有一个 10 秒的视频，结构如下：

```
ftyp  → 告诉播放器：这是 MP4
moov  → 元数据
   ├── mvhd: 时长=10秒, 时间刻度=1000
   ├── trak1: 视频轨道
   │   ├── tkhd: 1280x720, 30fps
   │   └── stbl: 说明每个视频帧在哪里
   └── trak2: 音频轨道
       ├── tkhd: 44100Hz, 立体声
       └── stbl: 说明每个音频帧在哪里
mdat  → 实际的视频和音频数据
```

---

## 🎞️ 视频编解码基础

### 什么是编码？

**编码**就是把原始视频数据**压缩变小**的过程。

#### 为什么要编码？

原始视频数据太大了！看个例子：

```
原始 720p 视频：
- 分辨率：1280 × 720 = 921,600 像素
- 每像素：3 字节（RGB）
- 每秒：30 帧

每秒数据量 = 1280 × 720 × 3 × 30 ≈ 81 MB/秒
10 秒视频 = 810 MB！😱

编码后（H.264）：
- 大概 2-5 MB/秒
- 10 秒视频 = 20-50 MB ✨
```

### 常见的视频编码格式

| 格式 | 全称 | 说明 | 特点 |
|-----|------|------|------|
| **H.264** | MPEG-4 AVC | 最常用 | 兼容性好，效率高 |
| **H.265** | HEVC | 新一代 | 更高效，文件更小 |
| **VP9** | Google VP9 | 开源 | Google 推广 |

### 视频编码的原理（简化版）

#### 1. 帧类型

| 帧类型 | 名称 | 说明 | 大小 |
|-------|------|------|------|
| **I 帧** | 关键帧（Intra Frame） | 完整的一张图片 | 最大 |
| **P 帧** | 预测帧（Predictive Frame） | 参考前一帧的差异 | 较小 |
| **B 帧** | 双向预测帧（Bidirectional） | 参考前后帧 | 最小 |

#### 2. 实际例子

```
视频序列：
I(0ms) → P(33ms) → P(66ms) → B(100ms) → P(133ms) → I(166ms)
 ↑         ↑        ↑         ↑          ↑         ↑
完整    参考I帧   参考P帧   双向参考    参考I帧    新关键帧
图片    只存差异   存差异    存差异      存差异    完整图片
```

### 什么是解码？

**解码**就是**编码的逆过程**，把压缩的数据变回原始视频帧。

```
编码过程：
原始视频帧 → 编码器 → 压缩数据

解码过程：
压缩数据 → 解码器 → 原始视频帧
```

---

## 🔊 音频编解码基础

### 常见的音频编码格式

| 格式 | 全称 | 说明 | 使用场景 |
|-----|------|------|---------|
| **AAC** | Advanced Audio Coding | 最常用 | MP4、YouTube |
| **MP3** | MPEG-1 Audio Layer 3 | 经典 | 音乐文件 |
| **OPUS** | Opus | 新格式 | 视频通话、音乐 |

### AAC 音频基础知识

#### 采样率
- **44100 Hz** (44.1kHz)：CD 音质
- **48000 Hz** (48kHz)：专业音频

#### 比特率
- **128 kbps**：普通品质
- **192 kbps**：良好品质
- **256 kbps**：高品质

#### 声道
- **单声道**：1 个声道
- **立体声**：2 个声道（左+右）

---

## 📱 Android 编解码 API 介绍

### 核心 API 类

| 类名 | 作用 | 比喻 |
|-----|------|------|
| **MediaExtractor** | 从 MP4 提取轨道 | 🗂️ 开包器 |
| **MediaCodec** | 编解码核心 | ⚙️ 加工机器 |
| **MediaMuxer** | 混合视频和音频 | 📦 打包器 |
| **MediaFormat** | 描述媒体格式 | 📋 说明书 |

### MediaExtractor 使用步骤

```kotlin
// 步骤 1：创建 Extractor
val extractor = MediaExtractor()

// 步骤 2：设置数据源
extractor.setDataSource("文件路径")

// 步骤 3：找到想要的轨道
for (i in 0 until extractor.trackCount) {
    val format = extractor.getTrackFormat(i)
    val mime = format.getString(MediaFormat.KEY_MIME)
    
    if (mime.startsWith("video/")) {
        extractor.selectTrack(i)  // 选中视频轨道
    }
}

// 步骤 4：读取数据
val buffer = ByteBuffer.allocate(1024*1024)
val sampleSize = extractor.readSampleData(buffer, 0)

// 步骤 5：释放资源
extractor.release()
```

### MediaCodec 使用步骤（解码）

```kotlin
// 步骤 1：创建解码器
val codec = MediaCodec.createDecoderByType("video/avc")

// 步骤 2：配置
codec.configure(format, surface, null, 0)

// 步骤 3：启动
codec.start()

// 步骤 4：循环处理
// - 把压缩数据送给解码器
// - 从解码器拿回原始帧

// 步骤 5：释放
codec.stop()
codec.release()
```

---

## 🔄 完整的解码流程

### 解码流程图

```
          ┌───────────────┐
          │   MP4 文件    │
          └───────┬───────┘
                  │
                  ▼
          ┌───────────────┐
          │ MediaExtractor│ ← 打开文件，找到轨道
          └───────┬───────┘
                  │
          ┌───────┴───────┐
          │  视频轨道     │  音频轨道
          └───────┬───────┘
                  │
                  ▼
          ┌───────────────┐
          │  MediaCodec   │ ← 解码
          │  (Decoder)    │
          └───────┬───────┘
                  │
                  ▼
          ┌───────────────┐
          │  原始视频帧   │  原始音频帧
          └───────────────┘
```

### 详细解码步骤

#### 第 1 步：准备工作

```kotlin
// 创建 MediaExtractor
val extractor = MediaExtractor()
extractor.setDataSource(inputFile)

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
val videoFormat = extractor.getTrackFormat(videoTrackIndex)
```

#### 第 2 步：创建并配置解码器

```kotlin
val decoder = MediaCodec.createDecoderByType(
    videoFormat.getString(MediaFormat.KEY_MIME)!!
)
decoder.configure(videoFormat, surface, null, 0)
decoder.start()
```

#### 第 3 步：循环解码（最重要！）

```kotlin
val bufferInfo = MediaCodec.BufferInfo()
val timeoutUs = 10000L  // 10ms 超时

var inputDone = false
var outputDone = false

while (!outputDone) {
    // ─────────────────────────────────────────────────────────
    // 第一部分：送数据给解码器
    // ─────────────────────────────────────────────────────────
    if (!inputDone) {
        val inputBufferIndex = decoder.dequeueInputBuffer(timeoutUs)
        
        if (inputBufferIndex >= 0) {
            val inputBuffer = decoder.getInputBuffer(inputBufferIndex)
            val sampleSize = extractor.readSampleData(inputBuffer!!, 0)
            
            if (sampleSize < 0) {
                // 没有数据了
                decoder.queueInputBuffer(
                    inputBufferIndex, 0, 0, 0L,
                    MediaCodec.BUFFER_FLAG_END_OF_STREAM
                )
                inputDone = true
            } else {
                // 送数据给解码器
                decoder.queueInputBuffer(
                    inputBufferIndex, 0, sampleSize,
                    extractor.sampleTime, extractor.sampleFlags
                )
                extractor.advance()
            }
        }
    }
    
    // ─────────────────────────────────────────────────────────
    // 第二部分：从解码器拿数据
    // ─────────────────────────────────────────────────────────
    val outputBufferIndex = decoder.dequeueOutputBuffer(bufferInfo, timeoutUs)
    
    when (outputBufferIndex) {
        MediaCodec.INFO_TRY_AGAIN_LATER -> {
            // 没准备好，稍后再试
        }
        MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> {
            // 输出格式变了
            val newFormat = decoder.outputFormat
        }
        else -> {
            // 拿到解码好的帧了！
            decoder.releaseOutputBuffer(outputBufferIndex, true)
            
            if (bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) {
                outputDone = true
            }
        }
    }
}
```

#### 第 4 步：释放资源

```kotlin
decoder.stop()
decoder.release()
extractor.release()
```

---

## 🔄 完整的编码流程

### 编码流程图

```
    ┌───────────────┐
    │  原始视频帧   │  原始音频帧
    └───────┬───────┘
            │
            ▼
    ┌───────────────┐
    │  MediaCodec   │ ← 编码
    │  (Encoder)    │
    └───────┬───────┘
            │
            ▼
    ┌───────────────┐
    │ MediaMuxer    │ ← 混合视频和音频
    └───────┬───────┘
            │
            ▼
    ┌───────────────┐
    │   MP4 文件    │
    └───────────────┘
```

### 详细编码步骤

#### 第 1 步：创建 MediaFormat

```kotlin
// 视频格式
val videoFormat = MediaFormat.createVideoFormat(
    "video/avc",  // H.264
    1280,         // 宽度
    720           // 高度
)

videoFormat.setInteger(MediaFormat.KEY_BIT_RATE, 5_000_000)     // 5 Mbps
videoFormat.setInteger(MediaFormat.KEY_FRAME_RATE, 30)          // 30 fps
videoFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 2)     // 2秒一个关键帧

// 音频格式
val audioFormat = MediaFormat.createAudioFormat(
    "audio/mp4a-latm",  // AAC
    44100,              // 采样率
    2                   // 立体声
)

audioFormat.setInteger(MediaFormat.KEY_BIT_RATE, 128_000)       // 128 kbps
```

#### 第 2 步：创建并配置编码器

```kotlin
val videoEncoder = MediaCodec.createEncoderByType("video/avc")
videoEncoder.configure(videoFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
videoEncoder.start()

val audioEncoder = MediaCodec.createEncoderByType("audio/mp4a-latm")
audioEncoder.configure(audioFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
audioEncoder.start()
```

#### 第 3 步：创建 MediaMuxer

```kotlin
val muxer = MediaMuxer(outputFile, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)

var videoTrackIndex = -1
var audioTrackIndex = -1
var muxerStarted = false
```

#### 第 4 步：循环编码

```kotlin
// 编码视频和音频，获取输出格式后添加轨道
// 然后把编码好的数据写入 muxer
// 这里就不展开完整代码了，看 CodecTutorial.kt
```

---

## 💡 实际应用示例

### 示例 1：简单的视频播放器

```kotlin
// 这是一个简化版本
fun playVideo(filePath: String, surface: Surface) {
    val extractor = MediaExtractor()
    extractor.setDataSource(filePath)
    
    // 找到视频轨道
    // ...
    
    val decoder = MediaCodec.createDecoderByType(videoMime)
    decoder.configure(videoFormat, surface, null, 0)
    decoder.start()
    
    // 循环解码并渲染
    // ...
}
```

### 示例 2：替换视频中的音频

```kotlin
// 1. 从原视频提取视频轨道
// 2. 从音频文件提取音频轨道
// 3. 用 MediaMuxer 混合起来
```

---

## ⚠️ 常见问题和注意事项

### 1. 资源释放！

**重要：用完一定要释放！**

```kotlin
// ✅ 正确做法
try {
    // 使用 codec
} finally {
    codec.stop()
    codec.release()
    extractor.release()
}
```

### 2. 时间戳同步

视频和音频有各自的时间戳，要确保它们同步播放！

### 3. 设备兼容性

不同设备的 MediaCodec 支持的格式可能不一样！

### 4. 后台线程

编解码操作应该在后台线程执行，不要阻塞 UI！

---

## 📖 扩展学习资源

### 官方文档
- [MediaCodec 官方文档](https://developer.android.com/reference/android/media/MediaCodec)
- [MediaExtractor 官方文档](https://developer.android.com/reference/android/media/MediaExtractor)

### 推荐项目
- [Grafika](https://github.com/google/grafika) - Google 官方示例
- [ExoPlayer](https://exoplayer.dev/) - Google 开源播放器

---

## 🎓 学习路线建议

### 第 1 阶段：理解概念
- ✅ 阅读本教程
- ✅ 理解 MP4 结构
- ✅ 理解编码/解码

### 第 2 阶段：简单实践
- ✅ 使用 MediaExtractor 读取文件
- ✅ 列出文件中的轨道信息
- ✅ 打印轨道格式

### 第 3 阶段：编解码
- ✅ 尝试解码视频
- ✅ 尝试编码视频
- ✅ 使用 Surface 渲染

### 第 4 阶段：高级应用
- ✅ 实现视频剪辑
- ✅ 实现视频转码
- ✅ 添加滤镜效果

---

## 🎉 总结

### 记住这几点：

1. **MP4 是容器**，装着视频和音频
2. **MediaExtractor** 用来拆包
3. **MediaCodec** 用来编解码
4. **MediaMuxer** 用来打包
5. **用完记得释放**资源！

### 下一步：

去看同目录下的 `Mp4CodecTutorial.kt` 文件！那里有完整的代码示例！

---

> 💡 提示：如果有不理解的地方，先看代码示例，再回来看这里的说明！

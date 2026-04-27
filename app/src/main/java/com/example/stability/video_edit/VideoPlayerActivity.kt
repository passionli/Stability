package com.example.stability.video_edit

import android.content.Intent
import android.media.MediaMetadataRetriever
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.MediaController
import android.widget.ScrollView
import android.widget.TextView
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.Locale

class VideoPlayerActivity : AppCompatActivity() {

    private lateinit var videoView: VideoView
    private lateinit var metadataText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 创建主布局
        val mainLayout = LinearLayout(this)
        mainLayout.orientation = LinearLayout.VERTICAL
        mainLayout.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )

        // 创建 VideoView
        videoView = VideoView(this)
        val videoParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            0,
            1f
        )
        videoView.layoutParams = videoParams

        // 创建元数据显示区域
        val scrollView = ScrollView(this)
        val scrollParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        scrollView.layoutParams = scrollParams
        scrollView.setBackgroundColor(0x80000000.toInt()) // 半透明黑色背景
        scrollView.setPadding(16, 16, 16, 16)

        metadataText = TextView(this)
        metadataText.setTextColor(0xFFFFFFFF.toInt()) // 白色文本
        metadataText.textSize = 14f
        metadataText.setLineSpacing(4f, 1f)
        scrollView.addView(metadataText)

        // 将组件添加到主布局
        mainLayout.addView(videoView)
        mainLayout.addView(scrollView)

        // 设置为内容视图
        setContentView(mainLayout)

        // 添加媒体控制器
        val mediaController = MediaController(this)
        mediaController.setAnchorView(videoView)
        videoView.setMediaController(mediaController)

        // 处理传入的 Intent
        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent) {
        // 从 Intent 中获取视频文件路径
        val videoPath = intent.getStringExtra("video_path")
        if (videoPath != null) {
            Log.d("VideoPlayerActivity", "Received video path: $videoPath")
            playVideo(videoPath)
        } else {
            // 尝试从 data 中获取视频 URI（兼容旧的方式）
            val dataUri = intent.data
            if (dataUri != null) {
                Log.d("VideoPlayerActivity", "Received video URI: $dataUri")
                playVideo(dataUri)
            } else {
                Log.e("VideoPlayerActivity", "No video path or URI received")
            }
        }
    }

    private fun playVideo(path: String) {
        val videoUri = android.net.Uri.fromFile(java.io.File(path))
        // 获取并显示视频元数据
        displayVideoMetadata(path)
        // 设置视频 URI
        videoView.setVideoURI(videoUri)

        // 开始播放
        videoView.start()

        // 添加播放完成监听器
        videoView.setOnCompletionListener {
            Log.d("VideoPlayerActivity", "Video playback ended")
            // 视频播放结束后返回上一个 Activity
            finish()
        }

        // 添加错误监听器
        videoView.setOnErrorListener { _, what, extra ->
            Log.e("VideoPlayerActivity", "Player error: what=$what, extra=$extra")
            true
        }
    }

    private fun displayVideoMetadata(videoPath: String) {
        val retriever = MediaMetadataRetriever()
        try {
            retriever.setDataSource(videoPath)
            val metadataBuilder = StringBuilder()
            
            // 基本信息
            metadataBuilder.append("视频文件路径:\n$videoPath\n\n")
            
            // 视频宽度和高度
            val width = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)
            val height = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)
            metadataBuilder.append("分辨率: ${width}x${height}\n")
            
            // 视频时长
            val durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            val durationMs = durationStr?.toLongOrNull() ?: 0
            val duration = formatDuration(durationMs)
            metadataBuilder.append("时长: $duration\n")
            
            // 音频采样率
            val audioSampleRate = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_SAMPLERATE)
            metadataBuilder.append("音频采样率: ${audioSampleRate}Hz\n")
            
            // 音频声道数
            val audioChannels = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_NUM_TRACKS)
            metadataBuilder.append("音频声道数: $audioChannels\n")
            
            // 视频比特率
            val bitrate = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE)
            metadataBuilder.append("比特率: ${bitrate}bps\n")
            
            // 文件大小
            val file = java.io.File(videoPath)
            val fileSize = file.length()
            metadataBuilder.append("文件大小: ${formatFileSize(fileSize)}\n")
            
            // 最后修改时间
            val lastModified = file.lastModified()
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val lastModifiedStr = dateFormat.format(lastModified)
            metadataBuilder.append("最后修改时间: $lastModifiedStr\n")
            
            // 显示元数据
            metadataText.text = metadataBuilder.toString()
        } catch (e: Exception) {
            Log.e("VideoPlayerActivity", "Error extracting metadata: ${e.message}")
            metadataText.text = "无法获取视频元数据: ${e.message}"
        } finally {
            retriever.release()
        }
    }

    private fun formatDuration(durationMs: Long): String {
        val seconds = (durationMs / 1000) % 60
        val minutes = (durationMs / (1000 * 60)) % 60
        val hours = durationMs / (1000 * 60 * 60)
        return if (hours > 0) {
            String.format("%02d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format("%02d:%02d", minutes, seconds)
        }
    }

    private fun formatFileSize(bytes: Long): String {
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        var size = bytes.toDouble()
        var unitIndex = 0
        while (size >= 1024 && unitIndex < units.size - 1) {
            size /= 1024
            unitIndex++
        }
        return String.format("%.2f %s", size, units[unitIndex])
    }

    private fun playVideo(uri: android.net.Uri) {
        // 设置视频 URI
        videoView.setVideoURI(uri)

        // 开始播放
        videoView.start()

        // 添加播放完成监听器
        videoView.setOnCompletionListener {
            Log.d("VideoPlayerActivity", "Video playback ended")
            // 视频播放结束后返回上一个 Activity
            finish()
        }

        // 添加错误监听器
        videoView.setOnErrorListener { _, what, extra ->
            Log.e("VideoPlayerActivity", "Player error: what=$what, extra=$extra")
            true
        }
    }

    override fun onPause() {
        super.onPause()
        if (videoView.isPlaying) {
            videoView.pause()
        }
    }

    override fun onStop() {
        super.onStop()
        videoView.stopPlayback()
    }

    override fun onDestroy() {
        super.onDestroy()
        videoView.stopPlayback()
    }
}
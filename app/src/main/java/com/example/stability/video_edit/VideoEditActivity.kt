package com.example.stability.video_edit

import android.content.Intent
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.media.MediaMuxer
import android.media.MediaCodec
import android.media.MediaFormat
import android.media.MediaCodecInfo
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.example.stability.R
import com.example.stability.video_edit.audio.Mp3ToPcmConverter
import com.example.stability.video_edit.audio.PcmToAacConverter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

/**
 * 视频编辑页面
 *
 * 功能：
 * 1. 页面中间播放视频
 * 2. 页面底部显示视频时间戳和对应的缩略图
 */
class VideoEditActivity : AppCompatActivity(), OnThumbnailClickListener {
    private val TAG = "VideoEditActivity"

    private var videoPlayer: ExoPlayer? = null
    private var musicPlayer: ExoPlayer? = null
    private var playerView: PlayerView? = null
    private var thumbnailTimeline: ThumbnailTimelineView? = null
    private var timelineContainer: android.widget.HorizontalScrollView? = null
    private var musicTimelineContainer: android.widget.HorizontalScrollView? = null
    private var musicTimeline: android.widget.LinearLayout? = null
    
    private var currentPosition: Long = 0L
    private var videoDuration: Long = 0L
    private var musicDuration: Long = 0L
    private var musicTimelineUpdated = false
    
    // 缩放相关变量
    private var scaleFactor = 1f
    private val maxScale = 3f
    private val minScale = 0.5f
    private var lastDistance = 0f
    
    private val thumbnailInterval: Long = 1000L // 每1秒一个缩略图

    private val PERMISSION_REQUEST_CODE = 1001

    private val VIDEO_SELECTION_REQUEST_CODE = 1002

    private var selectedVideoPath: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_edit)
        
        // 检查 intent 额外参数
        val isExportMode = intent?.getBooleanExtra("export", false)
        Log.d("VideoEditActivity", "onCreate: isExportMode = $isExportMode")
        Log.d("VideoEditActivity", "onCreate: intent = $intent")
        
        initViews()
        setupPlayer()
        
        // 检查并请求权限
        checkAndRequestPermissions()
        
        // 检查是否需要自动导出
        if (isExportMode == true) {
            Log.d("VideoEditActivity", "自动导出模式启动")
            // 延迟执行导出，确保权限检查和文件准备完成
            Handler().postDelayed({
                showExportDialog()
            }, 3000)
        }
    }
    
    /**
     * 测试导出功能的方法
     */
    fun testExport(view: android.view.View) {
        Log.d("VideoEditActivity", "testExport: 开始测试导出功能")
        showExportDialog()
    }

    fun onSelectVideoClicked(view: android.view.View) {
        Log.d("VideoEditActivity", "onSelectVideoClicked: 打开视频选择页面")
        val intent = Intent(this, MediaSelectionActivity::class.java)
        startActivityForResult(intent, VIDEO_SELECTION_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == VIDEO_SELECTION_REQUEST_CODE && resultCode == RESULT_OK) {
            val videoPath = data?.getStringExtra(MediaSelectionActivity.EXTRA_SELECTED_MEDIA_PATH)
            val mediaType = data?.getStringExtra(MediaSelectionActivity.EXTRA_SELECTED_MEDIA_TYPE)
            Log.d("VideoEditActivity", "onActivityResult: 视频路径 = $videoPath, 类型 = $mediaType")
            if (videoPath != null) {
                selectedVideoPath = videoPath
                loadSelectedVideo(videoPath)
            }
        }
    }

    private fun loadSelectedVideo(videoPath: String) {
        Log.d("VideoEditActivity", "loadSelectedVideo: 开始加载视频 = $videoPath")
        try {
            // 检查是否是内容 URI
            if (videoPath.startsWith("content://")) {
                // 直接使用内容 URI
                val mediaItem = MediaItem.fromUri(videoPath)
                videoPlayer?.let { player ->
                    player.setMediaItem(mediaItem)
                    player.prepare()
                    Log.d("VideoEditActivity", "视频加载成功，准备播放")
                    updateThumbnailTimelineForVideo(videoPath)
                }
            } else {
                // 处理文件路径
                val videoFile = File(videoPath)
                if (!videoFile.exists()) {
                    Log.e("VideoEditActivity", "视频文件不存在: $videoPath")
                    return
                }
                videoPlayer?.let { player ->
                    val mediaItem = MediaItem.fromUri(Uri.fromFile(videoFile))
                    player.setMediaItem(mediaItem)
                    player.prepare()
                    Log.d("VideoEditActivity", "视频加载成功，准备播放")
                    updateThumbnailTimelineForVideo(videoPath)
                }
            }
        } catch (e: Exception) {
            Log.e("VideoEditActivity", "加载视频失败: ${e.message}")
        }
    }

    private fun updateThumbnailTimelineForVideo(videoPath: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val retriever = MediaMetadataRetriever()
                if (videoPath.startsWith("content://")) {
                    // 使用内容 URI
                    retriever.setDataSource(this@VideoEditActivity, Uri.parse(videoPath))
                } else {
                    // 使用文件路径
                    retriever.setDataSource(videoPath)
                }
                val durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                val duration = durationStr?.toLongOrNull() ?: 0L
                withContext(Dispatchers.Main) {
                    videoDuration = duration
                    Log.d("VideoEditActivity", "视频时长: $videoDuration ms")
                    updateThumbnailTimeline()
                }
                retriever.release()
            } catch (e: Exception) {
                Log.e("VideoEditActivity", "获取视频信息失败: ${e.message}")
            }
        }
    }

    /**
     * 检查并请求权限
     */
    private fun checkAndRequestPermissions() {
        if (checkPermissions()) {
            // 先拷贝文件到外部存储并转换为 AAC（在后台线程）
            copyRawFilesToExternalStorage()
        } else {
            // 显示权限解释对话框
            showPermissionExplanation()
        }
    }

    /**
     * 拷贝 raw 目录下的文件到外部存储的 Download 目录，并进行格式转换
     */
    private fun copyRawFilesToExternalStorage() {
        Log.d("VideoEditActivity", "开始拷贝文件到外部存储...")
        
        // 在后台线程执行文件拷贝和转换
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // 拷贝视频文件
                copyRawFileToExternal(R.raw.sample_video_v1, "sample_video_v1.mp4")
                
                // 拷贝并转换音频文件：MP3 -> PCM -> AAC
                copyAndConvertAudioFile(R.raw.suzume_no_tojimari, "suzume_no_tojimari")
                
                Log.d("VideoEditActivity", "文件拷贝和转换完成")
                
                // 转换完成后，在主线程加载视频
                withContext(Dispatchers.Main) {
                    loadVideo()
                    
                    // 检查是否需要自动导出
                    if (intent?.getBooleanExtra("export", false) == true) {
                        Log.d("VideoEditActivity", "自动导出模式启动")
                        // 延迟执行导出，确保视频加载完成
                        Handler().postDelayed({
                            showExportDialog()
                        }, 2000)
                    }
                }
            } catch (e: Exception) {
                Log.e("VideoEditActivity", "文件处理失败: ${e.message}")
                // 即使失败也尝试加载视频
                withContext(Dispatchers.Main) {
                    loadVideo()
                }
            }
        }
    }

    /**
     * 拷贝单个 raw 文件到应用私有目录
     */
    private fun copyRawFileToExternal(rawResId: Int, fileName: String) {
        try {
            // 获取应用私有目录
            val appDir = filesDir
            val targetFile = java.io.File(appDir, fileName)
            
            // 检查文件是否已经存在
            if (targetFile.exists()) {
                Log.d("VideoEditActivity", "文件已存在: ${targetFile.absolutePath}")
                return
            }
            
            // 从 raw 资源读取文件
            val inputStream = resources.openRawResource(rawResId)
            val outputStream = java.io.FileOutputStream(targetFile)
            
            // 拷贝文件
            val buffer = ByteArray(4096)
            var bytesRead: Int
            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                outputStream.write(buffer, 0, bytesRead)
            }
            
            // 关闭流
            inputStream.close()
            outputStream.close()
            
            Log.d("VideoEditActivity", "文件拷贝成功: ${targetFile.absolutePath}")
            Log.d("VideoEditActivity", "文件大小: ${targetFile.length()} bytes")
            
        } catch (e: Exception) {
            Log.e("VideoEditActivity", "拷贝文件失败 ($fileName): ${e.message}")
        }
    }

    /**
     * 拷贝 MP3 文件到应用私有目录并转换为 AAC
     * 流程：MP3 -> PCM -> AAC
     */
    private fun copyAndConvertAudioFile(rawResId: Int, baseFileName: String) {
        try {
            // 获取应用私有目录
            val appDir = filesDir
            
            // 检查 AAC 文件是否已经存在
            val aacFile = java.io.File(appDir, "$baseFileName.aac")
            if (aacFile.exists()) {
                Log.d("VideoEditActivity", "AAC 文件已存在: ${aacFile.absolutePath}")
//                return
            }
            
            // 拷贝 MP3 文件
            val mp3File = java.io.File(appDir, "$baseFileName.mp3")
            if (true) {
                Log.d("VideoEditActivity", "开始拷贝 MP3 文件...")
                val inputStream = resources.openRawResource(rawResId)
                val outputStream = java.io.FileOutputStream(mp3File)
                
                val buffer = ByteArray(4096)
                var bytesRead: Int
                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    outputStream.write(buffer, 0, bytesRead)
                }
                
                inputStream.close()
                outputStream.close()
                
                Log.d("VideoEditActivity", "MP3 文件拷贝成功: ${mp3File.absolutePath}")
                Log.d("VideoEditActivity", "MP3 文件大小: ${mp3File.length()} bytes")
            } else {
                Log.d("VideoEditActivity", "MP3 文件已存在: ${mp3File.absolutePath}")
            }
            
            // 转换 MP3 到 PCM
            val pcmFile = java.io.File(appDir, "$baseFileName.pcm")
            if (true) {
                Log.d("VideoEditActivity", "开始转换 MP3 到 PCM...")
                
                val mp3ToPcmConverter = Mp3ToPcmConverter()
                val pcmResult = mp3ToPcmConverter.convertMp3ToPcmSync(mp3File.absolutePath, appDir.absolutePath)
                
                if (pcmResult.success && pcmResult.outputPath != null) {
                    // 重命名 PCM 文件
                    val tempPcmFile = java.io.File(pcmResult.outputPath)
                    if (tempPcmFile.renameTo(pcmFile)) {
                        Log.d("VideoEditActivity", "MP3 到 PCM 转换成功: ${pcmFile.absolutePath}")
                    } else {
                        Log.d("VideoEditActivity", "PCM 文件已在: ${tempPcmFile.absolutePath}")
                    }
                } else {
                    Log.e("VideoEditActivity", "MP3 到 PCM 转换失败: ${pcmResult.errorMessage}")
                    return
                }
            } else {
                Log.d("VideoEditActivity", "PCM 文件已存在: ${pcmFile.absolutePath}")
            }
            
            // 转换 PCM 到 AAC
            Log.d("VideoEditActivity", "开始转换 PCM 到 AAC...")
            
            val pcmToAacConverter = PcmToAacConverter()
            val aacResult = pcmToAacConverter.convertPcmToAacSync(
                pcmFile.absolutePath,
                aacFile.absolutePath,
                PcmToAacConverter.PcmConfig(sampleRate = 44100, channelCount = 2, bitRate = 128000)
            )
            
            if (aacResult.success) {
                Log.d("VideoEditActivity", "PCM 到 AAC 转换成功: ${aacFile.absolutePath}")
                
                // 保留 PCM 文件，用于导出时直接编码
                Log.d("VideoEditActivity", "保留 PCM 文件: ${pcmFile.absolutePath}")
            } else {
                Log.e("VideoEditActivity", "PCM 到 AAC 转换失败: ${aacResult.errorMessage}")
            }
            
        } catch (e: Exception) {
            Log.e("VideoEditActivity", "音频文件转换失败 ($baseFileName): ${e.message}")
        }
    }

    /**
     * 显示权限解释对话框
     */
    private fun showPermissionExplanation() {
        android.app.AlertDialog.Builder(this)
            .setTitle("需要存储权限")
            .setMessage("应用需要访问存储权限才能：\n\n1. 加载视频和音频文件\n2. 导出编辑后的视频\n\n请点击'授予权限'按钮来继续。")
            .setPositiveButton("授予权限") { _, _ ->
                requestPermissions()
            }
            .setCancelable(false) // 强制用户处理
            .show()
    }

    /**
     * 检查是否有必要的权限
     */
    private fun checkPermissions(): Boolean {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ 需要 READ_MEDIA_VIDEO 和 READ_MEDIA_AUDIO 权限
            val videoPerm = checkSelfPermission(android.Manifest.permission.READ_MEDIA_VIDEO)
            val audioPerm = checkSelfPermission(android.Manifest.permission.READ_MEDIA_AUDIO)
            
            Log.d("VideoEditActivity", "Android 13+ 权限检查:")
            Log.d("VideoEditActivity", "READ_MEDIA_VIDEO: ${if (videoPerm == android.content.pm.PackageManager.PERMISSION_GRANTED) "GRANTED" else "DENIED"}")
            Log.d("VideoEditActivity", "READ_MEDIA_AUDIO: ${if (audioPerm == android.content.pm.PackageManager.PERMISSION_GRANTED) "GRANTED" else "DENIED"}")
            
            return videoPerm == android.content.pm.PackageManager.PERMISSION_GRANTED &&
                   audioPerm == android.content.pm.PackageManager.PERMISSION_GRANTED
        } else {
            // Android 12- 只需要 READ_EXTERNAL_STORAGE 权限（写入到应用私有目录不需要权限）
            val readPerm = checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE)
            
            Log.d("VideoEditActivity", "Android 12- 权限检查:")
            Log.d("VideoEditActivity", "READ_EXTERNAL_STORAGE: ${if (readPerm == android.content.pm.PackageManager.PERMISSION_GRANTED) "GRANTED" else "DENIED"}")
            
            return readPerm == android.content.pm.PackageManager.PERMISSION_GRANTED
        }
    }

    /**
     * 请求必要的权限
     */
    private fun requestPermissions() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ 请求 READ_MEDIA_VIDEO 和 READ_MEDIA_AUDIO 权限
            requestPermissions(
                arrayOf(
                    android.Manifest.permission.READ_MEDIA_VIDEO,
                    android.Manifest.permission.READ_MEDIA_AUDIO
                ),
                PERMISSION_REQUEST_CODE
            )
        } else {
            // Android 12- 只请求 READ_EXTERNAL_STORAGE 权限（写入到应用私有目录不需要权限）
            requestPermissions(
                arrayOf(
                    android.Manifest.permission.READ_EXTERNAL_STORAGE
                ),
                PERMISSION_REQUEST_CODE
            )
        }
    }

    /**
     * 处理权限请求结果
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults.all { it == android.content.pm.PackageManager.PERMISSION_GRANTED }) {
                // 权限授予成功，加载视频
                loadVideo()
            } else {
                // 权限授予失败，显示提示并再次请求
                android.widget.Toast.makeText(this, "需要存储权限才能加载视频和音频文件", android.widget.Toast.LENGTH_LONG).show()
                // 1秒后再次请求权限
                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                    showPermissionExplanation()
                }, 1000)
            }
        }
    }
    
    private fun initViews() {
        playerView = findViewById(R.id.player_view)
        thumbnailTimeline = findViewById(R.id.thumbnail_timeline)
        timelineContainer = findViewById(R.id.timeline_container)
        musicTimelineContainer = findViewById(R.id.music_timeline_container)
        musicTimeline = findViewById(R.id.music_timeline)
        
        // 设置缩略图点击监听器
        thumbnailTimeline?.setThumbnailClickListener(this)
        
        // 添加触摸事件监听器，实现双指缩放
        playerView?.setOnTouchListener { v, event ->
            handleTouchEvent(event)
            true
        }
    }
    
    /**
     * 处理触摸事件，实现双指缩放
     */
    private fun handleTouchEvent(event: android.view.MotionEvent): Boolean {
        when (event.action and android.view.MotionEvent.ACTION_MASK) {
            android.view.MotionEvent.ACTION_POINTER_DOWN -> {
                // 双指按下，计算初始距离
                if (event.pointerCount >= 2) {
                    lastDistance = getDistance(event)
                }
            }
            android.view.MotionEvent.ACTION_MOVE -> {
                // 双指移动，计算当前距离
                if (event.pointerCount >= 2) {
                    val currentDistance = getDistance(event)
                    if (currentDistance > 10) {
                        // 计算缩放比例
                        val scale = currentDistance / lastDistance
                        scaleFactor *= scale
                        
                        // 限制缩放范围
                        scaleFactor = kotlin.math.max(minScale, kotlin.math.min(scaleFactor, maxScale))
                        
                        // 应用缩放
                        playerView?.scaleX = scaleFactor
                        playerView?.scaleY = scaleFactor
                        
                        lastDistance = currentDistance
                    }
                }
            }
        }
        return true
    }
    
    /**
     * 计算双指之间的距离
     */
    private fun getDistance(event: android.view.MotionEvent): Float {
        if (event.pointerCount < 2) {
            return 0f
        }
        val x = event.getX(0) - event.getX(1)
        val y = event.getY(0) - event.getY(1)
        return kotlin.math.sqrt(x * x + y * y)
    }
    
    private fun setupPlayer() {
        // 初始化视频播放器
        val exoPlayer = ExoPlayer.Builder(this).build()
        playerView?.player = exoPlayer
        
        exoPlayer.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                Log.d("VideoEditActivity", "Video player state: $playbackState")
                when (playbackState) {
                    Player.STATE_READY -> {
                        videoDuration = exoPlayer.duration
                        Log.d("VideoEditActivity", "Video duration: $videoDuration ms")
                        updateThumbnailTimeline()
                    }
                    Player.STATE_ENDED -> {
                        // 视频播放结束，停止音乐
                        musicPlayer?.stop()
                    }
                    Player.STATE_IDLE -> {
                        Log.d("VideoEditActivity", "Video player idle state")
                    }
                    Player.STATE_BUFFERING -> {
                        Log.d("VideoEditActivity", "Video player buffering state")
                    }
                }
            }
            
            override fun onPositionDiscontinuity(
                oldPosition: Player.PositionInfo,
                newPosition: Player.PositionInfo,
                reason: Int
            ) {
                currentPosition = newPosition.positionMs
                updatePlaybackPosition()
                // 同步音乐播放器位置
                musicPlayer?.seekTo(currentPosition)
            }
        })
        
        videoPlayer = exoPlayer
        
        // 初始化音乐播放器
        val musicExoPlayer = ExoPlayer.Builder(this).build()
        musicExoPlayer.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                Log.d("VideoEditActivity", "Music player state: $playbackState")
                if (playbackState == Player.STATE_READY) {
                    musicDuration = musicExoPlayer.duration
                    Log.d("VideoEditActivity", "Music duration: $musicDuration ms")
                    // 基于音乐时长更新音乐时间戳
                    updateMusicTimelineByDuration(musicDuration)
                    musicTimelineUpdated = true
                } else if (playbackState == Player.STATE_IDLE && !musicTimelineUpdated) {
                    // 如果音乐加载失败且时间戳尚未更新，使用默认时长显示时间戳
                    Log.d("VideoEditActivity", "Music loading failed, using default duration")
                    updateMusicTimelineByDuration(24000) // 默认24秒
                }
            }
        })
        
        musicPlayer = musicExoPlayer
    }
    
    private fun loadVideo() {
        // 获取应用私有目录
        val appDir = filesDir
        
        // 检查视频文件是否存在（在应用私有目录）
        val videoFile = java.io.File(appDir, "sample_video_v1.mp4")
        Log.d("VideoEditActivity", "Video file exists: ${videoFile.exists()}, size: ${if (videoFile.exists()) videoFile.length() else 0} bytes")
        
        // 读取视频文件的前 4 个字节（用于验证文件格式）
        if (videoFile.exists()) {
            try {
                // 直接读取文件头部
                val videoInputStream = java.io.FileInputStream(videoFile)
                val videoHeader = ByteArray(4)
                val videoBytesRead = videoInputStream.read(videoHeader)
                videoInputStream.close()
                
                if (videoBytesRead > 0) {
                    val videoHeaderHex = videoHeader.take(videoBytesRead).joinToString(" ") { String.format("%02X", it) }
                    val videoHeaderAscii = videoHeader.take(videoBytesRead).map { if (it in 32..126) it.toInt().toChar() else '.' }.joinToString("")
                    Log.d("VideoEditActivity", "Video first $videoBytesRead bytes (hex): $videoHeaderHex")
                    Log.d("VideoEditActivity", "Video first $videoBytesRead bytes (ascii): $videoHeaderAscii")
                    
                    // 根据文件头识别文件类型
                    val videoMagicNumber = when {
                        videoHeaderHex.startsWith("66 74 79 70") -> "MP4/MOV (ftyp)"
                        videoHeaderHex.startsWith("00 00 00") -> "Possible MP4/MOV"
                        else -> "Unknown"
                    }
                    Log.d("VideoEditActivity", "Video format guess: $videoMagicNumber")
                }
            } catch (e: Exception) {
                Log.e("VideoEditActivity", "Error reading video header: ${e.message}")
            }
        }
        
        // 加载视频文件（从应用私有目录）- 使用 file:// 格式
        val videoUri = Uri.fromFile(videoFile)
        Log.d("VideoEditActivity", "Loading video from: $videoUri")
        val videoMediaItem = MediaItem.fromUri(videoUri)
        videoPlayer?.setMediaItem(videoMediaItem)
        videoPlayer?.prepare()
        
        // 检查音乐文件是否存在（在应用私有目录）
        val musicFile = java.io.File(appDir, "suzume_no_tojimari.aac")
        Log.d("VideoEditActivity", "Music file exists: ${musicFile.exists()}, size: ${if (musicFile.exists()) musicFile.length() else 0} bytes")
        
        // 读取音频文件的前 4 个字节（用于验证文件格式）
        if (musicFile.exists()) {
            try {
                // 直接读取文件头部
                val musicInputStream = java.io.FileInputStream(musicFile)
                val musicHeader = ByteArray(4)
                val musicBytesRead = musicInputStream.read(musicHeader)
                musicInputStream.close()
                
                if (musicBytesRead > 0) {
                    val musicHeaderHex = musicHeader.take(musicBytesRead).joinToString(" ") { String.format("%02X", it) }
                    val musicHeaderAscii = musicHeader.take(musicBytesRead).map { if (it in 32..126) it.toInt().toChar() else '.' }.joinToString("")
                    Log.d("VideoEditActivity", "Music first $musicBytesRead bytes (hex): $musicHeaderHex")
                    Log.d("VideoEditActivity", "Music first $musicBytesRead bytes (ascii): $musicHeaderAscii")
                    
                    // 根据文件头识别文件类型
                    val musicMagicNumber = when {
                        musicHeaderHex.startsWith("49 44 33") -> "MP3 (ID3)"
                        musicHeaderHex.startsWith("FF FB") || musicHeaderHex.startsWith("FF F3") || musicHeaderHex.startsWith("FF F2") -> "MP3 (frame sync)"
                        musicHeaderHex.startsWith("4F 67 67") -> "OGG"
                        musicHeaderHex.startsWith("52 49 46 46") -> "WAV (RIFF)"
                        musicHeaderHex.startsWith("FF F1") || musicHeaderHex.startsWith("FF F9") || musicHeaderHex.startsWith("FF F0") -> "AAC (ADTS)"
                        musicHeaderHex.startsWith("21 11 45") -> "AAC (Raw)"
                        else -> "Unknown"
                    }
                    Log.d("VideoEditActivity", "Music format guess: $musicMagicNumber")
                }
            } catch (e: Exception) {
                Log.e("VideoEditActivity", "Error reading music header: ${e.message}")
            }
        }
        
        // 加载音乐文件（从应用私有目录）- 使用 file:// 格式
        try {
            // 尝试 1: 使用 file:// 格式
            val musicUri1 = Uri.fromFile(musicFile)
            Log.d("VideoEditActivity", "Trying music URI (file://): $musicUri1")
            val musicMediaItem1 = MediaItem.fromUri(musicUri1)
            musicPlayer?.setMediaItem(musicMediaItem1)
            musicPlayer?.prepare()
            Log.d("VideoEditActivity", "Music loaded with file:// URI")
        } catch (e1: Exception) {
            Log.e("VideoEditActivity", "Error loading music with file:// URI: ${e1.message}")
            // 音乐加载失败，使用默认时长 24 秒
            Log.d("VideoEditActivity", "Music loading failed, using default duration")
            updateMusicTimelineByDuration(24000) // 默认24秒
        }
        
        // 监听播放位置更新
        startPositionUpdates()
    }
    
    private fun startPositionUpdates() {
        videoPlayer?.let { exoPlayer ->
            val handler = android.os.Handler(android.os.Looper.getMainLooper())
            val updateRunnable = object : Runnable {
                override fun run() {
                    if (exoPlayer.isPlaying) {
                        currentPosition = exoPlayer.currentPosition
                        updatePlaybackPosition()
                        // 同步音乐播放器位置
                        musicPlayer?.seekTo(currentPosition)
                    }
                    handler.postDelayed(this, 100)
                }
            }
            handler.post(updateRunnable)
        }
    }
    
    private fun updatePlaybackPosition() {
        thumbnailTimeline?.setCurrentPosition(currentPosition, videoDuration)
        
        // 自动滚动时间轴到当前播放位置
        timelineContainer?.post {
            val currentSecond = (currentPosition / 1000).toInt()
            val thumbnailWidth = 136 // 每个缩略图项的大致宽度（120dp + 16dp padding）
            val scrollX = currentSecond * thumbnailWidth - 300 // 减去一个偏移量，使当前缩略图居中显示
            timelineContainer?.smoothScrollTo(if (scrollX > 0) scrollX else 0, 0)
            // 同步滚动音乐时间轴
            musicTimelineContainer?.smoothScrollTo(if (scrollX > 0) scrollX else 0, 0)
        }
    }
    
    /**
     * 从视频中提取缩略图
     */
    private fun extractThumbnail(videoUri: Uri, timeMs: Long): Bitmap? {
        val retriever = MediaMetadataRetriever()
        return try {
            Log.d("VideoEditActivity", "提取缩略图，URI: $videoUri, 时间: $timeMs ms")
            retriever.setDataSource(this, videoUri)
            // 获取指定时间的帧作为缩略图
            val bitmap = retriever.getFrameAtTime(timeMs * 1000, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
            Log.d("VideoEditActivity", "缩略图提取结果: ${if (bitmap != null) "成功" else "失败"}")
            bitmap
        } catch (e: Exception) {
            Log.e("VideoEditActivity", "提取缩略图失败: ${e.message}")
            e.printStackTrace()
            null
        } finally {
            try {
                retriever.release()
            } catch (e: Exception) {
                Log.e("VideoEditActivity", "释放 MediaMetadataRetriever 失败: ${e.message}")
                e.printStackTrace()
            }
        }
    }
    
    override fun onThumbnailClick(timeMs: Long) {
        // 跳转到视频的对应时间
        videoPlayer?.seekTo(timeMs)
        // 跳转到音乐的对应时间
        musicPlayer?.seekTo(timeMs)
        // 暂停视频播放
        videoPlayer?.pause()
        // 暂停音乐播放
        musicPlayer?.pause()
        currentPosition = timeMs
        updatePlaybackPosition()
    }
    
    private fun updateThumbnailTimeline() {
        if (videoDuration <= 0) return
        
        // 使用 file:// 格式的 URI（从应用私有目录）
        val videoFile = java.io.File(filesDir, "sample_video_v1.mp4")
        val videoUri = Uri.fromFile(videoFile)
        val thumbnailCount = (videoDuration / thumbnailInterval + 1).toInt()
        Log.d("VideoEditActivity", "Thumbnail count: $thumbnailCount, interval: $thumbnailInterval")
        val thumbnails = mutableListOf<ThumbnailInfo>()
        
        // 异步提取缩略图
        Thread {
            for (i in 0 until thumbnailCount) {
                val timeMs = i * thumbnailInterval
                val timeText = formatTime(timeMs)
                val bitmap = extractThumbnail(videoUri, timeMs)
                thumbnails.add(ThumbnailInfo(timeMs, timeText, bitmap))
            }
            
            // 在主线程更新 UI
            Handler(Looper.getMainLooper()).post {
                thumbnailTimeline?.setThumbnails(thumbnails)
            }
        }.start()
    }
    
    private fun formatTime(timeMs: Long): String {
        val totalSeconds = timeMs / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format("%02d:%02d", minutes, seconds)
    }
    
    /**
     * 更新音乐播放数据时间戳（基于视频时长）
     */
    private fun updateMusicTimeline(count: Int) {
        musicTimeline?.removeAllViews()
        
        for (i in 0 until count) {
            val timeMs = i * thumbnailInterval
            val timeText = formatTime(timeMs)
            
            val timeStampView = android.widget.LinearLayout(this).apply {
                orientation = android.widget.LinearLayout.VERTICAL
                setPadding(8, 8, 8, 8)
                layoutParams = android.widget.LinearLayout.LayoutParams(120, android.widget.LinearLayout.LayoutParams.MATCH_PARENT)
            }
            
            // 音乐波形示意图（使用简单的矩形表示）
            val waveformView = android.widget.LinearLayout(this).apply {
                orientation = android.widget.LinearLayout.HORIZONTAL
                layoutParams = android.widget.LinearLayout.LayoutParams(android.widget.LinearLayout.LayoutParams.MATCH_PARENT, 30)
                setPadding(0, 0, 0, 8)
            }
            
            // 添加几个不同高度的矩形来模拟波形
            for (j in 0 until 5) {
                val barHeight = (10 + (i + j) % 20).toInt()
                val bar = android.view.View(this).apply {
                    layoutParams = android.widget.LinearLayout.LayoutParams(8, barHeight)
                    setBackgroundColor(android.graphics.Color.GREEN)
                    if (j > 0) {
                        (layoutParams as android.widget.LinearLayout.LayoutParams).leftMargin = 2
                    }
                }
                waveformView.addView(bar)
            }
            
            // 时间戳文本
            val timeTextView = android.widget.TextView(this).apply {
                text = timeText
                textSize = 12f
                setTextColor(android.graphics.Color.WHITE)
                gravity = android.view.Gravity.CENTER
            }
            
            timeStampView.addView(waveformView)
            timeStampView.addView(timeTextView)
            musicTimeline?.addView(timeStampView)
        }
    }
    
    /**
     * 更新音乐播放数据时间戳（基于音乐实际时长）
     */
    private fun updateMusicTimelineByDuration(duration: Long) {
        musicTimeline?.removeAllViews()
        
        val musicCount = (duration / thumbnailInterval + 1).toInt()
        Log.d("VideoEditActivity", "Music timeline count: $musicCount")
        
        for (i in 0 until musicCount) {
            val timeMs = i * thumbnailInterval
            val timeText = formatTime(timeMs)
            
            val timeStampView = android.widget.LinearLayout(this).apply {
                orientation = android.widget.LinearLayout.VERTICAL
                setPadding(8, 8, 8, 8)
                layoutParams = android.widget.LinearLayout.LayoutParams(120, android.widget.LinearLayout.LayoutParams.MATCH_PARENT)
            }
            
            // 音乐波形示意图（使用简单的矩形表示）
            val waveformView = android.widget.LinearLayout(this).apply {
                orientation = android.widget.LinearLayout.HORIZONTAL
                layoutParams = android.widget.LinearLayout.LayoutParams(android.widget.LinearLayout.LayoutParams.MATCH_PARENT, 30)
                setPadding(0, 0, 0, 8)
            }
            
            // 添加几个不同高度的矩形来模拟波形
            for (j in 0 until 5) {
                val barHeight = (10 + (i + j) % 20).toInt()
                val bar = android.view.View(this).apply {
                    layoutParams = android.widget.LinearLayout.LayoutParams(8, barHeight)
                    setBackgroundColor(android.graphics.Color.GREEN)
                    if (j > 0) {
                        (layoutParams as android.widget.LinearLayout.LayoutParams).leftMargin = 2
                    }
                }
                waveformView.addView(bar)
            }
            
            // 时间戳文本
            val timeTextView = android.widget.TextView(this).apply {
                text = timeText
                textSize = 12f
                setTextColor(android.graphics.Color.WHITE)
                gravity = android.view.Gravity.CENTER
            }
            
            timeStampView.addView(waveformView)
            timeStampView.addView(timeTextView)
            musicTimeline?.addView(timeStampView)
        }
    }
    
    fun onPlayPauseClicked(view: View) {
        videoPlayer?.let {
            if (it.isPlaying) {
                it.pause()
                musicPlayer?.pause()
            } else {
                it.play()
                musicPlayer?.play()
            }
        }
    }
    
    fun onSeekTo(position: Long) {
        videoPlayer?.seekTo(position)
        musicPlayer?.seekTo(position)
        currentPosition = position
        updatePlaybackPosition()
    }
    
    override fun onStart() {
        super.onStart()
        videoPlayer?.play()
        musicPlayer?.play()
    }
    
    override fun onPause() {
        super.onPause()
        videoPlayer?.pause()
        musicPlayer?.pause()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        videoPlayer?.release()
        musicPlayer?.release()
        videoPlayer = null
        musicPlayer = null
    }

    // 验证导出结果
    private fun validateExportResult(outputPath: String): ValidationResult {
        try {
            val outputFile = java.io.File(outputPath)
            
            // 检查文件是否存在
            if (!outputFile.exists()) {
                return ValidationResult(false, "导出文件不存在")
            }
            
            // 检查文件大小
            val fileSize = outputFile.length()
            if (fileSize == 0L) {
                return ValidationResult(false, "导出文件为空")
            }
            
            // 使用 MediaMetadataRetriever 检查文件是否包含音频
            val retriever = android.media.MediaMetadataRetriever()
            retriever.setDataSource(outputPath)
            
            // 检查音频相关信息
            val hasAudio = try {
                val audioDuration = retriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_DURATION)
                val hasAudioTrack = retriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_HAS_AUDIO)
                Log.d("VideoEditActivity", "音频时长: $audioDuration")
                Log.d("VideoEditActivity", "是否有音频轨道: $hasAudioTrack")
                hasAudioTrack == "yes" || audioDuration != null
            } catch (e: Exception) {
                false
            } finally {
                try {
                    retriever.release()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            
            if (!hasAudio) {
                return ValidationResult(false, "导出文件不包含音频数据")
            }
            
            // 检查视频相关信息
            val retrieverVideo = android.media.MediaMetadataRetriever()
            retrieverVideo.setDataSource(outputPath)
            
            val hasVideo = try {
                val videoWidth = retrieverVideo.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)
                val videoHeight = retrieverVideo.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)
                val hasVideoTrack = retrieverVideo.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_HAS_VIDEO)
                Log.d("VideoEditActivity", "视频分辨率: ${videoWidth}x${videoHeight}")
                Log.d("VideoEditActivity", "是否有视频轨道: $hasVideoTrack")
                hasVideoTrack == "yes" || (videoWidth != null && videoHeight != null)
            } catch (e: Exception) {
                false
            } finally {
                try {
                    retrieverVideo.release()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            
            if (!hasVideo) {
                return ValidationResult(false, "导出文件不包含视频数据")
            }
            
            // 计算文件大小（转换为 MB）
            val fileSizeMB = fileSize / (1024.0 * 1024.0)
            
            return ValidationResult(true, "导出文件验证成功\n文件大小: %.2f MB\n包含音频和视频数据".format(fileSizeMB))
            
        } catch (e: Exception) {
            Log.e("VideoEditActivity", "验证导出结果失败: ${e.message}")
            e.printStackTrace()
            return ValidationResult(false, "验证过程出错: ${e.message}")
        }
    }

    // 验证结果数据类
    private data class ValidationResult(val isSuccess: Boolean, val message: String)

    // 导出视频和音频合成新文件
    fun onExportClicked(view: android.view.View) {
        // 显示导出对话框
        showExportDialog()
    }

    private fun showExportDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_export, null)
        val progressBar = dialogView.findViewById<ProgressBar>(R.id.export_progress)
        val progressText = dialogView.findViewById<TextView>(R.id.progress_text)
        val statusText = dialogView.findViewById<TextView>(R.id.status_text)

        val dialog = android.app.AlertDialog.Builder(this)
            .setTitle("导出视频")
            .setView(dialogView)
            .setCancelable(false)
            .create()

        dialog.show()

        // 在后台线程执行导出
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val outputPath = withContext(Dispatchers.IO) {
                    exportVideoWithAudio(progressBar, progressText, statusText)
                }

                // 验证导出结果
                val validationResult = withContext(Dispatchers.IO) {
                    validateExportResult(outputPath)
                }

                dialog.dismiss()
                if (validationResult.isSuccess) {
                    val text = "导出成功: $outputPath\n${validationResult.message}"
                    Log.d(TAG, "showExportDialog: $text")
                    // 使用显式 Intent 启动 VideoPlayerActivity
                    val intent = android.content.Intent(this@VideoEditActivity, VideoPlayerActivity::class.java)
                    intent.putExtra("video_path", outputPath)
                    startActivity(intent)
                } else {
                    val text = "导出完成但验证失败: ${validationResult.message}"
                    Log.d(TAG, "showExportDialog: $text")
                }
            } catch (e: Exception) {
                dialog.dismiss()
                val text = "导出失败: ${e.message}"
                Log.d(TAG, "showExportDialog: $text")
            }
        }
    }

    /**
     * 导出视频和音频合成新文件
     * @param progressBar 进度条，用于显示导出进度
     * @param progressText 进度文本，用于显示当前进度百分比
     * @param statusText 状态文本，用于显示当前操作状态
     * @return 导出文件的绝对路径
     */
    private fun exportVideoWithAudio(
        progressBar: ProgressBar,
        progressText: TextView,
        statusText: TextView
    ): String {
        // 创建临时文件路径，使用时间戳确保文件名唯一
        val outputDir = getExternalFilesDir(null) // 获取应用外部存储目录
        val outputFile = java.io.File(outputDir, "exported_video_${System.currentTimeMillis()}.mp4")

        // 更新UI（在主线程），显示准备导出的状态
        Handler(Looper.getMainLooper()).post {
            statusText.text = "正在准备导出..."
            progressBar.progress = 0
        }

        try {
            // 获取视频和音频资源
            val videoFile: java.io.File
            val videoUri: Uri
            val currentVideoPath = selectedVideoPath // 使用局部变量避免智能转换问题
            
            // 使用当前选择的视频路径，如果没有选择则使用默认视频
            if (currentVideoPath != null) {
                Log.d("VideoEditActivity", "使用选择的视频路径: $currentVideoPath")
                if (currentVideoPath.startsWith("content://")) {
                    // 直接使用内容 URI
                    videoUri = Uri.parse(currentVideoPath)
                    videoFile = java.io.File("") // 内容 URI 不需要文件对象
                } else {
                    // 使用文件路径
                    videoFile = java.io.File(currentVideoPath)
                    videoUri = Uri.fromFile(videoFile)
                }
            } else {
                // 使用默认视频文件（从应用私有目录）
                Log.d("VideoEditActivity", "使用默认视频文件")
                videoFile = java.io.File(filesDir, "sample_video_v1.mp4")
                videoUri = Uri.fromFile(videoFile)
            }
            
            val pcmFile = java.io.File(filesDir, "suzume_no_tojimari.pcm") // PCM 音频文件
            
            // 验证文件存在性并记录日志
            if (currentVideoPath != null && !currentVideoPath.startsWith("content://")) {
                Log.d("VideoEditActivity", "视频文件存在: ${videoFile.exists()}, 大小: ${if (videoFile.exists()) videoFile.length() else 0} bytes")
            } else if (currentVideoPath != null && currentVideoPath.startsWith("content://")) {
                Log.d("VideoEditActivity", "视频 URI: $videoUri")
            } else {
                // 使用默认视频文件的情况
                Log.d("VideoEditActivity", "视频文件存在: ${videoFile.exists()}, 大小: ${if (videoFile.exists()) videoFile.length() else 0} bytes")
            }
            Log.d("VideoEditActivity", "PCM 文件存在: ${pcmFile.exists()}, 大小: ${if (pcmFile.exists()) pcmFile.length() else 0} bytes")
            
            // 检查视频文件是否存在（非内容 URI 情况）
            if (currentVideoPath != null && !currentVideoPath.startsWith("content://") && !videoFile.exists()) {
                throw java.io.IOException("视频文件不存在: ${videoFile.absolutePath}")
            } else if (currentVideoPath == null && !videoFile.exists()) {
                // 使用默认视频文件的情况
                throw java.io.IOException("视频文件不存在: ${videoFile.absolutePath}")
            }

            Log.d("VideoEditActivity", "Exporting video from: $videoUri")

            // 更新UI（在主线程），显示处理视频的状态
            Handler(Looper.getMainLooper()).post {
                statusText.text = "正在处理视频..."
                progressBar.progress = 30
            }

            // 提取视频轨道（不含音频）
            val videoExtractor = android.media.MediaExtractor() // 创建媒体提取器
            if (currentVideoPath != null && currentVideoPath.startsWith("content://")) {
                // 使用内容 URI 设置数据源
                videoExtractor.setDataSource(this, videoUri, null) // 设置数据源
            } else {
                // 使用文件路径设置数据源
                videoExtractor.setDataSource(videoFile.absolutePath) // 设置数据源
            }
            
            var videoTrackIndex = -1 // 视频轨道索引
            var videoFormat: android.media.MediaFormat? = null // 视频格式
            
            // 遍历所有轨道，找到视频轨道
            for (i in 0 until videoExtractor.trackCount) {
                val format = videoExtractor.getTrackFormat(i) // 获取轨道格式
                val mime = format.getString(android.media.MediaFormat.KEY_MIME) // 获取 MIME 类型
                if (mime?.startsWith("video/") == true) { // 检查是否为视频轨道
                    videoFormat = format // 保存视频格式
                    // 打印视频轨道格式
                    Log.d("VideoEditActivity", "视频轨道格式: $format")
                    videoExtractor.selectTrack(i) // 选择视频轨道
                    break
                }
            }

            // 提取音频轨道格式
            var audioTrackIndex = -1 // 音频轨道索引
            var audioFormat: android.media.MediaFormat? = null // 音频格式

            // 获取音频格式，使用 PCM 文件
            audioFormat = getAudioFormat(pcmFile)
            // 打印音频格式
            Log.d("VideoEditActivity", "音频格式: $audioFormat")

            // 检查是否至少有一个轨道可用
            if (videoFormat == null) {
                throw java.io.IOException("无法找到视频轨道")
            }

            // 创建 MediaMuxer 并添加所有轨道
            val muxer = MediaMuxer(outputFile.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
            
            // 添加视频轨道
            videoTrackIndex = muxer.addTrack(videoFormat)
            Log.d("VideoEditActivity", "添加视频轨道成功，索引: $videoTrackIndex")
            
            // 添加音频轨道（如果音频格式不为空）
            if (audioFormat != null) {
                audioTrackIndex = muxer.addTrack(audioFormat)
                Log.d("VideoEditActivity", "添加音频轨道成功，索引: $audioTrackIndex")
            }

            // 启动 MediaMuxer
            muxer.start()
            Log.d("VideoEditActivity", "MediaMuxer 启动成功")

            // 更新UI（在主线程），显示处理音频的状态
            Handler(Looper.getMainLooper()).post {
                progressBar.progress = 50
                statusText.text = "正在处理音频..."
            }

            // 编码并写入音频轨道（如果音频轨道索引有效且 PCM 文件存在）
            if (audioTrackIndex != -1 && pcmFile.exists() && pcmFile.length() > 0) {
                try {
                    // 重新配置并启动编码器
                    val sampleRate = 44100 // 采样率
                    val channelCount = 2 // 声道数
                    val bitRate = 128000 // 比特率
                    
                    // 创建 AAC 编码器
                    val encoder = android.media.MediaCodec.createEncoderByType("audio/mp4a-latm")
                    // 创建音频格式
                    val aacFormat = android.media.MediaFormat.createAudioFormat(
                        "audio/mp4a-latm",
                        sampleRate,
                        channelCount
                    )
                    // 配置音频格式参数
                    aacFormat.setInteger(android.media.MediaFormat.KEY_BIT_RATE, bitRate)
                    aacFormat.setInteger(android.media.MediaFormat.KEY_AAC_PROFILE, android.media.MediaCodecInfo.CodecProfileLevel.AACObjectLC)
                    aacFormat.setInteger(android.media.MediaFormat.KEY_MAX_INPUT_SIZE, 1024 * 1024)
                    
                    // 配置编码器
                    encoder.configure(aacFormat, null, null, android.media.MediaCodec.CONFIGURE_FLAG_ENCODE)
                    encoder.start() // 启动编码器
                    
                    // 打开 PCM 文件
                    val inputStream = java.io.FileInputStream(pcmFile)
                    val bufferInfo = android.media.MediaCodec.BufferInfo() // 用于存储编码后的数据信息
                    
                    var inputDone = false // 输入是否完成
                    var outputDone = false // 输出是否完成
                    var presentationTimeUs = 0L // 时间戳（微秒）
                    
                    // 计算每帧的时间戳增量
                    val frameSize = channelCount * 2 // 16bit = 2 bytes per sample
                    val samplesPerFrame = 1024 // AAC 通常一帧 1024 个样本
                    val timePerFrame = (samplesPerFrame * 1000000L) / sampleRate
                    
                    Log.d("VideoEditActivity", "开始编码 PCM 到 AAC...")
                    Log.d("VideoEditActivity", "MediaMuxer 状态: 已启动")
                    
                    // 编码循环
                    while (!outputDone) {
                        // 向编码器输入数据
                        if (!inputDone) {
                            val inputIndex = encoder.dequeueInputBuffer(10000) // 获取输入缓冲区
                            if (inputIndex >= 0) {
                                val encoderInputBuffer = encoder.getInputBuffer(inputIndex) // 获取输入缓冲区
                                if (encoderInputBuffer != null) {
                                    encoderInputBuffer.clear() // 清空缓冲区
                                    
                                    val bufferSize = encoderInputBuffer.capacity() // 缓冲区容量
                                    val inputBuffer = ByteArray(bufferSize) // 创建字节数组
                                    val bytesRead = inputStream.read(inputBuffer) // 从 PCM 文件读取数据
                                    
                                    if (bytesRead < 0) {
                                        // 输入结束，发送结束标记
                                        encoder.queueInputBuffer(
                                            inputIndex,
                                            0,
                                            0,
                                            presentationTimeUs,
                                            android.media.MediaCodec.BUFFER_FLAG_END_OF_STREAM
                                        )
                                        inputDone = true
                                        Log.d("VideoEditActivity", "PCM 数据读取完成")
                                    } else {
                                        // 处理实际读取的数据
                                        encoderInputBuffer.put(inputBuffer, 0, bytesRead) // 将数据放入输入缓冲区

                                        Log.d(TAG, "exportVideoWithAudio: queueInputBuffer $bytesRead 字节")
                                        // 提交输入缓冲区
                                        encoder.queueInputBuffer(
                                            inputIndex,
                                            0,
                                            bytesRead,
                                            presentationTimeUs,
                                            0
                                        )
                                        
                                        // 更新时间戳
                                        val samplesProcessed = bytesRead / frameSize
                                        presentationTimeUs += (samplesProcessed * 1000000L) / sampleRate
                                    }
                                }
                            }
                        }
                        
                        // 从编码器获取输出
                        val encoderOutputIndex = encoder.dequeueOutputBuffer(bufferInfo, 10000) // 获取输出缓冲区
                        when (encoderOutputIndex) {
                            android.media.MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED -> {}
                            android.media.MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> {}
                            android.media.MediaCodec.INFO_TRY_AGAIN_LATER -> {}
                            else -> {
                                if (encoderOutputIndex >= 0) {
                                    val encoderOutputBuffer = encoder.getOutputBuffer(encoderOutputIndex) // 获取输出缓冲区
                                    if (encoderOutputBuffer != null) {
                                        if (bufferInfo.flags and android.media.MediaCodec.BUFFER_FLAG_CODEC_CONFIG != 0) {
                                            // 这是编解码器配置数据，跳过
                                            bufferInfo.size = 0
                                        }
                                        if (bufferInfo.size > 0) {
                                            try {
                                                // 写入编码后的数据到 muxer
                                                encoderOutputBuffer.position(bufferInfo.offset)
                                                encoderOutputBuffer.limit(bufferInfo.offset + bufferInfo.size)
                                                // 打印写入的音频数据大小和时间戳
//                                                Log.d(TAG, "exportVideoWithAudio: 写入音频数据大小: ${bufferInfo.size}, 时间戳: ${bufferInfo.presentationTimeUs} us")
                                                muxer.writeSampleData(audioTrackIndex, encoderOutputBuffer, bufferInfo)
                                            } catch (e: Exception) {
                                                Log.e("VideoEditActivity", "写入音频数据失败: ${e.message}")
                                                // 继续处理，不影响视频导出
                                            }
                                        }
                                        encoder.releaseOutputBuffer(encoderOutputIndex, false) // 释放输出缓冲区
                                        if (bufferInfo.flags and android.media.MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) {
                                            outputDone = true
                                        }
                                    }
                                }
                            }
                        }
                    }
                    
                    // 释放资源
                    inputStream.close()
                    encoder.stop()
                    encoder.release()
                    
                    Log.d("VideoEditActivity", "音频编码完成")
                } catch (e: Exception) {
                    Log.e("VideoEditActivity", "音频处理失败: ${e.message}")
                    e.printStackTrace()
                    // 音频处理失败，继续处理视频
                    audioTrackIndex = -1
                }
            }

            // 更新UI（在主线程），显示合成的状态
            Handler(Looper.getMainLooper()).post {
                progressBar.progress = 70
                statusText.text = "正在合成..."
            }

            // 写入视频数据
            val buffer = java.nio.ByteBuffer.allocate(1024 * 1024) // 创建缓冲区
            val bufferInfo = android.media.MediaCodec.BufferInfo() // 用于存储视频数据信息

            // 写入视频轨道
            var totalVideoSamples = 0 // 总视频样本数
            var processedVideoSamples = 0 // 已处理视频样本数
            
            // 先计算总样本数
            while (true) {
                val sampleSize = videoExtractor.readSampleData(buffer, 0) // 读取样本数据
                if (sampleSize < 0) break // 读取完成
                totalVideoSamples++ // 增加样本数
                videoExtractor.advance() // 前进到下一个样本
            }
            
            // 重置视频提取器
            videoExtractor.seekTo(0, android.media.MediaExtractor.SEEK_TO_PREVIOUS_SYNC)

            // 写入视频数据
            while (true) {
                val sampleSize = videoExtractor.readSampleData(buffer, 0) // 读取样本数据
                if (sampleSize < 0) break // 读取完成

                // 设置缓冲区信息
                bufferInfo.offset = 0
                bufferInfo.size = sampleSize
                bufferInfo.presentationTimeUs = videoExtractor.sampleTime
                bufferInfo.flags = videoExtractor.sampleFlags
                // 打印写入的视频数据大小
//                Log.d(TAG, "exportVideoWithAudio: 写入视频数据大小: ${bufferInfo.size}")
                // 写入数据到 muxer
                muxer.writeSampleData(videoTrackIndex, buffer, bufferInfo)
                videoExtractor.advance() // 前进到下一个样本
                processedVideoSamples++ // 增加已处理样本数

                // 计算视频进度
                val videoProgress = if (totalVideoSamples > 0) {
                    (processedVideoSamples * 20 / totalVideoSamples).toInt()
                } else {
                    20 // 最大进度
                }
                val currentProgress = 30 + videoProgress // 计算当前总进度
                
                // 更新UI（在主线程）
                Handler(Looper.getMainLooper()).post {
                    progressBar.progress = currentProgress.coerceIn(30, 70) // 确保进度在 30-70 之间
                }
            }

            // 音频已经在转换过程中写入，不需要单独处理
            if (audioTrackIndex != -1) {
                Log.d("VideoEditActivity", "音频已经在转换过程中写入完成")
            } else {
                // 音频转换失败，跳过音频轨道
                Log.d("VideoEditActivity", "音频转换失败，跳过音频轨道")
            }

            // 更新UI（在主线程），显示导出完成的状态
            Handler(Looper.getMainLooper()).post {
                progressBar.progress = 100
                statusText.text = if (audioTrackIndex != -1) "导出完成!" else "导出完成（仅视频）!"
            }

            // 释放资源
            muxer.stop()
            muxer.release()
            videoExtractor.release()

            Log.d("VideoEditActivity", "视频导出成功: ${outputFile.absolutePath}")
            return outputFile.absolutePath
        } catch (e: Exception) {
            Log.e("VideoEditActivity", "导出过程中出现错误: ${e.message}")
            e.printStackTrace()
            
            // 尝试只导出视频
            try {
                Log.d("VideoEditActivity", "尝试只导出视频...")
                
                // 创建新的 MediaMuxer
                val muxer = MediaMuxer(outputFile.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
                
                // 提取视频轨道
                val videoExtractor = android.media.MediaExtractor() // 创建媒体提取器
                val videoFile = java.io.File(filesDir, "sample_video_v1.mp4") // 视频文件
                if (!videoFile.exists()) {
                    throw java.io.IOException("视频文件不存在: ${videoFile.absolutePath}")
                }
                val videoUri = Uri.fromFile(videoFile) // 转换为 URI 格式
                videoExtractor.setDataSource(this, videoUri, null) // 设置数据源
                
                var videoTrackIndex = -1 // 视频轨道索引
                // 遍历所有轨道，找到视频轨道
                for (i in 0 until videoExtractor.trackCount) {
                    val format = videoExtractor.getTrackFormat(i) // 获取轨道格式
                    val mime = format.getString(android.media.MediaFormat.KEY_MIME) // 获取 MIME 类型
                    if (mime?.startsWith("video/") == true) { // 检查是否为视频轨道
                        videoTrackIndex = muxer.addTrack(format) // 添加视频轨道
                        videoExtractor.selectTrack(i) // 选择视频轨道
                        break
                    }
                }
                
                if (videoTrackIndex != -1) {
                    // 开始合成
                    muxer.start()
                    
                    // 写入视频数据
                    val buffer = java.nio.ByteBuffer.allocate(1024 * 1024) // 创建缓冲区
                    val bufferInfo = android.media.MediaCodec.BufferInfo() // 用于存储视频数据信息
                    
                    // 读取并写入视频数据
                    while (true) {
                        val sampleSize = videoExtractor.readSampleData(buffer, 0) // 读取样本数据
                        if (sampleSize < 0) break // 读取完成
                        
                        // 设置缓冲区信息
                        bufferInfo.offset = 0
                        bufferInfo.size = sampleSize
                        bufferInfo.presentationTimeUs = videoExtractor.sampleTime
                        bufferInfo.flags = videoExtractor.sampleFlags
                        
                        // 写入数据到 muxer
                        muxer.writeSampleData(videoTrackIndex, buffer, bufferInfo)
                        videoExtractor.advance() // 前进到下一个样本
                    }
                    
                    // 完成合成
                    muxer.stop()
                    muxer.release()
                    videoExtractor.release()
                    
                    // 更新UI（在主线程），显示仅视频导出完成的状态
                    Handler(Looper.getMainLooper()).post {
                        progressBar.progress = 100
                        statusText.text = "导出完成（仅视频）!"
                    }
                    
                    Log.d("VideoEditActivity", "仅视频导出成功: ${outputFile.absolutePath}")
                    return outputFile.absolutePath
                } else {
                    throw java.io.IOException("无法找到视频轨道")
                }
                
            } catch (innerE: Exception) {
                Log.e("VideoEditActivity", "仅视频导出也失败: ${innerE.message}")
                innerE.printStackTrace()
                outputFile.delete() // 删除失败的输出文件
                throw innerE // 抛出异常
            }
        }
    }

    private fun getAudioFormat(
        pcmFile: File
    ): MediaFormat? {
        var audioFormat: android.media.MediaFormat? = null
        try {
            // 直接使用 PCM 文件进行编码
            if (pcmFile.exists() && pcmFile.length() > 0) {
                Log.d("VideoEditActivity", "使用 PCM 文件进行编码: ${pcmFile.absolutePath}")

                // 配置 AAC 编码器
                val sampleRate = 44100
                val channelCount = 2
                val bitRate = 128000

                val encoder = MediaCodec.createEncoderByType("audio/mp4a-latm")
                val aacFormat = MediaFormat.createAudioFormat(
                    "audio/mp4a-latm",
                    sampleRate,
                    channelCount
                )
                aacFormat.setInteger(MediaFormat.KEY_BIT_RATE, bitRate)
                aacFormat.setInteger(
                    MediaFormat.KEY_AAC_PROFILE,
                    MediaCodecInfo.CodecProfileLevel.AACObjectLC
                )
                aacFormat.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, 1024 * 1024)

                // 打印编码器配置信息
                Log.d("VideoEditActivity", "编码器配置信息: $aacFormat")
                encoder.configure(aacFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
                encoder.start()

                // 获取编码器输出格式（包含音频轨道信息）
                var formatChanged = false
                while (!formatChanged) {
                    val encoderOutputIndex =
                        encoder.dequeueOutputBuffer(MediaCodec.BufferInfo(), 10000)
                    when (encoderOutputIndex) {
                        MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> {
                            audioFormat = encoder.outputFormat
                            Log.d(
                                "VideoEditActivity",
                                "编码器输出格式: ${audioFormat?.getString(MediaFormat.KEY_MIME)}"
                            )
                            formatChanged = true
                        }

                        MediaCodec.INFO_TRY_AGAIN_LATER -> {}
                        else -> {
                            if (encoderOutputIndex >= 0) {
                                encoder.releaseOutputBuffer(encoderOutputIndex, false)
                            }
                        }
                    }
                }

                encoder.stop()
                encoder.release()

            } else {
                Log.e("VideoEditActivity", "PCM 文件不存在或为空: ${pcmFile.absolutePath}")
            }
        } catch (e: Exception) {
            Log.e("VideoEditActivity", "音频格式获取失败: ${e.message}")
            e.printStackTrace()
        }
        return audioFormat
    }
}

/**
 * 缩略图信息数据类
 */
// 缩略图点击回调接口
interface OnThumbnailClickListener {
    fun onThumbnailClick(timeMs: Long)
}

data class ThumbnailInfo(
    val timeMs: Long,      // 时间戳（毫秒）
    val timeText: String,  // 格式化的时间文本
    val bitmap: Bitmap? // 缩略图位图
)

/**
 * 缩略图时间轴视图
 *
 * 显示视频的时间轴和对应时间点的缩略图
 */
class ThumbnailTimelineView(context: android.content.Context, attrs: android.util.AttributeSet) 
    : android.widget.LinearLayout(context, attrs) {
    
    private val thumbnailViews = mutableListOf<ThumbnailItemView>()
    private var currentPositionMs: Long = 0L
    private var totalDurationMs: Long = 0L
    private var thumbnailClickListener: OnThumbnailClickListener? = null
    
    init {
        orientation = HORIZONTAL
        setPadding(8, 8, 8, 8)
        setBackgroundColor(android.graphics.Color.DKGRAY)
    }
    
    fun setThumbnailClickListener(listener: OnThumbnailClickListener) {
        this.thumbnailClickListener = listener
    }
    
    fun setThumbnails(thumbnails: List<ThumbnailInfo>) {
        removeAllViews()
        thumbnailViews.clear()
        
        thumbnails.forEach { thumbnail ->
            val itemView = ThumbnailItemView(context, thumbnailClickListener).apply {
                setThumbnailInfo(thumbnail)
            }
            addView(itemView)
            thumbnailViews.add(itemView)
        }
    }
    
    fun setCurrentPosition(positionMs: Long, durationMs: Long) {
        currentPositionMs = positionMs
        totalDurationMs = durationMs
        
        // 更新每个缩略图项的选中状态
        thumbnailViews.forEachIndexed { index, view ->
            val thumbnail = view.getThumbnailInfo()
            val isSelected = index == (positionMs / 1000).toInt()
            view.setSelected(isSelected)
        }
        
        invalidate()
    }
    
    fun getCurrentPosition(): Long = currentPositionMs
    
    fun getTotalDuration(): Long = totalDurationMs
}

/**
 * 单个缩略图项视图
 */
class ThumbnailItemView(context: android.content.Context, private val listener: OnThumbnailClickListener? = null) : android.widget.LinearLayout(context) {
    
    private var thumbnailInfo: ThumbnailInfo? = null
    private var isSelected: Boolean = false
    
    private val timeTextView: android.widget.TextView
    private val thumbnailImageView: android.widget.ImageView
    
    init {
        orientation = VERTICAL
        setPadding(8, 8, 8, 8)
        
        thumbnailImageView = android.widget.ImageView(context).apply {
            layoutParams = LayoutParams(120, 68)
            setBackgroundColor(android.graphics.Color.DKGRAY)
            scaleType = android.widget.ImageView.ScaleType.CENTER_CROP
        }
        addView(thumbnailImageView)
        
        timeTextView = android.widget.TextView(context).apply {
            layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
            gravity = android.view.Gravity.CENTER
            setTextColor(android.graphics.Color.WHITE)
            textSize = 12f
        }
        addView(timeTextView)
        
        // 添加点击事件
        setOnClickListener {
            thumbnailInfo?.let {
                listener?.onThumbnailClick(it.timeMs)
            }
        }
        
        // 更改光标为点击样式
        isClickable = true
        isFocusable = true
    }
    
    fun setThumbnailInfo(info: ThumbnailInfo) {
        thumbnailInfo = info
        timeTextView.text = info.timeText
        
        if (info.bitmap != null) {
            // 设置真实的缩略图
            thumbnailImageView.setImageBitmap(info.bitmap)
        } else {
            // 设置默认背景色作为占位
            thumbnailImageView.setBackgroundColor(android.graphics.Color.rgb(50, 50, 50))
        }
    }
    
    fun getThumbnailInfo(): ThumbnailInfo? = thumbnailInfo
    
    override fun setSelected(selected: Boolean) {
        isSelected = selected
        setBackgroundColor(
            if (selected) android.graphics.Color.argb(100, 0, 120, 215)
            else android.graphics.Color.TRANSPARENT
        )
    }
}

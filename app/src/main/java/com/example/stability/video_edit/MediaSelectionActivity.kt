package com.example.stability.video_edit

import android.Manifest
import android.app.Activity
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.stability.video_edit.ui.theme.StabilityTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MediaSelectionActivity : ComponentActivity() {

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        Log.d(TAG, "Permissions granted: $allGranted")
        if (allGranted) {
            Log.d(TAG, "Permissions granted, reloading media")
            // 权限授予后重新加载媒体
            setContent {
                StabilityTheme {
                    MediaSelectionScreen(
                        onMediaSelected = { mediaItem ->
                            Log.d(TAG, "Media selected: ${mediaItem.path}")
                            val resultIntent = Intent().apply {
                                putExtra(EXTRA_SELECTED_MEDIA_PATH, mediaItem.path)
                                putExtra(EXTRA_SELECTED_MEDIA_TYPE, mediaItem.mediaType.name)
                            }
                            setResult(Activity.RESULT_OK, resultIntent)
                            finish()
                        },
                        onBack = { finish() }
                    )
                }
            }
        } else {
            Log.e(TAG, "Some permissions were denied")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!hasRequiredPermissions()) {
            requestPermissions()
        }

        setContent {
            StabilityTheme {
                MediaSelectionScreen(
                    onMediaSelected = { mediaItem ->
                        Log.d(TAG, "Media selected: ${mediaItem.path}")
                        val resultIntent = Intent().apply {
                            putExtra(EXTRA_SELECTED_MEDIA_PATH, mediaItem.path)
                            putExtra(EXTRA_SELECTED_MEDIA_TYPE, mediaItem.mediaType.name)
                        }
                        setResult(Activity.RESULT_OK, resultIntent)
                        finish()
                    },
                    onBack = { finish() }
                )
            }
        }
    }

    private fun hasRequiredPermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_MEDIA_VIDEO
            ) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_MEDIA_IMAGES
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestPermissions() {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                Manifest.permission.READ_MEDIA_VIDEO,
                Manifest.permission.READ_MEDIA_IMAGES
            )
        } else {
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        permissionLauncher.launch(permissions)
    }

    companion object {
        const val EXTRA_SELECTED_MEDIA_PATH = "selected_media_path"
        const val EXTRA_SELECTED_MEDIA_TYPE = "selected_media_type"
    }
}

private const val TAG = "MediaSelectionActivity"

private fun getVideoThumbnailPainter(context: Context, videoPath: String, duration: Long): androidx.compose.ui.graphics.painter.Painter? {
    return try {
        val retriever = MediaMetadataRetriever()
        if (videoPath.startsWith("content://")) {
            retriever.setDataSource(context, Uri.parse(videoPath))
        } else {
            retriever.setDataSource(videoPath)
        }
        // 获取视频第一帧作为缩略图
        val bitmap = retriever.getFrameAtTime(0, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
        retriever.release()
        bitmap?.let { androidx.compose.ui.graphics.painter.BitmapPainter(it.asImageBitmap()) }
    } catch (e: Exception) {
        Log.e(TAG, "Error getting video thumbnail: ${e.message}")
        null
    }
}

enum class MediaType {
    IMAGE,
    VIDEO
}

data class MediaItemData(
    val id: Long,
    val path: String,
    val name: String,
    val mediaType: MediaType,
    val dateAdded: Long,
    val size: Long,
    val duration: Long = 0
)

class MediaViewModel : ViewModel() {
    private val TAG = "MediaViewModel"
    private val _imageList = MutableStateFlow<List<MediaItemData>>(emptyList())
    val imageList: StateFlow<List<MediaItemData>> = _imageList.asStateFlow()

    private val _videoList = MutableStateFlow<List<MediaItemData>>(emptyList())
    val videoList: StateFlow<List<MediaItemData>> = _videoList.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun loadMedia(context: Context) {
        _isLoading.value = true
        MainScope().launch {
            withContext(Dispatchers.IO) {
                val images = queryImages(context)
                val videos = queryVideos(context)
                _imageList.value = images
                _videoList.value = videos
            }
            _isLoading.value = false
        }
    }

    private fun queryImages(context: Context): List<MediaItemData> {
        val images = mutableListOf<MediaItemData>()
        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        }

        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.DATE_ADDED,
            MediaStore.Images.Media.SIZE
        )

        val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"

        context.contentResolver.query(
            collection,
            projection,
            null,
            null,
            sortOrder
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
            val dateColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED)
            val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val name = cursor.getString(nameColumn)
                val dateAdded = cursor.getLong(dateColumn)
                val size = cursor.getLong(sizeColumn)
                
                // 构建内容 URI 而不是使用 DATA 列
                val contentUri = ContentUris.withAppendedId(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id
                )
                val path = contentUri.toString()

                Log.d(TAG, "Found image: $name, path: $path")

                images.add(
                    MediaItemData(
                        id = id,
                        path = path,
                        name = name,
                        mediaType = MediaType.IMAGE,
                        dateAdded = dateAdded,
                        size = size
                    )
                )
            }
        }
        Log.d(TAG, "Found ${images.size} images")
        return images
    }

    private fun queryVideos(context: Context): List<MediaItemData> {
        val videos = mutableListOf<MediaItemData>()
        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        }

        val projection = arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.DISPLAY_NAME,
            MediaStore.Video.Media.DATE_ADDED,
            MediaStore.Video.Media.SIZE,
            MediaStore.Video.Media.DURATION
        )

        val sortOrder = "${MediaStore.Video.Media.DATE_ADDED} DESC"

        context.contentResolver.query(
            collection,
            projection,
            null,
            null,
            sortOrder
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
            val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
            val dateColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED)
            val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)
            val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val name = cursor.getString(nameColumn)
                val dateAdded = cursor.getLong(dateColumn)
                val size = cursor.getLong(sizeColumn)
                val duration = cursor.getLong(durationColumn)
                
                // 构建内容 URI 而不是使用 DATA 列
                val contentUri = ContentUris.withAppendedId(
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id
                )
                val path = contentUri.toString()

                Log.d(TAG, "Found video: $name, path: $path, duration: $duration")

                videos.add(
                    MediaItemData(
                        id = id,
                        path = path,
                        name = name,
                        mediaType = MediaType.VIDEO,
                        dateAdded = dateAdded,
                        size = size,
                        duration = duration
                    )
                )
            }
        }
        Log.d(TAG, "Found ${videos.size} videos")
        return videos
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaSelectionScreen(
    viewModel: MediaViewModel = viewModel(),
    onMediaSelected: (MediaItemData) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val imageList by viewModel.imageList.collectAsState()
    val videoList by viewModel.videoList.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("图片", "视频")

    var selectedItem by remember { mutableStateOf<MediaItemData?>(null) }

    LaunchedEffect(Unit) {
        viewModel.loadMedia(context)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("选择媒体文件") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            TabRow(selectedTabIndex = selectedTabIndex) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(title) },
                        icon = {
                            Icon(
                                imageVector = if (index == 0) Icons.Default.Image else Icons.Default.VideoLibrary,
                                contentDescription = null
                            )
                        }
                    )
                }
            }

            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                when {
                    isLoading -> {
                        CircularProgressIndicator()
                    }
                    selectedTabIndex == 0 -> {
                        MediaGrid(
                            mediaList = imageList,
                            mediaType = MediaType.IMAGE,
                            selectedItem = selectedItem,
                            onItemClick = { item ->
                                selectedItem = item
                                onMediaSelected(item)
                            }
                        )
                    }
                    else -> {
                        MediaGrid(
                            mediaList = videoList,
                            mediaType = MediaType.VIDEO,
                            selectedItem = selectedItem,
                            onItemClick = { item ->
                                selectedItem = item
                                onMediaSelected(item)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MediaGrid(
    mediaList: List<MediaItemData>,
    mediaType: MediaType,
    selectedItem: MediaItemData?,
    onItemClick: (MediaItemData) -> Unit
) {
    if (mediaList.isEmpty()) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Icon(
                imageVector = if (mediaType == MediaType.IMAGE) Icons.Default.Image else Icons.Default.VideoLibrary,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = Color.Gray
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = if (mediaType == MediaType.IMAGE) "没有找到图片" else "没有找到视频",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Gray
            )
        }
    } else {
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            contentPadding = PaddingValues(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(mediaList, key = { it.id }) { mediaItem ->
                MediaGridItem(
                    mediaItem = mediaItem,
                    isSelected = selectedItem?.id == mediaItem.id,
                    onClick = { onItemClick(mediaItem) }
                )
            }
        }
    }
}

@Composable
fun MediaGridItem(
    mediaItem: MediaItemData,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surface
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            when (mediaItem.mediaType) {
                MediaType.IMAGE -> {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(mediaItem.path)
                            .crossfade(true)
                            .build(),
                        contentDescription = mediaItem.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                MediaType.VIDEO -> {
                    // 使用视频第一帧作为缩略图
                    val thumbnailPainter = remember {
                        getVideoThumbnailPainter(context, mediaItem.path, mediaItem.duration)
                    }
                    thumbnailPainter?.let {
                        androidx.compose.foundation.Image(
                            painter = it,
                            contentDescription = mediaItem.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } ?: run {
                        // 如果获取缩略图失败，显示视频图标
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Gray.copy(alpha = 0.3f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.VideoFile,
                                contentDescription = "Video",
                                modifier = Modifier.size(48.dp),
                                tint = Color.Gray
                            )
                        }
                    }
                    // 视频时长显示在右上角
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .background(Color.Black.copy(alpha = 0.7f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                            .clip(RoundedCornerShape(4.dp))
                    ) {
                        Text(
                            text = formatDuration(mediaItem.duration),
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }

            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .size(24.dp)
                )
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            ) {
                Text(
                    text = mediaItem.name,
                    color = Color.White,
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

private fun formatDuration(durationMs: Long): String {
    if (durationMs <= 0) return "00:00"
    val totalSeconds = durationMs / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d", minutes, seconds)
}
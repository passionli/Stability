package com.example.stability.webview

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.webkit.JavascriptInterface
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController

@Composable
fun WebViewScreen(navController: NavController) {
    val viewModel: WebViewModel = viewModel()
    val context = LocalContext.current
    
    val showJsDialog = remember { mutableStateOf(false) }
    var jsMessage by remember { mutableStateOf("") }

    LaunchedEffect(viewModel.jsMessage.value) {
        viewModel.jsMessage.value?.let {
            jsMessage = it
            showJsDialog.value = true
            viewModel.clearJsMessage()
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        WebViewToolbar(
            viewModel = viewModel,
            onBack = { navController.popBackStack() }
        )
        
        Box(modifier = Modifier.weight(1f)) {
            WebViewContent(viewModel = viewModel)
            
            when (val state = viewModel.uiState.value) {
                is WebViewUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .size(64.dp)
                            .align(Alignment.Center)
                    )
                }
                is WebViewUiState.Error -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "加载失败: ${state.message}",
                            color = MaterialTheme.colorScheme.error
                        )
                        Button(
                            onClick = { viewModel.reload() },
                            modifier = Modifier.padding(top = 16.dp)
                        ) {
                            Text("重试")
                        }
                    }
                }
                else -> {}
            }
        }
    }

    if (showJsDialog.value) {
        JsMessageDialog(
            message = jsMessage,
            onDismiss = { showJsDialog.value = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WebViewToolbar(
    viewModel: WebViewModel,
    onBack: () -> Unit
) {
    var url by remember { mutableStateOf("https://www.baidu.com") }

    Column(modifier = Modifier.padding(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "返回")
            }
            
            IconButton(onClick = { viewModel.goBack() }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "后退")
            }
            
            IconButton(onClick = { viewModel.goForward() }) {
                Icon(Icons.Default.ArrowForward, contentDescription = "前进")
            }
            
            IconButton(onClick = { viewModel.reload() }) {
                Icon(Icons.Default.Refresh, contentDescription = "刷新")
            }

            OutlinedTextField(
                value = url,
                onValueChange = { url = it },
                placeholder = { Text("输入网址") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                keyboardActions = KeyboardActions(onGo = { viewModel.loadUrl(url) })
            )

            Button(
                onClick = { viewModel.loadUrl(url) },
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Text("访问")
            }
        }
        
        Text(
            text = viewModel.pageTitle.value.ifEmpty { "WebView 示例" },
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(start = 16.dp)
        )
    }
}

@SuppressLint("SetJavaScriptEnabled", "AddJavascriptInterface")
@Composable
fun WebViewContent(viewModel: WebViewModel) {
    val context = LocalContext.current
    var fileUploadCallback by remember { mutableStateOf<ValueCallback<Array<Uri>>?>(null) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        fileUploadCallback?.onReceiveValue(uris?.toTypedArray())
        fileUploadCallback = null
    }

    AndroidView(
        factory = { ctx ->
            WebView(ctx).apply {
                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    allowFileAccess = true
                    allowContentAccess = true
                    mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                    setSupportZoom(true)
                    builtInZoomControls = true
                    displayZoomControls = false
                }

                addJavascriptInterface(
                    JsInterface { message ->
                        viewModel.onJsMessage(message)
                    }, "AndroidInterface"
                )

                webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(
                        view: WebView?,
                        request: WebResourceRequest?
                    ): Boolean {
                        val url = request?.url?.toString() ?: return false
                        if (url.startsWith("http://") || url.startsWith("https://")) {
                            return false
                        }
                        try {
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                        return true
                    }

                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        url?.let { viewModel.onPageFinished(view?.title ?: "") }
                    }
                }

                webChromeClient = object : WebChromeClient() {
                    override fun onShowFileChooser(
                        webView: WebView?,
                        filePathCallback: ValueCallback<Array<Uri>>?,
                        fileChooserParams: FileChooserParams?
                    ): Boolean {
                        fileUploadCallback?.onReceiveValue(null)
                        fileUploadCallback = filePathCallback
                        filePickerLauncher.launch("*/*")
                        return true
                    }
                }

                loadUrl("file:///android_asset/webview_demo.html")
            }
        },
        modifier = Modifier.fillMaxSize(),
        update = { webView ->
            when (val state = viewModel.uiState.value) {
                is WebViewUiState.GoBack -> {
                    if (webView.canGoBack()) {
                        webView.goBack()
                    }
                }
                is WebViewUiState.GoForward -> {
                    if (webView.canGoForward()) {
                        webView.goForward()
                    }
                }
                is WebViewUiState.Reload -> {
                    webView.reload()
                }
                else -> {}
            }
        }
    )
}

class JsInterface(private val callback: (String) -> Unit) {
    @JavascriptInterface
    fun sendMessage(message: String) {
        callback(message)
    }

    @JavascriptInterface
    fun getDeviceInfo(): String {
        return "Android ${Build.VERSION.RELEASE}"
    }

    @JavascriptInterface
    fun showToast(message: String) {
        callback("Toast: $message")
    }
}

@Composable
fun JsMessageDialog(message: String, onDismiss: () -> Unit) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("JavaScript 消息") },
        text = { Text(message) },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("确定")
            }
        }
    )
}

@Composable
fun Row(
    modifier: Modifier = Modifier,
    verticalAlignment: Alignment.Vertical = Alignment.Top,
    content: @Composable () -> Unit
) {
    androidx.compose.foundation.layout.Row(
        modifier = modifier,
        verticalAlignment = verticalAlignment
    ) {
        content()
    }
}

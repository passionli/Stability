package com.example.stability.webview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class WebViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<WebViewUiState>(WebViewUiState.Idle)
    val uiState: StateFlow<WebViewUiState> = _uiState.asStateFlow()

    private val _currentUrl = MutableStateFlow("")
    val currentUrl: StateFlow<String> = _currentUrl.asStateFlow()

    private val _pageTitle = MutableStateFlow("")
    val pageTitle: StateFlow<String> = _pageTitle.asStateFlow()

    private val _jsMessage = MutableStateFlow<String?>(null)
    val jsMessage: StateFlow<String?> = _jsMessage.asStateFlow()

    fun loadUrl(url: String) {
        _uiState.value = WebViewUiState.Loading
        _currentUrl.value = url
        viewModelScope.launch(Dispatchers.IO) {
            kotlinx.coroutines.delay(500)
            _uiState.value = WebViewUiState.Loaded
        }
    }

    fun onPageFinished(title: String) {
        _pageTitle.value = title
        _uiState.value = WebViewUiState.Loaded
    }

    fun onLoadError(error: String) {
        _uiState.value = WebViewUiState.Error(error)
    }

    fun onJsMessage(message: String) {
        _jsMessage.value = message
    }

    fun clearJsMessage() {
        _jsMessage.value = null
    }

    fun goBack() {
        _uiState.value = WebViewUiState.GoBack
    }

    fun goForward() {
        _uiState.value = WebViewUiState.GoForward
    }

    fun reload() {
        _uiState.value = WebViewUiState.Reload
    }
}

sealed interface WebViewUiState {
    object Idle : WebViewUiState
    object Loading : WebViewUiState
    object Loaded : WebViewUiState
    data class Error(val message: String) : WebViewUiState
    object GoBack : WebViewUiState
    object GoForward : WebViewUiState
    object Reload : WebViewUiState
}

package com.example.stability.voiceassistant.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.stability.voiceassistant.data.model.DialogMessage
import com.example.stability.voiceassistant.data.model.IntentType
import com.example.stability.voiceassistant.data.model.VoiceAssistantState
import com.example.stability.voiceassistant.manager.IntentParser
import com.example.stability.voiceassistant.manager.SpeechRecognizer
import com.example.stability.voiceassistant.manager.TtsManager
import com.example.stability.voiceassistant.manager.WakeUpManager
import com.example.stability.voiceassistant.service.AirConditionService
import com.example.stability.voiceassistant.service.MediaService
import com.example.stability.voiceassistant.service.NavigationService
import com.example.stability.voiceassistant.service.WindowService
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class VoiceAssistantViewModel(application: Application) : AndroidViewModel(application) {
    private val wakeUpManager = WakeUpManager()
    private val speechRecognizer = SpeechRecognizer()
    private val intentParser = IntentParser()
    private val ttsManager = TtsManager(application)
    private val navigationService = NavigationService()
    private val mediaService = MediaService()
    private val airConditionService = AirConditionService()
    private val windowService = WindowService()

    private val _state = MutableLiveData(VoiceAssistantState())
    val state: LiveData<VoiceAssistantState> = _state

    init {
        setupListeners()
        ttsManager.init()
        startWakeUpListening()
    }

    private fun setupListeners() {
        wakeUpManager.setListener(object : WakeUpManager.WakeUpListener {
            override fun onWakeUp() {
                handleWakeUp()
            }

            override fun onWakeUpFailed() {
            }
        })

        speechRecognizer.setListener(object : SpeechRecognizer.RecognitionListener {
            override fun onResult(text: String, confidence: Float) {
                handleRecognitionResult(text, confidence)
            }

            override fun onError(error: String) {
                addMessage(error, false)
                updateStatus("识别失败: $error")
            }

            override fun onListeningStart() {
                updateStatus("正在听...")
                _state.value = _state.value?.copy(isListening = true)
            }

            override fun onListeningEnd() {
                _state.value = _state.value?.copy(isListening = false)
            }
        })

        ttsManager.setListener(object : TtsManager.TtsListener {
            override fun onStart() {
                updateStatus("正在说话...")
                _state.value = _state.value?.copy(isSpeaking = true)
            }

            override fun onComplete() {
                updateStatus("待机中")
                _state.value = _state.value?.copy(isSpeaking = false)
                if (_state.value?.isAwake == true) {
                    startListening()
                }
            }

            override fun onError(error: String) {
                updateStatus("语音合成失败: $error")
                _state.value = _state.value?.copy(isSpeaking = false)
            }
        })
    }

    fun startWakeUpListening() {
        wakeUpManager.startListening()
        updateStatus("等待唤醒...")
    }

    fun stopWakeUpListening() {
        wakeUpManager.stopListening()
    }

    fun processInput(input: String) {
        if (wakeUpManager.isWakeWord(input)) {
            wakeUpManager.processAudio(input)
        } else if (_state.value?.isAwake == true) {
            speechRecognizer.simulateRecognition(input)
        }
    }

    private fun handleWakeUp() {
        updateStatus("已唤醒")
        _state.value = _state.value?.copy(isAwake = true)
        addMessage("你好，我是小迪，请问有什么可以帮您？", false)
        
        viewModelScope.launch {
            delay(500)
            ttsManager.speak("你好，我是小迪，请问有什么可以帮您？")
        }
    }

    private fun handleRecognitionResult(text: String, confidence: Float) {
        addMessage(text, true)
        updateStatus("正在分析...")

        val command = intentParser.parse(text)
        
        viewModelScope.launch {
            delay(800)
            
            val response = when (command.intentType) {
                IntentType.NAVIGATION -> navigationService.handleCommand(command)
                IntentType.MEDIA -> mediaService.handleCommand(command)
                IntentType.AIR_CONDITION -> airConditionService.handleCommand(command)
                IntentType.WINDOW -> windowService.handleCommand(command)
                else -> "抱歉，我不太明白您的意思"
            }

            addMessage(response, false)
            ttsManager.speak(response)
        }
    }

    private fun startListening() {
        speechRecognizer.startListening()
    }

    private fun stopListening() {
        speechRecognizer.stopListening()
    }

    fun goToSleep() {
        stopListening()
        _state.value = _state.value?.copy(isAwake = false)
        updateStatus("待机中")
        startWakeUpListening()
    }

    private fun addMessage(text: String, isUser: Boolean) {
        val newMessage = DialogMessage(
            id = System.currentTimeMillis().toString(),
            text = text,
            isUser = isUser
        )
        val updatedHistory = _state.value?.dialogHistory.orEmpty().toMutableList()
        updatedHistory.add(newMessage)
        _state.value = _state.value?.copy(dialogHistory = updatedHistory)
    }

    private fun updateStatus(status: String) {
        _state.value = _state.value?.copy(currentStatus = status)
    }

    override fun onCleared() {
        super.onCleared()
        ttsManager.release()
        stopWakeUpListening()
    }
}

package com.example.stability.voiceassistant.data.model

data class VoiceAssistantState(
    val isAwake: Boolean = false,
    val isListening: Boolean = false,
    val isSpeaking: Boolean = false,
    val dialogHistory: List<DialogMessage> = emptyList(),
    val currentStatus: String = "待机中"
)

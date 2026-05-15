package com.example.stability.voiceassistant.data.model

data class VoiceCommand(
    val text: String,
    val intentType: IntentType,
    val parameters: Map<String, String> = emptyMap(),
    val confidence: Float = 1.0f
)

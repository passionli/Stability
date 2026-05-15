package com.example.stability.voiceassistant.data.model

data class DialogMessage(
    val id: String,
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val status: MessageStatus = MessageStatus.SENT
)

enum class MessageStatus {
    SENT,
    RECEIVING,
    RECEIVED
}

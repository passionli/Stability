package com.example.stability.voiceassistant.manager

class WakeUpManager {
    companion object {
        private const val WAKE_WORD = "你好，小迪"
        private const val WAKE_WORD_ALT = "你好小迪"
    }

    interface WakeUpListener {
        fun onWakeUp()
        fun onWakeUpFailed()
    }

    private var listener: WakeUpListener? = null
    private var isListening = false

    fun setListener(listener: WakeUpListener) {
        this.listener = listener
    }

    fun startListening() {
        isListening = true
    }

    fun stopListening() {
        isListening = false
    }

    fun processAudio(input: String) {
        if (!isListening) return

        val normalizedInput = input.trim()
        if (normalizedInput == WAKE_WORD || normalizedInput == WAKE_WORD_ALT) {
            listener?.onWakeUp()
        }
    }

    fun isWakeWord(input: String): Boolean {
        val normalizedInput = input.trim()
        return normalizedInput == WAKE_WORD || normalizedInput == WAKE_WORD_ALT
    }
}

package com.example.stability.voiceassistant.manager

class SpeechRecognizer {
    interface RecognitionListener {
        fun onResult(text: String, confidence: Float)
        fun onError(error: String)
        fun onListeningStart()
        fun onListeningEnd()
    }

    private var listener: RecognitionListener? = null
    private var isListening = false

    fun setListener(listener: RecognitionListener) {
        this.listener = listener
    }

    fun startListening() {
        isListening = true
        listener?.onListeningStart()
    }

    fun stopListening() {
        isListening = false
        listener?.onListeningEnd()
    }

    fun simulateRecognition(input: String) {
        if (!isListening) {
            listener?.onError("Not listening")
            return
        }

        val processedText = input.trim()
        if (processedText.isEmpty()) {
            listener?.onError("Empty input")
            return
        }

        val confidence = calculateConfidence(processedText)
        listener?.onResult(processedText, confidence)
    }

    private fun calculateConfidence(text: String): Float {
        val keywords = listOf("导航", "播放", "音乐", "空调", "温度", "车窗", "打开", "关闭", "调高", "调低")
        val matchedCount = keywords.count { text.contains(it) }
        val baseConfidence = 0.85f
        val bonus = matchedCount * 0.03f
        return minOf(baseConfidence + bonus, 0.99f)
    }
}

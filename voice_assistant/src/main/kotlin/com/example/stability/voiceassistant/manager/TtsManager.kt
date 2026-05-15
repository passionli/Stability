package com.example.stability.voiceassistant.manager

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import java.util.Locale

class TtsManager(private val context: Context) {
    interface TtsListener {
        fun onStart()
        fun onComplete()
        fun onError(error: String)
    }

    private var tts: TextToSpeech? = null
    private var listener: TtsListener? = null
    private var isInitialized = false

    fun setListener(listener: TtsListener) {
        this.listener = listener
    }

    fun init() {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = tts?.setLanguage(Locale.CHINA)
                if (result == TextToSpeech.LANG_MISSING_DATA || 
                    result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    listener?.onError("中文语言包不可用")
                } else {
                    isInitialized = true
                    tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                        override fun onStart(utteranceId: String?) {
                            listener?.onStart()
                        }

                        override fun onDone(utteranceId: String?) {
                            listener?.onComplete()
                        }

                        override fun onError(utteranceId: String?) {
                            listener?.onError("语音合成失败")
                        }
                    })
                }
            } else {
                listener?.onError("TTS初始化失败")
            }
        }
    }

    fun speak(text: String) {
        if (!isInitialized || tts == null) {
            listener?.onError("TTS未初始化")
            return
        }

        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "utterance_${System.currentTimeMillis()}")
    }

    fun stop() {
        tts?.stop()
    }

    fun release() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        isInitialized = false
    }
}

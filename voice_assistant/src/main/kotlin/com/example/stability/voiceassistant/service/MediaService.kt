package com.example.stability.voiceassistant.service

import com.example.stability.voiceassistant.data.model.VoiceCommand

class MediaService {
    private var isPlaying: Boolean = false
    private var currentSong: String = ""
    private var volume: Int = 50

    fun handleCommand(command: VoiceCommand): String {
        val action = command.parameters["action"]
        val content = command.parameters["content"]
        val value = command.parameters["value"]

        return when (action) {
            "play" -> {
                isPlaying = true
                if (!content.isNullOrEmpty()) {
                    currentSong = content
                    "好的，正在为您播放${currentSong}"
                } else {
                    currentSong = "默认音乐"
                    "好的，继续播放音乐"
                }
            }
            "pause" -> {
                isPlaying = false
                "已暂停播放"
            }
            "stop" -> {
                isPlaying = false
                currentSong = ""
                "已停止播放"
            }
            "next" -> {
                currentSong = "下一首歌曲"
                "正在播放下一首"
            }
            "previous" -> {
                currentSong = "上一首歌曲"
                "正在播放上一首"
            }
            "volume" -> {
                when (value) {
                    "up" -> {
                        volume = minOf(volume + 10, 100)
                        "音量已调高，当前音量${volume}%"
                    }
                    "down" -> {
                        volume = maxOf(volume - 10, 0)
                        "音量已调低，当前音量${volume}%"
                    }
                    else -> {
                        value?.toIntOrNull()?.let {
                            volume = it.coerceIn(0, 100)
                            "音量已设置为${volume}%"
                        } ?: "请说出具体的音量数值"
                    }
                }
            }
            else -> {
                if (isPlaying) {
                    "当前正在播放${currentSong}"
                } else {
                    "当前没有播放音乐"
                }
            }
        }
    }

    fun isPlaying(): Boolean = isPlaying
    fun getCurrentSong(): String = currentSong
    fun getVolume(): Int = volume
}

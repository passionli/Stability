package com.example.stability.voiceassistant.manager

import com.example.stability.voiceassistant.data.model.IntentType
import com.example.stability.voiceassistant.data.model.VoiceCommand

class IntentParser {
    fun parse(text: String): VoiceCommand {
        val normalizedText = text.trim()
        val intentType = determineIntentType(normalizedText)
        val parameters = extractParameters(normalizedText, intentType)
        return VoiceCommand(text, intentType, parameters)
    }

    private fun determineIntentType(text: String): IntentType {
        return when {
            text.contains("导航") || text.contains("目的地") || text.contains("路线") -> IntentType.NAVIGATION
            text.contains("播放") || text.contains("音乐") || text.contains("歌曲") || 
            text.contains("音量") || text.contains("切歌") || text.contains("暂停") || 
            text.contains("停止") -> IntentType.MEDIA
            text.contains("空调") || text.contains("温度") || text.contains("制冷") || 
            text.contains("制热") || text.contains("风量") || text.contains("模式") -> IntentType.AIR_CONDITION
            text.contains("车窗") || text.contains("窗户") || text.contains("玻璃") -> IntentType.WINDOW
            text.contains("你好") && (text.contains("小迪") || text.contains("比亚迪")) -> IntentType.WAKE_UP
            else -> IntentType.UNKNOWN
        }
    }

    private fun extractParameters(text: String, intentType: IntentType): Map<String, String> {
        val parameters = mutableMapOf<String, String>()
        
        when (intentType) {
            IntentType.NAVIGATION -> {
                extractNavigationParameters(text, parameters)
            }
            IntentType.MEDIA -> {
                extractMediaParameters(text, parameters)
            }
            IntentType.AIR_CONDITION -> {
                extractAirConditionParameters(text, parameters)
            }
            IntentType.WINDOW -> {
                extractWindowParameters(text, parameters)
            }
            else -> {}
        }
        
        return parameters
    }

    private fun extractNavigationParameters(text: String, parameters: MutableMap<String, String>) {
        val destination = extractDestination(text)
        if (destination.isNotEmpty()) {
            parameters["destination"] = destination
        }
        
        if (text.contains("高速") || text.contains("最快")) {
            parameters["route_type"] = "fastest"
        } else if (text.contains("最短")) {
            parameters["route_type"] = "shortest"
        } else if (text.contains("躲避")) {
            parameters["route_type"] = "avoid_toll"
        }
    }

    private fun extractDestination(text: String): String {
        val patterns = listOf(
            "导航到(.+)",
            "去(.+)",
            "目的地(.+)",
            "到(.+)去"
        )
        
        for (pattern in patterns) {
            val matchResult = Regex(pattern).find(text)
            if (matchResult != null) {
                return matchResult.groupValues[1].trim()
            }
        }
        
        return text.replace("导航", "").replace("去", "").replace("到", "").trim()
    }

    private fun extractMediaParameters(text: String, parameters: MutableMap<String, String>) {
        when {
            text.contains("暂停") -> parameters["action"] = "pause"
            text.contains("停止") -> parameters["action"] = "stop"
            text.contains("播放") -> {
                parameters["action"] = "play"
                val songName = text.replace("播放", "").replace("暂停", "").trim()
                if (songName.isNotEmpty()) {
                    parameters["content"] = songName
                }
            }
            text.contains("切歌") || text.contains("下一首") -> parameters["action"] = "next"
            text.contains("上一首") -> parameters["action"] = "previous"
            text.contains("音量") -> {
                parameters["action"] = "volume"
                when {
                    text.contains("调高") || text.contains("增大") -> parameters["value"] = "up"
                    text.contains("调低") || text.contains("减小") -> parameters["value"] = "down"
                    else -> {
                        val numMatch = Regex("音量(\\d+)").find(text)
                        numMatch?.let { parameters["value"] = it.groupValues[1] }
                    }
                }
            }
        }
    }

    private fun extractAirConditionParameters(text: String, parameters: MutableMap<String, String>) {
        when {
            text.contains("打开") || text.contains("开启") -> parameters["action"] = "on"
            text.contains("关闭") || text.contains("关掉") -> parameters["action"] = "off"
            text.contains("温度") -> {
                parameters["target"] = "temperature"
                when {
                    text.contains("调高") || text.contains("升高") -> parameters["value"] = "up"
                    text.contains("调低") || text.contains("降低") -> parameters["value"] = "down"
                    else -> {
                        val numMatch = Regex("(\\d+)度").find(text)
                        numMatch?.let { parameters["value"] = it.groupValues[1] }
                    }
                }
            }
            text.contains("风量") || text.contains("风速") -> {
                parameters["target"] = "fan_speed"
                when {
                    text.contains("调高") || text.contains("增大") -> parameters["value"] = "up"
                    text.contains("调低") || text.contains("减小") -> parameters["value"] = "down"
                }
            }
            text.contains("模式") -> {
                parameters["target"] = "mode"
                when {
                    text.contains("制冷") || text.contains("冷风") -> parameters["value"] = "cool"
                    text.contains("制热") || text.contains("暖风") -> parameters["value"] = "heat"
                    text.contains("除湿") -> parameters["value"] = "dehumidify"
                    text.contains("自动") -> parameters["value"] = "auto"
                }
            }
        }
    }

    private fun extractWindowParameters(text: String, parameters: MutableMap<String, String>) {
        when {
            text.contains("打开") -> {
                parameters["action"] = "open"
                determineWindowPosition(text, parameters)
            }
            text.contains("关闭") -> {
                parameters["action"] = "close"
                determineWindowPosition(text, parameters)
            }
            text.contains("降下") || text.contains("下降") -> {
                parameters["action"] = "open"
                determineWindowPosition(text, parameters)
            }
            text.contains("升起") || text.contains("上升") -> {
                parameters["action"] = "close"
                determineWindowPosition(text, parameters)
            }
        }
    }

    private fun determineWindowPosition(text: String, parameters: MutableMap<String, String>) {
        when {
            text.contains("主驾") || text.contains("司机") -> parameters["position"] = "driver"
            text.contains("副驾") -> parameters["position"] = "passenger"
            text.contains("左后") -> parameters["position"] = "rear_left"
            text.contains("右后") -> parameters["position"] = "rear_right"
            else -> parameters["position"] = "all"
        }
    }
}

package com.example.stability.voiceassistant.service

import com.example.stability.voiceassistant.data.model.VoiceCommand

class AirConditionService {
    private var isOn: Boolean = false
    private var temperature: Int = 24
    private var fanSpeed: Int = 3
    private var mode: String = "auto"

    fun handleCommand(command: VoiceCommand): String {
        val action = command.parameters["action"]
        val target = command.parameters["target"]
        val value = command.parameters["value"]

        if (action == "on") {
            isOn = true
            return "空调已开启"
        }

        if (action == "off") {
            isOn = false
            return "空调已关闭"
        }

        if (!isOn) {
            return "空调当前处于关闭状态，请先打开空调"
        }

        return when (target) {
            "temperature" -> handleTemperature(value)
            "fan_speed" -> handleFanSpeed(value)
            "mode" -> handleMode(value)
            else -> {
                "当前空调温度${temperature}度，风量${fanSpeed}档，模式${getModeName(mode)}"
            }
        }
    }

    private fun handleTemperature(value: String?): String {
        return when (value) {
            "up" -> {
                temperature = minOf(temperature + 1, 32)
                "温度已调高到${temperature}度"
            }
            "down" -> {
                temperature = maxOf(temperature - 1, 16)
                "温度已调低到${temperature}度"
            }
            else -> {
                value?.toIntOrNull()?.let {
                    if (it in 16..32) {
                        temperature = it
                        "温度已设置为${temperature}度"
                    } else {
                        "温度范围应在16到32度之间"
                    }
                } ?: "请说出具体的温度数值"
            }
        }
    }

    private fun handleFanSpeed(value: String?): String {
        return when (value) {
            "up" -> {
                fanSpeed = minOf(fanSpeed + 1, 5)
                "风量已调高到${fanSpeed}档"
            }
            "down" -> {
                fanSpeed = maxOf(fanSpeed - 1, 1)
                "风量已调低到${fanSpeed}档"
            }
            else -> "请说调高或调低风量"
        }
    }

    private fun handleMode(value: String?): String {
        return when (value) {
            "cool" -> {
                mode = "cool"
                "已切换到制冷模式"
            }
            "heat" -> {
                mode = "heat"
                "已切换到制热模式"
            }
            "dehumidify" -> {
                mode = "dehumidify"
                "已切换到除湿模式"
            }
            "auto" -> {
                mode = "auto"
                "已切换到自动模式"
            }
            else -> "当前模式为${getModeName(mode)}"
        }
    }

    private fun getModeName(mode: String): String {
        return when (mode) {
            "cool" -> "制冷"
            "heat" -> "制热"
            "dehumidify" -> "除湿"
            "auto" -> "自动"
            else -> "自动"
        }
    }

    fun isOn(): Boolean = isOn
    fun getTemperature(): Int = temperature
    fun getFanSpeed(): Int = fanSpeed
    fun getMode(): String = mode
}

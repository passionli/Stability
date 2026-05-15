package com.example.stability.voiceassistant.service

import com.example.stability.voiceassistant.data.model.VoiceCommand

class WindowService {
    private val windowStates = mutableMapOf<String, Boolean>().apply {
        put("driver", false)
        put("passenger", false)
        put("rear_left", false)
        put("rear_right", false)
    }

    fun handleCommand(command: VoiceCommand): String {
        val action = command.parameters["action"]
        val position = command.parameters["position"] ?: "all"

        return when (action) {
            "open" -> handleOpen(position)
            "close" -> handleClose(position)
            else -> "请说打开或关闭车窗"
        }
    }

    private fun handleOpen(position: String): String {
        val windowsToOpen = if (position == "all") {
            windowStates.keys.toList()
        } else {
            listOf(position)
        }

        windowsToOpen.forEach { windowStates[it] = true }
        return "已打开${getPositionName(position)}车窗"
    }

    private fun handleClose(position: String): String {
        val windowsToClose = if (position == "all") {
            windowStates.keys.toList()
        } else {
            listOf(position)
        }

        windowsToClose.forEach { windowStates[it] = false }
        return "已关闭${getPositionName(position)}车窗"
    }

    private fun getPositionName(position: String): String {
        return when (position) {
            "driver" -> "主驾"
            "passenger" -> "副驾"
            "rear_left" -> "左后"
            "rear_right" -> "右后"
            "all" -> "所有"
            else -> ""
        }
    }

    fun getWindowState(position: String): Boolean {
        return windowStates[position] ?: false
    }
}

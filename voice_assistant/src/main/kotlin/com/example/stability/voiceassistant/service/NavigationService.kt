package com.example.stability.voiceassistant.service

import com.example.stability.voiceassistant.data.model.VoiceCommand

class NavigationService {
    private var currentDestination: String = ""
    private var isNavigating: Boolean = false
    private var routeType: String = "fastest"

    fun handleCommand(command: VoiceCommand): String {
        val action = command.parameters["action"]
        val destination = command.parameters["destination"]
        val route = command.parameters["route_type"]

        return when {
            destination.isNullOrEmpty().not() -> {
                currentDestination = destination!!
                route?.let { routeType = it }
                isNavigating = true
                "好的，正在为您导航到${currentDestination}，选择${getRouteTypeName(routeType)}路线"
            }
            action == "cancel" -> {
                isNavigating = false
                currentDestination = ""
                "已取消导航"
            }
            action == "pause" -> {
                "已暂停导航"
            }
            action == "resume" -> {
                "已恢复导航"
            }
            else -> {
                if (isNavigating) {
                    "当前正在导航到${currentDestination}"
                } else {
                    "请问您要导航到哪里？"
                }
            }
        }
    }

    private fun getRouteTypeName(type: String): String {
        return when (type) {
            "fastest" -> "最快"
            "shortest" -> "最短"
            "avoid_toll" -> "躲避收费"
            else -> "最快"
        }
    }

    fun getCurrentDestination(): String = currentDestination
    fun isNavigating(): Boolean = isNavigating
}

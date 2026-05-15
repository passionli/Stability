package com.example.stability.voiceassistant

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.rememberNavController
import com.example.stability.voiceassistant.ui.VoiceAssistantScreen

class VoiceAssistantActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()
            VoiceAssistantScreen(navController)
        }
    }
}

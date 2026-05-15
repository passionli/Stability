package com.example.stability.voiceassistant.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.stability.voiceassistant.data.model.DialogMessage
import com.example.stability.voiceassistant.data.model.VoiceAssistantState
import com.example.stability.voiceassistant.viewmodel.VoiceAssistantViewModel

@Composable
fun VoiceAssistantScreen(navController: NavController) {
    val viewModel: VoiceAssistantViewModel = viewModel()
    val state = viewModel.state.value ?: VoiceAssistantState()

    var inputText by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.startWakeUpListening()
    }

    Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFF1a1a2e)) {
        Column(modifier = Modifier.fillMaxSize()) {
            Header(state.currentStatus)

            DialogHistory(state.dialogHistory)

            InputArea(
                inputText = inputText,
                onInputChange = { inputText = it },
                onSend = {
                    if (it.isNotBlank()) {
                        viewModel.processInput(it)
                        inputText = ""
                    }
                },
                isAwake = state.isAwake,
                isListening = state.isListening
            )

            if (state.isAwake) {
                ActionButtons(
                    onSleep = { viewModel.goToSleep() },
                    isSpeaking = state.isSpeaking
                )
            }
        }
    }
}

@Composable
fun Header(status: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF16213e))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "小迪",
                style = MaterialTheme.typography.headlineMedium,
                color = Color(0xFF00d9ff)
            )
            Text(
                text = status,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
fun DialogHistory(messages: List<DialogMessage>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .padding(16.dp),
        verticalArrangement = Arrangement.Bottom
    ) {
        items(messages) { message ->
            MessageBubble(message)
        }
    }
}

@Composable
fun MessageBubble(message: DialogMessage) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {
        Box(
            modifier = Modifier
                .padding(8.dp)
                .background(
                    color = if (message.isUser) Color(0xFF00d9ff) else Color(0xFF3f3f5a),
                    shape = if (message.isUser) {
                        RoundedCornerShape(16.dp, 16.dp, 0.dp, 16.dp)
                    } else {
                        RoundedCornerShape(16.dp, 16.dp, 16.dp, 0.dp)
                    }
                )
                .padding(12.dp)
        ) {
            Text(
                text = message.text,
                color = if (message.isUser) Color.Black else Color.White,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun InputArea(
    inputText: String,
    onInputChange: (String) -> Unit,
    onSend: (String) -> Unit,
    isAwake: Boolean,
    isListening: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF16213e))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = onInputChange,
                placeholder = { Text(text = if (isAwake) "请输入指令..." else "说\"你好，小迪\"唤醒我") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { onSend(inputText) }),
                enabled = isAwake
            )

            Button(
                onClick = { onSend(inputText) },
                enabled = isAwake && inputText.isNotBlank(),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00d9ff)),
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "发送",
                    tint = Color.Black
                )
            }

            WakeUpButton(isAwake = isAwake, isListening = isListening)
        }
    }
}

@Composable
fun WakeUpButton(isAwake: Boolean, isListening: Boolean) {
    Button(
        onClick = {},
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isAwake) {
                if (isListening) Color(0xFF00ff88) else Color(0xFF00d9ff)
            } else {
                Color(0xFF3f3f5a)
            }
        ),
        modifier = Modifier.size(48.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Mic,
            contentDescription = "麦克风",
            tint = Color.Black
        )
    }
}

@Composable
fun ActionButtons(onSleep: () -> Unit, isSpeaking: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0f0f1a))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = onSleep,
                enabled = !isSpeaking,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFff6b6b)),
                modifier = Modifier.padding(8.dp)
            ) {
                Text(text = "休眠", color = Color.White)
            }
        }
    }
}

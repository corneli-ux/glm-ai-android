package com.glm.aiapp.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.glm.aiapp.domain.model.ChatModel
import com.glm.aiapp.domain.model.ThinkingMode
import com.glm.aiapp.ui.theme.SettingsViewModel
import com.glm.aiapp.ui.screens.login.LoginViewModel

@Composable
fun SettingsScreen(vm: SettingsViewModel = hiltViewModel(), loginVm: LoginViewModel = hiltViewModel()) {
    val settings by vm.settings.collectAsStateWithLifecycle()
    val scroll = rememberScrollState()
    val s = settings ?: return

    Column(
        Modifier.fillMaxSize().background(Color.Black).verticalScroll(scroll).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Account
        SectionCard("Account") {
            Surface(color = Color(0xFF111111), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Person, contentDescription = null, tint = Color.White, modifier = Modifier.size(32.dp))
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(s.userName.ifBlank { "Signed in" }, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                        Text(s.userEmail, color = Color(0xFF666666), fontSize = 12.sp)
                    }
                }
            }
            TextButton(onClick = { loginVm.logout() }) {
                Icon(Icons.Filled.Logout, contentDescription = null, tint = Color(0xFFFF4444), modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Sign out", color = Color(0xFFFF4444), fontSize = 13.sp)
            }
        }

        // Model
        SectionCard("Model") {
            ChatModel.entries.forEach { m ->
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    RadioButton(selected = s.chatParams.model == m, onClick = { vm.updateChatParams(s.chatParams.copy(model = m)) }, colors = RadioButtonDefaults.colors(selectedColor = Color.White, unselectedColor = Color(0xFF444444)))
                    Column {
                        Text(m.label, color = Color.White, fontSize = 14.sp)
                        Text(m.description, color = Color(0xFF555555), fontSize = 11.sp)
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Text("Thinking mode", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            ThinkingMode.entries.forEach { t ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = s.chatParams.thinking == t, onClick = { vm.updateChatParams(s.chatParams.copy(thinking = t)) }, colors = RadioButtonDefaults.colors(selectedColor = Color.White, unselectedColor = Color(0xFF444444)))
                    Text(t.label, color = Color.White, fontSize = 14.sp)
                }
            }
        }

        // Generation
        SectionCard("Generation") {
            Text("Temperature: ${"%.1f".format(s.chatParams.temperature)}", color = Color.White, fontSize = 13.sp)
            Slider(value = s.chatParams.temperature, onValueChange = { vm.updateChatParams(s.chatParams.copy(temperature = it)) }, valueRange = 0f..2f, colors = SliderDefaults.colors(thumbColor = Color.White, activeTrackColor = Color.White))
            Text("Max tokens: ${s.chatParams.maxTokens}", color = Color.White, fontSize = 13.sp)
            Slider(value = s.chatParams.maxTokens.toFloat(), onValueChange = { vm.updateChatParams(s.chatParams.copy(maxTokens = it.toInt())) }, valueRange = 256f..32768f, colors = SliderDefaults.colors(thumbColor = Color.White, activeTrackColor = Color.White))
        }

        // About
        SectionCard("About") {
            Text("Pullarao 1", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Text("Version 1.0.0", color = Color(0xFF555555), fontSize = 12.sp)
            Text("Powered by GLM-5.2 open source", color = Color(0xFF555555), fontSize = 11.sp)
        }
    }
}

@Composable
private fun SectionCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Surface(color = Color(0xFF0A0A0A), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text(title, color = Color(0xFF888888), fontSize = 12.sp, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}

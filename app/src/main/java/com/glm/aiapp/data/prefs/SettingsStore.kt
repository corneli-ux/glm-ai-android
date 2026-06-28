package com.glm.aiapp.data.prefs

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.glm.aiapp.domain.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("glm_settings")

class SettingsStore(private val context: Context) {

    private object Keys {
        val API_KEY = stringPreferencesKey("api_key")
        val BASE_URL = stringPreferencesKey("base_url")
        val MODEL = stringPreferencesKey("model")
        val TEMPERATURE = floatPreferencesKey("temperature")
        val MAX_TOKENS = intPreferencesKey("max_tokens")
        val TOP_P = floatPreferencesKey("top_p")
        val THINKING = stringPreferencesKey("thinking")
        val STREAMING = booleanPreferencesKey("streaming")
        val SYSTEM_PROMPT = stringPreferencesKey("system_prompt")
        val THEME = stringPreferencesKey("theme")
    }

    val settings: Flow<AppSettings> = context.dataStore.data.map { p ->
        AppSettings(
            apiKey = p[Keys.API_KEY] ?: "",
            baseUrl = p[Keys.BASE_URL] ?: DEFAULT_BASE_URL,
            chatParams = ChatParams(
                model = ChatModel.fromId(p[Keys.MODEL]),
                temperature = p[Keys.TEMPERATURE] ?: 0.7f,
                maxTokens = p[Keys.MAX_TOKENS] ?: 4096,
                topP = p[Keys.TOP_P] ?: 0.9f,
                thinking = ThinkingMode.entries.firstOrNull { it.id == p[Keys.THINKING] } ?: ThinkingMode.DISABLED,
                systemPrompt = p[Keys.SYSTEM_PROMPT] ?: "",
                streaming = p[Keys.STREAMING] ?: true
            ),
            themeMode = ThemeMode.entries.firstOrNull { it.name.equals(p[Keys.THEME], true) } ?: ThemeMode.SYSTEM
        )
    }

    suspend fun updateApiKey(key: String) = context.dataStore.edit { it[Keys.API_KEY] = key }
    suspend fun updateBaseUrl(url: String) = context.dataStore.edit { it[Keys.BASE_URL] = url }
    suspend fun updateChatParams(params: ChatParams) = context.dataStore.edit {
        it[Keys.MODEL] = params.model.id
        it[Keys.TEMPERATURE] = params.temperature
        it[Keys.MAX_TOKENS] = params.maxTokens
        it[Keys.TOP_P] = params.topP
        it[Keys.THINKING] = params.thinking.id
        it[Keys.STREAMING] = params.streaming
        it[Keys.SYSTEM_PROMPT] = params.systemPrompt
    }
    suspend fun updateThemeMode(mode: ThemeMode) = context.dataStore.edit { it[Keys.THEME] = mode.name }
}

package com.glm.aiapp.data.repository

import com.glm.aiapp.data.prefs.SettingsStore
import com.glm.aiapp.domain.model.AppSettings
import com.glm.aiapp.domain.model.ChatParams
import com.glm.aiapp.domain.model.ThemeMode
import com.glm.aiapp.domain.repository.SettingsRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    private val store: SettingsStore
) : SettingsRepository {

    override val settings = store.settings

    override suspend fun updateApiKey(key: String) = store.updateApiKey(key)
    override suspend fun updateBaseUrl(url: String) = store.updateBaseUrl(url)
    override suspend fun updateChatParams(params: ChatParams) = store.updateChatParams(params)
    override suspend fun updateThemeMode(mode: ThemeMode) = store.updateThemeMode(mode)
}

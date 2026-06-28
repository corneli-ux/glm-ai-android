package com.glm.aiapp.data.repository

import com.glm.aiapp.data.api.GlmApi
import com.glm.aiapp.data.api.StreamingChatClient
import com.glm.aiapp.data.db.AppDatabase
import com.glm.aiapp.data.db.ConversationEntity
import com.glm.aiapp.data.db.MessageEntity
import com.glm.aiapp.data.dto.*
import com.glm.aiapp.domain.model.*
import com.glm.aiapp.domain.repository.ChatRepository
import com.glm.aiapp.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepositoryImpl @Inject constructor(
    private val db: AppDatabase,
    private val api: GlmApi,
    private val streamClient: StreamingChatClient,
    private val settingsRepo: SettingsRepository,
    private val json: Json
) : ChatRepository {

    override fun observeConversations(): Flow<List<Conversation>> =
        db.conversationDao().observeAll().map { list -> list.map { it.toDomain() } }

    override fun observeConversation(id: String): Flow<Conversation?> =
        combine(
            db.conversationDao().observeById(id),
            db.messageDao().observeByConversation(id)
        ) { conv, msgs ->
            conv?.toDomain()?.copy(messages = msgs.map { it.toDomain() })
        }

    override suspend fun createConversation(title: String, systemPrompt: String, model: String): Conversation {
        val now = System.currentTimeMillis()
        val conv = ConversationEntity(UUID.randomUUID().toString(), title, systemPrompt, model, false, now, now)
        db.conversationDao().upsert(conv)
        return conv.toDomain()
    }

    override suspend fun deleteConversation(id: String) = db.conversationDao().delete(id)

    override suspend fun appendMessage(conversationId: String, message: Message) {
        db.messageDao().upsert(message.toEntity())
        db.conversationDao().touch(conversationId, System.currentTimeMillis())
    }

    override suspend fun updateMessage(message: Message) = db.messageDao().upsert(message.toEntity())

    override suspend fun streamChat(
        conversationId: String,
        params: ChatParams,
        onToken: (String) -> Unit,
        onThinking: (String) -> Unit
    ): Message {
        val settings = settingsRepo.settings.first()
        val conv = db.conversationDao().getById(conversationId)
            ?: error("Conversation $conversationId not found")
        val history = db.messageDao().observeByConversation(conversationId).first()

        val messages: MutableList<ChatMessage> = mutableListOf()
        if (conv.systemPrompt.isNotBlank()) {
            messages += ChatMessage("system", JsonPrimitive(conv.systemPrompt))
        }
        history.forEach { m ->
            messages += ChatMessage(m.role.lowercase(), JsonPrimitive(m.content))
        }

        val baseUrl = settings.baseUrl.trimEnd('/')
        val url = "$baseUrl/chat/completions"
        val request = ChatCompletionRequest(
            model = params.model.id,
            messages = messages,
            temperature = params.temperature,
            maxTokens = params.maxTokens,
            topP = params.topP,
            stream = true,
            thinking = ThinkingConfig(params.thinking.id)
        )
        val payload = json.encodeToString(request)

        val contentBuilder = StringBuilder()
        val thinkingBuilder = StringBuilder()
        var usage: Usage? = null

        if (params.streaming) {
            streamClient.stream(url, settings.apiKey, payload).collect { chunk ->
                chunk.choices.forEach { choice ->
                    choice.delta?.content?.let { onToken(it); contentBuilder.append(it) }
                    choice.delta?.thinking?.let { onThinking(it); thinkingBuilder.append(it) }
                }
                chunk.usage?.let { usage = it }
            }
        } else {
            val response = api.chatCompletion(request.copy(stream = false))
            response.choices.firstOrNull()?.message?.content?.let { raw ->
                val text = runCatching { json.decodeFromString<JsonElement>(raw.toString()) }
                    .map { element -> element.jsonPrimitive.content }
                    .getOrElse { raw.toString() }
                contentBuilder.append(text)
                onToken(text)
            }
            usage = response.usage
        }

        val msg = Message(
            id = UUID.randomUUID().toString(),
            conversationId = conversationId,
            role = Role.ASSISTANT,
            content = contentBuilder.toString(),
            thinking = thinkingBuilder.toString().ifBlank { null },
            tokens = usage?.totalTokens,
            createdAt = System.currentTimeMillis()
        )
        appendMessage(conversationId, msg)
        return msg
    }

    // ---- Mappers ----

    private fun ConversationEntity.toDomain() = Conversation(
        id = id,
        title = title,
        systemPrompt = systemPrompt,
        model = model,
        thinkingEnabled = thinkingEnabled,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    private fun MessageEntity.toDomain() = Message(
        id = id,
        conversationId = conversationId,
        role = Role.valueOf(role.uppercase()),
        content = content,
        thinking = thinking,
        tokens = tokens,
        createdAt = createdAt
    )

    private fun Message.toEntity() = MessageEntity(
        id = id,
        conversationId = conversationId,
        role = role.name.lowercase(),
        content = content,
        thinking = thinking,
        tokens = tokens,
        createdAt = createdAt
    )
}

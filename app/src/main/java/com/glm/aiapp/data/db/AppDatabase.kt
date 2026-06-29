package com.glm.aiapp.data.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    version = 1,
    exportSchema = true,
    entities = [
        ConversationEntity::class,
        MessageEntity::class,
        AttachmentEntity::class,
        GeneratedImageEntity::class,
        GeneratedVideoEntity::class,
        VoiceClipEntity::class,
        TranscriptionEntity::class,
        FineTuneDatasetEntity::class,
        FineTuneJobEntity::class
    ]
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun conversationDao(): ConversationDao
    abstract fun messageDao(): MessageDao
    abstract fun generatedImageDao(): GeneratedImageDao
    abstract fun generatedVideoDao(): GeneratedVideoDao
    abstract fun voiceClipDao(): VoiceClipDao
    abstract fun transcriptionDao(): TranscriptionDao
    abstract fun fineTuneDatasetDao(): FineTuneDatasetDao
    abstract fun fineTuneJobDao(): FineTuneJobDao

    companion object {
        const val NAME = "glm_ai.db"
    }
}

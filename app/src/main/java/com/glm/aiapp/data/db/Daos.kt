package com.glm.aiapp.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ConversationDao {
    @Query("SELECT * FROM conversations ORDER BY updatedAt DESC")
    fun observeAll(): Flow<List<ConversationEntity>>

    @Query("SELECT * FROM conversations WHERE id = :id")
    fun observeById(id: String): Flow<ConversationEntity?>

    @Query("SELECT * FROM conversations WHERE id = :id")
    suspend fun getById(id: String): ConversationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: ConversationEntity)

    @Query("DELETE FROM conversations WHERE id = :id")
    suspend fun delete(id: String)

    @Query("UPDATE conversations SET updatedAt = :ts WHERE id = :id")
    suspend fun touch(id: String, ts: Long)
}

@Dao
interface MessageDao {
    @Query("SELECT * FROM messages WHERE conversationId = :cid ORDER BY createdAt ASC")
    fun observeByConversation(cid: String): Flow<List<MessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: MessageEntity)

    @Query("DELETE FROM messages WHERE conversationId = :cid")
    suspend fun deleteByConversation(cid: String)
}

@Dao
interface GeneratedImageDao {
    @Query("SELECT * FROM generated_images ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<GeneratedImageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: GeneratedImageEntity)

    @Query("DELETE FROM generated_images WHERE id = :id")
    suspend fun delete(id: String)
}

@Dao
interface GeneratedVideoDao {
    @Query("SELECT * FROM generated_videos ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<GeneratedVideoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: GeneratedVideoEntity)

    @Query("DELETE FROM generated_videos WHERE id = :id")
    suspend fun delete(id: String)
}

@Dao
interface VoiceClipDao {
    @Query("SELECT * FROM voice_clips ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<VoiceClipEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: VoiceClipEntity)
}

@Dao
interface TranscriptionDao {
    @Query("SELECT * FROM transcriptions ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<TranscriptionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: TranscriptionEntity)
}

@Dao
interface FineTuneDatasetDao {
    @Query("SELECT * FROM finetune_datasets ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<FineTuneDatasetEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: FineTuneDatasetEntity)
}

@Dao
interface FineTuneJobDao {
    @Query("SELECT * FROM finetune_jobs ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<FineTuneJobEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: FineTuneJobEntity)

    @Query("SELECT * FROM finetune_jobs WHERE id = :id")
    suspend fun getById(id: String): FineTuneJobEntity?
}

package com.glm.aiapp.data.repository

import com.glm.aiapp.data.db.AppDatabase
import com.glm.aiapp.data.db.FineTuneDatasetEntity
import com.glm.aiapp.data.db.FineTuneJobEntity
import com.glm.aiapp.domain.model.FineTuneDataset
import com.glm.aiapp.domain.model.FineTuneJob
import com.glm.aiapp.domain.model.FineTuneStatus
import com.glm.aiapp.domain.repository.FineTuneRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Stub repository — persists jobs/datasets locally so the UI is fully wired.
 * Replace `createJob` / `refreshJobStatus` with real API calls when the
 * fine-tune endpoint is available on your GLM deployment.
 */
@Singleton
class FineTuneRepositoryImpl @Inject constructor(
    private val db: AppDatabase
) : FineTuneRepository {

    override fun observeDatasets() =
        db.fineTuneDatasetDao().observeAll().map { list -> list.map { it.toDomain() } }

    override fun observeJobs() =
        db.fineTuneJobDao().observeAll().map { list -> list.map { it.toDomain() } }

    override suspend fun createJob(
        name: String,
        baseModel: String,
        datasetId: String,
        epochs: Int,
        learningRate: Float
    ): FineTuneJob {
        val dataset = db.fineTuneDatasetDao().observeAll().first()
            .firstOrNull { it.id == datasetId }
            ?: error("Dataset $datasetId not found")
        val entity = FineTuneJobEntity(
            id = UUID.randomUUID().toString(),
            name = name,
            status = FineTuneStatus.QUEUED.name,
            baseModel = baseModel,
            datasetName = dataset.name,
            examples = dataset.examples,
            epochs = epochs,
            learningRate = learningRate,
            progress = 0f,
            loss = null,
            createdAt = System.currentTimeMillis()
        )
        db.fineTuneJobDao().upsert(entity)
        return entity.toDomain()
    }

    override suspend fun refreshJobStatus(id: String): FineTuneJob {
        val current = db.fineTuneJobDao().getById(id) ?: error("Job $id not found")
        // Simulate progress — replace with real poll when API is available
        val newProgress = (current.progress + 0.15f).coerceAtMost(1f)
        val newStatus = when {
            newProgress >= 1f -> FineTuneStatus.COMPLETED
            current.status == FineTuneStatus.QUEUED.name -> FineTuneStatus.TRAINING
            else -> FineTuneStatus.valueOf(current.status)
        }
        val updated = current.copy(
            progress = newProgress,
            status = newStatus.name,
            loss = if (newStatus == FineTuneStatus.TRAINING) (0.42f - newProgress * 0.3f) else current.loss
        )
        db.fineTuneJobDao().upsert(updated)
        return updated.toDomain()
    }

    private fun FineTuneDatasetEntity.toDomain() = FineTuneDataset(
        id = id, name = name, description = description, examples = examples,
        sizeBytes = sizeBytes, format = format, createdAt = createdAt
    )

    private fun FineTuneJobEntity.toDomain() = FineTuneJob(
        id = id, name = name,
        status = FineTuneStatus.entries.firstOrNull { it.name == status } ?: FineTuneStatus.DRAFT,
        baseModel = baseModel, datasetName = datasetName, examples = examples,
        epochs = epochs, learningRate = learningRate, progress = progress,
        loss = loss, createdAt = createdAt
    )
}

package com.hoaxdetector.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import com.hoaxdetector.app.data.remote.model.AnalysisResult

class AnalysisResultConverter {
    private val json = Json { ignoreUnknownKeys = true }

    @TypeConverter
    fun fromAnalysisResult(value: AnalysisResult?): String? {
        return value?.let { json.encodeToString(it) }
    }

    @TypeConverter
    fun toAnalysisResult(value: String?): AnalysisResult? {
        return value?.let { json.decodeFromString(it) }
    }
}

@Entity(tableName = "check_history")
@TypeConverters(AnalysisResultConverter::class)
data class CheckHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: String, // "text" or "url"
    val inputText: String, // Original text or URL
    val articleTitle: String? = null,
    val status: String,
    val statusColor: String,
    val confidenceScore: Int,
    val ringkasan: String,
    val analysisResult: AnalysisResult,
    val timestamp: Long = System.currentTimeMillis()
)

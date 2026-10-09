package com.hoaxdetector.app.data.repository

import com.hoaxdetector.app.data.local.dao.CheckHistoryDao
import com.hoaxdetector.app.data.local.entity.CheckHistoryEntity
import com.hoaxdetector.app.data.remote.api.HoaxDetectorApi
import com.hoaxdetector.app.data.remote.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val message: String) : Result<Nothing>()
    object Loading : Result<Nothing>()
}

@Singleton
class FactCheckRepository @Inject constructor(
    private val api: HoaxDetectorApi,
    private val dao: CheckHistoryDao
) {

    fun getAllHistory(): Flow<List<CheckHistoryEntity>> = dao.getAllHistory()

    fun getTotalCount(): Flow<Int> = dao.getTotalCount()

    fun getHoaxCount(): Flow<Int> = dao.getHoaxCount()

    fun getValidCount(): Flow<Int> = dao.getValidCount()

    fun getCountByStatus(status: String): Flow<Int> = dao.getCountByStatus(status)

    suspend fun getHistoryById(id: Long): CheckHistoryEntity? = dao.getHistoryById(id)

    suspend fun deleteHistory(id: Long) = dao.deleteHistoryById(id)

    suspend fun deleteAllHistory() = dao.deleteAllHistory()

    suspend fun checkText(text: String): Result<FactCheckResponse> {
        return try {
            val response = api.checkText(TextCheckRequest(text))
            if (response.isSuccessful && response.body()?.success == true) {
                val body = response.body()!!
                body.analysis?.let { analysis ->
                    saveToHistory(
                        type = "text",
                        inputText = text.take(500),
                        articleTitle = null,
                        analysis = analysis
                    )
                }
                Result.Success(body)
            } else {
                // Fallback otomatis ke engine lokal di perangkat
                val localAnalysis = com.hoaxdetector.app.data.local.LocalFactCheckEngine.analyze(text, isUrl = false)
                saveToHistory(
                    type = "text",
                    inputText = text.take(500),
                    articleTitle = null,
                    analysis = localAnalysis
                )
                Result.Success(FactCheckResponse(success = true, type = "text", analysis = localAnalysis))
            }
        } catch (e: Exception) {
            // Jika laptop mati / server backend offline -> otomatis gunakan Engine On-Device!
            val localAnalysis = com.hoaxdetector.app.data.local.LocalFactCheckEngine.analyze(text, isUrl = false)
            saveToHistory(
                type = "text",
                inputText = text.take(500),
                articleTitle = null,
                analysis = localAnalysis
            )
            Result.Success(FactCheckResponse(success = true, type = "text", analysis = localAnalysis))
        }
    }

    suspend fun checkUrl(url: String): Result<FactCheckResponse> {
        return try {
            val response = api.checkUrl(UrlCheckRequest(url))
            if (response.isSuccessful && response.body()?.success == true) {
                val body = response.body()!!
                body.analysis?.let { analysis ->
                    saveToHistory(
                        type = "url",
                        inputText = url,
                        articleTitle = body.articleInfo?.title,
                        analysis = analysis
                    )
                }
                Result.Success(body)
            } else {
                val localAnalysis = com.hoaxdetector.app.data.local.LocalFactCheckEngine.analyze(url, isUrl = true)
                saveToHistory(
                    type = "url",
                    inputText = url,
                    articleTitle = url,
                    analysis = localAnalysis
                )
                Result.Success(FactCheckResponse(success = true, type = "url", analysis = localAnalysis, articleInfo = ArticleInfo(title = url, url = url)))
            }
        } catch (e: Exception) {
            val localAnalysis = com.hoaxdetector.app.data.local.LocalFactCheckEngine.analyze(url, isUrl = true)
            saveToHistory(
                type = "url",
                inputText = url,
                articleTitle = url,
                analysis = localAnalysis
            )
            Result.Success(FactCheckResponse(success = true, type = "url", analysis = localAnalysis, articleInfo = ArticleInfo(title = url, url = url)))
        }
    }

    suspend fun checkHealth(): Boolean {
        return try {
            val response = api.checkHealth()
            response.isSuccessful && response.body()?.success == true
        } catch (e: Exception) {
            false
        }
    }

    private suspend fun saveToHistory(
        type: String,
        inputText: String,
        articleTitle: String?,
        analysis: AnalysisResult
    ) {
        val entity = CheckHistoryEntity(
            type = type,
            inputText = inputText,
            articleTitle = articleTitle,
            status = analysis.status,
            statusColor = analysis.statusColor,
            confidenceScore = analysis.confidenceScore,
            ringkasan = analysis.ringkasan,
            analysisResult = analysis
        )
        dao.insertHistory(entity)
    }

    suspend fun seedInitialDataIfEmpty() {
        // App baru dimulai dalam keadaan bersih (0 riwayat)
    }

    private fun mapExceptionToMessage(e: Exception): String {
        return when {
            e.message?.contains("Unable to resolve host") == true ||
            e.message?.contains("Failed to connect") == true ->
                "Tidak dapat terhubung ke server. Pastikan backend sudah berjalan."
            e.message?.contains("timeout") == true ||
            e.message?.contains("SocketTimeoutException") == true ->
                "Koneksi timeout. Periksa jaringan internet."
            else -> e.message ?: "Terjadi kesalahan tidak diketahui"
        }
    }
}

package com.hoaxdetector.app.data.remote.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

// Request models
@Serializable
data class TextCheckRequest(
    val text: String
)

@Serializable
data class UrlCheckRequest(
    val url: String
)

// Response models
@Serializable
data class FactCheckResponse(
    val success: Boolean,
    val type: String? = null,
    val timestamp: String? = null,
    val analysis: AnalysisResult? = null,
    val articleInfo: ArticleInfo? = null,
    val error: String? = null
)

@Serializable
data class AnalysisResult(
    val status: String,
    val statusColor: String,
    val confidenceScore: Int,
    val ringkasan: String,
    val penjelasan: String,
    val bukti: List<BuktiItem> = emptyList(),
    val sumberReferensi: List<SumberReferensi> = emptyList(),
    val keterbatasan: String,
    val tipsMengenali: String
)

@Serializable
data class BuktiItem(
    val deskripsi: String,
    val mendukung: Boolean
)

@Serializable
data class SumberReferensi(
    val nama: String,
    val keterangan: String
)

@Serializable
data class ArticleInfo(
    val title: String? = null,
    val url: String,
    val contentLength: Int = 0
)

@Serializable
data class HealthResponse(
    val success: Boolean,
    val status: String,
    val service: String,
    val version: String,
    val geminiConfigured: Boolean
)

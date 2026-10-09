package com.hoaxdetector.app.data.local.mafindo

import com.hoaxdetector.app.data.remote.model.BuktiItem
import com.hoaxdetector.app.data.remote.model.SumberReferensi

/**
 * Representasi satu data verifikasi fakta dari basis data MAFINDO / TurnBackHoax.id.
 */
data class MafindoFactItem(
    val id: String,
    val category: String, // "POLITIK", "KESEHATAN", "PANGAN", "PENIPUAN", "BENCANA", "SOSIAL"
    val claimTitle: String,
    val keywords: List<String>,
    val verdict: String, // "BERTENTANGAN DENGAN BUKTI", "MENYESATKAN", "DIDUKUNG BUKTI"
    val statusColor: String, // "red", "yellow", "green"
    val baseConfidence: Int, // 90-99
    val factSummary: String,
    val fullExplanation: String,
    val evidenceList: List<BuktiItem>,
    val references: List<SumberReferensi>,
    val debunkUrl: String = "https://turnbackhoax.id"
)

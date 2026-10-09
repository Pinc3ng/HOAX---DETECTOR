package com.hoaxdetector.app.data.local.nlp

import com.hoaxdetector.app.data.local.mafindo.MafindoFactItem
import com.hoaxdetector.app.data.local.mafindo.MafindoKnowledgeBase

data class NlpMatchResult(
    val matchedItem: MafindoFactItem?,
    val matchScore: Float, // 0.0f - 1.0f
    val isFactualJournalism: Boolean,
    val isClickbait: Boolean
)

/**
 * Natural Language Processing (NLP) Engine On-Device untuk Bahasa Indonesia.
 * Melakukan tokenisasi, penghapusan stop words, kalkulasi kemiripan semantik Jaccard,
 * dan pencocokan berbasis pengetahuan MAFINDO.
 */
object IndonesianNlpEngine {

    // Indonesian Stopwords (kata sambung & partikel umum yang tidak bermakna diskriminatif)
    private val stopWords = setOf(
        "adalah", "yang", "di", "ke", "dari", "pada", "untuk", "ini", "itu",
        "dan", "atau", "sebagai", "dengan", "oleh", "bahwa", "dalam", "bisa",
        "dapat", "akan", "telah", "sudah", "hanya", "juga", "bagi", "karena",
        "secara", "tentang", "seperti", "jika", "kalau", "apakah", "kenapa",
        "mengapa", "bagaimana", "pun", "saja", "ada", "tidak", "bukan"
    )

    // Tokenizer dengan normalisasi
    fun tokenize(text: String): List<String> {
        val clean = text.lowercase()
            .replace(Regex("[^a-z0-9\\s-]"), " ")
            .split("\\s+".toRegex())
            .filter { it.isNotBlank() && it.length > 2 }
        return clean.filterNot { stopWords.contains(it) }
    }

    /**
     * Menghitung kemiripan semantik antara teks masukan dengan basis data MAFINDO.
     */
    fun findBestFactMatch(inputText: String): NlpMatchResult {
        val tokens = tokenize(inputText)
        val lowerText = inputText.lowercase().trim()

        if (tokens.isEmpty()) {
            return NlpMatchResult(null, 0f, isFactualJournalism = false, isClickbait = false)
        }

        var bestMatch: MafindoFactItem? = null
        var highestScore = 0f

        for (item in MafindoKnowledgeBase.items) {
            val itemKeywords = item.keywords.map { it.lowercase() }.toSet()

            // 1. Hitung jumlah token input yang cocok dengan keywords item
            val matchedTokenCount = tokens.count { token ->
                itemKeywords.any { kw -> kw.contains(token) || token.contains(kw) }
            }

            // 2. Hitung Jaccard Index
            val unionCount = (tokens.toSet() + itemKeywords).size
            val jaccard = if (unionCount > 0) matchedTokenCount.toFloat() / unionCount else 0f

            // 3. Keyword Overlap Ratio
            val keywordCoverage = if (itemKeywords.isNotEmpty()) {
                matchedTokenCount.toFloat() / itemKeywords.size
            } else 0f

            // 4. Bonus jika kata kunci spesifik unik (misal: "purbaya", "haarp", "microchip") ada di input
            var entityBonus = 0f
            for (kw in itemKeywords) {
                if (kw.length >= 5 && lowerText.contains(kw)) {
                    entityBonus += 0.25f
                }
            }

            val totalScore = (jaccard * 0.4f) + (keywordCoverage * 0.4f) + entityBonus

            if (totalScore > highestScore) {
                highestScore = totalScore
                bestMatch = item
            }
        }

        // Deteksi Jurnalistik Resmi & Otoritatif
        val isFactualJournalism = checkFactualJournalism(lowerText)

        // Deteksi Clickbait
        val isClickbait = checkClickbait(lowerText)

        return NlpMatchResult(
            matchedItem = if (highestScore >= 0.35f) bestMatch else null,
            matchScore = highestScore,
            isFactualJournalism = isFactualJournalism,
            isClickbait = isClickbait
        )
    }

    private fun checkFactualJournalism(lower: String): Boolean {
        val hasOfficial = lower.contains("presiden") || lower.contains("menteri") ||
                lower.contains("kementerian") || lower.contains("kemendikbud") || lower.contains("kemenkes") ||
                lower.contains("bmkg") || lower.contains("kpu") || lower.contains("polri") ||
                lower.contains("tni") || lower.contains("bpom") || lower.contains("brin") ||
                lower.contains("who") || lower.contains("unesco") || lower.contains("fifa") ||
                lower.contains("bank indonesia") || lower.contains("pertamina") || lower.contains("pemprov")

        val hasAction = lower.contains("resmi") || lower.contains("mengumumkan") ||
                lower.contains("meresmikan") || lower.contains("melantik") || lower.contains("menetapkan") ||
                lower.contains("meraih") || lower.contains("menjuarai") || lower.contains("menang") ||
                lower.contains("berdasarkan data") || lower.contains("hasil riset") ||
                lower.contains("penelitian") || lower.contains("studi")

        val hasConspiracy = lower.contains("anak angkat") || lower.contains("selingkuh") ||
                lower.contains("rahasia keluarga") || lower.contains("palsu") || lower.contains("dalang")

        return hasOfficial && hasAction && !hasConspiracy
    }

    private fun checkClickbait(lower: String): Boolean {
        return lower.contains("viral!") || lower.contains("rahasia terbongkar") ||
                lower.contains("gempar!") || lower.contains("jangan sampai telat") ||
                lower.contains("sebarkan sebelum dihapus") || lower.contains("dilarang dokter") ||
                lower.contains("mengejutkan publik") || lower.contains("bocor!") ||
                lower.contains("konspirasi elit") || lower.contains("semua orang tertipu")
    }
}

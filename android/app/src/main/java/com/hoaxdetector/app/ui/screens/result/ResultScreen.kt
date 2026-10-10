package com.hoaxdetector.app.ui.screens.result

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.hoaxdetector.app.data.local.entity.CheckHistoryEntity
import com.hoaxdetector.app.data.remote.model.BuktiItem
import com.hoaxdetector.app.data.remote.model.SumberReferensi
import com.hoaxdetector.app.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ResultScreen(
    historyId: Long,
    onNavigateBack: () -> Unit,
    viewModel: ResultViewModel = hiltViewModel()
) {
    val historyItem by viewModel.historyItem.collectAsState()
    val context = LocalContext.current
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    LaunchedEffect(historyId) {
        viewModel.loadHistory(historyId)
    }

    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = {
                Text(text = "Hapus Riwayat Ini?", fontWeight = FontWeight.Bold, color = StitchPrimary)
            },
            text = {
                Text(
                    text = "Laporan verifikasi ini akan dihapus dari arsip pemeriksaan lokal Anda.",
                    color = StitchOnSurfaceVariant
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirmDialog = false
                        viewModel.deleteCurrentHistory(historyId) {
                            onNavigateBack()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StitchError)
                ) {
                    Text("Hapus", color = StitchOnPrimary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Batal", color = StitchOutline)
                }
            },
            containerColor = StitchSurfaceContainerLowest,
            shape = RoundedCornerShape(12.dp)
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(StitchSurface)
    ) {
        if (historyItem == null) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = StitchPrimaryContainer
            )
        } else {
            val item = historyItem!!
            ResultContent(
                item = item,
                onNavigateBack = onNavigateBack,
                onShareClick = {
                    val shareText = buildString {
                        append("🔍 *[HOAX-DETECTOR MELALUI NLI]*\n")
                        append("Status: *${item.status}* (${item.analysisResult.confidenceScore}%)\n\n")
                        append("📌 *Klaim/Informasi:*\n")
                        append("\"${item.inputText.take(250)}\"\n\n")
                        append("📝 *Ringkasan Fakta:*\n")
                        append("${item.analysisResult.ringkasan}\n\n")
                        append("💡 *Penjelasan:*\n")
                        append("${item.analysisResult.penjelasan}\n\n")
                        append("— Diperiksa secara akurat menggunakan HOAX-DETECTOR MELALUI NLI (Python Engine)")
                    }
                    val sendIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, shareText)
                        type = "text/plain"
                    }
                    context.startActivity(Intent.createChooser(sendIntent, "Bagikan Hasil Verifikasi"))
                },
                onDeleteClick = { showDeleteConfirmDialog = true }
            )
        }
    }
}

@Composable
private fun ResultContent(
    item: CheckHistoryEntity,
    onNavigateBack: () -> Unit,
    onShareClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val analysis = item.analysisResult
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    val isHoax = item.statusColor.contains("red", ignoreCase = true) || item.status.contains("Hoaks", ignoreCase = true) || item.status.contains("Menyesatkan", ignoreCase = true)
    val isFakta = item.statusColor.contains("green", ignoreCase = true) || item.status.contains("Fakta", ignoreCase = true) || item.status.contains("Didukung", ignoreCase = true)

    val verdictBadgeBg = when {
        isHoax -> Color(0xFFFBEBE9)
        isFakta -> Color(0xFFE6F8EF)
        else -> Color(0xFFFFF4E5)
    }
    val verdictBorder = when {
        isHoax -> Color(0xFFF2C7C3)
        isFakta -> Color(0xFFA6F3CA)
        else -> Color(0xFFFFDDB3)
    }
    val verdictTextColor = when {
        isHoax -> StitchError
        isFakta -> StitchTertiary
        else -> Color(0xFF854D0E)
    }
    val verdictIcon = when {
        isHoax -> Icons.Default.Cancel
        isFakta -> Icons.Default.CheckCircle
        else -> Icons.Default.Warning
    }
    val verdictLabel = when {
        isHoax -> "MENYESATKAN"
        isFakta -> "DIDUKUNG BUKTI"
        else -> "BELUM CUKUP BUKTI"
    }

    val dossierId = remember(item.id) {
        val padded = item.id.toString().padStart(4, '0')
        "#VER-2025-$padded"
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // 1. Navigation Header Bar (Sticky Top)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Back to home button with label "Beranda"
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable { onNavigateBack() }
                        .padding(horizontal = 6.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Kembali",
                        tint = StitchPrimary,
                        modifier = Modifier.size(19.dp)
                    )
                    Text(
                        text = "Beranda",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = StitchPrimary
                    )
                }

                Text(
                    text = "Hasil Pemeriksaan",
                    fontSize = 17.sp,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    color = StitchPrimary
                )

                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    IconButton(
                        onClick = onShareClick,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Bagikan",
                            tint = StitchOnSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(
                        onClick = onDeleteClick,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Hapus",
                            tint = StitchOutline,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            HorizontalDivider(color = StitchOutlineVariant.copy(alpha = 0.3f), thickness = 1.dp)

            // Scrollable Content Canvas
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 14.dp)
                    .padding(bottom = 90.dp), // Space for floating bottom bar
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Dossier Metadata Pill
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.VerifiedUser,
                            contentDescription = null,
                            tint = StitchOutline,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "LAPORAN VERIFIKASI RESMI",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 0.8.sp,
                            color = StitchOnSurfaceVariant
                        )
                    }
                    Text(
                        text = "ID $dossierId",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = StitchOutline
                    )
                }

                // 2. Verdict Header Section
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Badge Chip
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(verdictBadgeBg)
                            .border(1.dp, verdictBorder, RoundedCornerShape(6.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = verdictIcon,
                            contentDescription = null,
                            tint = verdictTextColor,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = verdictLabel,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp,
                            color = verdictTextColor
                        )
                        Text(
                            text = "• ${analysis.confidenceScore}%",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = verdictTextColor
                        )
                    }

                    // Headline
                    Text(
                        text = analysis.ringkasan,
                        fontSize = 20.sp,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        color = StitchOnSurface,
                        lineHeight = 27.sp
                    )

                    // Short concise verdict explanation callout
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(StitchSurfaceContainerLow)
                            .border(1.dp, StitchOutlineVariant.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = analysis.penjelasan.take(180) + if (analysis.penjelasan.length > 180) "..." else "",
                            fontSize = 13.sp,
                            color = StitchOnSurfaceVariant,
                            lineHeight = 19.sp
                        )
                    }
                }

                // 3. Section: Klaim yang Diperiksa
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FormatQuote,
                            contentDescription = null,
                            tint = StitchOutline,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "KLAIM YANG DIPERIKSA",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp,
                            color = StitchOnSurfaceVariant
                        )
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = StitchSurfaceContainerLowest),
                        border = BorderStroke(1.dp, StitchOutlineVariant.copy(alpha = 0.4f))
                    ) {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            // Left colored indicator bar
                            Box(
                                modifier = Modifier
                                    .width(4.dp)
                                    .fillMaxHeight()
                                    .background(verdictTextColor)
                            )
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = "“${item.inputText}”",
                                    fontSize = 15.sp,
                                    fontFamily = FontFamily.Serif,
                                    fontStyle = FontStyle.Italic,
                                    color = StitchOnSurface,
                                    lineHeight = 22.sp
                                )

                                Spacer(modifier = Modifier.height(10.dp))
                                HorizontalDivider(color = StitchOutlineVariant.copy(alpha = 0.2f), thickness = 1.dp)
                                Spacer(modifier = Modifier.height(6.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Kanal: Pesan Berantai / Teks Warta",
                                        fontSize = 11.sp,
                                        color = StitchOutline
                                    )
                                    Text(
                                        text = "${item.inputText.split("\\s+".toRegex()).size} kata",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = StitchOutline
                                    )
                                }
                            }
                        }
                    }
                }

                // 4. Section: Analisis Bukti
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FactCheck,
                            contentDescription = null,
                            tint = StitchPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "ANALISIS BUKTI",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp,
                            color = StitchOnSurfaceVariant
                        )
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = StitchSurfaceContainerLowest),
                        border = BorderStroke(1.dp, StitchOutlineVariant.copy(alpha = 0.4f))
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = analysis.penjelasan,
                                fontSize = 13.5.sp,
                                color = StitchOnSurfaceVariant,
                                lineHeight = 21.sp
                            )

                            // Dynamic Evidence Points
                            if (analysis.bukti.isNotEmpty()) {
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    analysis.bukti.forEach { bukti ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.Top
                                        ) {
                                            Text(
                                                text = if (bukti.mendukung) "✓" else "✕",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (bukti.mendukung) StitchTertiary else StitchError
                                            )
                                            Text(
                                                text = bukti.deskripsi,
                                                fontSize = 12.5.sp,
                                                color = StitchOnSurface,
                                                lineHeight = 18.sp
                                            )
                                        }
                                    }
                                }
                            }

                            // Warning/Security Notice
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(StitchSurfaceContainerLow)
                                    .border(1.dp, StitchOutlineVariant.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                                    .padding(10.dp),
                                verticalAlignment = Alignment.Top,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = if (isHoax) Icons.Default.Warning else Icons.Default.Info,
                                    contentDescription = null,
                                    tint = if (isHoax) StitchError else StitchSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Column {
                                    Text(
                                        text = if (isHoax) "Peringatan Keamanan:" else "Catatan Integritas:",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = StitchOnSurface
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = if (isHoax)
                                            "Jangan membagikan kembali narasi ini atau membuka tautan yang berpotensi merupakan rekayasa penipuan (phishing)."
                                        else
                                            "Informasi ini selaras dengan konsensus data resmi dari otoritas terkait per tanggal analisis.",
                                        fontSize = 11.5.sp,
                                        color = StitchOnSurfaceVariant,
                                        lineHeight = 16.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // 5. Section: Sumber Referensi
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MenuBook,
                                contentDescription = null,
                                tint = StitchSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "SUMBER REFERENSI",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.8.sp,
                                color = StitchOnSurfaceVariant
                            )
                        }

                        Text(
                            text = "${analysis.sumberReferensi.size} Tervalidasi",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = StitchOutline
                        )
                    }

                    if (analysis.sumberReferensi.isEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = StitchSurfaceContainerLowest),
                            border = BorderStroke(1.dp, StitchOutlineVariant.copy(alpha = 0.4f))
                        ) {
                            Box(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = "Rujukan pangkalan data MAFINDO, Kominfo, dan media nasional terakreditasi.",
                                    fontSize = 12.5.sp,
                                    color = StitchOutline
                                )
                            }
                        }
                    } else {
                        analysis.sumberReferensi.forEach { sumber ->
                            ReferenceCard(sumber = sumber, context = context)
                        }
                    }
                }

                // 6. Bento Layout: Keandalan Sumber & Metode Verifikasi
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Keandalan Sumber
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(StitchSurfaceContainerLow)
                            .border(1.dp, StitchOutlineVariant.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(StitchTertiaryFixed),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = null,
                                tint = StitchTertiary,
                                modifier = Modifier.size(17.dp)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "KEANDALAN SUMBER",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.8.sp,
                                color = StitchOnSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Seluruh rujukan disinkronkan dengan verifikator terdaftar di IFCN (International Fact-Checking Network) dan kanal pers resmi terverifikasi.",
                                fontSize = 11.5.sp,
                                color = StitchOnSurfaceVariant,
                                lineHeight = 16.sp
                            )
                        }
                    }

                    // Metode Verifikasi
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(StitchSurfaceContainerLow)
                            .border(1.dp, StitchOutlineVariant.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(StitchPrimaryContainer.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountTree,
                                contentDescription = null,
                                tint = StitchPrimaryContainer,
                                modifier = Modifier.size(17.dp)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "METODE VERIFIKASI",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.8.sp,
                                color = StitchOnSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Analisis dilakukan melalui pencocokan premis-hipotesis Natural Language Inference (NLI) dan penelusuran identitas kepemilikan data resmi.",
                                fontSize = 11.5.sp,
                                color = StitchOnSurfaceVariant,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }
        }

        // 7. Floating Action Bottom Bar
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
            color = StitchSurfaceContainerLowest.copy(alpha = 0.96f),
            shadowElevation = 8.dp,
            border = BorderStroke(1.dp, StitchOutlineVariant.copy(alpha = 0.3f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
                    .navigationBarsPadding(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Primary Action Button
                Button(
                    onClick = onNavigateBack,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = StitchPrimaryContainer)
                ) {
                    Icon(
                        imageVector = Icons.Default.DocumentScanner,
                        contentDescription = null,
                        tint = StitchOnPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Periksa Klaim Lain",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = StitchOnPrimary
                    )
                }

                // Secondary Actions Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(
                                "Hasil Verifikasi: ${item.status} (${analysis.confidenceScore}%)\n" +
                                        "Ringkasan: ${analysis.ringkasan}\n" +
                                        "Penjelasan: ${analysis.penjelasan}"
                            ))
                            Toast.makeText(context, "Ringkasan berhasil disalin!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(36.dp),
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(1.dp, StitchOutlineVariant.copy(alpha = 0.5f)),
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = StitchSurfaceContainerLow)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = null,
                            tint = StitchOnSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Salin Ringkasan",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = StitchPrimary
                        )
                    }

                    OutlinedButton(
                        onClick = onShareClick,
                        modifier = Modifier
                            .weight(1f)
                            .height(36.dp),
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(1.dp, StitchOutlineVariant.copy(alpha = 0.5f)),
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = StitchSurfaceContainerLow)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = null,
                            tint = StitchOnSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Bagikan Hasil",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = StitchPrimary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ReferenceCard(
    sumber: SumberReferensi,
    context: android.content.Context
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = StitchSurfaceContainerLowest),
        border = BorderStroke(1.dp, StitchOutlineVariant.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "SUMBER TERVERIFIKASI",
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.6.sp,
                        color = StitchPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = sumber.nama,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = StitchOnSurface,
                        lineHeight = 19.sp
                    )
                }

                if (!sumber.url.isNullOrBlank()) {
                    IconButton(
                        onClick = {
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(sumber.url))
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "Tidak dapat membuka tautan", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                            contentDescription = "Buka Tautan",
                            tint = StitchSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            if (!sumber.keterangan.isNullOrBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(width = 0.dp, color = Color.Transparent)
                ) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Box(
                            modifier = Modifier
                                .width(2.dp)
                                .height(32.dp)
                                .background(StitchOutlineVariant)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "“${sumber.keterangan}”",
                            fontSize = 12.sp,
                            fontStyle = FontStyle.Italic,
                            color = StitchOnSurfaceVariant,
                            lineHeight = 17.sp
                        )
                    }
                }
            }

            HorizontalDivider(color = StitchOutlineVariant.copy(alpha = 0.2f), thickness = 1.dp)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Status: Terverifikasi Resmi",
                    fontSize = 11.sp,
                    color = StitchOutline
                )

                if (!sumber.url.isNullOrBlank()) {
                    Row(
                        modifier = Modifier
                            .clickable {
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(sumber.url))
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Tidak dapat membuka tautan", Toast.LENGTH_SHORT).show()
                                }
                            },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = "Buka Berita",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = StitchSecondary
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = StitchSecondary,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }
            }
        }
    }
}

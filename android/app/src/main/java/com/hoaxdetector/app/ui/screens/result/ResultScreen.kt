package com.hoaxdetector.app.ui.screens.result

import android.content.Intent
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
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
                Text(text = "Hapus Riwayat Ini?", fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
            },
            text = {
                Text(
                    text = "Hasil analisis ini akan dihapus dari riwayat pemeriksaan lokal Anda.",
                    color = Color(0xFF475569)
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
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                ) {
                    Text("Hapus", color = White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Batal", color = Color(0xFF64748B))
                }
            },
            containerColor = White,
            shape = RoundedCornerShape(18.dp)
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NavyDeep)
    ) {
        if (historyItem == null) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = ElectricBlue
            )
        } else {
            val item = historyItem!!
            ResultContent(
                item = item,
                onNavigateBack = onNavigateBack,
                onShareClick = {
                    val shareText = buildString {
                        append("🔍 *[HOAX DETECTOR AI]*\n")
                        append("Status: *${item.status}* (${item.analysisResult.confidenceScore}%)\n\n")
                        append("📌 *Klaim/Informasi:*\n")
                        append("\"${item.inputText.take(250)}\"\n\n")
                        append("📝 *Ringkasan Fakta:*\n")
                        append("${item.analysisResult.ringkasan}\n\n")
                        append("💡 *Penjelasan:*\n")
                        append("${item.analysisResult.penjelasan}\n\n")
                        append("— Diperiksa secara akurat menggunakan Aplikasi Hoax Detector")
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
    val statusColor = getStatusColor(item.statusColor)
    val statusBgColor = getStatusBgColor(item.statusColor)
    val statusIcon = getStatusIcon(item.statusColor)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .statusBarsPadding()
    ) {
        // Top Bar dengan Tombol Back, Share, dan Delete
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Kembali",
                        tint = TextPrimary
                    )
                }
                Text(
                    text = "Hasil Analisis",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                // Tombol Bagikan
                IconButton(onClick = onShareClick) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Bagikan",
                        tint = ElectricBlue
                    )
                }

                // Tombol Hapus
                IconButton(onClick = onDeleteClick) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Hapus Riwayat",
                        tint = TextSecondary
                    )
                }
            }
        }

        // Status Banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(NavyMedium, NavyDeep),
                        start = Offset(0f, 0f),
                        end = Offset(Float.POSITIVE_INFINITY, 0f)
                    )
                )
                .padding(horizontal = 20.dp, vertical = 20.dp)
        ) {
            Column {
                // Big Status Badge
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(statusBgColor)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = statusIcon,
                        contentDescription = null,
                        tint = statusColor,
                        modifier = Modifier.size(28.dp)
                    )
                    Column {
                        Text(
                            text = item.status,
                            style = MaterialTheme.typography.titleLarge,
                            color = statusColor,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Skor Kepercayaan: ${analysis.confidenceScore}%",
                            style = MaterialTheme.typography.bodySmall,
                            color = statusColor.copy(alpha = 0.7f)
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Confidence progress bar
                LinearProgressIndicator(
                    progress = { analysis.confidenceScore / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = statusColor,
                    trackColor = statusBgColor
                )

                Spacer(Modifier.height(16.dp))

                // Summary
                Text(
                    text = analysis.ringkasan,
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextPrimary,
                    fontWeight = FontWeight.Medium
                )

                Spacer(Modifier.height(8.dp))

                // Timestamp
                val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id"))
                Text(
                    text = "Diperiksa: ${dateFormat.format(Date(item.timestamp))}",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        Column(
            modifier = Modifier.padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Source info
            if (item.type == "url") {
                InfoCard(
                    icon = Icons.Default.Link,
                    title = "Sumber Artikel",
                    content = item.articleTitle ?: item.inputText,
                    tint = ElectricBlue
                )
            }

            // Explanation
            SectionCard(
                icon = Icons.Default.Analytics,
                title = "Penjelasan AI",
                iconColor = ElectricBlue
            ) {
                Text(
                    text = analysis.penjelasan,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }

            // Evidence
            if (analysis.bukti.isNotEmpty()) {
                SectionCard(
                    icon = Icons.Default.Gavel,
                    title = "Bukti yang Ditemukan (${analysis.bukti.size})",
                    iconColor = StatusYellow
                ) {
                    analysis.bukti.forEachIndexed { index, bukti ->
                        EvidenceItem(bukti = bukti)
                        if (index < analysis.bukti.size - 1) {
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 8.dp),
                                color = DividerColor
                            )
                        }
                    }
                }
            }

            // Sources
            if (analysis.sumberReferensi.isNotEmpty()) {
                SectionCard(
                    icon = Icons.Default.Source,
                    title = "Sumber Referensi",
                    iconColor = StatusGreen
                ) {
                    analysis.sumberReferensi.forEach { sumber ->
                        SourceItem(sumber = sumber)
                        Spacer(Modifier.height(6.dp))
                    }
                }
            }

            // Limitations
            SectionCard(
                icon = Icons.Default.Warning,
                title = "Keterbatasan Analisis",
                iconColor = StatusYellow
            ) {
                Text(
                    text = analysis.keterbatasan,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }

            // Tips
            SectionCard(
                icon = Icons.Default.Lightbulb,
                title = "Tips Mengenali Berita Serupa",
                iconColor = ElectricBlueLight
            ) {
                Text(
                    text = analysis.tipsMengenali,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }

            // Disclaimer
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = NavyLight),
                border = BorderStroke(1.dp, BorderColor)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(14.dp).padding(top = 2.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Analisis ini bersifat informatif dan dihasilkan oleh AI. Selalu verifikasi dengan sumber terpercaya sebelum menyebarkan informasi.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )
                }
            }

            // Tombol Cek Berita Lain
            Button(
                onClick = onNavigateBack,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue)
            ) {
                Text(
                    text = "Kembali ke Beranda",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = White
                )
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun InfoCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    content: String,
    tint: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = NavyLight)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(10.dp))
            Column {
                Text(title, style = MaterialTheme.typography.labelMedium, color = tint)
                Spacer(Modifier.height(2.dp))
                Text(
                    content,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    maxLines = 3
                )
            }
        }
    }
}

@Composable
private fun SectionCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    iconColor: Color,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = NavyLight)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(iconColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(Modifier.width(10.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun EvidenceItem(bukti: BuktiItem) {
    Row(
        verticalAlignment = Alignment.Top,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Icon(
            imageVector = if (bukti.mendukung) Icons.Default.CheckCircle else Icons.Default.Cancel,
            contentDescription = null,
            tint = if (bukti.mendukung) StatusGreen else StatusRed,
            modifier = Modifier.size(16.dp).padding(top = 2.dp)
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text = bukti.deskripsi,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )
    }
}

@Composable
private fun SourceItem(sumber: SumberReferensi) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(NavySurface)
            .padding(10.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            Icons.Default.BookmarkAdded,
            contentDescription = null,
            tint = StatusGreen,
            modifier = Modifier.size(14.dp).padding(top = 2.dp)
        )
        Spacer(Modifier.width(8.dp))
        Column {
            Text(
                text = sumber.nama,
                style = MaterialTheme.typography.labelMedium,
                color = StatusGreen,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = sumber.keterangan,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }
    }
}

private fun getStatusColor(colorStr: String): Color = when (colorStr) {
    "green" -> StatusGreen
    "yellow" -> StatusYellow
    "red" -> StatusRed
    else -> StatusGray
}

private fun getStatusBgColor(colorStr: String): Color = when (colorStr) {
    "green" -> StatusGreenDim
    "yellow" -> StatusYellowDim
    "red" -> StatusRedDim
    else -> StatusGrayDim
}

private fun getStatusIcon(colorStr: String) = when (colorStr) {
    "green" -> Icons.Default.CheckCircle
    "yellow" -> Icons.Default.Warning
    "red" -> Icons.Default.Cancel
    else -> Icons.Default.HelpOutline
}

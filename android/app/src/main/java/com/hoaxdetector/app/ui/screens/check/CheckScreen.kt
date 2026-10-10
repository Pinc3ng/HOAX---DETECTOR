package com.hoaxdetector.app.ui.screens.check

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.hoaxdetector.app.ui.theme.*

@Composable
fun CheckScreen(
    initialTab: Int = 0,
    onNavigateBack: () -> Unit = {},
    onNavigateToResult: (Long) -> Unit,
    viewModel: CheckViewModel = hiltViewModel()
) {
    val checkState by viewModel.checkState.collectAsState()
    val selectedTab by viewModel.selectedTab.collectAsState()
    val textInput by viewModel.textInput.collectAsState()
    val urlInput by viewModel.urlInput.collectAsState()
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    // Set initial tab if passed
    LaunchedEffect(initialTab) {
        if (initialTab in 0..1) {
            viewModel.selectedTab.value = initialTab
        }
    }

    // Navigate to result on success
    LaunchedEffect(checkState) {
        if (checkState is CheckState.Success) {
            val historyId = (checkState as CheckState.Success).historyId
            onNavigateToResult(historyId)
            viewModel.resetState()
        }
    }

    val currentInput = if (selectedTab == 0) textInput else urlInput
    val isValid = if (selectedTab == 0) textInput.trim().length >= 20 else urlInput.trim().startsWith("http")
    val isLoading = checkState is CheckState.Loading

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF4F7FB))
    ) {
        // Top Header matching Figma image 3 (Dark Navy Bar with Back Arrow + Periksa informasi)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(NavyDeep)
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Kembali",
                        tint = White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Text(
                    text = "Periksa informasi",
                    fontSize = 20.sp,
                    color = White,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Scrollable Body (Light background matching Figma image 3)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 18.dp)
        ) {
            // Tab Selector matching Figma image 3
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFFE2E8F0))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Tab Teks berita
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (selectedTab == 0) White else Color.Transparent)
                        .clickable { viewModel.selectedTab.value = 0 }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Teks berita",
                        fontSize = 14.sp,
                        fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Medium,
                        color = if (selectedTab == 0) Color(0xFF0F172A) else Color(0xFF64748B)
                    )
                }

                // Tab Tautan artikel
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (selectedTab == 1) White else Color.Transparent)
                        .clickable { viewModel.selectedTab.value = 1 }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Tautan artikel",
                        fontSize = 14.sp,
                        fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Medium,
                        color = if (selectedTab == 1) Color(0xFF0F172A) else Color(0xFF64748B)
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            // White Input Card matching Figma image 3 with Paste & Clear controls
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = White),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Action Bar on top of input: Quick Paste and Clear
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (selectedTab == 0) "Klaim / Teks" else "URL Berita",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF94A3B8)
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Tombol Tempel dari Clipboard
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFEFF6FF))
                                    .clickable {
                                        val clipText = clipboardManager.getText()?.text
                                        if (!clipText.isNullOrBlank()) {
                                            if (selectedTab == 0) {
                                                viewModel.textInput.value = clipText
                                            } else {
                                                viewModel.urlInput.value = clipText
                                            }
                                            Toast.makeText(context, "Teks berhasil ditempel!", Toast.LENGTH_SHORT).show()
                                        } else {
                                            Toast.makeText(context, "Clipboard kosong", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ContentPaste,
                                        contentDescription = "Tempel",
                                        tint = ElectricBlue,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Text(
                                        text = "Tempel",
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ElectricBlue
                                    )
                                }
                            }

                            // Tombol Bersihkan jika ada teks
                            if (currentInput.isNotEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFFFEE2E2))
                                        .clickable {
                                            if (selectedTab == 0) viewModel.clearText()
                                            else viewModel.clearUrl()
                                        }
                                        .padding(horizontal = 8.dp, vertical = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Hapus",
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFDC2626)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(10.dp))

                    BasicTextField(
                        value = if (selectedTab == 0) textInput else urlInput,
                        onValueChange = {
                            if (selectedTab == 0) viewModel.textInput.value = it
                            else viewModel.urlInput.value = it
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 180.dp, max = 260.dp),
                        textStyle = MaterialTheme.typography.bodyMedium.copy(
                            color = Color(0xFF0F172A),
                            fontSize = 14.5.sp,
                            lineHeight = 22.sp
                        ),
                        decorationBox = { innerTextField ->
                            Box {
                                if (currentInput.isEmpty()) {
                                    Text(
                                        text = if (selectedTab == 0)
                                            "Tempel teks berita, klaim viral, atau pesan berantai di sini..."
                                        else
                                            "Tempel tautan / link berita di sini (contoh: https://kompas.com/...)",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            color = Color(0xFF94A3B8),
                                            fontSize = 14.sp,
                                            lineHeight = 21.sp
                                        )
                                    )
                                }
                                innerTextField()
                            }
                        }
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // Subtitle counter matching Figma image 3
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (selectedTab == 0) {
                        if (currentInput.isNotEmpty() && currentInput.length < 20)
                            "Kurang ${20 - currentInput.length} karakter lagi"
                        else
                            "Minimal 20 karakter untuk analisis akurat"
                    } else {
                        "Pastikan format URL diawali http:// atau https://"
                    },
                    fontSize = 11.5.sp,
                    color = if (currentInput.isNotEmpty() && !isValid) Color(0xFFE11D48) else Color(0xFF64748B)
                )
                Text(
                    text = "${currentInput.length} karakter",
                    fontSize = 11.5.sp,
                    color = Color(0xFF64748B)
                )
            }

            Spacer(Modifier.height(12.dp))

            // Contoh Klaim Cepat (1-Tap Test Chips)
            Text(
                text = "Coba contoh klaim instan:",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF64748B)
            )
            Spacer(Modifier.height(6.dp))

            if (selectedTab == 0) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    QuickSampleChip(
                        label = "⚡ Hoax Vaksin 5G",
                        onClick = {
                            viewModel.textInput.value =
                                "Klaim: Vaksin COVID-19 mengandung chip mikroskopik 5G yang dapat memprogram dan mengendalikan pikiran manusia dari satelit jarak jauh."
                        }
                    )
                    QuickSampleChip(
                        label = "⚡ Hoax Rebusan Garam",
                        onClick = {
                            viewModel.textInput.value =
                                "Klaim: Menghirup uap air garam rebusan panas selama 5 menit setiap pagi terbukti membunuh seluruh virus di saluran pernapasan secara instan."
                        }
                    )
                    QuickSampleChip(
                        label = "✓ Fakta Riset Kopi",
                        onClick = {
                            viewModel.textInput.value =
                                "Artikel: Riset kesehatan menunjukkan konsumsi kopi hitam tanpa gula dalam batas wajar dapat meningkatkan fokus dan menurunkan risiko penyakit kardiovaskular."
                        }
                    )
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    QuickSampleChip(
                        label = "🔗 TurnBackHoax.id",
                        onClick = {
                            viewModel.urlInput.value = "https://turnbackhoax.id/2023/11/klaim-bawang-putih-sembuhkan-virus"
                        }
                    )
                    QuickSampleChip(
                        label = "🔗 Kompas Cek Fakta",
                        onClick = {
                            viewModel.urlInput.value = "https://www.kompas.com/cekfakta/bawang-putih-hoax"
                        }
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Info Security Banner matching Figma image 3
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFFE0F2FE))
                    .padding(14.dp),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.VerifiedUser,
                    contentDescription = null,
                    tint = Color(0xFF0284C7),
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Data yang Anda masukkan diproses secara aman. Kami tidak menyimpan informasi pribadi atau membagikannya dengan pihak ketiga.",
                    fontSize = 12.sp,
                    lineHeight = 17.sp,
                    color = Color(0xFF0369A1)
                )
            }

            Spacer(Modifier.height(20.dp))

            // Error display if any
            if (checkState is CheckState.Error) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEE2E2))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ErrorOutline,
                            contentDescription = null,
                            tint = Color(0xFFDC2626),
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = (checkState as CheckState.Error).message,
                            color = Color(0xFFDC2626),
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            // Bottom Action Button: Analisis dengan AI matching Figma image 3
            Button(
                onClick = {
                    if (selectedTab == 0) viewModel.checkText() else viewModel.checkUrl()
                },
                enabled = isValid && !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1E88E5),
                    disabledContainerColor = Color(0xFF93C5FD)
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = White,
                        strokeWidth = 2.dp
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = "Menganalisis dengan Algoritma NLI (Python)...",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = White
                    )
                } else {
                    Text(
                        text = "Analisis Berbasis NLI",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = White
                    )
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun QuickSampleChip(
    label: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFFE2E8F0))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF334155)
        )
    }
}

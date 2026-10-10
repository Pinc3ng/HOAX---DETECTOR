package com.hoaxdetector.app.ui.screens.check

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.hoaxdetector.app.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

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

    val currentDateStr = remember {
        SimpleDateFormat("d MMMM yyyy", Locale("id", "ID")).format(Date())
    }

    val wordCount = remember(currentInput) {
        if (currentInput.isBlank()) 0 else currentInput.trim().split("\\s+".toRegex()).size
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(StitchSurface)
    ) {
        if (isLoading) {
            // ==========================================
            // STITCH SCREEN 2: PROSES VERIFIKASI (AUDIT CADENCE)
            // ==========================================
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
            ) {
                // Top Minimal Navigation Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .border(width = 0.dp, color = Color.Transparent),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = { viewModel.resetState() },
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(
                            text = "Batal",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = StitchSecondary
                        )
                    }

                    Text(
                        text = "Proses Verifikasi",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = StitchOnSurface,
                        letterSpacing = (-0.2).sp
                    )

                    // Spacer for balanced center title
                    Spacer(modifier = Modifier.width(36.dp))
                }

                HorizontalDivider(color = StitchOutlineVariant.copy(alpha = 0.3f), thickness = 1.dp)

                // Scrollable Editorial Verification Canvas
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 1. Submitted Claim Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = StitchSurfaceContainerLowest),
                        border = BorderStroke(1.dp, StitchOutlineVariant.copy(alpha = 0.4f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
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
                                        imageVector = Icons.Default.ManageSearch,
                                        contentDescription = null,
                                        tint = StitchPrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "KLAIM YANG DIPERIKSA",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        letterSpacing = 0.8.sp,
                                        color = StitchPrimary
                                    )
                                }
                                Text(
                                    text = "${if (selectedTab == 0) "Input Teks" else "Tautan URL"} • $currentDateStr",
                                    fontSize = 11.sp,
                                    color = StitchOutline
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = "“${currentInput.take(220)}${if (currentInput.length > 220) "..." else ""}”",
                                fontSize = 16.sp,
                                fontFamily = FontFamily.Serif,
                                fontStyle = FontStyle.Italic,
                                fontWeight = FontWeight.Normal,
                                color = StitchOnSurface,
                                lineHeight = 24.sp
                            )

                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider(color = StitchOutlineVariant.copy(alpha = 0.2f), thickness = 1.dp)
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Sumber masukan: ${if (selectedTab == 0) "Pesan Berantai / Naskah" else "Tautan Berita Web"}",
                                    fontSize = 11.sp,
                                    color = StitchOutline
                                )
                                Text(
                                    text = "Panjang: $wordCount kata",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = StitchOutline
                                )
                            }
                        }
                    }

                    // 2. Progress Architecture & Verification Cadence
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = StitchSurfaceContainerLowest),
                        border = BorderStroke(1.dp, StitchOutlineVariant.copy(alpha = 0.4f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Bottom
                            ) {
                                Column {
                                    Text(
                                        text = "STATUS AUDIT",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        letterSpacing = 0.8.sp,
                                        color = StitchOutline
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Kajian Komprehensif Berlangsung",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = StitchPrimary
                                    )
                                }
                                Text(
                                    text = "78%",
                                    fontSize = 22.sp,
                                    fontFamily = FontFamily.Serif,
                                    fontWeight = FontWeight.Bold,
                                    color = StitchPrimary
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Editorial Hairline Progress Meter
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(StitchSurfaceContainerHighest)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(0.78f)
                                        .fillMaxHeight()
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(StitchPrimaryContainer)
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Memindai pola semantik Natural Language Inference (NLI) dan keterkaitan basis data kredibel secara bertahap.",
                                fontSize = 12.sp,
                                color = StitchOnSurfaceVariant,
                                lineHeight = 17.sp
                            )
                        }
                    }

                    // 3. Step-by-Step Audit Log
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = StitchSurfaceContainerLowest),
                        border = BorderStroke(1.dp, StitchOutlineVariant.copy(alpha = 0.4f))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            // Step 1: Checked
                            AuditStepRow(
                                title = "Memahami intisari dan entitas klaim",
                                status = "Selesai",
                                statusColor = StitchTertiary,
                                subtitle = "Entitas diidentifikasi: Verifikasi sintaksis & semantik NLI.",
                                isDone = true
                            )

                            HorizontalDivider(color = StitchOutlineVariant.copy(alpha = 0.2f), thickness = 1.dp)

                            // Step 2: Checked
                            AuditStepRow(
                                title = "Mencari rekam jejak pada pangkalan data Cek Fakta",
                                status = "Selesai",
                                statusColor = StitchTertiary,
                                subtitle = "Pencocokan TurnBackHoax, Kominfo & media kredibel.",
                                isDone = true
                            )

                            HorizontalDivider(color = StitchOutlineVariant.copy(alpha = 0.2f), thickness = 1.dp)

                            // Step 3: Active / Sedang Berjalan
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(StitchSurfaceContainerLow.copy(alpha = 0.6f))
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier
                                        .padding(top = 2.dp)
                                        .size(18.dp),
                                    color = StitchPrimaryContainer,
                                    strokeWidth = 2.dp
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Menganalisis bukti dan rujukan berita resmi...",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = StitchPrimary
                                        )
                                        Text(
                                            text = "BERJALAN",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 0.5.sp,
                                            color = StitchSecondary
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Membandingkan data silang dengan model premis-hipotesis NLI.",
                                        fontSize = 11.5.sp,
                                        color = StitchOnSurfaceVariant
                                    )
                                }
                            }

                            HorizontalDivider(color = StitchOutlineVariant.copy(alpha = 0.2f), thickness = 1.dp)

                            // Step 4: Pending / Menunggu
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Box(
                                    modifier = Modifier
                                        .padding(top = 2.dp)
                                        .size(18.dp)
                                        .clip(CircleShape)
                                        .background(StitchSurfaceContainerHigh)
                                        .border(1.dp, StitchOutlineVariant, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(5.dp)
                                            .clip(CircleShape)
                                            .background(StitchOutline)
                                    )
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Menyusun kesimpulan & tingkat keandalan sumber",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Normal,
                                            color = StitchOnSurface.copy(alpha = 0.7f)
                                        )
                                        Text(
                                            text = "MENUNGGU",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = StitchOutline
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Vonis fakta definitif akan dirilis setelah seluruh rujukan teruji.",
                                        fontSize = 11.5.sp,
                                        color = StitchOutline
                                    )
                                }
                            }
                        }
                    }

                    // 4. Calming Notice Box
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(StitchSurfaceContainerLow)
                            .border(1.dp, StitchOutlineVariant.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.VerifiedUser,
                            contentDescription = null,
                            tint = StitchPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Kami membandingkan informasi dengan konsensus media kredibel dan pangkalan data verifikasi independen secara transparan.",
                            fontSize = 12.sp,
                            color = StitchOnSurfaceVariant,
                            lineHeight = 17.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(30.dp))
                }
            }
        } else {
            // ==========================================
            // STITCH SCREEN 2: INPUT / SUBMIT STATE
            // ==========================================
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
            ) {
                // Top Header: Back + Title
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali",
                            tint = StitchPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Text(
                        text = "Verifikasi Fakta",
                        fontSize = 18.sp,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        color = StitchPrimary
                    )

                    Spacer(modifier = Modifier.size(36.dp))
                }

                HorizontalDivider(color = StitchOutlineVariant.copy(alpha = 0.3f), thickness = 1.dp)

                // Main Form Content
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 18.dp, vertical = 16.dp)
                ) {
                    // Header prompt
                    Text(
                        text = "Masukkan Materi Verifikasi",
                        fontSize = 20.sp,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        color = StitchOnSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Gunakan mesin NLI untuk mencocokkan premis berita terhadap pangkalan data fakta teruji.",
                        fontSize = 13.sp,
                        color = StitchOnSurfaceVariant,
                        lineHeight = 19.sp
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // Input Card with Tabs
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = StitchSurfaceContainerLowest),
                        border = BorderStroke(1.dp, StitchOutlineVariant.copy(alpha = 0.4f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            // Tab Selector
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 12.dp)
                            ) {
                                // Tab Teks
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { viewModel.selectedTab.value = 0 }
                                        .padding(bottom = 6.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Article,
                                            contentDescription = null,
                                            tint = if (selectedTab == 0) StitchPrimaryContainer else StitchOutline,
                                            modifier = Modifier.size(17.dp)
                                        )
                                        Text(
                                            text = "Teks berita",
                                            fontSize = 13.5.sp,
                                            fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Medium,
                                            color = if (selectedTab == 0) StitchPrimaryContainer else StitchOutline
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    if (selectedTab == 0) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth(0.8f)
                                                .height(2.5.dp)
                                                .background(StitchPrimaryContainer, RoundedCornerShape(2.dp))
                                        )
                                    }
                                }

                                // Tab Link
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { viewModel.selectedTab.value = 1 }
                                        .padding(bottom = 6.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Link,
                                            contentDescription = null,
                                            tint = if (selectedTab == 1) StitchPrimaryContainer else StitchOutline,
                                            modifier = Modifier.size(17.dp)
                                        )
                                        Text(
                                            text = "Tautan artikel",
                                            fontSize = 13.5.sp,
                                            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Medium,
                                            color = if (selectedTab == 1) StitchPrimaryContainer else StitchOutline
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    if (selectedTab == 1) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth(0.8f)
                                                .height(2.5.dp)
                                                .background(StitchPrimaryContainer, RoundedCornerShape(2.dp))
                                        )
                                    }
                                }
                            }

                            // Action Bar inside card: Quick Paste & Clear
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (selectedTab == 0) "Isi teks atau naskah klaim:" else "URL alamat artikel:",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = StitchOutline
                                )

                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    // Paste button
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(StitchSecondaryFixed.copy(alpha = 0.5f))
                                            .clickable {
                                                val clipText = clipboardManager.getText()?.text
                                                if (!clipText.isNullOrBlank()) {
                                                    if (selectedTab == 0) {
                                                        viewModel.textInput.value = clipText
                                                    } else {
                                                        viewModel.urlInput.value = clipText
                                                    }
                                                    Toast.makeText(context, "Teks berhasil ditempel", Toast.LENGTH_SHORT).show()
                                                } else {
                                                    Toast.makeText(context, "Papan klip kosong", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                            .padding(horizontal = 8.dp, vertical = 3.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.ContentPaste,
                                                contentDescription = null,
                                                tint = StitchSecondary,
                                                modifier = Modifier.size(12.dp)
                                            )
                                            Text(
                                                text = "Tempel",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = StitchSecondary
                                            )
                                        }
                                    }

                                    if (currentInput.isNotEmpty()) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(StitchErrorContainer.copy(alpha = 0.5f))
                                                .clickable {
                                                    if (selectedTab == 0) viewModel.clearText()
                                                    else viewModel.clearUrl()
                                                }
                                                .padding(horizontal = 8.dp, vertical = 3.dp)
                                        ) {
                                            Text(
                                                text = "Hapus",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = StitchError
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            // Text area
                            BasicTextField(
                                value = if (selectedTab == 0) textInput else urlInput,
                                onValueChange = {
                                    if (selectedTab == 0) viewModel.textInput.value = it
                                    else viewModel.urlInput.value = it
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 150.dp, max = 220.dp),
                                textStyle = MaterialTheme.typography.bodyMedium.copy(
                                    color = StitchOnSurface,
                                    fontSize = 14.5.sp,
                                    lineHeight = 22.sp
                                ),
                                decorationBox = { innerTextField ->
                                    Box {
                                        if (currentInput.isEmpty()) {
                                            Text(
                                                text = if (selectedTab == 0)
                                                    "Tempel klaim viral, narasi berantai, atau isi warta di sini..."
                                                else
                                                    "Tempel alamat link artikel berita resmi (contoh: https://antaranews.com/...)",
                                                fontSize = 14.sp,
                                                color = StitchOutline.copy(alpha = 0.7f),
                                                lineHeight = 21.sp
                                            )
                                        }
                                        innerTextField()
                                    }
                                }
                            )

                            HorizontalDivider(
                                color = StitchOutlineVariant.copy(alpha = 0.3f),
                                thickness = 1.dp,
                                modifier = Modifier.padding(top = 8.dp, bottom = 6.dp)
                            )

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
                                            "Minimal 20 karakter untuk akurasi optimal"
                                    } else {
                                        "Format diawali https://"
                                    },
                                    fontSize = 11.sp,
                                    color = if (currentInput.isNotEmpty() && !isValid) StitchError else StitchOutline
                                )
                                Text(
                                    text = "${currentInput.length} karakter",
                                    fontSize = 11.sp,
                                    color = StitchOutline
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Instant Sample Chips
                    Text(
                        text = "Contoh klaim cepat untuk pengujian:",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = StitchOutline
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        QuickSampleChip(
                            label = "⚡ Subsidi Listrik Telegram (Hoaks)",
                            onClick = {
                                viewModel.selectedTab.value = 0
                                viewModel.textInput.value =
                                    "Pemerintah resmi membagikan bantuan dana subsidi listrik tunai 2,5 juta rupiah untuk pengguna kartu SIM lama lewat tautan grup Telegram resmi BUMN."
                            }
                        )
                        QuickSampleChip(
                            label = "⚡ Hoaks Vaksin 5G Chip",
                            onClick = {
                                viewModel.selectedTab.value = 0
                                viewModel.textInput.value =
                                    "Vaksin COVID-19 dilaporkan mengandung microchip 5G yang dapat mengontrol pikiran manusia secara langsung dari menara satelit."
                            }
                        )
                        QuickSampleChip(
                            label = "✓ Fakta BMKG Gempa Terkini",
                            onClick = {
                                viewModel.selectedTab.value = 0
                                viewModel.textInput.value =
                                    "BMKG merilis informasi gempa bumi tektonik berkekuatan magnitudo 5,2 di wilayah pesisir selatan Jawa Barat tidak berpotensi tsunami."
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Notice box
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(StitchSurfaceContainerLow)
                            .border(1.dp, StitchOutlineVariant.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = StitchPrimaryContainer,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Data diverifikasi menggunakan model NLI internal tanpa mengirimkan naskah pribadi ke layanan pihak ketiga komersial.",
                            fontSize = 11.5.sp,
                            color = StitchOnSurfaceVariant,
                            lineHeight = 16.sp
                        )
                    }

                    // Error notice if any
                    if (checkState is CheckState.Error) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(StitchErrorContainer.copy(alpha = 0.4f))
                                .border(1.dp, StitchError.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                            .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ErrorOutline,
                                contentDescription = null,
                                tint = StitchError,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = (checkState as CheckState.Error).message,
                                fontSize = 12.sp,
                                color = StitchError,
                                lineHeight = 16.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Submit Button: Stitch Editorial Style
                    Button(
                        onClick = {
                            if (selectedTab == 0) viewModel.checkText() else viewModel.checkUrl()
                        },
                        enabled = isValid,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = StitchPrimaryContainer,
                            disabledContainerColor = StitchOutlineVariant
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.VerifiedUser,
                            contentDescription = null,
                            tint = StitchOnPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Verifikasi Fakta Sekarang",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = StitchOnPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(30.dp))
                }
            }
        }
    }
}

@Composable
private fun AuditStepRow(
    title: String,
    status: String,
    statusColor: Color,
    subtitle: String,
    isDone: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .padding(top = 2.dp)
                .size(18.dp)
                .clip(CircleShape)
                .background(StitchTertiaryFixed),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = StitchTertiary,
                modifier = Modifier.size(13.dp)
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = StitchOnSurface
                )
                Text(
                    text = status.uppercase(),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = statusColor
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 11.5.sp,
                color = StitchOutline
            )
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
            .clip(RoundedCornerShape(6.dp))
            .background(StitchSurfaceContainerLowest)
            .border(1.dp, StitchOutlineVariant.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            fontSize = 11.5.sp,
            fontWeight = FontWeight.Medium,
            color = StitchPrimary
        )
    }
}

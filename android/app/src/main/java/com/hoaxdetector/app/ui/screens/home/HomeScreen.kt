package com.hoaxdetector.app.ui.screens.home

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.hoaxdetector.app.data.local.entity.CheckHistoryEntity
import com.hoaxdetector.app.ui.theme.*

@Composable
fun HomeScreen(
    onNavigateToCheck: (Int) -> Unit = {},
    onNavigateToHistory: () -> Unit,
    onNavigateToEducation: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToResult: (Long) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val stats by viewModel.stats.collectAsState()
    val recentHistory by viewModel.recentHistory.collectAsState()
    var selectedTab by remember { mutableStateOf(0) } // 0 = Teks, 1 = Link
    var inputText by remember { mutableStateOf("") }
    val clipboardManager = LocalClipboardManager.current

    val wordCount = if (inputText.isBlank()) 0 else inputText.trim().split("\\s+".toRegex()).size

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(StitchSurface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .padding(horizontal = 18.dp)
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // 1. Top Bar matching Stitch: Verified Icon + HOAX DETECTOR + Notification & User Pill
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Verified,
                        contentDescription = null,
                        tint = StitchPrimaryContainer,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "HOAX DETECTOR",
                        fontSize = 20.sp,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        color = StitchPrimary,
                        letterSpacing = 0.5.sp
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = onNavigateToSettings,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Notifikasi",
                            tint = StitchOnSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // User Pill
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(StitchSurfaceContainer)
                            .border(1.dp, StitchOutlineVariant.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                            .clickable { onNavigateToSettings() }
                            .padding(start = 10.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Tamu",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = StitchOnSurfaceVariant
                        )
                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .clip(CircleShape)
                                .background(StitchPrimaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = White,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 2. Editorial Hero Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(StitchPrimaryContainer)
                )
                Text(
                    text = "RUANG VERIFIKASI PUBLIK",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = StitchPrimaryContainer,
                    letterSpacing = 1.sp
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Jangan langsung percaya.\nPeriksa dulu.",
                fontSize = 28.sp,
                lineHeight = 34.sp,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                color = StitchOnSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Kenali informasi yang perlu diverifikasi sebelum kamu membagikannya.",
                fontSize = 14.sp,
                lineHeight = 20.sp,
                color = StitchOnSurfaceVariant
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 3. Main Verification Input Box matching Stitch Redesign
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = StitchSurfaceContainerLowest),
                border = BorderStroke(1.dp, StitchOutlineVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Tab Switcher
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    ) {
                        // Tab Teks
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedTab = 0 }
                                .padding(bottom = 8.dp),
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
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "Teks berita",
                                    fontSize = 14.sp,
                                    fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
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
                                .clickable { selectedTab = 1 }
                                .padding(bottom = 8.dp),
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
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "Link berita",
                                    fontSize = 14.sp,
                                    fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
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

                    // Input Text Area
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 100.dp),
                        placeholder = {
                            Text(
                                text = if (selectedTab == 0)
                                    "Tempel klaim atau isi berita di sini..."
                                else
                                    "Masukkan atau tempel URL berita (https://...)...",
                                fontSize = 14.sp,
                                color = StitchOutline
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            focusedContainerColor = StitchSurfaceContainerLowest,
                            unfocusedContainerColor = StitchSurfaceContainerLowest
                        ),
                        maxLines = 5
                    )

                    // Quick Action & Counter
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp, bottom = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "📋 Tempel Teks",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = StitchSecondary,
                            modifier = Modifier.clickable {
                                val clip = clipboardManager.getText()?.text
                                if (!clip.isNullOrBlank()) {
                                    inputText = clip
                                }
                            }
                        )
                        Text(
                            text = "$wordCount / 1000 kata",
                            fontSize = 12.sp,
                            color = StitchOutline
                        )
                    }

                    // Primary Button: Periksa Klaim
                    Button(
                        onClick = {
                            onNavigateToCheck(selectedTab)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = StitchPrimaryContainer)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Periksa Klaim",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = White
                            )
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // Trust Reminder
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = StitchOutline,
                            modifier = Modifier.size(14.dp).padding(top = 2.dp)
                        )
                        Text(
                            text = "Hasil pemeriksaan adalah bantuan verifikasi NLI, bukan pengganti penilaian kritis.",
                            fontSize = 11.sp,
                            color = StitchOutline,
                            lineHeight = 15.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 4. Stats Summary Cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Card 1: Total
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = StitchSurfaceContainerLowest),
                    border = BorderStroke(1.dp, StitchOutlineVariant.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Total Audit",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = StitchOnSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stats.total.toString(),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = StitchPrimary
                        )
                    }
                }

                // Card 2: Valid
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = StitchSurfaceContainerLowest),
                    border = BorderStroke(1.dp, StitchOutlineVariant.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Didukung Bukti",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = StitchOnSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stats.validCount.toString(),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = StatusGreen
                        )
                    }
                }

                // Card 3: Menyesatkan
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = StitchSurfaceContainerLowest),
                    border = BorderStroke(1.dp, StitchOutlineVariant.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Menyesatkan",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = StitchOnSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stats.hoaxCount.toString(),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = StatusRed
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 5. Section: Pemeriksaan Terkini
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.HistoryEdu,
                        contentDescription = null,
                        tint = StitchOutline,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Pemeriksaan Terkini",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = StitchOnSurface
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onNavigateToHistory() }
                ) {
                    Text(
                        text = "Lihat semua",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = StitchSecondary
                    )
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = StitchSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // List Pemeriksaan Terkini
            if (recentHistory.isNotEmpty()) {
                recentHistory.forEach { item ->
                    StitchEditorialArticleCard(
                        item = item,
                        onClick = { onNavigateToResult(item.id) }
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = StitchSurfaceContainerLowest),
                    border = BorderStroke(1.dp, StitchOutlineVariant.copy(alpha = 0.4f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.HistoryEdu,
                            contentDescription = null,
                            tint = StitchOutline,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Belum Ada Riwayat Pemeriksaan",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = StitchOnSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Tempel berita atau klaim untuk memulai audit verifikasi pertama Anda.",
                            fontSize = 12.sp,
                            color = StitchOnSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
private fun StitchEditorialArticleCard(
    item: CheckHistoryEntity,
    onClick: () -> Unit
) {
    val isHoax = item.statusColor == "red" || item.status == "BERTENTANGAN DENGAN BUKTI"
    val isMisleading = item.statusColor == "yellow" || item.status == "MENYESATKAN"

    val (badgeBg, badgeText, badgeIcon) = when {
        isHoax -> Triple(StatusRedContainer, StatusRedText, Icons.Default.Cancel)
        isMisleading -> Triple(StatusYellowContainer, StatusYellowText, Icons.Default.Warning)
        else -> Triple(StatusGreenContainer, StatusGreenText, Icons.Default.CheckCircle)
    }

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = StitchSurfaceContainerLowest),
        border = BorderStroke(1.dp, StitchOutlineVariant.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Badge
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(badgeBg)
                        .padding(horizontal = 8.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = badgeIcon,
                        contentDescription = null,
                        tint = badgeText,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = item.status,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = badgeText
                    )
                }

                Text(
                    text = formatHomeTime(item.timestamp),
                    fontSize = 11.sp,
                    color = StitchOutline
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "“${item.articleTitle ?: item.inputText}”",
                fontSize = 15.sp,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.SemiBold,
                color = StitchOnSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = item.ringkasan,
                fontSize = 12.sp,
                color = StitchOnSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 17.sp
            )
        }
    }
}

private fun formatHomeTime(timestamp: Long): String {
    val diff = System.currentTimeMillis() - timestamp
    val hours = diff / (1000 * 60 * 60)
    val days = hours / 24
    return when {
        days > 7 -> "${days / 7} minggu lalu"
        days > 0 -> "$days hari lalu"
        hours > 0 -> "$hours jam lalu"
        else -> "Baru saja"
    }
}

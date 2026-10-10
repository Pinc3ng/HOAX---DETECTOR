package com.hoaxdetector.app.ui.screens.history

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.hoaxdetector.app.data.local.entity.CheckHistoryEntity
import com.hoaxdetector.app.ui.theme.*
import java.util.concurrent.TimeUnit

@Composable
fun HistoryScreen(
    onNavigateToResult: (Long) -> Unit,
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val history by viewModel.history.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("Semua") }
    var itemToDelete by remember { mutableStateOf<CheckHistoryEntity?>(null) }

    val filteredHistory = history.filter { item ->
        val matchesSearch = item.ringkasan.contains(searchQuery, ignoreCase = true) ||
                (item.articleTitle?.contains(searchQuery, ignoreCase = true) == true) ||
                item.inputText.contains(searchQuery, ignoreCase = true)
        val matchesFilter = when (selectedFilter) {
            "Menyesatkan" -> item.statusColor.contains("red", true) || item.status.contains("Hoaks", true) || item.status.contains("Menyesatkan", true)
            "Didukung Bukti" -> item.statusColor.contains("green", true) || item.status.contains("Fakta", true) || item.status.contains("Didukung", true)
            "Belum Cukup Bukti" -> item.statusColor.contains("yellow", true) || item.status.contains("Perlu", true) || item.status.contains("Belum", true)
            else -> true
        }
        matchesSearch && matchesFilter
    }

    // Dialog Konfirmasi Hapus Satu Riwayat
    if (itemToDelete != null) {
        AlertDialog(
            onDismissRequest = { itemToDelete = null },
            title = {
                Text(text = "Hapus Riwayat?", fontWeight = FontWeight.Bold, color = StitchPrimary)
            },
            text = {
                Text(
                    text = "Apakah Anda yakin ingin menghapus arsip verifikasi ini dari riwayat pemeriksaan lokal?",
                    color = StitchOnSurfaceVariant
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        itemToDelete?.let { viewModel.deleteHistory(it.id) }
                        itemToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StitchError)
                ) {
                    Text("Hapus", color = StitchOnPrimary)
                }
            },
            dismissButton = {
                TextButton(onClick = { itemToDelete = null }) {
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // 1. Top Bar: HD Logo + HOAX DETECTOR + Notification
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(StitchPrimaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "HD",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = StitchOnPrimary
                        )
                    }
                    Text(
                        text = "HOAX DETECTOR",
                        fontSize = 18.sp,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        color = StitchPrimary
                    )
                }

                IconButton(
                    onClick = { /* Notifikasi info */ },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Notifikasi",
                        tint = StitchOnSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // 2. Title Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "Riwayat Pemeriksaan",
                    fontSize = 22.sp,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    color = StitchPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Arsip verifikasi informasi dan rekam jejak investigasi pribadi",
                    fontSize = 12.5.sp,
                    color = StitchOnSurfaceVariant
                )
                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = StitchOutlineVariant.copy(alpha = 0.3f), thickness = 1.dp)
            }

            // 3. Search Field
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 10.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            text = "Cari riwayat klaim atau kata kunci...",
                            fontSize = 13.5.sp,
                            color = StitchOutline
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = StitchOutline,
                            modifier = Modifier.size(19.dp)
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Hapus teks",
                                    tint = StitchOutline,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = StitchOnSurface,
                        unfocusedTextColor = StitchOnSurface,
                        focusedBorderColor = StitchPrimaryContainer,
                        unfocusedBorderColor = StitchOutlineVariant.copy(alpha = 0.6f),
                        focusedContainerColor = StitchSurfaceContainerLowest,
                        unfocusedContainerColor = StitchSurfaceContainerLowest
                    )
                )
            }

            // 4. Horizontal Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 18.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Chip 1: Semua
                FilterChipItem(
                    label = "Semua",
                    isSelected = selectedFilter == "Semua",
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Archive,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                    },
                    onClick = { selectedFilter = "Semua" }
                )

                // Chip 2: Didukung Bukti
                FilterChipItem(
                    label = "Didukung Bukti",
                    isSelected = selectedFilter == "Didukung Bukti",
                    dotColor = StitchTertiary,
                    onClick = { selectedFilter = "Didukung Bukti" }
                )

                // Chip 3: Menyesatkan
                FilterChipItem(
                    label = "Menyesatkan",
                    isSelected = selectedFilter == "Menyesatkan",
                    dotColor = StitchError,
                    onClick = { selectedFilter = "Menyesatkan" }
                )

                // Chip 4: Belum Cukup Bukti
                FilterChipItem(
                    label = "Belum Cukup Bukti",
                    isSelected = selectedFilter == "Belum Cukup Bukti",
                    dotColor = StitchSecondary,
                    onClick = { selectedFilter = "Belum Cukup Bukti" }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 5. Verification Chronological List
            if (filteredHistory.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 60.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.HistoryEdu,
                            contentDescription = null,
                            tint = StitchOutline,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(Modifier.height(10.dp))
                        Text(
                            text = if (searchQuery.isNotEmpty() || selectedFilter != "Semua")
                                "Tidak ada arsip yang cocok"
                            else
                                "Belum ada riwayat pemeriksaan",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = StitchPrimary
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "Klaim yang Anda periksa akan tersimpan di sini",
                            fontSize = 12.5.sp,
                            color = StitchOnSurfaceVariant
                        )

                        if (searchQuery.isNotEmpty() || selectedFilter != "Semua") {
                            Spacer(Modifier.height(14.dp))
                            Button(
                                onClick = {
                                    searchQuery = ""
                                    selectedFilter = "Semua"
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = StitchPrimaryContainer),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text("Reset Filter & Pencarian", color = StitchOnPrimary, fontSize = 12.5.sp)
                            }
                        }
                    }
                }
            } else {
                val now = System.currentTimeMillis()
                val oneDayMillis = TimeUnit.DAYS.toMillis(1)

                val todayItems = filteredHistory.filter { (now - it.timestamp) < oneDayMillis }
                val olderItems = filteredHistory.filter { (now - it.timestamp) >= oneDayMillis }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    if (todayItems.isNotEmpty()) {
                        item {
                            GroupHeader(title = "HARI INI")
                        }
                        items(todayItems, key = { it.id }) { item ->
                            EditorialHistoryItem(
                                item = item,
                                onClick = { onNavigateToResult(item.id) },
                                onDelete = { itemToDelete = item }
                            )
                        }
                    }

                    if (olderItems.isNotEmpty()) {
                        item {
                            GroupHeader(title = if (todayItems.isEmpty()) "SEMUA ARSIP" else "KEMARIN & SEBELUMNYA")
                        }
                        items(olderItems, key = { it.id }) { item ->
                            EditorialHistoryItem(
                                item = item,
                                onClick = { onNavigateToResult(item.id) },
                                onDelete = { itemToDelete = item }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GroupHeader(title: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = title,
            fontSize = 10.5.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.8.sp,
            color = StitchOutline
        )
        HorizontalDivider(
            color = StitchOutlineVariant.copy(alpha = 0.4f),
            thickness = 1.dp,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun EditorialHistoryItem(
    item: CheckHistoryEntity,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val isHoax = item.statusColor.contains("red", true) || item.status.contains("Hoaks", true) || item.status.contains("Menyesatkan", true)
    val isFakta = item.statusColor.contains("green", true) || item.status.contains("Fakta", true) || item.status.contains("Didukung", true)

    val badgeBg = when {
        isHoax -> Color(0xFFFBEBE9)
        isFakta -> Color(0xFFE6F8EF)
        else -> Color(0xFFFFF4E5)
    }
    val badgeBorder = when {
        isHoax -> Color(0xFFF2C7C3)
        isFakta -> Color(0xFFA6F3CA)
        else -> Color(0xFFFFDDB3)
    }
    val badgeColor = when {
        isHoax -> StitchError
        isFakta -> StitchTertiary
        else -> Color(0xFF854D0E)
    }
    val badgeIcon = when {
        isHoax -> Icons.Default.Cancel
        isFakta -> Icons.Default.CheckCircle
        else -> Icons.Default.Warning
    }
    val badgeText = when {
        isHoax -> "MENYESATKAN"
        isFakta -> "DIDUKUNG BUKTI"
        else -> "BELUM CUKUP BUKTI"
    }

    val timeLabel = formatRelativeTime(item.timestamp)
    val headlineText = item.articleTitle ?: item.inputText

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = StitchSurfaceContainerLowest),
        border = BorderStroke(1.dp, StitchOutlineVariant.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = headlineText,
                    fontSize = 15.sp,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    color = StitchOnSurface,
                    lineHeight = 21.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .size(24.dp)
                        .padding(start = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Hapus Riwayat",
                        tint = StitchOutline,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Vonis Badge
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(badgeBg)
                            .border(1.dp, badgeBorder, RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = badgeIcon,
                            contentDescription = null,
                            tint = badgeColor,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = badgeText,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = badgeColor,
                            letterSpacing = 0.4.sp
                        )
                    }

                    Text(
                        text = "•",
                        fontSize = 10.sp,
                        color = StitchOutlineVariant
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = StitchOutline,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = timeLabel,
                            fontSize = 11.sp,
                            color = StitchOutline
                        )
                    }
                }

                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = StitchOutline,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun FilterChipItem(
    label: String,
    isSelected: Boolean,
    icon: (@Composable () -> Unit)? = null,
    dotColor: Color? = null,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (isSelected) StitchPrimaryContainer else StitchSurfaceContainerLowest)
            .border(
                1.dp,
                if (isSelected) StitchPrimaryContainer else StitchOutlineVariant.copy(alpha = 0.5f),
                RoundedCornerShape(6.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (icon != null) {
                icon()
            } else if (dotColor != null) {
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(dotColor)
                )
            }
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                color = if (isSelected) StitchOnPrimary else StitchOnSurfaceVariant
            )
        }
    }
}

private fun formatRelativeTime(timestamp: Long): String {
    val diff = System.currentTimeMillis() - timestamp
    val minutes = diff / (1000 * 60)
    val hours = diff / (1000 * 60 * 60)
    val days = hours / 24
    return when {
        minutes < 5 -> "Baru saja"
        minutes < 60 -> "$minutes menit lalu"
        hours < 24 -> "$hours jam lalu"
        days == 1L -> "Kemarin"
        days < 7 -> "$days hari lalu"
        else -> "${days / 7} minggu lalu"
    }
}

package com.hoaxdetector.app.ui.screens.history

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.hoaxdetector.app.data.local.entity.CheckHistoryEntity
import com.hoaxdetector.app.ui.theme.*

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
            "Menyesatkan" -> item.statusColor == "red" || item.statusColor == "yellow"
            "Didukung Bukti" -> item.statusColor == "green"
            else -> true
        }
        matchesSearch && matchesFilter
    }

    // Dialog Konfirmasi Hapus Satu Riwayat
    if (itemToDelete != null) {
        AlertDialog(
            onDismissRequest = { itemToDelete = null },
            title = {
                Text(text = "Hapus Riwayat", fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
            },
            text = {
                Text(
                    text = "Apakah Anda yakin ingin menghapus item ini dari riwayat pemeriksaan?",
                    color = Color(0xFF475569)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        itemToDelete?.let { viewModel.deleteHistory(it.id) }
                        itemToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                ) {
                    Text("Hapus", color = White)
                }
            },
            dismissButton = {
                TextButton(onClick = { itemToDelete = null }) {
                    Text("Batal", color = Color(0xFF64748B))
                }
            },
            containerColor = White,
            shape = RoundedCornerShape(16.dp)
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .statusBarsPadding()
            .padding(horizontal = 20.dp)
    ) {
        Spacer(Modifier.height(16.dp))

        // Title matching Figma image 4
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Riwayat",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A)
            )

            if (history.isNotEmpty()) {
                Text(
                    text = "${filteredHistory.size} item",
                    fontSize = 13.sp,
                    color = Color(0xFF64748B)
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // Search Bar matching Figma image 4
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(
                    text = "Cari riwayat pemeriksaan...",
                    fontSize = 14.sp,
                    color = Color(0xFF94A3B8)
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = Color(0xFF94A3B8),
                    modifier = Modifier.size(20.dp)
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Bersihkan",
                            tint = Color(0xFF94A3B8)
                        )
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color(0xFF0F172A),
                unfocusedTextColor = Color(0xFF0F172A),
                focusedBorderColor = ElectricBlue,
                unfocusedBorderColor = Color(0xFFE2E8F0),
                focusedContainerColor = White,
                unfocusedContainerColor = White
            )
        )

        Spacer(Modifier.height(14.dp))

        // Filter Chips matching Figma image 4 ("Semua", "Menyesatkan", "Didukung Bukti")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            listOf("Semua", "Menyesatkan", "Didukung Bukti").forEach { filter ->
                val isSelected = selectedFilter == filter
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isSelected) Color(0xFFDBEAFE) else Color(0xFFF1F5F9))
                        .clickable { selectedFilter = filter }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = filter,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) Color(0xFF1D4ED8) else Color(0xFF64748B)
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // List of Cards matching Figma image 4
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
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(54.dp)
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = if (searchQuery.isNotEmpty() || selectedFilter != "Semua")
                            "Tidak ada riwayat yang cocok"
                        else
                            "Belum ada riwayat pemeriksaan",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF475569)
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "Riwayat verifikasi klaim akan muncul di sini",
                        fontSize = 13.sp,
                        color = Color(0xFF94A3B8)
                    )

                    if (searchQuery.isNotEmpty() || selectedFilter != "Semua") {
                        Spacer(Modifier.height(16.dp))
                        Button(
                            onClick = {
                                searchQuery = ""
                                selectedFilter = "Semua"
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Reset Filter & Pencarian", color = White)
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(filteredHistory, key = { it.id }) { item ->
                    val isHoax = item.statusColor == "red" || item.statusColor == "yellow"
                    val badgeBg = if (isHoax) Color(0xFFFEE2E2) else Color(0xFFDCFCE7)
                    val badgeColor = if (isHoax) Color(0xFFDC2626) else Color(0xFF16A34A)
                    val formattedTime = formatTimestamp(item.timestamp)

                    FigmaHistoryCard(
                        badgeText = item.status,
                        badgeBg = badgeBg,
                        badgeColor = badgeColor,
                        time = formattedTime,
                        title = item.articleTitle ?: item.inputText,
                        onClick = { onNavigateToResult(item.id) },
                        onDeleteClick = { itemToDelete = item }
                    )
                }
            }
        }
    }
}

@Composable
private fun FigmaHistoryCard(
    badgeText: String,
    badgeBg: Color,
    badgeColor: Color,
    time: String,
    title: String,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Top Row: Badge + Time + Delete Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(badgeBg)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = badgeText,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = badgeColor
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = time,
                        fontSize = 12.sp,
                        color = Color(0xFF94A3B8)
                    )
                    IconButton(
                        onClick = onDeleteClick,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Hapus",
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            // Title
            Text(
                text = title,
                fontSize = 14.5.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A),
                lineHeight = 20.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(Modifier.height(10.dp))

            // Action: Lihat Hasil >
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Lihat Hasil ›",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF2563EB)
                )
            }
        }
    }
}

private fun formatTimestamp(timestamp: Long): String {
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

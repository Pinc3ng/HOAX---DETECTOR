package com.hoaxdetector.app.ui.screens.settings

import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.hoaxdetector.app.data.local.UserSessionManager
import com.hoaxdetector.app.ui.theme.*

@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToAuth: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val sessionManager = remember { UserSessionManager(context) }
    val userSession = remember { sessionManager.getCurrentUser() }

    val selectedTheme by viewModel.selectedTheme.collectAsState()
    val aiNotificationEnabled by viewModel.aiNotificationEnabled.collectAsState()
    val backendStatus by viewModel.backendStatus.collectAsState()

    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showClearConfirmDialog by remember { mutableStateOf(false) }
    var showLogoutConfirmDialog by remember { mutableStateOf(false) }

    // Dialog Konfirmasi Keluar (Logout)
    if (showLogoutConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirmDialog = false },
            title = {
                Text(text = "Keluar dari Akun?", fontWeight = FontWeight.Bold, color = StitchPrimary)
            },
            text = {
                Text(
                    text = "Anda akan dialihkan kembali ke halaman masuk. Data riwayat lokal tetap tersimpan di perangkat.",
                    color = StitchOnSurfaceVariant,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutConfirmDialog = false
                        viewModel.logout {
                            onNavigateToAuth()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StitchError)
                ) {
                    Text("Keluar", color = StitchOnPrimary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutConfirmDialog = false }) {
                    Text("Batal", color = StitchOutline)
                }
            },
            containerColor = StitchSurfaceContainerLowest,
            shape = RoundedCornerShape(12.dp)
        )
    }

    // Dialog Konfirmasi Bersihkan Riwayat
    if (showClearConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showClearConfirmDialog = false },
            title = {
                Text(text = "Hapus Seluruh Riwayat?", fontWeight = FontWeight.Bold, color = StitchPrimary)
            },
            text = {
                Text(
                    text = "Semua arsip hasil pemeriksaan di perangkat ini akan dihapus secara permanen.",
                    color = StitchOnSurfaceVariant,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showClearConfirmDialog = false
                        viewModel.clearAllHistory()
                        Toast.makeText(context, "Semua riwayat berhasil dihapus", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StitchError)
                ) {
                    Text("Hapus Semua", color = StitchOnPrimary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirmDialog = false }) {
                    Text("Batal", color = StitchOutline)
                }
            },
            containerColor = StitchSurfaceContainerLowest,
            shape = RoundedCornerShape(12.dp)
        )
    }

    // Dialog Kebijakan Privasi
    if (showPrivacyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyDialog = false },
            title = {
                Text(text = "Privasi & Arsitektur NLI", fontWeight = FontWeight.Bold, color = StitchPrimary)
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "1. HOAX-DETECTOR MELALUI NLI sepenuhnya memproses inferensi menggunakan mesin Python lokal tanpa mengirimkan teks ke Gemini API atau model komersial eksternal.",
                        fontSize = 12.5.sp,
                        color = StitchOnSurfaceVariant,
                        lineHeight = 18.sp
                    )
                    Text(
                        text = "2. Riwayat disimpan di SQLite lokal perangkat Anda dan tidak dibagikan ke pihak ketiga mana pun.",
                        fontSize = 12.5.sp,
                        color = StitchOnSurfaceVariant,
                        lineHeight = 18.sp
                    )
                    Text(
                        text = "3. Rujukan fakta bersumber dari konsensus MAFINDO TurnBackHoax, Kominfo RI, ANTARA News, BMKG, dan Kemenkes RI.",
                        fontSize = 12.5.sp,
                        color = StitchOnSurfaceVariant,
                        lineHeight = 18.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showPrivacyDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = StitchPrimaryContainer)
                ) {
                    Text("Tutup", color = StitchOnPrimary)
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
            // 1. Top Bar: Back Button + Title
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
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
                    text = "Profil & Pengaturan",
                    fontSize = 18.sp,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    color = StitchPrimary
                )

                IconButton(
                    onClick = { /* Notif */ },
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

            // Scrollable Settings Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 18.dp, vertical = 8.dp)
                    .padding(bottom = 30.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                // Editorial Section Title
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "AKUN VERIFIKATOR PUBLIK",
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.8.sp,
                        color = StitchOutline
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Profil & Pengaturan",
                        fontSize = 22.sp,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        color = StitchOnSurface
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = StitchOutlineVariant.copy(alpha = 0.3f), thickness = 1.dp)
                }

                // 2. User / Account Card
                val displayName = userSession?.username ?: "Ahmad Fauzi"
                val initials = displayName.split(" ").mapNotNull { it.firstOrNull()?.toString() }.take(2).joinToString("").uppercase().ifEmpty { "AF" }
                val userIdStr = remember(displayName) { "ID-JKT-${(displayName.hashCode() and 0xFFFF).toString().padStart(6, '0')}" }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = StitchSurfaceContainerLowest),
                    border = BorderStroke(1.dp, StitchOutlineVariant.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // User Avatar
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(StitchPrimaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = initials,
                                    fontSize = 20.sp,
                                    fontFamily = FontFamily.Serif,
                                    fontWeight = FontWeight.Bold,
                                    color = StitchOnPrimary
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = displayName,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = StitchOnSurface
                                    )
                                    Row(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(StitchTertiaryFixed.copy(alpha = 0.5f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = StitchTertiary,
                                            modifier = Modifier.size(11.dp)
                                        )
                                        Text(
                                            text = "TERVERIFIKASI",
                                            fontSize = 9.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = StitchTertiary
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Verifikator Publik • Mode Akun Aktif",
                                    fontSize = 12.sp,
                                    color = StitchOnSurfaceVariant
                                )
                                Text(
                                    text = "ID: $userIdStr",
                                    fontSize = 11.sp,
                                    color = StitchOutline,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = StitchOutlineVariant.copy(alpha = 0.25f), thickness = 1.dp)
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Sesi Kredensial Pengguna",
                                fontSize = 12.sp,
                                color = StitchOnSurfaceVariant
                            )
                            TextButton(
                                onClick = { showLogoutConfirmDialog = true },
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text(
                                    text = "Ganti Akun",
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = StitchSecondary
                                )
                            }
                        }
                    }
                }

                // 3. Section: TAMPILAN & TEMA
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = null,
                            tint = StitchOutline,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = "TAMPILAN & TEMA",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp,
                            color = StitchOutline
                        )
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = StitchSurfaceContainerLowest),
                        border = BorderStroke(1.dp, StitchOutlineVariant.copy(alpha = 0.4f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Tema Aplikasi",
                                fontSize = 14.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = StitchOnSurface
                            )
                            Text(
                                text = "Menyesuaikan kontras pembacaan naskah investigasi editorial",
                                fontSize = 12.sp,
                                color = StitchOnSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Segmented Selector: Terang (Aktif) vs Gelap
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(StitchSurfaceContainer)
                                    .border(1.dp, StitchOutlineVariant.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                    .padding(3.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                // Terang
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (selectedTheme != "Mode Gelap") StitchSurfaceContainerLowest else Color.Transparent)
                                        .clickable {
                                            viewModel.selectedTheme.value = "Mode Terang"
                                            Toast.makeText(context, "Tema Terang Editorial aktif", Toast.LENGTH_SHORT).show()
                                        }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.LightMode,
                                            contentDescription = null,
                                            tint = if (selectedTheme != "Mode Gelap") StitchPrimary else StitchOutline,
                                            modifier = Modifier.size(15.dp)
                                        )
                                        Text(
                                            text = "Terang (Aktif)",
                                            fontSize = 12.sp,
                                            fontWeight = if (selectedTheme != "Mode Gelap") FontWeight.Bold else FontWeight.Medium,
                                            color = if (selectedTheme != "Mode Gelap") StitchPrimary else StitchOutline
                                        )
                                    }
                                }

                                // Gelap
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (selectedTheme == "Mode Gelap") StitchSurfaceContainerLowest else Color.Transparent)
                                        .clickable {
                                            viewModel.selectedTheme.value = "Mode Gelap"
                                            Toast.makeText(context, "Mode Gelap", Toast.LENGTH_SHORT).show()
                                        }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.DarkMode,
                                            contentDescription = null,
                                            tint = if (selectedTheme == "Mode Gelap") StitchPrimary else StitchOutline,
                                            modifier = Modifier.size(15.dp)
                                        )
                                        Text(
                                            text = "Gelap",
                                            fontSize = 12.sp,
                                            fontWeight = if (selectedTheme == "Mode Gelap") FontWeight.Bold else FontWeight.Medium,
                                            color = if (selectedTheme == "Mode Gelap") StitchPrimary else StitchOutline
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // 4. Section: KONEKSI & PANGKALAN DATA
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Storage,
                            contentDescription = null,
                            tint = StitchOutline,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = "KONEKSI & PANGKALAN DATA",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp,
                            color = StitchOutline
                        )
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = StitchSurfaceContainerLowest),
                        border = BorderStroke(1.dp, StitchOutlineVariant.copy(alpha = 0.4f))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            // Row 1: Status Server
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Status Server Verifikasi",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = StitchOnSurface
                                    )
                                    Text(
                                        text = "Sinkronisasi mesin NLI (Python FastAPI)",
                                        fontSize = 12.sp,
                                        color = StitchOnSurfaceVariant
                                    )
                                }

                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(StitchTertiaryFixed.copy(alpha = 0.5f))
                                        .border(1.dp, StitchTertiary.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(7.dp)
                                            .clip(CircleShape)
                                            .background(StitchTertiary)
                                    )
                                    Text(
                                        text = "Terhubung (28ms)",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = StitchTertiary
                                    )
                                }
                            }

                            HorizontalDivider(color = StitchOutlineVariant.copy(alpha = 0.25f), thickness = 1.dp)

                            // Row 2: Sumber Data Terakreditasi
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = "Pangkalan Data Cek Fakta",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = StitchOnSurface
                                )
                                Text(
                                    text = "TurnBackHoax, Kominfo Aduan, IFCN API",
                                    fontSize = 12.sp,
                                    color = StitchOnSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(2.dp))

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    listOf("TurnBackHoax v3.2", "Kominfo AduanKonten", "IFCN Signatory", "BMKG Real-time").forEach { tag ->
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(StitchSurfaceContainer)
                                                .border(1.dp, StitchOutlineVariant.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                                                .padding(horizontal = 8.dp, vertical = 3.dp)
                                        ) {
                                            Text(
                                                text = tag,
                                                fontSize = 11.sp,
                                                color = StitchOnSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // 5. Section: PENGATURAN PRIVASI & DATA
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = StitchOutline,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = "PRIVASI & PENYIMPANAN",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp,
                            color = StitchOutline
                        )
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = StitchSurfaceContainerLowest),
                        border = BorderStroke(1.dp, StitchOutlineVariant.copy(alpha = 0.4f))
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                            // Kebijakan Privasi
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showPrivacyDialog = true }
                                    .padding(vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Kebijakan Privasi & Arsitektur NLI",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = StitchOnSurface
                                    )
                                    Text(
                                        text = "Tanpa ketergantungan API pihak ketiga eksternal",
                                        fontSize = 11.5.sp,
                                        color = StitchOutline
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = null,
                                    tint = StitchOutline,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            HorizontalDivider(color = StitchOutlineVariant.copy(alpha = 0.25f), thickness = 1.dp)

                            // Bersihkan Riwayat
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showClearConfirmDialog = true }
                                    .padding(vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Bersihkan Semua Riwayat",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = StitchError
                                    )
                                    Text(
                                        text = "Hapus seluruh arsip pemeriksaan dari memori lokal",
                                        fontSize = 11.5.sp,
                                        color = StitchOutline
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Default.DeleteOutline,
                                    contentDescription = null,
                                    tint = StitchError,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                // App Footer Info
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "HOAX-DETECTOR MELALUI NLI",
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        color = StitchPrimary
                    )
                    Text(
                        text = "Versi 1.0.0 (Python NLI Engine) • Stitch Editorial Edition",
                        fontSize = 11.sp,
                        color = StitchOutline
                    )
                }
            }
        }
    }
}

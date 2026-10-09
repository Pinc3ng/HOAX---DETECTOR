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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.hoaxdetector.app.ui.theme.*

/**
 * Layar Pengaturan sesuai desain Figma Image 6 dengan seluruh interaktivitas aktif
 */
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToAuth: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val selectedTheme by viewModel.selectedTheme.collectAsState()
    val aiNotificationEnabled by viewModel.aiNotificationEnabled.collectAsState()
    val backendStatus by viewModel.backendStatus.collectAsState()

    var showThemeDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showClearConfirmDialog by remember { mutableStateOf(false) }
    var showLogoutConfirmDialog by remember { mutableStateOf(false) }

    // Dialog Konfirmasi Keluar (Logout)
    if (showLogoutConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirmDialog = false },
            title = {
                Text(text = "Keluar dari Akun?", fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
            },
            text = {
                Text(
                    text = "Anda akan dialihkan kembali ke halaman masuk. Data riwayat lokal tetap tersimpan di perangkat.",
                    color = Color(0xFF475569),
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
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                ) {
                    Text("Keluar", color = White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutConfirmDialog = false }) {
                    Text("Batal", color = Color(0xFF64748B))
                }
            },
            containerColor = White,
            shape = RoundedCornerShape(18.dp)
        )
    }

    // Dialog Pilihan Tema
    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = {
                Text(text = "Pilih Tema Aplikasi", fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    listOf("Sistem", "Mode Gelap", "Mode Terang").forEach { themeName ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    viewModel.selectedTheme.value = themeName
                                    showThemeDialog = false
                                    Toast.makeText(context, "Tema diatur ke: $themeName", Toast.LENGTH_SHORT).show()
                                }
                                .padding(vertical = 12.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = themeName,
                                fontSize = 15.sp,
                                fontWeight = if (selectedTheme == themeName) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTheme == themeName) ElectricBlue else Color(0xFF1E293B)
                            )
                            if (selectedTheme == themeName) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = ElectricBlue,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showThemeDialog = false }) {
                    Text("Tutup", color = ElectricBlue)
                }
            },
            containerColor = White,
            shape = RoundedCornerShape(18.dp)
        )
    }

    // Dialog Privasi & Keamanan
    if (showPrivacyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyDialog = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = ElectricBlue,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "Privasi & Keamanan",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Komitmen Keamanan Data:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.5.sp,
                        color = Color(0xFF0F172A)
                    )
                    Text(
                        text = "1. Penyimpanan Lokal: Seluruh riwayat pemeriksaan Anda disimpan secara aman hanya di memori internal ponsel melalui Room Database lokal.\n\n" +
                                "2. Tanpa Pelacakan: Aplikasi tidak mengumpulkan informasi identitas pribadi, kontak, atau lokasi GPS.\n\n" +
                                "3. Transmisi Aman: Teks yang Anda periksa hanya dikirimkan ke model AI untuk inferensi fakta dan tidak dibagikan kepada pihak ketiga.",
                        fontSize = 12.5.sp,
                        lineHeight = 18.sp,
                        color = Color(0xFF475569)
                    )

                    Spacer(Modifier.height(8.dp))

                    Button(
                        onClick = {
                            showPrivacyDialog = false
                            showClearConfirmDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFEE2E2)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteForever,
                            contentDescription = null,
                            tint = Color(0xFFDC2626),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Hapus Semua Riwayat Pemeriksaan",
                            color = Color(0xFFDC2626),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showPrivacyDialog = false }) {
                    Text("Tutup", color = ElectricBlue)
                }
            },
            containerColor = White,
            shape = RoundedCornerShape(18.dp)
        )
    }

    // Dialog Konfirmasi Hapus Semua Riwayat
    if (showClearConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showClearConfirmDialog = false },
            title = {
                Text(text = "Hapus Semua Riwayat?", fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
            },
            text = {
                Text(
                    text = "Tindakan ini akan menghapus seluruh data riwayat pemeriksaan dari database lokal. Tindakan ini tidak dapat dibatalkan.",
                    color = Color(0xFF475569),
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearAllHistory {
                            Toast.makeText(context, "Semua riwayat berhasil dihapus", Toast.LENGTH_SHORT).show()
                        }
                        showClearConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                ) {
                    Text("Hapus Semuanya", color = White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirmDialog = false }) {
                    Text("Batal", color = Color(0xFF64748B))
                }
            },
            containerColor = White,
            shape = RoundedCornerShape(18.dp)
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF4F7FB))
    ) {
        // Top Header: Dark Navy bar with Back Button + Pengaturan matching Figma image 6
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
                    text = "Pengaturan",
                    fontSize = 20.sp,
                    color = White,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Content Area (Light Background matching Figma image 6)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 20.dp)
        ) {
            // Card Profil Pengguna & Keluar
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = White),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFEFF6FF)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (viewModel.isGuest) Icons.Default.PersonOutline else Icons.Default.Person,
                                contentDescription = null,
                                tint = ElectricBlue,
                                modifier = Modifier.size(26.dp)
                            )
                        }

                        Column {
                            Text(
                                text = viewModel.userName,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                            Text(
                                text = if (viewModel.isGuest) "Mode Tamu (Tanpa Akun)" else viewModel.userEmail,
                                fontSize = 12.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                    }

                    Button(
                        onClick = { showLogoutConfirmDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFEE2E2)),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "Keluar",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFDC2626)
                        )
                    }
                }
            }

            Spacer(Modifier.height(18.dp))

            // Card 1: Menu Pengaturan
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = White),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Column(modifier = Modifier.padding(horizontal = 18.dp, vertical = 6.dp)) {
                    // Item 1: Tema Aplikasi (Klik untuk memilih tema)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showThemeDialog = true }
                            .padding(vertical = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Tema Aplikasi",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = selectedTheme,
                                fontSize = 14.sp,
                                color = Color(0xFF64748B)
                            )
                            Text(
                                text = "›",
                                fontSize = 18.sp,
                                color = Color(0xFF94A3B8),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)

                    // Item 2: Privasi & Keamanan (Klik untuk membaca info dan hapus data)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showPrivacyDialog = true }
                            .padding(vertical = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Privasi & Keamanan",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = "›",
                            fontSize = 20.sp,
                            color = Color(0xFF94A3B8),
                            fontWeight = FontWeight.Bold
                        )
                    }

                    HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)

                    // Item 3: Pemberitahuan AI (Switch toggle interaktif)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Pemberitahuan AI",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                            Text(
                                text = if (aiNotificationEnabled) "Peringatan hoaks aktif" else "Dinonaktifkan",
                                fontSize = 12.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                        Switch(
                            checked = aiNotificationEnabled,
                            onCheckedChange = { isChecked ->
                                viewModel.aiNotificationEnabled.value = isChecked
                                Toast.makeText(
                                    context,
                                    if (isChecked) "Pemberitahuan AI diaktifkan" else "Pemberitahuan dinonaktifkan",
                                    Toast.LENGTH_SHORT
                                ).show()
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = White,
                                checkedTrackColor = ElectricBlue,
                                uncheckedTrackColor = Color(0xFFE2E8F0)
                            )
                        )
                    }
                }
            }

            Spacer(Modifier.height(18.dp))

            // Card Server Backend Status (Fitur verifikasi konektivitas untuk presentasi)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = White),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudQueue,
                                contentDescription = null,
                                tint = ElectricBlue,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Status Server Backend",
                                fontSize = 14.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                        }

                        Button(
                            onClick = { viewModel.checkBackendHealth() },
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEFF6FF))
                        ) {
                            Text(
                                text = "Uji Koneksi",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = ElectricBlue
                            )
                        }
                    }

                    Spacer(Modifier.height(10.dp))

                    when (backendStatus) {
                        is BackendStatus.Idle -> {
                            Text(
                                text = "Tekan 'Uji Koneksi' untuk memeriksa konektivitas ke server AI Node.js.",
                                fontSize = 12.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                        is BackendStatus.Checking -> {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(14.dp),
                                    strokeWidth = 2.dp,
                                    color = ElectricBlue
                                )
                                Text(
                                    text = "Menghubungi server backend...",
                                    fontSize = 12.sp,
                                    color = ElectricBlue
                                )
                            }
                        }
                        is BackendStatus.Online -> {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(text = "🟢", fontSize = 12.sp)
                                Text(
                                    text = (backendStatus as BackendStatus.Online).message,
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF16A34A)
                                )
                            }
                        }
                        is BackendStatus.Offline -> {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(text = "🔴", fontSize = 12.sp)
                                Text(
                                    text = (backendStatus as BackendStatus.Offline).message,
                                    fontSize = 12.sp,
                                    color = Color(0xFFDC2626)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(18.dp))

            // Card 2: Info Aplikasi / Credits matching Figma image 6
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = White),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 26.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Shield Icon with light blue circular background
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE0F2FE)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.VerifiedUser,
                            contentDescription = "Logo",
                            tint = Color(0xFF1E88E5),
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(Modifier.height(14.dp))

                    // Title HOAXDETECTOR
                    Text(
                        text = "HOAXDETECTOR",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A),
                        letterSpacing = 1.sp
                    )

                    Spacer(Modifier.height(4.dp))

                    // Version text
                    Text(
                        text = "Versi 1.0.0 (University Project)",
                        fontSize = 13.sp,
                        color = Color(0xFF64748B)
                    )

                    Spacer(Modifier.height(12.dp))

                    // Developers credits
                    Text(
                        text = "Dikembangkan oleh Vincent, Leonardo Angkasa,\ndan Gilliant.",
                        fontSize = 12.5.sp,
                        lineHeight = 18.sp,
                        color = Color(0xFF64748B),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

package com.hoaxdetector.app.ui.screens.education

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hoaxdetector.app.ui.theme.*

data class EducationDetail(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val iconColor: Color,
    val iconBg: Color,
    val description: String,
    val warningPoints: List<String>,
    val tips: List<String>
)

/**
 * Layar Pusat Edukasi (Navbar Tab 4 & Tombol Panduan)
 * Sesuai desain Figma Image 5 dengan fitur detail interaktif
 */
@Composable
fun EducationScreen(
    onNavigateBack: () -> Unit = {},
    onNavigateToCheck: () -> Unit = {}
) {
    var selectedEducation by remember { mutableStateOf<EducationDetail?>(null) }

    // Dialog Detail Edukasi Interaktif
    if (selectedEducation != null) {
        val detail = selectedEducation!!
        AlertDialog(
            onDismissRequest = { selectedEducation = null },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(detail.iconBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = detail.icon,
                            contentDescription = null,
                            tint = detail.iconColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Text(
                        text = detail.title,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = detail.description,
                        fontSize = 13.5.sp,
                        lineHeight = 20.sp,
                        color = Color(0xFF334155)
                    )

                    // Ciri-Ciri Utama
                    Text(
                        text = "🚨 Tanda-Tanda Utama:",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                    detail.warningPoints.forEach { point ->
                        Row(
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(text = "•", color = Color(0xFFDC2626), fontWeight = FontWeight.Bold)
                            Text(
                                text = point,
                                fontSize = 12.5.sp,
                                lineHeight = 18.sp,
                                color = Color(0xFF475569)
                            )
                        }
                    }

                    // Tips Penanganan
                    Text(
                        text = "💡 Langkah Terbaik:",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                    detail.tips.forEach { tip ->
                        Row(
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(text = "✓", color = Color(0xFF16A34A), fontWeight = FontWeight.Bold)
                            Text(
                                text = tip,
                                fontSize = 12.5.sp,
                                lineHeight = 18.sp,
                                color = Color(0xFF475569)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        selectedEducation = null
                        onNavigateToCheck()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Coba Cek Informasi", color = White)
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedEducation = null }) {
                    Text("Tutup", color = Color(0xFF64748B))
                }
            },
            containerColor = White,
            shape = RoundedCornerShape(20.dp)
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NavyDeep)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(16.dp))

            // Title matching Figma image 5
            Text(
                text = "Pusat Edukasi",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = White
            )

            Spacer(Modifier.height(8.dp))

            // Subtitle matching Figma image 5
            Text(
                text = "Tingkatkan literasi digital Anda dengan panduan antihoaks.",
                fontSize = 14.sp,
                lineHeight = 20.sp,
                color = TextSecondary
            )

            Spacer(Modifier.height(24.dp))

            // List of 4 Cards matching Figma image 5 (Sekarang bisa diklik untuk membaca panduan!)
            Column(
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Card 1: Cara mengenali judul provokatif (Yellow Warning)
                FigmaEducationCard(
                    icon = Icons.Default.Warning,
                    iconBg = Color(0xFF2D2817),
                    iconTint = Color(0xFFFBBF24),
                    title = "Cara mengenali judul provokatif",
                    subtitle = "Clickbait dan manipulasi emosi pembaca",
                    onClick = {
                        selectedEducation = EducationDetail(
                            title = "Judul Provokatif & Clickbait",
                            subtitle = "Clickbait dan manipulasi emosi pembaca",
                            icon = Icons.Default.Warning,
                            iconColor = Color(0xFFFBBF24),
                            iconBg = Color(0xFFFEF3C7),
                            description = "Judul provokatif dirancang sengaja untuk memicu kemarahan, kepanikan, atau rasa penasaran berlebihan tanpa menyajikan konteks sebenarnya.",
                            warningPoints = listOf(
                                "Menggunakan kata bombastis seperti 'VIRAL!', 'BOCOR!', 'GEMPAR!', atau 'JANGAN SAMPAI KETINGGALAN!'.",
                                "Huruf kapital berlebihan dan banyak tanda seru (!!!).",
                                "Isi berita tidak menjawab atau bahkan bertentangan dengan judul."
                            ),
                            tips = listOf(
                                "Baca seluruh isi artikel, jangan hanya menyimpulkan dari judul.",
                                "Tahan diri untuk tidak langsung membagikan sebelum membaca lengkap."
                            )
                        )
                    }
                )

                // Card 2: Memeriksa sumber berita (Green Shield)
                FigmaEducationCard(
                    icon = Icons.Default.VerifiedUser,
                    iconBg = Color(0xFF132B25),
                    iconTint = Color(0xFF10B981),
                    title = "Memeriksa sumber berita",
                    subtitle = "Validasi kredibilitas media dan penulis",
                    onClick = {
                        selectedEducation = EducationDetail(
                            title = "Validasi Sumber Berita",
                            subtitle = "Validasi kredibilitas media dan penulis",
                            icon = Icons.Default.VerifiedUser,
                            iconColor = Color(0xFF10B981),
                            iconBg = Color(0xFFD1FAE5),
                            description = "Media kredibel memiliki badan hukum resmi, alamat redaksi jelas, dan mematuhi Kode Etik Jurnalistik Dewan Pers.",
                            warningPoints = listOf(
                                "Menggunakan domain gratisan atau domain tiruan (misal: detik.news-portal.xyz atau tiruan-kompas.blogspot.com).",
                                "Tidak ada susunan redaksi atau kontak resmi redaksi.",
                                "Artikel tidak mencantumkan nama jurnalis atau narasumber ahli yang jelas."
                            ),
                            tips = listOf(
                                "Periksa status verifikasi media di laman resmi dewanpers.or.id.",
                                "Cari apakah media nasional lain juga memberitakan peristiwa yang sama."
                            )
                        )
                    }
                )

                // Card 3: Mengenali foto di luar konteks (Blue Search)
                FigmaEducationCard(
                    icon = Icons.Default.Search,
                    iconBg = Color(0xFF17253D),
                    iconTint = Color(0xFF38BDF8),
                    title = "Mengenali foto di luar konteks",
                    subtitle = "Reverse image search dan manipulasi visual",
                    onClick = {
                        selectedEducation = EducationDetail(
                            title = "Foto & Video Di Luar Konteks",
                            subtitle = "Reverse image search dan manipulasi visual",
                            icon = Icons.Default.Search,
                            iconColor = Color(0xFF38BDF8),
                            iconBg = Color(0xFFE0F2FE),
                            description = "Banyak penyebar hoaks mengambil foto bencana atau aksi unjuk rasa lama, lalu diklaim sebagai kejadian baru yang sedang berlangsung.",
                            warningPoints = listOf(
                                "Foto buram atau dipotong sempit (cropped) untuk menyembunyikan plang nama jalan/bendera lokasi asli.",
                                "Manipulasi gambar buatan AI: detail tangan/jari tidak proporsional, teks di latar belakang kabur atau tidak terbaca.",
                                "Deskripsi foto tidak mencantumkan tanggal dan fotografer asli."
                            ),
                            tips = listOf(
                                "Gunakan fitur Google Lens atau Google Images Search untuk mencari foto asli.",
                                "Periksa metadata atau pencahayaan gambar jika mencurigakan."
                            )
                        )
                    }
                )

                // Card 4: Menghindari pesan berantai (Red Share)
                FigmaEducationCard(
                    icon = Icons.Default.Share,
                    iconBg = Color(0xFF2E1C2B),
                    iconTint = Color(0xFFF43F5E),
                    title = "Menghindari pesan berantai",
                    subtitle = "Ciri-ciri hoaks WhatsApp dan grup chat",
                    onClick = {
                        selectedEducation = EducationDetail(
                            title = "Mewaspadai Pesan Berantai",
                            subtitle = "Ciri-ciri hoaks WhatsApp dan grup chat",
                            icon = Icons.Default.Share,
                            iconColor = Color(0xFFF43F5E),
                            iconBg = Color(0xFFFFE4E6),
                            description = "Pesan berantai di WhatsApp dan grup keluarga merupakan sarana paling rentan untuk penyebaran misinformasi dan penipuan daring.",
                            warningPoints = listOf(
                                "Memiliki label 'Diteruskan berkali-kali' (Forwarded many times).",
                                "Mencatut nama dokter, kementerian, atau tokoh terkenal tanpa tautan rujukan resmi.",
                                "Mengandung kalimat intimidasi atau doa berantai ('Sebarkan ini jika Anda menyayangi keluarga')."
                            ),
                            tips = listOf(
                                "Salin teks pesan lalu tempel dan periksa di aplikasi Hoax Detector ini.",
                                "Beri tahu pengirim pesan secara santun jika isi pesan terbukti tidak benar."
                            )
                        )
                    }
                )
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun FigmaEducationCard(
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF101F38))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.weight(1f)
            ) {
                // Circular Icon container matching Figma image 5
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(iconBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Column {
                    Text(
                        text = title,
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = White
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = subtitle,
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            }

            // Chevron Right
            Text(
                text = "›",
                fontSize = 20.sp,
                color = Color(0xFF64748B),
                fontWeight = FontWeight.Bold
            )
        }
    }
}

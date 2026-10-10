package com.hoaxdetector.app.ui.screens.education

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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hoaxdetector.app.ui.theme.*

data class EducationModule(
    val id: Int,
    val title: String,
    val readTime: String,
    val summary: String,
    val tag: String,
    val level: String,
    val fullContent: String,
    val checklist: List<String>,
    val practicalTips: List<String>
)

@Composable
fun EducationScreen(
    onNavigateBack: () -> Unit = {},
    onNavigateToCheck: () -> Unit = {}
) {
    var selectedModule by remember { mutableStateOf<EducationModule?>(null) }

    val modules = remember {
        listOf(
            EducationModule(
                id = 1,
                title = "Periksa Sumber Berita",
                readTime = "4 mnt",
                summary = "Membedakan media resmi dan portal kloningan berniat jahat.",
                tag = "Verifikasi Domain",
                level = "Tingkat Dasar",
                fullContent = "Banyak situs penyebar hoaks meniru nama portal media terkenal dengan memodifikasi sedikit ekstensi domain (misal .com diganti .co.vu, .xyz, atau .blogspot). Media kredibel selalu mencantumkan susunan redaksi, alamat kantor fisik, dan terdaftar resmi di Dewan Pers.",
                checklist = listOf(
                    "Cek URL di address bar, pastikan berdomain resmi (.com, .co.id, .id)",
                    "Periksa halaman 'Tentang Kami' dan pedoman media siber",
                    "Pastikan portal memiliki verifikasi Dewan Pers"
                ),
                practicalTips = listOf(
                    "Gunakan mesin pencari untuk mengecek apakah media arus utama lain juga memberitakan kabar serupa.",
                    "Hati-hati terhadap domain tiruan yang sengaja menyisipkan logo media nasional."
                )
            ),
            EducationModule(
                id = 2,
                title = "Cermati Bukti Foto & Video",
                readTime = "6 mnt",
                summary = "Teknik reverse image search dan deteksi rekayasa foto AI.",
                tag = "Forensik Visual",
                level = "Menengah",
                fullContent = "Gambar atau video sering kali asli tetapi dipelintir waktu atau lokasinya (misal video bencana luar negeri diklaim terjadi di Indonesia). Selain itu, marak gambar sintetis buatan AI generatif yang memiliki ketidakwajaran pada jari tangan, tekstur kulit, dan tulisan latar belakang.",
                checklist = listOf(
                    "Lakukan pencarian gambar terbalik (Google Lens / Yandex Images)",
                    "Perhatikan detail tekstur pencahayaan dan proporsi anatomi tubuh",
                    "Cari tahu kapan foto pertama kali diunggah ke internet"
                ),
                practicalTips = listOf(
                    "Jika melihat foto sensasional tokoh politik atau bencana besar, telusuri rilis foto resmi kantor berita ANTARA.",
                    "Periksa apakah ada distorsi di bagian jari, gigi, atau bayangan yang janggal."
                )
            ),
            EducationModule(
                id = 3,
                title = "Waspada Manipulasi Emosi",
                readTime = "5 mnt",
                summary = "Mengenali kalimat provokatif bernada ancaman atau kepanikan.",
                tag = "Psikologi Misinformasi",
                level = "Tingkat Dasar",
                fullContent = "Hoaks sengaja dirancang untuk memicu reaksi emosional instan—seperti kemarahan, ketakutan, atau euforia berlebihan—sebelum nalar kritis sempat bekerja. Kalimat seperti 'Sebarkan sebelum dihapus pemerintah!' atau 'Bila peduli keluarga, teruskan sekarang!' adalah ciri khas manipulasi emosi.",
                checklist = listOf(
                    "Kenali ajakan menyebarkan pesan secara tergesa-gesa",
                    "Waspadai huruf kapital berlebihan dan tanda seru ganda (!!!)",
                    "Tanyakan pada diri sendiri: 'Apakah informasi ini memicu amarah tanpa data?'"
                ),
                practicalTips = listOf(
                    "Terapkan jeda 1 menit sebelum membagikan informasi apapun ke grup keluarga atau media sosial.",
                    "Informasi resmi dari pemerintah selalu disampaikan dengan bahasa lugas dan tenang."
                )
            ),
            EducationModule(
                id = 4,
                title = "Periksa Tanggal & Konteks Lama",
                readTime = "3 mnt",
                summary = "Mencegah daur ulang warta basi yang sengaja diviralkan kembali.",
                tag = "Validasi Kronologis",
                level = "Tingkat Dasar",
                fullContent = "Daur ulang berita lama adalah taktik umum pelaku misinformasi. Artikel tentang bentrokan atau kebijakan dari tahun 2017 disebarkan ulang di tahun 2025 seolah-olah baru saja terjadi kemarin demi memancing kegaduhan publik.",
                checklist = listOf(
                    "Periksa tanggal publikasi asli di bagian bawah judul artikel",
                    "Cari tahun peristiwa di mesin pencari",
                    "Verifikasi apakah kasus tersebut sudah terselesaikan di masa lampau"
                ),
                practicalTips = listOf(
                    "Tangkapan layar judul berita tanpa tanggal adalah sinyal kuat informasi yang direkayasa atau dipotong konteksnya.",
                    "Selalu buka tautan aslinya untuk membaca kronologi lengkap."
                )
            )
        )
    }

    // Interactive Modal Dialog for Module Detail
    if (selectedModule != null) {
        val mod = selectedModule!!
        AlertDialog(
            onDismissRequest = { selectedModule = null },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(StitchPrimaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${mod.id}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = StitchOnPrimary
                        )
                    }
                    Text(
                        text = mod.title,
                        fontSize = 17.sp,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        color = StitchPrimary
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
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = mod.readTime,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = StitchSecondary
                        )
                        Text(text = "•", fontSize = 11.sp, color = StitchOutline)
                        Text(
                            text = mod.level,
                            fontSize = 11.5.sp,
                            color = StitchOutline
                        )
                    }

                    Text(
                        text = mod.fullContent,
                        fontSize = 13.5.sp,
                        color = StitchOnSurfaceVariant,
                        lineHeight = 20.sp
                    )

                    HorizontalDivider(color = StitchOutlineVariant.copy(alpha = 0.3f), thickness = 1.dp)

                    Text(
                        text = "📋 Langkah Verifikasi Mandiri:",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = StitchPrimary
                    )
                    mod.checklist.forEach { item ->
                        Row(
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(text = "✓", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = StitchTertiary)
                            Text(text = item, fontSize = 12.5.sp, color = StitchOnSurface, lineHeight = 18.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "💡 Tips Praktis:",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = StitchPrimary
                    )
                    mod.practicalTips.forEach { tip ->
                        Row(
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(text = "•", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = StitchSecondary)
                            Text(text = tip, fontSize = 12.5.sp, color = StitchOnSurfaceVariant, lineHeight = 18.sp)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { selectedModule = null },
                    colors = ButtonDefaults.buttonColors(containerColor = StitchPrimaryContainer),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text("Paham & Tutup", color = StitchOnPrimary, fontSize = 13.sp)
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
            // 1. Top Bar: Header Branding
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
                    onClick = { /* Info */ },
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

            // Scrollable Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 18.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 2. Section Header: Magazine Masthead Style
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(StitchPrimaryContainer)
                        )
                        Text(
                            text = "LITERASI MEDIA & KRITIS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp,
                            color = StitchPrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Lebih Cermat Menilai Informasi",
                        fontSize = 24.sp,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        color = StitchOnSurface,
                        letterSpacing = (-0.3).sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Panduan praktis mengenali misinformasi, disinformasi, dan manipulasi media di ruang digital Indonesia.",
                        fontSize = 13.sp,
                        color = StitchOnSurfaceVariant,
                        lineHeight = 19.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = StitchOutlineVariant.copy(alpha = 0.3f), thickness = 1.dp)
                }

                // 3. Featured Editorial Hero Card
                Card(
                    onClick = {
                        selectedModule = EducationModule(
                            id = 0,
                            title = "Kenali Judul Clickbait & Trik Rekayasa Konteks",
                            readTime = "5 Menit Baca",
                            summary = "Bagaimana mengenali judul sensasional yang tidak sesuai dengan isi artikel sebenarnya.",
                            tag = "Sorotan Utama",
                            level = "Tingkat Dasar",
                            fullContent = "Clickbait sengaja memotong informasi penting dari judul demi memancing rasa penasaran berlebihan (curiosity gap). Sering kali isi beritanya biasa saja atau bahkan membantah apa yang disiratkan dalam judul bombastis tersebut.",
                            checklist = listOf(
                                "Baca seluruh badan artikel sebelum menyimpulkan",
                                "Cek apakah narasumber yang dikutip benar-benar ada",
                                "Perhatikan apakah judul memakai tanda tanya provokatif"
                            ),
                            practicalTips = listOf(
                                "Hindari membagikan artikel yang hanya Anda baca judulnya saja.",
                                "Bandingkan judul berita dengan 2 media terpercaya lainnya."
                            )
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = StitchSurfaceContainerLowest),
                    border = BorderStroke(1.dp, StitchOutlineVariant.copy(alpha = 0.4f))
                ) {
                    Column {
                        // Banner Area with Deep Teal accent
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .background(StitchPrimaryContainer)
                                .padding(16.dp),
                            contentAlignment = Alignment.BottomStart
                        ) {
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(StitchSurface.copy(alpha = 0.95f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoStories,
                                    contentDescription = null,
                                    tint = StitchPrimary,
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = "SOROTAN UTAMA",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.6.sp,
                                    color = StitchPrimary
                                )
                            }
                        }

                        // Content
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Schedule,
                                        contentDescription = null,
                                        tint = StitchPrimaryContainer,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Text(
                                        text = "5 Menit Baca",
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = StitchPrimaryContainer
                                    )
                                }
                                Text(text = "•", fontSize = 11.sp, color = StitchOutline)
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(StitchSurfaceContainer)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "Tingkat Dasar",
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = StitchOnSurfaceVariant
                                    )
                                }
                            }

                            Text(
                                text = "Kenali Judul Clickbait & Trik Rekayasa Konteks",
                                fontSize = 17.sp,
                                fontFamily = FontFamily.Serif,
                                fontWeight = FontWeight.Bold,
                                color = StitchOnSurface,
                                lineHeight = 23.sp
                            )

                            Text(
                                text = "Bagaimana mengenali judul sensasional yang tidak sesuai dengan isi artikel sebenarnya dalam lanskap media digital.",
                                fontSize = 12.5.sp,
                                color = StitchOnSurfaceVariant,
                                lineHeight = 18.sp
                            )

                            HorizontalDivider(
                                color = StitchOutlineVariant.copy(alpha = 0.3f),
                                thickness = 1.dp,
                                modifier = Modifier.padding(top = 4.dp)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = "Baca Kajian Lengkap",
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = StitchSecondary
                                    )
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                        contentDescription = null,
                                        tint = StitchSecondary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }

                                Icon(
                                    imageVector = Icons.Default.BookmarkBorder,
                                    contentDescription = null,
                                    tint = StitchOutline,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                // 4. Practical Learning Modules List
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Modul Pembelajaran",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = StitchOnSurface
                        )
                        Text(
                            text = "4 Modul Tersedia",
                            fontSize = 11.5.sp,
                            color = StitchOutline
                        )
                    }

                    modules.forEach { mod ->
                        EditorialModuleCard(
                            module = mod,
                            onClick = { selectedModule = mod }
                        )
                    }
                }

                // 5. Quick S.I.F.T Fact-Checking Checklist Box
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = StitchSurfaceContainerLow),
                    border = BorderStroke(1.dp, StitchOutlineVariant.copy(alpha = 0.35f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.FactCheck,
                                contentDescription = null,
                                tint = StitchPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "METODE S.I.F.T (CEK CEPAT)",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.8.sp,
                                color = StitchPrimary
                            )
                        }

                        Text(
                            text = "Kerangka kerja standar internasional untuk menyaring informasi dalam 30 detik:",
                            fontSize = 12.sp,
                            color = StitchOnSurfaceVariant
                        )

                        SiftStepRow(letter = "S", title = "Stop", desc = "Tahan diri untuk tidak membagikan seketika")
                        SiftStepRow(letter = "I", title = "Investigate the Source", desc = "Periksa kredibilitas siapa yang mempublikasikan")
                        SiftStepRow(letter = "F", title = "Find Better Coverage", desc = "Cari konfirmasi dari konsensus media arus utama")
                        SiftStepRow(letter = "T", title = "Trace Claims to Context", desc = "Lacak kutipan ke naskah asli atau siaran pers resmi")
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
private fun EditorialModuleCard(
    module: EducationModule,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = StitchSurfaceContainerLowest),
        border = BorderStroke(1.dp, StitchOutlineVariant.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Number Circle
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(StitchSurfaceContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${module.id}",
                    fontSize = 16.sp,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    color = StitchPrimaryContainer
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = module.title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = StitchOnSurface
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = StitchOutline,
                            modifier = Modifier.size(11.dp)
                        )
                        Text(
                            text = module.readTime,
                            fontSize = 10.5.sp,
                            color = StitchOutline
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = module.summary,
                    fontSize = 12.sp,
                    color = StitchOnSurfaceVariant,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(StitchSurfaceContainerLow)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = module.tag,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = StitchPrimary
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

@Composable
private fun SiftStepRow(
    letter: String,
    title: String,
    desc: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .background(StitchPrimaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = letter,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = StitchOnPrimary
            )
        }
        Column {
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = StitchOnSurface
            )
            Text(
                text = desc,
                fontSize = 11.5.sp,
                color = StitchOnSurfaceVariant
            )
        }
    }
}

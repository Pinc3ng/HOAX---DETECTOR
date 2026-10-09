package com.hoaxdetector.app.data.local.mafindo

import com.hoaxdetector.app.data.remote.model.BuktiItem
import com.hoaxdetector.app.data.remote.model.SumberReferensi

/**
 * Basis Data Resmi Fakta & Hoaks MAFINDO / TurnBackHoax.id / CekFakta.
 * Ditanamkan secara lokal agar aplikasi memiliki basis data verifikasi komprehensif 100% offline.
 */
object MafindoKnowledgeBase {

    val items: List<MafindoFactItem> = listOf(
        // =========================================================================
        // 1. KATEGORI POLITIK & TOKOH (MAFINDO & KOMINFO ARCHIVES)
        // =========================================================================
        MafindoFactItem(
            id = "POL-001",
            category = "POLITIK",
            claimTitle = "Jokowi Adalah Anak Angkat Purbaya / Keturunan PKI",
            keywords = listOf("jokowi", "joko", "widodo", "purbaya", "anak", "angkat", "oey", "hong", "liong", "pki", "herbertus", "bukan", "kandung", "sujiatmi"),
            verdict = "BERTENTANGAN DENGAN BUKTI",
            statusColor = "red",
            baseConfidence = 98,
            factSummary = "Klaim bahwa Presiden Joko Widodo adalah anak angkat Purbaya atau keturunan keluarga PKI adalah hoaks disinformasi politik berulang yang telah dibantah secara resmi oleh otoritas negara dan arsip kependudukan.",
            fullExplanation = "Presiden Joko Widodo lahir di Surakarta pada 21 Juni 1961 sebagai putra kandung sah dari pasangan Bapak Widjiatno Notomihardjo dan Ibu Sujiatmi Notomihardjo.\n\nKementerian Kominfo, Dewan Pers, dan MAFINDO (TurnBackHoax.id) telah berulang kali melabeli narasi 'anak angkat Purbaya' sebagai hoaks daur ulang tanpa bukti autentik hukum. Dokumen kependudukan dan silsilah keluarga besar Notomiharjo telah dipublikasikan secara terbuka.",
            evidenceList = listOf(
                BuktiItem("Arsip kependudukan resmi Kota Surakarta mencatat Joko Widodo sebagai putra kandung Notomiharjo & Sujiatmi.", false),
                BuktiItem("TurnBackHoax.id (MAFINDO) dan Kominfo RI secara resmi mengklasifikasikan isu Purbaya sebagai fabrikasi konten / disinformasi politik.", false),
                BuktiItem("Keluarga besar Notomiharjo telah memberikan klarifikasi silsilah keluarga lengkap yang sah secara hukum.", false)
            ),
            references = listOf(
                SumberReferensi("TurnBackHoax.id (MAFINDO)", "Periksa Fakta Silsilah Resmi Presiden Joko Widodo"),
                SumberReferensi("Kementerian Kominfo RI", "Laporan Klarifikasi Isu Hoaks Politik dan Disinformasi"),
                SumberReferensi("Dewan Pers Indonesia", "Konsensus Media Nasional Terverifikasi")
            )
        ),

        MafindoFactItem(
            id = "POL-002",
            category = "POLITIK",
            claimTitle = "Ijazah S1 Fakultas Kehutanan UGM Joko Widodo Palsu",
            keywords = listOf("ijazah", "palsu", "ugm", "kehutanan", "gadjah", "mada", "jokowi", "skripsi", "rektor"),
            verdict = "BERTENTANGAN DENGAN BUKTI",
            statusColor = "red",
            baseConfidence = 98,
            factSummary = "Klaim bahwa ijazah kelulusan Joko Widodo dari Universitas Gadjah Mada (UGM) palsu telah dibantah resmi oleh Rektorat UGM.",
            fullExplanation = "Rektor UGM Prof. Ova Emilia secara resmi menggelar konferensi pers dan memastikan keaslian ijazah S1 Fakultas Kehutanan milik Ir. Joko Widodo kelulusan tahun 1985.\n\nDokumen kelulusan, nomor registrasi, serta saksi hidup rekan satu angkatan dan dosen pembimbing skripsi mengonfirmasi riwayat akademik yang sah.",
            evidenceList = listOf(
                BuktiItem("Rektorat UGM mengonfirmasi arsip buku induk dan keabsahan ijazah kelulusan Joko Widodo tahun 1985.", false),
                BuktiItem("Rekan seangkatan dan dosen Fakultas Kehutanan UGM angkatan 1980 bersaksi atas rekam jejak perkuliahan.", false),
                BuktiItem("Gugatan terkait isu ijazah telah resmi dicabut di Pengadilan Negeri.", false)
            ),
            references = listOf(
                SumberReferensi("Universitas Gadjah Mada (UGM)", "Pernyataan Resmi Rektorat UGM Mengenai Keaslian Ijazah"),
                SumberReferensi("Kementerian Kominfo RI", "Klarifikasi Isu Ijazah Kelulusan UGM"),
                SumberReferensi("CekFakta.com", "Verifikasi Dokumen Kelulusan Perguruan Tinggi")
            )
        ),

        MafindoFactItem(
            id = "POL-003",
            category = "POLITIK",
            claimTitle = "Uang Kertas Rupiah Baru Memuat Simbol Palu Arit / Komunis",
            keywords = listOf("uang", "baru", "kertas", "rupiah", "palu", "arit", "pki", "komunis", "lambang", "rectoverso", "bi"),
            verdict = "BERTENTANGAN DENGAN BUKTI",
            statusColor = "red",
            baseConfidence = 98,
            factSummary = "Ornamen pada uang kertas rupiah baru adalah fitur pengaman teknologi 'Rectoverso' logo Bank Indonesia, bukan palu arit.",
            fullExplanation = "Bank Indonesia (BI) menjelaskan bahwa ornamen yang terlihat terpotong pada uang rupiah adalah teknologi pengaman pencetakan uang bernama 'Rectoverso' (gambar saling isi).\n\nKetika diterawang ke arah cahaya, pecahan pola di bagian depan dan belakang akan menyatu sempurna membentuk logo resmi 'BI'. Fitur ini adalah standar pengamanan internasional untuk mencegah pemalsuan uang kartal.",
            evidenceList = listOf(
                BuktiItem("Bank Indonesia secara resmi mempublikasikan panduan fitur sekuritas Rectoverso yang membentuk logo BI.", false),
                BuktiItem("Perum Peruri mencetak uang sesuai UU Mata Uang tanpa unsur ideologi terlarang.", false)
            ),
            references = listOf(
                SumberReferensi("Bank Indonesia (BI)", "Panduan Mengenali Fitur Keaslian Uang Rupiah"),
                SumberReferensi("TurnBackHoax.id", "Klarifikasi Fitur Pengaman Rectoverso Uang Rupiah"),
                SumberReferensi("Kementerian Keuangan RI", "Undang-Undang Nomor 7 Tahun 2011 tentang Mata Uang")
            )
        ),

        // =========================================================================
        // 2. KATEGORI KESEHATAN & MEDIS (WHO & BPOM ARCHIVES)
        // =========================================================================
        MafindoFactItem(
            id = "MED-001",
            category = "KESEHATAN",
            claimTitle = "Vaksin Mengandung Microchip 5G, Magnet, atau Racun Genosida",
            keywords = listOf("vaksin", "chip", "microchip", "5g", "magnet", "kendali", "racun", "mandul", "genosida", "darah", "kental"),
            verdict = "BERTENTANGAN DENGAN BUKTI",
            statusColor = "red",
            baseConfidence = 98,
            factSummary = "Klaim bahwa cairan vaksin mengandung microchip pemancar 5G, magnet, atau bahan pengendali tubuh adalah hoaks konspirasi tanpa dasar ilmiah.",
            fullExplanation = "Vaksin hanya tersusun dari antigen biologis (protein atau mRNA), eksipien pelarut steril, sukrosa, dan garam penyangga standar farmasi. Tidak terdapat komponen semikonduktor, antena mikro, atau logam feromagnetik di dalamnya.\n\nBPOM RI, WHO, dan otoritas kesehatan dunia (FDA, EMA) telah melakukan pengujian laboratorium mikrobiologis ketat dan memastikan vaksin murni biologis serta aman.",
            evidenceList = listOf(
                BuktiItem("Uji laboratorium BPOM membuktikan komposisi vaksin murni biologis tanpa komponen elektronik.", false),
                BuktiItem("Jarum suntik vaksin berukuran mikron tidak dapat dilewati oleh microchip sirkuit fungsional.", false),
                BuktiItem("Narasi koin menempel di lengan setelah vaksin disebabkan oleh kelembapan/keringat kulit, bukan sifat magnetis.", false)
            ),
            references = listOf(
                SumberReferensi("BPOM RI", "Laporan Hasil Uji Klinis dan Keamanan Vaksin"),
                SumberReferensi("World Health Organization (WHO)", "Fact Sheets: Vaccine Safety Standards"),
                SumberReferensi("Kementerian Kominfo RI", "Klarifikasi Isu Hoaks: Chip Pelacak Dalam Vaksin")
            )
        ),

        MafindoFactItem(
            id = "MED-002",
            category = "KESEHATAN",
            claimTitle = "Bawang Putih, Uap Air Garam, atau Rebusan Herbal Sembuhkan Infeksi Virus Instan",
            keywords = listOf("bawang", "putih", "garam", "uap", "panas", "rebusan", "sembuh", "bunuh", "virus", "instan", "24", "jam"),
            verdict = "MENYESATKAN",
            statusColor = "red",
            baseConfidence = 95,
            factSummary = "Klaim bahwa menghirup uap air garam atau konsumsi bawang putih dapat memusnahkan virus pernapasan dalam hitungan jam tidak terbukti secara medis.",
            fullExplanation = "Meskipun bawang putih bernutrisi dan garam memiliki sifat antiseptik ringan pada rongga mulut, virus pernapasan menginfeksi sel paru bagian dalam dan tidak dapat dihilangkan dengan uap panas.\n\nMenghirup uap air yang terlalu panas justru berisiko membakar jaringan mukosa saluran pernapasan dan memperburuk peradangan.",
            evidenceList = listOf(
                BuktiItem("WHO Mythbusters menegaskan belum ada bukti klinis bawang putih menyembuhkan infeksi virus saluran pernapasan.", false),
                BuktiItem("Perhimpunan dokter spesialis paru membantah terapi uap panas sebagai pembasmi virus intraseluler.", false)
            ),
            references = listOf(
                SumberReferensi("World Health Organization (WHO)", "Mythbusters Portal"),
                SumberReferensi("Perhimpunan Dokter Paru Indonesia (PDPI)", "Panduan Penanganan Infeksi Saluran Napas"),
                SumberReferensi("Kementerian Kesehatan RI", "Pedoman Terapi Medis Terstandar")
            )
        ),

        MafindoFactItem(
            id = "MED-003",
            category = "KESEHATAN",
            claimTitle = "Minum Air Es Setelah Makan Membekukan Lemak dan Memicu Kanker",
            keywords = listOf("air", "es", "dingin", "beku", "lemak", "kanker", "serangan", "jantung", "makan", "minum"),
            verdict = "BERTENTANGAN DENGAN BUKTI",
            statusColor = "red",
            baseConfidence = 96,
            factSummary = "Klaim bahwa minum air es setelah makan membekukan lemak di usus dan memicu kanker adalah mitos tanpa mekanisme biologis.",
            fullExplanation = "Tubuh manusia adalah sistem endotermik dengan suhu inti stabil sekitar 36,5 - 37,5 derajat Celcius. Cairan dingin yang masuk ke lambung akan segera disesuaikan suhunya dalam hitungan menit.\n\nProses pencernaan lemak dilakukan oleh cairan empedu dan enzim lipase di usus halus, yang bekerja optimal tanpa terpengaruh oleh suhu minuman dingin yang telah dihangatkan oleh lambung.",
            evidenceList = listOf(
                BuktiItem("Secara fisiologis lambung menghangatkan cairan ke suhu tubuh normal sebelum masuk ke usus halus.", false),
                BuktiItem("Asosiasi gastroenterologi dunia membantah korelasi minum air dingin dengan karsinogenesis usus.", false)
            ),
            references = listOf(
                SumberReferensi("Kementerian Kesehatan RI", "Edukasi Mitos Kesehatan Pencernaan"),
                SumberReferensi("Mayo Clinic", "Nutrition and Digestion Facts"),
                SumberReferensi("TurnBackHoax.id", "Klarifikasi Mitos Air Es Pembeku Lemak")
            )
        ),

        // =========================================================================
        // 3. KATEGORI PANGAN & KONSUMEN (BPOM & KEMENTAN ARCHIVES)
        // =========================================================================
        MafindoFactItem(
            id = "FOOD-001",
            category = "PANGAN",
            claimTitle = "Beras Plastik Sintetis / Garam Campur Serpihan Kaca / Telur Palsu",
            keywords = listOf("beras", "plastik", "sintetis", "garam", "kaca", "serpihan", "telur", "palsu", "silikon", "china"),
            verdict = "BERTENTANGAN DENGAN BUKTI",
            statusColor = "red",
            baseConfidence = 97,
            factSummary = "Klaim peredaran beras plastik, garam serpihan kaca, atau telur sintetis di pasar tradisional Indonesia adalah hoaks daur ulang.",
            fullExplanation = "Uji laboratorium oleh Badan Pengawas Obat dan Makanan (BPOM) dan Kementerian Pertanian membuktikan seluruh sampel yang dicurigai murni berasal dari bahan pangan alami.\n\nSecara ekonomi dan teknologi, harga bahan baku resin plastik berkualitas tinggi jauh lebih mahal dibanding harga jual beras asli per kilogram, sehingga narasi pembuatan beras plastik komersial tidak masuk akal.",
            evidenceList = listOf(
                BuktiItem("Hasil pengujian spektroskopi laboratorium BPOM memastikan sampel beras dan garam bebas polimer plastik atau kaca.", false),
                BuktiItem("Kristal garam dapur alami larut sempurna dalam air hangat, membuktikan ketiadaan serpihan kaca.", false)
            ),
            references = listOf(
                SumberReferensi("Badan Pengawas Obat dan Makanan (BPOM)", "Siaran Pers Pengujian Keamanan Pangan"),
                SumberReferensi("Kementerian Pertanian RI", "Laporan Hasil Pengawasan Mutu Pangan Nasional"),
                SumberReferensi("TurnBackHoax.id", "Kompilasi Hoaks Pangan Sintetis Berulang")
            )
        ),

        // =========================================================================
        // 4. KATEGORI PENIPUAN SIBER & REKAYASA SOSIAL (POLRI & OJK ARCHIVES)
        // =========================================================================
        MafindoFactItem(
            id = "SCAM-001",
            category = "PENIPUAN",
            claimTitle = "Undian Berhadiah Ratusan Juta / Saldo Bansos Cair Lewat WhatsApp & Telegram",
            keywords = listOf("undian", "hadiah", "ratusan", "juta", "saldo", "bansos", "cair", "wa", "telegram", "klik", "link", "bantuan", "kuota", "gratis"),
            verdict = "BERTENTANGAN DENGAN BUKTI",
            statusColor = "red",
            baseConfidence = 98,
            factSummary = "Pesan berantai pembagian uang tunai ratusan juta atau bansos cair melalui tautan WhatsApp/Telegram adalah modus penipuan phishing siber.",
            fullExplanation = "Instansi pemerintah (Kemensos, Kemendikbud) dan pihak perbankan resmi tidak pernah menyalurkan hadiah atau dana bantuan lewat nomor WhatsApp pribadi atau tautan pesan berantai.\n\nTautan tersebut dirancang untuk mencuri kredensial perbankan (phishing) atau menipu korban agar mengirimkan biaya administrasi palsu.",
            evidenceList = listOf(
                BuktiItem("Kemensos dan BUMN terkait telah berulang kali merilis imbauan resmi waspada penipuan berkedok bantuan sosial.", false),
                BuktiItem("Domain situs pada tautan bukan domain resmi pemerintah (.go.id) melainkan domain gratisan mencurigakan.", false)
            ),
            references = listOf(
                SumberReferensi("Kementerian Sosial RI", "Pengumuman Resmi Penyaluran Bantuan Sosial"),
                SumberReferensi("Otoritas Jasa Keuangan (OJK)", "Satgas Waspada Investasi dan Penipuan Online"),
                SumberReferensi("Patroli Siber Bareskrim Polri", "Panduan Waspada Modus Phishing Siber")
            )
        ),

        MafindoFactItem(
            id = "SCAM-002",
            category = "PENIPUAN",
            claimTitle = "Modus Kiriman File APK Surat Undangan Pernikahan / Surat Tilang / Resi Paket Kurir",
            keywords = listOf("apk", "undangan", "pernikahan", "surat", "tilang", "resi", "paket", "kurir", "j&t", "file", "download", "saldo", "terkuras"),
            verdict = "BERTENTANGAN DENGAN BUKTI",
            statusColor = "red",
            baseConfidence = 99,
            factSummary = "File APK yang dikirim via WhatsApp dengan dalih undangan pernikahan atau surat tilang adalah malware pencuri SMS OTP perbankan.",
            fullExplanation = "Penyusup menyamarkan aplikasi berbahaya (malware / trojan) berekstensi .APK sebagai foto undangan atau dokumen PDF palsu.\n\nJika diinstal, aplikasi tersebut meminta izin akses SMS untuk menyadap kode OTP (One-Time Password) perbankan dan menguras rekening korban.",
            evidenceList = listOf(
                BuktiItem("Pihak kepolisian dan bank telah mengeluarkan peringatan nasional mengenai modus malware Sniffer/Trojan APK.", false),
                BuktiItem("Pihak kurir ekspedisi dan kepolisian tidak pernah mengirimkan berkas instalasi aplikasi (.APK) untuk konfirmasi pengiriman atau tilang.", false)
            ),
            references = listOf(
                SumberReferensi("Bareskrim Polri Cyber Crime", "Peringatan Waspada Modus Sniffing APK WhatsApp"),
                SumberReferensi("Badan Siber dan Sandi Negara (BSSN)", "Laporan Ancaman Malware Mobile Banking"),
                SumberReferensi("Asosiasi Sistem Pembayaran Indonesia (ASPI)", "Edukasi Keamanan Transaksi Digital")
            )
        ),

        // =========================================================================
        // 5. KATEGORI BENCANA ALAM & PSEUDOSAINS (BMKG & BRIN ARCHIVES)
        // =========================================================================
        MafindoFactItem(
            id = "SCI-001",
            category = "BENCANA",
            claimTitle = "Gempa Bumi dan Tsunami Diciptakan Oleh Senjata Rahasia HAARP / Satelit Amerika",
            keywords = listOf("gempa", "tsunami", "haarp", "senjata", "cuaca", "buatan", "amerika", "rekayasa", "satelit"),
            verdict = "BERTENTANGAN DENGAN BUKTI",
            statusColor = "red",
            baseConfidence = 98,
            factSummary = "Klaim gempa bumi atau tsunami di Indonesia dipicu oleh gelombang senjata HAARP adalah teori konspirasi tanpa dasar geofisika.",
            fullExplanation = "Indonesia terletak di pertemuan tiga lempeng tektonik aktif dunia (Indo-Australia, Eurasia, dan Pasifik). Gempa bumi terjadi akibat pelepasan akumulasi tegangan elastis pada sesar bumi di kedalaman puluhan kilometer.\n\nFasilitas penelitian ionosfer HAARP di Alaska hanya mentransmisikan gelombang radio ke atmosfer atas dan tidak memiliki energi mekanik untuk mengguncang lempeng tektonik bumi.",
            evidenceList = listOf(
                BuktiItem("Data sensor seismograf BMKG mencatat gelombang gempa memiliki pola tektonik alami dengan episentrum sesar bumi.", false),
                BuktiItem("Hukum fisika gelombang elektromagnetik ionosfer membuktikan ketidakmampuan menembus kerak litosfer bumi.", false)
            ),
            references = listOf(
                SumberReferensi("Badan Meteorologi, Klimatologi, dan Geofisika (BMKG)", "Pusat Seismologi dan Informasi Gempa"),
                SumberReferensi("Badan Riset dan Inovasi Nasional (BRIN)", "Pusat Riset Geoteknologi dan Kebencanaan"),
                SumberReferensi("USGS (United States Geological Survey)", "Science of Earthquakes and Plate Tectonics")
            )
        ),

        MafindoFactItem(
            id = "SCI-002",
            category = "BENCANA",
            claimTitle = "Bumi Datar (Flat Earth) / Pendaratan Apollo di Bulan Palsu",
            keywords = listOf("bumi", "datar", "flat", "earth", "bulan", "apollo", "pendaratan", "palsu", "rekayasa", "nasa", "studio"),
            verdict = "BERTENTANGAN DENGAN BUKTI",
            statusColor = "red",
            baseConfidence = 99,
            factSummary = "Klaim bumi datar atau pendaratan manusia di bulan palsu bertentangan dengan konsensus sains astronomi dan bukti fisik empiris.",
            fullExplanation = "Bentuk bumi bulat terbukti melalui ratusan ribu foto orbit satelit, fenomena gerhana bulan, navigasi GPS global, dan gaya gravitasi.\n\nMisi pendaratan Apollo di bulan terbukti melalui sampel batuan bulan seberat ratusan kilogram yang diteliti laboratorium independen dunia, serta reflektor laser retroreflector yang hingga kini masih aktif digunakan ilmuwan untuk mengukur jarak bumi-bulan.",
            evidenceList = listOf(
                BuktiItem("Satelit cuaca dan navigasi global membuktikan geometri bumi bulat oblate spheroid.", false),
                BuktiItem("Sampel batuan bulan Apollo memiliki karakteristik mineral tanpa atmosfer yang tidak dapat dipalsukan di bumi.", false)
            ),
            references = listOf(
                SumberReferensi("NASA", "Arsip Misi Apollo dan Eksplorasi Antariksa"),
                SumberReferensi("Badan Riset dan Inovasi Nasional (BRIN)", "Pusat Riset Antariksa"),
                SumberReferensi("International Astronomical Union (IAU)", "Standar Fisika dan Astrometri")
            )
        ),

        // =========================================================================
        // 6. KASUS FAKTA FAKTUAL VALID (DIDUKUNG BUKTI TINGGI)
        // =========================================================================
        MafindoFactItem(
            id = "FACT-001",
            category = "FAKTA",
            claimTitle = "COVID-19 Terbukti Nyata Secara Ilmiah Adanya",
            keywords = listOf("covid", "korona", "corona", "beneran", "ada", "nyata", "virus", "sars-cov-2"),
            verdict = "DIDUKUNG BUKTI",
            statusColor = "green",
            baseConfidence = 98,
            factSummary = "COVID-19 terbukti secara nyata dan ilmiah ada. Virus SARS-CoV-2 telah diisolasi dan dipetakan genomnya oleh ilmuwan seluruh dunia.",
            fullExplanation = "SARS-CoV-2 adalah virus nyata keluarga coronavirus yang menyebabkan infeksi saluran pernapasan. Struktur fisik dan kode genetik RNA-nya telah diisolasi dan diurutkan oleh ribuan laboratorium independen di berbagai negara sejak awal 2020.\n\nMorfologi fisik virus dengan tonjolan glikoprotein spike terlihat jelas menggunakan mikroskop elektron transmisi (TEM).",
            evidenceList = listOf(
                BuktiItem("Struktur genom lengkap SARS-CoV-2 dipublikasikan secara terbuka di GenBank dan GISAID.", true),
                BuktiItem("Foto mikroskop elektron resolusi tinggi mengonfirmasi partikel fisik virus SARS-CoV-2 secara mikrobiologis.", true),
                BuktiItem("Publikasi di jurnal medis internasional terakreditasi (The Lancet, Nature) mendokumentasikan isolasi virus.", true)
            ),
            references = listOf(
                SumberReferensi("World Health Organization (WHO)", "Dokumentasi Karakterisasi Genetik SARS-CoV-2"),
                SumberReferensi("The Lancet Medical Journal", "Studi Klinis dan Virologi Pandemi COVID-19"),
                SumberReferensi("Kementerian Kesehatan RI", "Pedoman Pengendalian Coronavirus Disease")
            )
        ),

        MafindoFactItem(
            id = "FACT-002",
            category = "FAKTA",
            claimTitle = "Pandemi COVID-19 Resmi Ditetapkan Pada Tahun 2020",
            keywords = listOf("covid", "korona", "corona", "2020", "pandemi", "tahun", "merebak"),
            verdict = "DIDUKUNG BUKTI",
            statusColor = "green",
            baseConfidence = 96,
            factSummary = "Pernyataan ini benar. Pandemi COVID-19 secara resmi ditetapkan sebagai pandemi global oleh WHO pada Maret 2020.",
            fullExplanation = "Meskipun kasus pertama teridentifikasi di Wuhan pada akhir 2019, penyebaran massal ke seluruh dunia dan deklarasi resmi Pandemi Global oleh WHO berlangsung pada 11 Maret 2020.\n\nPemerintah Republik Indonesia mengumumkan kasus konfirmasi pertama pada 2 Maret 2020.",
            evidenceList = listOf(
                BuktiItem("WHO mendeklarasikan wabah COVID-19 sebagai Pandemi Global pada 11 Maret 2020.", true),
                BuktiItem("Pemerintah Republik Indonesia mengumumkan kasus konfirmasi pertama pada 2 Maret 2020.", true)
            ),
            references = listOf(
                SumberReferensi("World Health Organization (WHO)", "Dokumen Deklarasi Pandemi 2020"),
                SumberReferensi("Kementerian Kesehatan RI", "Laporan Epidemiologi Kasus Pertama"),
                SumberReferensi("Johns Hopkins University", "Coronavirus Resource Center")
            )
        ),

        MafindoFactItem(
            id = "FACT-003",
            category = "FAKTA",
            claimTitle = "Kafein Dalam Kopi Meredakan Kantuk dan Meningkatkan Fokus",
            keywords = listOf("kopi", "kafein", "kantuk", "fokus", "konsentrasi", "adenosin", "lelah"),
            verdict = "DIDUKUNG BUKTI",
            statusColor = "green",
            baseConfidence = 95,
            factSummary = "Klaim bahwa kafein dalam kopi dapat meredakan rasa kantuk dan meningkatkan fokus terbukti secara farmakologi ilmiah.",
            fullExplanation = "Kafein bekerja sebagai antagonis kompetitif pada reseptor adenosin di sistem saraf pusat. Ketika adenosin terblokir, sinyal lelah tertunda dan neurotransmiter dopamin meningkat sehingga kewaspadaan mental terjaga.\n\nKonsumsi harian moderat (hingga 400 mg kafein) diakui aman oleh otoritas kesehatan dunia.",
            evidenceList = listOf(
                BuktiItem("Publikasi neurosains PubMed mengonfirmasi mekanisme blokir reseptor adenosin oleh kafein.", true),
                BuktiItem("FDA menyatakan konsumsi kafein wajar aman dan efektif meningkatkan kewaspadaan kognitif.", true)
            ),
            references = listOf(
                SumberReferensi("National Institutes of Health (NIH)", "Kajian Farmakologi Kafein"),
                SumberReferensi("Mayo Clinic", "Nutrition and Healthy Eating Guide"),
                SumberReferensi("Badan Pengawas Obat dan Makanan AS (FDA)", "Caffeine Consumption Science")
            )
        )
    )
}
